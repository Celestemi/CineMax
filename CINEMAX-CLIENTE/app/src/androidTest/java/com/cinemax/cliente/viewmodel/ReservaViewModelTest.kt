package com.cinemax.cliente.viewmodel

import com.cinemax.cliente.data.BaseDePrueba
import com.cinemax.cliente.data.Catalogo
import com.cinemax.cliente.data.aSesionDePrueba
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.ReservaEntity
import com.cinemax.cliente.data.repository.RepositoriosCliente
import com.cinemax.cliente.data.repository.SalaButacaRepository
import com.cinemax.cliente.model.EstadoReserva
import com.cinemax.cliente.state.MotivoValidacionReserva
import com.cinemax.cliente.state.ReservaUiState
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
 * FASE 5 - Casos K a P: la compra de punta a punta.
 *
 * `ReservaViewModel` recibe la sesion por `StateFlow` desde `AuthViewModel`; aqui
 * se simula con un `MutableStateFlow` para poder abrir y cerrar sesion en cada
 * prueba sin tocar la base.
 */
class ReservaViewModelTest {

    private lateinit var database: CineMaxClienteDatabase
    private lateinit var alcance: CoroutineScope
    private lateinit var sesion: MutableStateFlow<com.cinemax.cliente.state.UsuarioSesion?>
    private lateinit var viewModel: ReservaViewModel
    private lateinit var salas: SalaButacaRepository
    private lateinit var catalogo: Catalogo

    private var funcionId: Int = 0
    private var salaId: Int = 0
    private var precio: Double = 0.0
    private var libres: List<Int> = emptyList()

    @Before
    fun setUp() = runBlocking<Unit> {
        database = BaseDePrueba.abrirEnMemoria()
        catalogo = BaseDePrueba.sembrarYLeer(database)
        alcance = CoroutineScope(Dispatchers.IO)
        salas = RepositoriosCliente.salas(database)
        val cliente = catalogo.usuarios.first { it.usuario == "cliente" }
        sesion = MutableStateFlow(cliente.aSesionDePrueba())
        viewModel = ReservaViewModel(
            reservas = RepositoriosCliente.reservas(database),
            funciones = RepositoriosCliente.funciones(database),
            salas = salas,
            sesion = sesion,
            reloj = { "2026-05-01" },
            alcance = alcance
        )

        val funcion = catalogo.funciones.first()
        funcionId = funcion.id
        salaId = funcion.salaId
        precio = funcion.precioEntrada
        libres = catalogo.butacas(salaId).map { it.id } -
            salas.observarOcupadas(funcionId).first().map { it.id }.toSet()
    }

    @After
    fun tearDown() {
        alcance.cancel()
        database.close()
    }

    private suspend fun esperar(objetivo: (ReservaUiState) -> Boolean): ReservaUiState =
        withTimeout(10_000) { viewModel.uiState.first { objetivo(it) } }

    private suspend fun cotizado(): ReservaUiState.Calculando = esperar {
        it is ReservaUiState.Calculando || it is ReservaUiState.ValidacionInvalida
    }.let { estado ->
        assertTrue("Se esperaba cotizacion, fue $estado", estado is ReservaUiState.Calculando)
        estado as ReservaUiState.Calculando
    }

    // ------------------------------------------------------------------
    // K) Calcular el total
    // ------------------------------------------------------------------

    @Test
    fun k_elTotalEsPrecioPorCantidad() = runBlocking<Unit> {
        viewModel.preparar(funcionId, libres.take(3))

        val estado = cotizado()

        assertEquals(3, estado.cantidad)
        assertEquals(precio * 3, estado.total, 0.0001)
    }

    @Test
    fun k_unaButacaSumaElPrecioDeUnaEntrada() = runBlocking<Unit> {
        viewModel.preparar(funcionId, libres.take(1))

        val estado = cotizado()

        assertEquals(1, estado.cantidad)
        assertEquals(precio, estado.total, 0.0001)
    }

    @Test
    fun k_laCotizacionMuestraLosCodigosDeLasButacas() = runBlocking<Unit> {
        val elegidas = libres.take(2)
        viewModel.preparar(funcionId, elegidas)

        val estado = cotizado()

        val codigosEsperados = elegidas
            .map { id -> catalogo.butacas(salaId).first { it.id == id }.codigo }
            .sorted()
        assertEquals(codigosEsperados, estado.codigosButacas.sorted())
    }

    // ------------------------------------------------------------------
    // L) Reserva valida
    // ------------------------------------------------------------------

    @Test
    fun l_unaReservaValidaSeConfirmaYDevuelveCodigo() = runBlocking<Unit> {
        viewModel.preparar(funcionId, libres.take(2))
        val cotizacion = cotizado()

        viewModel.confirmar()
        val estado = esperar { it is ReservaUiState.Exito } as ReservaUiState.Exito

        assertTrue(estado.codigo.startsWith("CINEMAX-"))
        assertEquals(2, estado.cantidad)
        assertEquals(cotizacion.total, estado.total, 0.0001)
        assertEquals(2, estado.codigosButacas.size)
        assertEquals(funcionId, estado.reserva.funcionId)
    }

    @Test
    fun l_laReservaQuedaPersistidaConSusButacas() = runBlocking<Unit> {
        val elegidas = libres.take(2)
        viewModel.preparar(funcionId, elegidas)
        cotizado()

        viewModel.confirmar()
        val estado = esperar { it is ReservaUiState.Exito } as ReservaUiState.Exito

        val enBase = database.reservaDao().buscarPorCodigo(estado.codigo)
        assertNotNull(enBase)
        assertEquals(estado.reserva.id, enBase!!.id)
        assertEquals(2, database.reservaDao().contarButacasDeReserva(enBase.id))
        assertTrue(elegidas.all { id -> salas.observarOcupadas(funcionId).first().any { it.id == id } })
    }

    @Test
    fun l_laReservaQuedaAsociadaAlUsuarioDeLaSesion() = runBlocking<Unit> {
        val cliente = catalogo.usuarios.first { it.usuario == "cliente" }
        viewModel.preparar(funcionId, libres.take(1))
        cotizado()

        viewModel.confirmar()
        val estado = esperar { it is ReservaUiState.Exito } as ReservaUiState.Exito

        assertEquals(cliente.id, estado.reserva.usuarioId)
    }

    @Test
    fun l_confirmarDosVecesNoCreaDosReservas() = runBlocking<Unit> {
        viewModel.preparar(funcionId, libres.take(2))
        cotizado()
        val antes = database.reservaDao().contar()

        viewModel.confirmar()
        esperar { it is ReservaUiState.Exito }

        // Tras el exito la seleccion se consume: un segundo confirmar no reescribe.
        viewModel.confirmar()
        val estado = esperar { it is ReservaUiState.ValidacionInvalida } as ReservaUiState.ValidacionInvalida

        assertEquals(MotivoValidacionReserva.SinFuncion, estado.motivo)
        assertEquals(antes + 1, database.reservaDao().contar())
    }

    // ------------------------------------------------------------------
    // M) Sin butacas
    // ------------------------------------------------------------------

    @Test
    fun m_sinButacasNoSePuedeConfirmar() = runBlocking<Unit> {
        // El seed ya dejo 3 reservas: lo que se comprueba es que esta pantalla
        // no añada ninguna, no que la tabla este vacia.
        val antes = database.reservaDao().contar()
        viewModel.preparar(funcionId, emptyList())

        val estado = esperar { it is ReservaUiState.ValidacionInvalida } as ReservaUiState.ValidacionInvalida

        assertEquals(MotivoValidacionReserva.SinButacas, estado.motivo)
        assertTrue(estado.requiereButacas)
        assertEquals(antes, database.reservaDao().contar())
    }

    @Test
    fun m_confirmarSinPrepararNadaNoInventaUnaReserva() = runBlocking<Unit> {
        val antes = database.reservaDao().contar()

        viewModel.confirmar()
        val estado = esperar { it is ReservaUiState.ValidacionInvalida } as ReservaUiState.ValidacionInvalida

        assertTrue(estado.motivo is MotivoValidacionReserva.SinFuncion)
        assertEquals(antes, database.reservaDao().contar())
    }

    // ------------------------------------------------------------------
    // N) Funcion inexistente o inactiva
    // ------------------------------------------------------------------

    @Test
    fun n_unaFuncionInexistenteSeRechaza() = runBlocking<Unit> {
        val antes = database.reservaDao().contar()

        viewModel.preparar(999_999, libres.take(1))
        val estado = esperar { it is ReservaUiState.ValidacionInvalida } as ReservaUiState.ValidacionInvalida

        assertEquals(MotivoValidacionReserva.FuncionInvalida, estado.motivo)
        assertEquals(antes, database.reservaDao().contar())
    }

    @Test
    fun n_unaFuncionQueSeDesactivaSeRechaza() = runBlocking<Unit> {
        viewModel.preparar(funcionId, libres.take(1))
        cotizado()

        database.funcionDao().actualizar(catalogo.funcion(funcionId).copy(activa = false))
        viewModel.confirmar()

        val estado = esperar { it is ReservaUiState.ValidacionInvalida } as ReservaUiState.ValidacionInvalida

        assertEquals(MotivoValidacionReserva.FuncionInvalida, estado.motivo)
    }

    // ------------------------------------------------------------------
    // O) Sin sesion
    // ------------------------------------------------------------------

    @Test
    fun o_sinSesionNoSePuedeConfirmar() = runBlocking<Unit> {
        viewModel.preparar(funcionId, libres.take(1))
        cotizado()
        sesion.value = null
        val antes = database.reservaDao().contar()

        viewModel.confirmar()
        val estado = esperar { it is ReservaUiState.ValidacionInvalida } as ReservaUiState.ValidacionInvalida

        assertEquals(MotivoValidacionReserva.SinSesion, estado.motivo)
        assertTrue(estado.requiereLogin)
        assertEquals(antes, database.reservaDao().contar())
    }

    @Test
    fun o_trasCerrarLaSesionSePuedeVolverAComprar() = runBlocking<Unit> {
        val cliente = catalogo.usuarios.first { it.usuario == "cliente" }
        viewModel.preparar(funcionId, libres.take(1))
        cotizado()
        sesion.value = null
        viewModel.confirmar()
        esperar { it is ReservaUiState.ValidacionInvalida }

        sesion.value = cliente.aSesionDePrueba()
        viewModel.confirmar()
        val estado = esperar { it is ReservaUiState.Exito } as ReservaUiState.Exito

        assertEquals(1, estado.cantidad)
    }

    // ------------------------------------------------------------------
    // P) La butaca se ocupa entre la cotizacion y la confirmacion
    // ------------------------------------------------------------------

    @Test
    fun p_unaButacaOcupadaDuranteLaCompraSeRechaza() = runBlocking<Unit> {
        val elegidas = libres.take(2)
        viewModel.preparar(funcionId, elegidas)
        cotizado()

        // Otra persona compra la segunda butaca de la seleccion.
        val intrusa = catalogo.butacas(salaId).first { it.id == elegidas[1] }
        database.reservaDao().registrarReserva(
            reserva = ReservaEntity(
                codigo = "CINEMAX-8001",
                usuarioId = catalogo.usuarios.first { it.usuario == "andres" }.id,
                funcionId = funcionId,
                fechaCompra = catalogo.funcion(funcionId).fecha,
                total = precio,
                estado = EstadoReserva.CONFIRMADA.valorPersistido
            ),
            butacas = listOf(intrusa)
        )

        viewModel.confirmar()
        val estado = esperar { it is ReservaUiState.ValidacionInvalida || it is ReservaUiState.Exito } as ReservaUiState.ValidacionInvalida

        assertTrue(
            "Motivo inesperado: ${estado.motivo}",
            estado.motivo is MotivoValidacionReserva.ButacasNoDisponibles ||
                estado.motivo is MotivoValidacionReserva.Rechazada
        )
        assertTrue(estado.requiereButacas)
    }

    @Test
    fun p_noSeReservaNingunaButacaSiUnaYaEstaOcupada() = runBlocking<Unit> {
        val elegidas = libres.take(2)
        viewModel.preparar(funcionId, elegidas)
        cotizado()
        val ocupadasAntes = salas.contarOcupadas(funcionId)

        database.reservaDao().registrarReserva(
            reserva = ReservaEntity(
                codigo = "CINEMAX-8002",
                usuarioId = catalogo.usuarios.first { it.usuario == "andres" }.id,
                funcionId = funcionId,
                fechaCompra = catalogo.funcion(funcionId).fecha,
                total = precio,
                estado = EstadoReserva.CONFIRMADA.valorPersistido
            ),
            butacas = listOf(catalogo.butacas(salaId).first { it.id == elegidas[1] })
        )
        // Se cuenta DESPUES de la compra ajena: el objetivo es que `confirmar` no
        // añada la suya, no que la tabla siga como estaba antes del intruso.
        val trasLaCompraAjena = database.reservaDao().contar()
        viewModel.confirmar()
        esperar { it is ReservaUiState.ValidacionInvalida }

        assertEquals(trasLaCompraAjena, database.reservaDao().contar())
        assertEquals(ocupadasAntes + 1, salas.contarOcupadas(funcionId))
    }

    // ------------------------------------------------------------------
    // Ciclo de vida
    // ------------------------------------------------------------------

    @Test
    fun limpiarVuelveAlEstadoInicial() = runBlocking<Unit> {
        viewModel.preparar(funcionId, libres.take(1))
        cotizado()

        viewModel.limpiar()

        assertTrue(viewModel.uiState.value is ReservaUiState.Inicial)
    }

    @Test
    fun laCotizacionUsaLaFechaDelRelojInyectado() = runBlocking<Unit> {
        viewModel.preparar(funcionId, libres.take(1))
        cotizado()

        viewModel.confirmar()
        val estado = esperar { it is ReservaUiState.Exito } as ReservaUiState.Exito

        assertEquals("2026-05-01", estado.reserva.fechaCompra)
    }
}
