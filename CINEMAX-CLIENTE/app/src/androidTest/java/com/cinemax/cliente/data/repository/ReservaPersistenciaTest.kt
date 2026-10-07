package com.cinemax.cliente.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cinemax.cliente.data.BaseDePrueba
import com.cinemax.cliente.data.Catalogo
import com.cinemax.cliente.data.aSesionDePrueba
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.model.EstadoReserva
import com.cinemax.cliente.state.RolCineMax
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * FASE 5 - Persistencia: lo que se escribe tiene que sobrevivir al cierre de la
 * app.
 *
 * A diferencia del resto de pruebas, aqui la base NO es `inMemoryDatabase`: se
 * escribe un archivo SQLite de verdad, se cierra la base y se vuelve a abrir. Asi
 * se comprueba que no se esta dependiendo de la cache en memoria de Room y que
 * las relaciones de una reserva siguen intactas al releer.
 */
@RunWith(AndroidJUnit4::class)
class ReservaPersistenciaTest {

    private lateinit var archivo: File

    @Before
    fun setUp() {
        // OJO: `Room.databaseBuilder` NO crea el archivo en `cacheDir`, lo crea en
        // `context.getDatabasePath(nombre)`. Si se borrara el fichero de otra ruta
        // los datos de una prueba se filtrarian a la siguiente.
        archivo = InstrumentationRegistry.getInstrumentation().targetContext
            .getDatabasePath("persistencia-fase5.db")
        borrarArchivos()
        assertTrue("El archivo de la prueba anterior debe estar borrado", !archivo.exists())
    }

    @After
    fun tearDown() {
        borrarArchivos()
    }

    private fun borrarArchivos() {
        for (sufijo in listOf("", "-wal", "-shm")) {
            val auxiliar = File(archivo.path + sufijo)
            if (auxiliar.exists()) auxiliar.delete()
        }
    }

    private fun abrir(): CineMaxClienteDatabase = Room
        .databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            CineMaxClienteDatabase::class.java,
            archivo.name
        )
        .allowMainThreadQueries()
        .build()

    /**
     * Abre una base de archivo, ejecuta [bloque] y la cierra.
     *
     * Es el "sesion 1" / "sesion 2" de estas pruebas: lo escrito por el primer
     * bloque tiene que seguir ahi cuando el segundo vuelve a abrir el archivo.
     */
    private suspend fun <T> cerrandoAlTerminar(bloque: suspend (CineMaxClienteDatabase) -> T): T {
        val abierta = abrir()
        return try {
            bloque(abierta)
        } finally {
            abierta.close()
        }
    }

    @Test
    fun laReservaSobreviveAlCierreDeLaApp() = runBlocking<Unit> {
        var funcionId = 0
        var butacas: List<Int> = emptyList()
        var usuarioId = 0
        lateinit var codigo: String

        // --- Sesion 1: comprar ---
        cerrandoAlTerminar { primera ->
            val catalogo: Catalogo = BaseDePrueba.sembrarYLeer(primera)
            val repos = RepositoriosCliente.reservas(primera)
            val salas = RepositoriosCliente.salas(primera)
            val funcion = catalogo.funciones.first()
            val ocupadas = salas.observarOcupadas(funcion.id).first().map { it.id }.toSet()
            butacas = catalogo.butacas(funcion.salaId)
                .filter { !ocupadas.contains(it.id) }
                .take(2)
                .map { it.id }
            funcionId = funcion.id
            usuarioId = catalogo.usuarios.first { it.usuario == "cliente" }.id

            val resultado = repos.registrar(
                SolicitudReserva(
                    usuarioId = usuarioId,
                    funcionId = funcionId,
                    butacaIds = butacas,
                    total = funcion.precioEntrada * 2,
                    fechaCompra = "2026-05-01"
                )
            )
            assertTrue("Se esperaba Exito, fue $resultado", resultado is ResultadoReserva.Exito)
            codigo = (resultado as ResultadoReserva.Exito).reserva.codigo
        }

        // --- Sesion 2: reabrir y releer ---
        val segunda = abrir()
        try {
            val repos = RepositoriosCliente.reservas(segunda)
            val salas = RepositoriosCliente.salas(segunda)

            val releyda = repos.obtenerPorCodigo(codigo)
            assertNotNull("La reserva debe seguir en la base tras reabrir", releyda)
            assertEquals(funcionId, releyda!!.funcionId)
            assertEquals(usuarioId, releyda.usuarioId)
            assertEquals(EstadoReserva.CONFIRMADA.valorPersistido, releyda.estado)

            val detalle = repos.observarButacasDeReserva(releyda.id).first()
            assertEquals(2, detalle.size)
            assertEquals(butacas.toSet(), detalle.map { it.id }.toSet())

            // Y las butacas siguen ocupadas para esa funcion.
            val ocupadas = salas.observarOcupadas(funcionId).first().map { it.id }
            assertTrue(butacas.all { ocupadas.contains(it) })
        } finally {
            segunda.close()
        }
    }

    @Test
    fun unaReservaCanceladaSigueCanceladaAlReabrir() = runBlocking<Unit> {
        lateinit var codigo: String
        var funcionId = 0
        var butacas: List<Int> = emptyList()

        cerrandoAlTerminar { primera ->
            val catalogo = BaseDePrueba.sembrarYLeer(primera)
            val repos = RepositoriosCliente.reservas(primera)
            val salas = RepositoriosCliente.salas(primera)
            val funcion = catalogo.funciones.first()
            val ocupadas = salas.observarOcupadas(funcion.id).first().map { it.id }.toSet()
            butacas = catalogo.butacas(funcion.salaId)
                .filter { !ocupadas.contains(it.id) }
                .take(1)
                .map { it.id }
            funcionId = funcion.id

            val exito = repos.registrar(
                SolicitudReserva(
                    usuarioId = catalogo.usuarios.first { it.usuario == "cliente" }.id,
                    funcionId = funcionId,
                    butacaIds = butacas,
                    total = funcion.precioEntrada,
                    fechaCompra = "2026-05-01"
                )
            ) as ResultadoReserva.Exito
            codigo = exito.reserva.codigo
            repos.cancelar(codigo, exito.reserva.usuarioId)
        }

        val segunda = abrir()
        try {
            val repos = RepositoriosCliente.reservas(segunda)
            val salas = RepositoriosCliente.salas(segunda)

            val releyda = repos.obtenerPorCodigo(codigo)
            assertNotNull(releyda)
            assertEquals(EstadoReserva.CANCELADA.valorPersistido, releyda!!.estado)
            assertEquals(0, repos.contarButacasDeReserva(releyda.id))
            // La butaca quedo liberada de forma permanente.
            val ocupadas = salas.observarOcupadas(funcionId).first().map { it.id }
            assertTrue(butacas.none { ocupadas.contains(it) })
        } finally {
            segunda.close()
        }
    }

    @Test
    fun elHistorialDelUsuarioPersiste() = runBlocking<Unit> {
        var usuarioId = 0
        var codigos: List<String> = emptyList()
        // El seed ya deja reservas CONFIRMADAS de este cliente: se parte de las
        // que hay y se comprueba que se conservan y se suman las nuevas.
        var confirmadasDelSeed = 0

        cerrandoAlTerminar { primera ->
            val catalogo = BaseDePrueba.sembrarYLeer(primera)
            val repos = RepositoriosCliente.reservas(primera)
            val salas = RepositoriosCliente.salas(primera)
            usuarioId = catalogo.usuarios.first { it.usuario == "cliente" }.id
            confirmadasDelSeed = repos.observarPorUsuarioYEstado(usuarioId, EstadoReserva.CONFIRMADA).first().size

            codigos = catalogo.fechas().map { fecha ->
                val funcion = catalogo.funciones.first { it.fecha == fecha }
                val yaOcupadas = salas.observarOcupadas(funcion.id).first().map { it.id }.toSet()
                val libre = catalogo.butacas(funcion.salaId)
                    .map { it.id }
                    .first { id -> !yaOcupadas.contains(id) }
                val exito = repos.registrar(
                    SolicitudReserva(
                        usuarioId = usuarioId,
                        funcionId = funcion.id,
                        butacaIds = listOf(libre),
                        total = funcion.precioEntrada,
                        fechaCompra = fecha
                    )
                ) as ResultadoReserva.Exito
                exito.reserva.codigo
            }
        }

        val segunda = abrir()
        try {
            val repos = RepositoriosCliente.reservas(segunda)

            val historial = repos.observarPorUsuario(usuarioId).first()
            assertTrue(historial.map { it.codigo }.containsAll(codigos))
            assertEquals(
                confirmadasDelSeed + codigos.size,
                repos.observarPorUsuarioYEstado(usuarioId, EstadoReserva.CONFIRMADA).first().size
            )
        } finally {
            segunda.close()
        }
    }

    @Test
    fun laSesionDelUsuarioNoCambiaAlReabrirLaBase() = runBlocking<Unit> {
        val cliente = cerrandoAlTerminar { primera ->
            val catalogo = BaseDePrueba.sembrarYLeer(primera)
            catalogo.usuarios.first { it.usuario == "cliente" }
        }
        val sesion = cliente.aSesionDePrueba()

        assertEquals(cliente.id, sesion.id)
        assertTrue(sesion.cumpleRolPermitido())
        assertEquals(RolCineMax.CLIENTE, sesion.rol)
    }

    @Test
    fun unaBaseVaciaNoTraeCartelera() = runBlocking<Unit> {
        cerrandoAlTerminar { vacia ->
            val funciones = RepositoriosCliente.funciones(vacia)
            val peliculas = RepositoriosCliente.peliculas(vacia)

            assertTrue(funciones.observarActivas().first().isEmpty())
            assertTrue(peliculas.observarActivas().first().isEmpty())
            assertNull(funciones.obtenerCompletaPorId(1))
        }
    }
}
