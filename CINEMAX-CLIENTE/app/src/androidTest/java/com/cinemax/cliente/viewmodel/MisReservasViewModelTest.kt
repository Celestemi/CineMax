package com.cinemax.cliente.viewmodel

import com.cinemax.cliente.data.BaseDePrueba
import com.cinemax.cliente.data.Catalogo
import com.cinemax.cliente.data.aSesionDePrueba
import com.cinemax.cliente.data.clienteSinReservas
import com.cinemax.cliente.data.funcionLibre
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.FuncionEntity
import com.cinemax.cliente.data.local.UsuarioEntity
import com.cinemax.cliente.data.ocupacionSembrada
import com.cinemax.cliente.data.repository.RepositoriosCliente
import com.cinemax.cliente.data.repository.ResultadoReserva
import com.cinemax.cliente.data.repository.SolicitudReserva
import com.cinemax.cliente.model.EstadoReserva
import com.cinemax.cliente.state.MisReservasUiState
import com.cinemax.cliente.state.UsuarioSesion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * FASE 7 - "MIS RESERVAS" de punta a punta: la sesion decide QUE historial se ve
 * y el historial sale de las reservas REALES de Room.
 *
 * Lo importante no es que se muestren los datos, sino el AISLAMIENTO: el
 * `usuarioId` sale de [MutableStateFlow] de sesion, nunca de un parametro de la
 * pantalla, asi que es imposible que un usuario vea las reservas de otro.
 *
 * ## Ojo con el seed
 * El catalogo demo siembra 3 reservas de ejemplo: dos del primer cliente y una
 * CANCELADA del segundo. Por eso el historial "vacio" se prueba con
 * [clienteSinReservas] y las compras usan [funcionLibre]: si se usara el primer
 * cliente, su historial ya traeria las reservas del seed y las butacas compradas
 * pueden coincidir con las que el seed ya vendio.
 */
class MisReservasViewModelTest {

    private lateinit var database: CineMaxClienteDatabase
    private lateinit var alcance: CoroutineScope
    private lateinit var catalogo: Catalogo
    private lateinit var ocupadas: Map<Int, Int>

    @Before
    fun setUp() = runBlocking<Unit> {
        database = BaseDePrueba.abrirEnMemoria()
        catalogo = BaseDePrueba.sembrarYLeer(database)
        ocupadas = ocupacionSembrada(database)
        alcance = CoroutineScope(Dispatchers.IO)
    }

    @After
    fun tearDown() {
        alcance.cancel()
        database.close()
    }

    private fun crearViewModel(sesion: MutableStateFlow<UsuarioSesion?>): MisReservasViewModel =
        MisReservasViewModel(
            reservas = RepositoriosCliente.reservas(database),
            peliculas = RepositoriosCliente.peliculas(database),
            sesion = sesion,
            alcance = alcance
        )

    private suspend fun esperarExito(viewModel: MisReservasViewModel): MisReservasUiState.Exito =
        withTimeout(10_000) { viewModel.uiState.first { it is MisReservasUiState.Exito } }
            as MisReservasUiState.Exito

    private fun sesionDe(usuario: UsuarioEntity): MutableStateFlow<UsuarioSesion?> =
        MutableStateFlow(usuario.aSesionDePrueba())

    /** Compra [cuantas] butacas en una funcion que el seed deja libre. */
    private suspend fun comprar(
        usuario: UsuarioEntity,
        funcion: FuncionEntity,
        cuantas: Int
    ): String {
        val butacas = catalogo.butacas(funcion.salaId).take(cuantas)
        val resultado = RepositoriosCliente.reservas(database).registrar(
            SolicitudReserva(
                usuarioId = usuario.id,
                funcionId = funcion.id,
                butacaIds = butacas.map { it.id },
                total = funcion.precioEntrada * butacas.size,
                fechaCompra = "2026-03-10"
            )
        )
        assertTrue(
            "la compra de prueba debe aceptarse, fue $resultado",
            resultado is ResultadoReserva.Exito
        )
        return (resultado as ResultadoReserva.Exito).reserva.codigo
    }

    @Test
    fun unUsuarioSinComprasVeElHistorialVacioYNoUnError() = runBlocking<Unit> {
        val usuario = catalogo.clienteSinReservas(database)
        val viewModel = crearViewModel(sesionDe(usuario))

        val estado = esperarExito(viewModel)

        // El estado vacio es `Exito` con lista vacia, NO `Error`: no tener reservas
        // es el estado normal de quien acaba de registrarse.
        assertTrue(estado.vacio)
        assertEquals(0, estado.total)
    }

    @Test
    fun laReservaConfirmadaApareceConSuCodigoYSuTotalReales() = runBlocking<Unit> {
        val usuario = catalogo.clienteSinReservas(database)
        val sesion = sesionDe(usuario)
        val viewModel = crearViewModel(sesion)
        esperarExito(viewModel)

        val funcion = catalogo.funcionLibre(ocupadas)
        val codigo = comprar(usuario, funcion, cuantas = 2)
        val estado = esperarHasta(viewModel) { !it.vacio }

        val linea = estado.reservas.first { it.codigo == codigo }
        assertEquals(codigo, linea.codigo)
        assertEquals(funcion.precioEntrada * 2, linea.total, 0.0001)
        assertEquals(funcion.fecha, linea.fechaFuncion)
        assertEquals(funcion.hora, linea.horaFuncion)
        assertEquals(EstadoReserva.CONFIRMADA, linea.estado)
        assertTrue(linea.confirmada)
    }

    @Test
    fun elTituloMuestraEsElDeLaPeliculaDeLaFuncionReservada() = runBlocking<Unit> {
        val usuario = catalogo.clienteSinReservas(database)
        val viewModel = crearViewModel(sesionDe(usuario))
        esperarExito(viewModel)
        val funcion = catalogo.funcionLibre(ocupadas)
        comprar(usuario, funcion, cuantas = 1)

        val estado = esperarHasta(viewModel) { !it.vacio }
        val tituloEsperado = catalogo.pelicula(funcion.peliculaId).titulo

        assertEquals(tituloEsperado, estado.reservas.first().tituloPelicula)
    }

    @Test
    fun unUsuarioNoVeLasReservasDeOtro() = runBlocking<Unit> {
        val primero = catalogo.usuario("cliente")
        val segundo = catalogo.clienteSinReservas(database)
        val codigoAjeno = comprar(primero, catalogo.funcionLibre(ocupadas), cuantas = 1)

        val viewModel = crearViewModel(sesionDe(segundo))
        val estado = esperarExito(viewModel)

        // El segundo usuario no ha comprado nada: su historial esta vacio aunque
        // en la base haya una reserva. El `usuarioId` sale de la sesion.
        assertTrue(estado.vacio)
        assertFalse(estado.reservas.any { it.codigo == codigoAjeno })
    }

    @Test
    fun dosUsuariosConComprasVenCadaUnoLoSuyo() = runBlocking<Unit> {
        val primero = catalogo.clienteSinReservas(database)
        val segundo = catalogo.clienteSinReservasDistintoDe(primero)
        val libres = catalogo.funciones.filter { (ocupadas[it.id] ?: 0) == 0 }
        val codigoPrimero = comprar(primero, libres[0], cuantas = 1)
        val codigoSegundo = comprar(segundo, libres[1], cuantas = 1)

        val delPrimero = esperarHasta(crearViewModel(sesionDe(primero))) { !it.vacio }
        val delSegundo = esperarHasta(crearViewModel(sesionDe(segundo))) { !it.vacio }

        assertEquals(listOf(codigoPrimero), delPrimero.reservas.map { it.codigo })
        assertEquals(listOf(codigoSegundo), delSegundo.reservas.map { it.codigo })
    }

    @Test
    fun elHistorialSeVuelveACargarCuandoCambiaLaSesion() = runBlocking<Unit> {
        val primero = catalogo.clienteSinReservas(database)
        val segundo = catalogo.clienteSinReservasDistintoDe(primero)
        val libres = catalogo.funciones.filter { (ocupadas[it.id] ?: 0) == 0 }
        val codigoPrimero = comprar(primero, libres[0], cuantas = 1)
        val codigoSegundo = comprar(segundo, libres[1], cuantas = 1)

        val sesion = sesionDe(primero)
        val viewModel = crearViewModel(sesion)
        assertEquals(
            listOf(codigoPrimero),
            esperarHasta(viewModel) { !it.vacio }.reservas.map { it.codigo }
        )

        // Cambiar de usuario debe reconstruir el historial entero, no acumular el
        // anterior: `flatMapLatest` sobre la sesion es lo que lo garantiza.
        sesion.value = segundo.aSesionDePrueba()
        val estado = withTimeout(10_000) {
            viewModel.uiState.first { estado ->
                estado is MisReservasUiState.Exito &&
                    estado.reservas.any { it.codigo == codigoSegundo }
            }
        } as MisReservasUiState.Exito

        assertFalse(estado.reservas.any { it.codigo == codigoPrimero })
    }

    @Test
    fun alCerrarLaSesionElHistorialSeVacia() = runBlocking<Unit> {
        val usuario = catalogo.clienteSinReservas(database)
        comprar(usuario, catalogo.funcionLibre(ocupadas), cuantas = 1)
        val sesion = sesionDe(usuario)
        val viewModel = crearViewModel(sesion)
        esperarHasta(viewModel) { !it.vacio }

        sesion.value = null
        val estado = withTimeout(10_000) {
            viewModel.uiState.first { it is MisReservasUiState.Inicial }
        }

        // Sin sesion no hay nada que mostrar: el historial anterior no se queda
        // colgado en pantalla mientras `PantallaProtegida` devuelve al login.
        assertTrue(estado is MisReservasUiState.Inicial)
    }

    @Test
    fun sinSesionElHistorialNoArrancaEnCargandoNiEnError() = runBlocking<Unit> {
        val viewModel = crearViewModel(MutableStateFlow(null))

        val estado = withTimeout(10_000) {
            viewModel.uiState.first { it is MisReservasUiState.Inicial }
        }

        assertNotNull(estado)
    }

    @Test
    fun variasReservasDelMismoUsuarioAparecenTodas() = runBlocking<Unit> {
        val usuario = catalogo.clienteSinReservas(database)
        val viewModel = crearViewModel(sesionDe(usuario))
        esperarExito(viewModel)

        val libres = catalogo.funciones.filter { (ocupadas[it.id] ?: 0) == 0 }
        val codigos = listOf(
            comprar(usuario, libres[0], cuantas = 1),
            comprar(usuario, libres[1], cuantas = 2),
            comprar(usuario, libres[2], cuantas = 1)
        )

        val estado = esperarHasta(viewModel) { it.total == codigos.size }
        assertEquals(codigos.toSet(), estado.reservas.map { it.codigo }.toSet())
    }

    /** Segundo cliente del seed que tampoco tiene reservas, para las pruebas de aislamiento. */
    private suspend fun Catalogo.clienteSinReservasDistintoDe(
        usuario: UsuarioEntity
    ): UsuarioEntity = usuarios.first { it.id != usuario.id && sinReservas(it) }

    private suspend fun Catalogo.sinReservas(usuario: UsuarioEntity): Boolean =
        database.reservaDao().porUsuario(usuario.id).first().isEmpty()

    private suspend fun esperarHasta(
        viewModel: MisReservasViewModel,
        objetivo: (MisReservasUiState.Exito) -> Boolean
    ): MisReservasUiState.Exito = withTimeout(10_000) {
        viewModel.uiState.first { estado -> estado is MisReservasUiState.Exito && objetivo(estado) }
    } as MisReservasUiState.Exito
}
