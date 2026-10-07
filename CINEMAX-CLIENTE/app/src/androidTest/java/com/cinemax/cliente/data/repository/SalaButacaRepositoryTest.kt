package com.cinemax.cliente.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cinemax.cliente.data.BaseDePrueba
import com.cinemax.cliente.data.Catalogo
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.ReservaEntity
import com.cinemax.cliente.data.seed.SeedData
import com.cinemax.cliente.model.EstadoReserva
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * FASE 5 - Casos G, H, I y J: mapa de butacas, ocupacion por funcion y
 * disponibilidad real consultada a Room.
 *
 * El punto que mas se repite aqui es que la ocupacion es POR FUNCION, no por
 * sala: la misma butaca puede estar ocupada a las 10:00 y libre a las 15:00 de
 * la misma sala.
 */
@RunWith(AndroidJUnit4::class)
class SalaButacaRepositoryTest {

    private lateinit var database: CineMaxClienteDatabase
    private lateinit var salas: SalaButacaRepository
    private lateinit var funciones: FuncionRepository
    private lateinit var catalogo: Catalogo

    @Before
    fun setUp() = runBlocking<Unit> {
        database = BaseDePrueba.abrirEnMemoria()
        catalogo = BaseDePrueba.sembrarYLeer(database)
        salas = RepositoriosCliente.salas(database)
        funciones = RepositoriosCliente.funciones(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ------------------------------------------------------------------
    // G) Mapa de butacas de la sala
    // ------------------------------------------------------------------

    @Test
    fun g_laSalaTraeSuMapaDeButacasCompleto() = runBlocking<Unit> {
        val sala = catalogo.sala(catalogo.salas.first().id)

        val resultado = salas.observarSalaConButacas(sala.id).first()

        assertEquals(sala, resultado?.sala)
        assertEquals(SeedData.BUTACAS_POR_SALA, resultado?.butacas?.size)
        assertEquals(SeedData.FILAS_POR_SALA * SeedData.COLUMNAS_POR_SALA, resultado?.butacas?.size)
    }

    @Test
    fun g_lasButacasVienenOrdenadasPorFilaYNumero() = runBlocking<Unit> {
        val salaId = catalogo.salas.first().id

        val resultado = salas.observarButacasDeSala(salaId).first()

        // Cada fila aparece con sus COLUMNAS_POR_SALA butacas, y las filas van de
        // la A a la H: comparar la lista completa de `fila` con `SeedData.FILAS`
        // no valeria porque hay 80 butacas y solo 8 filas.
        assertEquals(SeedData.FILAS, resultado.map { it.fila }.distinct())
        assertTrue(resultado.all { it.fila == it.fila.uppercase() })
        assertEquals(1, resultado.first().numero)
        assertEquals("A1", resultado.first().codigo)
        val primeraFila = resultado.filter { it.fila == SeedData.FILAS.first() }
        assertEquals(SeedData.COLUMNAS_POR_SALA, primeraFila.size)
        assertEquals(primeraFila.map { it.numero }, SeedData.NUMEROS)
        assertEquals("A${SeedData.COLUMNAS_POR_SALA}", primeraFila.last().codigo)
    }

    @Test
    fun g_todasLasButacasPertenecenASuSala() = runBlocking<Unit> {
        val salaId = catalogo.salas.first().id

        val resultado = salas.observarButacasDeSala(salaId).first()

        assertTrue(resultado.all { it.salaId == salaId })
        assertEquals(SeedData.BUTACAS_POR_SALA, salas.contarButacasDeSala(salaId))
    }

    @Test
    fun g_laSalaDeUnaFuncionSeResuelveDesdeLaFuncion() = runBlocking<Unit> {
        val funcion = catalogo.funciones.first()
        val salaEsperada = catalogo.sala(funcion.salaId)

        assertEquals(salaEsperada, salas.obtenerSalaDeFuncion(funcion.id))
        assertEquals(salaEsperada, salas.obtenerSalaPorId(funcion.salaId))
    }

    @Test
    fun g_unaFuncionInexistenteNoTieneSala() = runBlocking<Unit> {
        assertNull(salas.obtenerSalaDeFuncion(999_999))
    }

    @Test
    fun g_unaSalaInexistenteDevuelveMapaVacio() = runBlocking<Unit> {
        assertNull(salas.observarSalaConButacas(999_999).first())
        assertTrue(salas.observarButacasDeSala(999_999).first().isEmpty())
        assertEquals(0, salas.contarButacasDeSala(999_999))
    }

    // ------------------------------------------------------------------
    // H) Butacas ocupadas de una funcion
    // ------------------------------------------------------------------

    @Test
    fun h_lasButacasOcupadasDeUnaFuncionSonLasReservadas() = runBlocking<Unit> {
        // El seed reserva A1 y A2 de la funcion 0.
        val funcionOcupada = catalogo.funcionDePrograma(0)
        val esperadas = SeedData.reservasDemo.first { it.funcion == 0 }.butacas

        val ocupadas = salas.observarOcupadas(funcionOcupada.id).first()

        assertEquals(esperadas.sorted(), ocupadas.map { it.codigo }.sorted())
        assertEquals(2, salas.contarOcupadas(funcionOcupada.id))
        assertTrue(ocupadas.all { it.salaId == funcionOcupada.salaId })
    }

    @Test
    fun h_lasReservasCanceladasNoBloqueanButacas() = runBlocking<Unit> {
        // La reserva demo 3 esta CANCELADA: sus butacas deben quedar libres.
        val cancelada = SeedData.reservasDemo.last { it.estado == EstadoReserva.CANCELADA }
        val funcion = catalogo.funcionDePrograma(cancelada.funcion)

        val ocupadas = salas.observarOcupadas(funcion.id).first()

        assertFalse(ocupadas.isNotEmpty())
        assertEquals(0, salas.contarOcupadas(funcion.id))
    }

    @Test
    fun h_unaReservaConfirmadaOcupaSusButacas() = runBlocking<Unit> {
        val confirmada = SeedData.reservasDemo.first { it.estado == EstadoReserva.CONFIRMADA }
        val funcion = catalogo.funcionDePrograma(confirmada.funcion)
        val ocupables = salas.butacasDeSala(funcion.salaId).map { it.codigo }

        val ocupadas = salas.observarOcupadas(funcion.id).first()

        assertEquals(confirmada.butacas.sorted(), ocupadas.map { it.codigo }.sorted())
        assertTrue(ocupadas.all { ocupables.contains(it.codigo) })
    }

    // ------------------------------------------------------------------
    // La misma butaca, distinta funcion
    // ------------------------------------------------------------------

    @Test
    fun unaButacaOcupadaEnUnaFuncionSigueLibreEnOtraDeLaMismaSala() = runBlocking<Unit> {
        // El seed reserva B3, B4 y B5 en la funcion 6. Se busca otra funcion de la
        // MISMA sala y sin reservas propias para comparar la misma butaca.
        val conOcupadas = catalogo.funcionDePrograma(6)
        val mismaSala = catalogo.funciones.filter { it.salaId == conOcupadas.salaId && it.id != conOcupadas.id }
        assertTrue("El seed debe tener otra funcion en la misma sala", mismaSala.isNotEmpty())
        val otra = mismaSala.first { salas.contarOcupadas(it.id) == 0 }

        val butacaTomada = SeedData.reservasDemo.first { it.funcion == 6 }.butacas.first()
        val idButaca = catalogo.butaca(conOcupadas.salaId, butacaTomada).id

        val ocupadaEnUna = salas.observarOcupadas(conOcupadas.id).first()
        val ocupadaEnOtra = salas.observarOcupadas(otra.id).first()

        assertEquals(conOcupadas.salaId, otra.salaId)
        assertTrue(ocupadaEnUna.any { it.id == idButaca })
        assertFalse(ocupadaEnOtra.any { it.id == idButaca })
        assertEquals(0, salas.contarOcupadas(otra.id))
    }

    @Test
    fun laMismaFilaExisteComoButacasDistintasEnSalasDistintas() = runBlocking<Unit> {
        // `salas` viene en `ORDER BY nombre`, no por sede: hay que descartar
        // explicitamente la sala de partida o el "A1" puede ser la misma butaca.
        val primera = catalogo.salas.first()
        val segunda = catalogo.salas.first { it.id != primera.id && it.sedeId != primera.sedeId }

        val butacaA = catalogo.butaca(primera.id, "A1")
        val butacaB = catalogo.butaca(segunda.id, "A1")

        assertEquals("A1", butacaA.codigo)
        assertEquals("A1", butacaB.codigo)
        assertNotEquals(primera.id, segunda.id)
        assertNotEquals(butacaA.id, butacaB.id)
    }

    // ------------------------------------------------------------------
    // I) Butacas disponibles
    // ------------------------------------------------------------------

    @Test
    fun i_lasDisponiblesSonLaSalaMenosLasOcupadas() = runBlocking<Unit> {
        val funcion = catalogo.funciones.first()
        val capacidad = salas.contarButacasDeSala(funcion.salaId)
        val ocupadas = salas.contarOcupadas(funcion.id)

        val disponibles = salas.observarDisponibles(funcion.id, funcion.salaId).first()

        assertEquals(capacidad - ocupadas, disponibles.size)
        assertEquals(SeedData.BUTACAS_POR_SALA - 2, disponibles.size)
    }

    @Test
    fun i_ningunaButacaOcupadaApareceEntreLasDisponibles() = runBlocking<Unit> {
        val funcion = catalogo.funciones.first()

        val ocupadas = salas.observarOcupadas(funcion.id).first().map { it.id }.toSet()
        val disponibles = salas.observarDisponibles(funcion.id, funcion.salaId).first()

        assertTrue(disponibles.none { ocupadas.contains(it.id) })
    }

    @Test
    fun i_unaButacaOcupadaDesapareceDeLasDisponibles() = runBlocking<Unit> {
        val funcion = catalogo.funciones.first()
        val disponiblesAntes = salas.observarDisponibles(funcion.id, funcion.salaId).first()
        // Se parte de una butaca LIBRE: si se cogiera "A1" el seed ya la tendria
        // ocupada y el UNIQUE(funcion_id, butaca_id) reventaria el insert.
        val butaca = disponiblesAntes.first()
        val codigosAntes = disponiblesAntes.map { it.codigo }

        // Se ocupa con una reserva real para comprobar que el Flow reacciona.
        database.reservaDao().registrarReserva(
            reserva = ReservaEntity(
                codigo = "CINEMAX-9001",
                usuarioId = catalogo.usuarios.first().id,
                funcionId = funcion.id,
                fechaCompra = funcion.fecha,
                total = funcion.precioEntrada,
                estado = EstadoReserva.CONFIRMADA.valorPersistido
            ),
            butacas = listOf(butaca)
        )

        val codigosDespues = salas.observarDisponibles(funcion.id, funcion.salaId).first().map { it.codigo }
        assertTrue(codigosAntes.contains(butaca.codigo))
        assertEquals(codigosAntes.size - 1, codigosDespues.size)
        assertFalse(codigosDespues.contains(butaca.codigo))
    }

    // ------------------------------------------------------------------
    // Revalidacion puntual (la que usa la transaccion de reserva)
    // ------------------------------------------------------------------

    @Test
    fun laRevalidacionInformaQueButacasYaEstanOcupadas() = runBlocking<Unit> {
        val funcion = catalogo.funciones.first()
        val salaId = funcion.salaId
        val ocupadasDelSeed = SeedData.reservasDemo.first { it.funcion == 0 }.butacas
        val idsOcupadas = ocupadasDelSeed.map { codigo -> catalogo.butaca(salaId, codigo).id }
        val libres = catalogo.butacas(salaId).map { it.id } - idsOcupadas.toSet()

        val ocupadas = salas.butacasOcupadas(funcion.id, idsOcupadas)
        val libresRechazados = salas.butacasOcupadas(funcion.id, libres.take(2))

        assertEquals(ocupadasDelSeed.size, ocupadas.size)
        assertTrue(ocupadas.all { butaca -> idsOcupadas.contains(butaca.id) })
        assertTrue(libresRechazados.isEmpty())
    }

    @Test
    fun laRevalidacionDeUnaListaVaciaNoConsultaNada() = runBlocking<Unit> {
        assertTrue(salas.butacasOcupadas(catalogo.funciones.first().id, emptyList()).isEmpty())
    }
}
