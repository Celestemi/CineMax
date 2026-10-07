package com.cinemax.cliente.viewmodel

import com.cinemax.cliente.data.BaseDePrueba
import com.cinemax.cliente.data.Catalogo
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.ReservaEntity
import com.cinemax.cliente.data.repository.RepositoriosCliente
import com.cinemax.cliente.data.repository.SalaButacaRepository
import com.cinemax.cliente.model.EstadoButaca
import com.cinemax.cliente.model.EstadoReserva
import com.cinemax.cliente.state.SeleccionButacasUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
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
 * FASE 5 - Casos G, H, I y J en la pantalla de eleccion de butacas: cargar el
 * mapa, refusal de las ocupadas y calculo del total en vivo.
 */
class SeleccionButacasViewModelTest {

    private lateinit var database: CineMaxClienteDatabase
    private lateinit var alcance: CoroutineScope
    private lateinit var viewModel: SeleccionButacasViewModel
    private lateinit var salas: SalaButacaRepository
    private lateinit var catalogo: Catalogo

    /** La funcion 0 del seed: tiene A1 y A2 ya ocupadas. */
    private var funcionConOcupadas: Int = 0
    private var salaId: Int = 0
    private var ocupadas: List<Int> = emptyList()
    private var libres: List<Int> = emptyList()

    @Before
    fun setUp() = runBlocking<Unit> {
        database = BaseDePrueba.abrirEnMemoria()
        catalogo = BaseDePrueba.sembrarYLeer(database)
        alcance = CoroutineScope(Dispatchers.IO)
        salas = RepositoriosCliente.salas(database)
        viewModel = SeleccionButacasViewModel(
            funciones = RepositoriosCliente.funciones(database),
            salas = salas,
            alcance = alcance
        )
        funcionConOcupadas = catalogo.funciones.first().id
        salaId = catalogo.funcion(funcionConOcupadas).salaId
        ocupadas = salas.observarOcupadas(funcionConOcupadas).first().map { it.id }
        libres = catalogo.butacas(salaId).map { it.id } - ocupadas.toSet()
    }

    @After
    fun tearDown() {
        alcance.cancel()
        database.close()
    }

    private suspend fun exito(): SeleccionButacasUiState.Exito = withTimeout(10_000) {
        viewModel.uiState.first { it is SeleccionButacasUiState.Exito }
    } as SeleccionButacasUiState.Exito

    private suspend fun esperar(objetivo: (SeleccionButacasUiState) -> Boolean): SeleccionButacasUiState =
        withTimeout(10_000) { viewModel.uiState.first { objetivo(it) } }

    // ------------------------------------------------------------------
    // G) Cargar el mapa
    // ------------------------------------------------------------------

    @Test
    fun g_laPantallaCargaElMapaDeButacas() = runBlocking<Unit> {
        viewModel.cargar(funcionConOcupadas)

        val estado = exito()

        assertEquals(funcionConOcupadas, estado.funcion.id)
        assertNotNull(estado.pelicula)
        assertNotNull(estado.sede)
        assertEquals(catalogo.butacas(salaId).size, estado.butacas.size)
        assertEquals(estado.butacas.size, estado.capacidad)
    }

    @Test
    fun g_unaFuncionInexistenteTerminaEnFuncionNoEncontrada() = runBlocking<Unit> {
        viewModel.cargar(999_999)

        val estado = esperar { it is SeleccionButacasUiState.FuncionNoEncontrada }

        assertTrue(estado is SeleccionButacasUiState.FuncionNoEncontrada)
    }

    @Test
    fun g_alAbrirNoHayNadaSeleccionadoNiTotal() = runBlocking<Unit> {
        viewModel.cargar(funcionConOcupadas)
        val estado = exito()

        assertEquals(0, estado.seleccionadas.size)
        // Sin seleccion el total todavia no es calculable, asi que no hay importe.
        assertEquals(null, estado.importe)
        assertFalse(estado.haySeleccion)
        assertFalse(estado.puedeContinuar)
        assertEquals(emptySet<Int>(), viewModel.seleccion.value)
    }

    // ------------------------------------------------------------------
    // H) Las ocupadas se distinguen de las libres
    // ------------------------------------------------------------------

    @Test
    fun h_lasButacasOcupadasAparecenComoOcupadas() = runBlocking<Unit> {
        viewModel.cargar(funcionConOcupadas)

        val estado = exito()

        assertEquals(ocupadas.size, estado.ocupadas)
        assertEquals(estado.butacas.size - estado.ocupadas, estado.libres)
        assertTrue(estado.butacas.filter { it.estado == EstadoButaca.OCUPADA }
            .all { ocupadas.contains(it.butaca.id) })
    }

    // ------------------------------------------------------------------
    // I) Seleccionar una butaca libre
    // ------------------------------------------------------------------

    @Test
    fun i_seleccionarUnaButacaLibreActualizaElTotal() = runBlocking<Unit> {
        viewModel.cargar(funcionConOcupadas)
        exito()
        val precio = catalogo.funcion(funcionConOcupadas).precioEntrada
        val butaca = libres.first()

        viewModel.seleccionar(butaca)
        val estado = esperar { it is SeleccionButacasUiState.Exito && it.seleccionadas.size == 1 } as SeleccionButacasUiState.Exito

        assertEquals(1, estado.seleccionadas.size)
        assertEquals(precio, estado.importe!!, 0.0001)
        assertTrue(estado.puedeContinuar)
        assertEquals(setOf(butaca), viewModel.seleccion.value)
        assertEquals(estado.butacas.first { it.butaca.id == butaca }.estado, EstadoButaca.SELECCIONADA)
    }

    @Test
    fun i_seleccionarVariasButacasMultiplicaElTotal() = runBlocking<Unit> {
        viewModel.cargar(funcionConOcupadas)
        exito()
        val precio = catalogo.funcion(funcionConOcupadas).precioEntrada
        val elegidas = libres.take(3)

        elegidas.forEach { viewModel.seleccionar(it) }
        val estado = esperar { it is SeleccionButacasUiState.Exito && it.seleccionadas.size == 3 } as SeleccionButacasUiState.Exito

        assertEquals(3, estado.seleccionadas.size)
        assertEquals(precio * 3, estado.importe!!, 0.0001)
        assertEquals(elegidas.toSet(), viewModel.seleccion.value)
    }

    @Test
    fun i_deseleccionarRestaElButacaDelTotal() = runBlocking<Unit> {
        viewModel.cargar(funcionConOcupadas)
        exito()
        val precio = catalogo.funcion(funcionConOcupadas).precioEntrada
        val elegidas = libres.take(2)
        elegidas.forEach { viewModel.seleccionar(it) }
        esperar { it is SeleccionButacasUiState.Exito && it.seleccionadas.size == 2 }

        viewModel.deseleccionar(elegidas.first())
        val estado = esperar { it is SeleccionButacasUiState.Exito && it.seleccionadas.size == 1 } as SeleccionButacasUiState.Exito

        assertEquals(precio, estado.importe!!, 0.0001)
        assertEquals(setOf(elegidas.last()), viewModel.seleccion.value)
    }

    @Test
    fun i_alternarSeleccionaYDeselecciona() = runBlocking<Unit> {
        viewModel.cargar(funcionConOcupadas)
        exito()
        val butaca = libres.first()

        viewModel.alternar(butaca)
        esperar { it is SeleccionButacasUiState.Exito && it.seleccionadas.size == 1 }
        viewModel.alternar(butaca)
        val estado = esperar { it is SeleccionButacasUiState.Exito && it.seleccionadas.size == 0 } as SeleccionButacasUiState.Exito

        assertFalse(estado.puedeContinuar)
    }

    @Test
    fun i_seleccionarTodasLasLibresIgnoraLasOcupadas() = runBlocking<Unit> {
        viewModel.cargar(funcionConOcupadas)
        exito()
        val precio = catalogo.funcion(funcionConOcupadas).precioEntrada

        viewModel.seleccionarTodasLasLibres()
        val estado = esperar { it is SeleccionButacasUiState.Exito && it.seleccionadas.size > 0 } as SeleccionButacasUiState.Exito

        assertEquals(estado.butacas.size - ocupadas.size, estado.seleccionadas.size)
        assertEquals(precio * estado.seleccionadas.size, estado.importe!!, 0.0001)
        assertTrue(viewModel.seleccion.value.none { ocupadas.contains(it) })
    }

    @Test
    fun limpiarSeleccionVuelveACero() = runBlocking<Unit> {
        viewModel.cargar(funcionConOcupadas)
        exito()
        libres.take(2).forEach { viewModel.seleccionar(it) }
        esperar { it is SeleccionButacasUiState.Exito && it.seleccionadas.size == 2 }

        viewModel.limpiarSeleccion()
        val estado = esperar { it is SeleccionButacasUiState.Exito && it.seleccionadas.size == 0 } as SeleccionButacasUiState.Exito

        assertTrue(viewModel.seleccion.value.isEmpty())
        assertFalse(estado.puedeContinuar)
    }

    // ------------------------------------------------------------------
    // J) No se puede seleccionar una butaca ocupada
    // ------------------------------------------------------------------

    @Test
    fun j_seleccionarUnaButacaOcupadaSeIgnoraConAviso() = runBlocking<Unit> {
        viewModel.cargar(funcionConOcupadas)
        exito()
        val ocupada = ocupadas.first()

        viewModel.seleccionar(ocupada)
        val estado = esperar { it is SeleccionButacasUiState.Exito && it.aviso != null } as SeleccionButacasUiState.Exito

        assertEquals(0, estado.seleccionadas.size)
        assertTrue(viewModel.seleccion.value.isEmpty())
        assertNotNull(estado.aviso)
        assertTrue(estado.aviso!!.isNotBlank())
    }

    @Test
    fun j_elAvisoDesapareceEnLaSiguienteSeleccionValida() = runBlocking<Unit> {
        viewModel.cargar(funcionConOcupadas)
        exito()
        viewModel.seleccionar(ocupadas.first())
        esperar { it is SeleccionButacasUiState.Exito && it.aviso != null }

        viewModel.seleccionar(libres.first())
        val estado = esperar { it is SeleccionButacasUiState.Exito && it.seleccionadas.size == 1 } as SeleccionButacasUiState.Exito

        assertEquals(null, estado.aviso)
    }

    @Test
    fun j_unaButacaQueSeOcupaDesapareceDeLaSeleccion() = runBlocking<Unit> {
        viewModel.cargar(funcionConOcupadas)
        exito()
        val butaca = libres.first()
        viewModel.seleccionar(butaca)
        esperar { it is SeleccionButacasUiState.Exito && it.seleccionadas.size == 1 }

        // Otra persona compra esa butaca mientras el usuario decide.
        database.reservaDao().registrarReserva(
            reserva = ReservaEntity(
                codigo = "CINEMAX-8001",
                usuarioId = catalogo.usuarios.first().id,
                funcionId = funcionConOcupadas,
                fechaCompra = catalogo.funcion(funcionConOcupadas).fecha,
                total = 15.0,
                estado = EstadoReserva.CONFIRMADA.valorPersistido
            ),
            butacas = listOf(catalogo.butaca(salaId, catalogo.butacas(salaId).first { it.id == butaca }.codigo))
        )

        val estado = esperar {
            it is SeleccionButacasUiState.Exito && it.seleccionadas.size == 0 && it.ocupadas == ocupadas.size + 1
        } as SeleccionButacasUiState.Exito

        assertTrue(viewModel.seleccion.value.isEmpty())
        assertNotNull(estado.aviso)
    }

    // ------------------------------------------------------------------
    // Limpiar
    // ------------------------------------------------------------------

    @Test
    fun limpiarVuelveAlEstadoInicial() = runBlocking<Unit> {
        viewModel.cargar(funcionConOcupadas)
        exito()

        viewModel.limpiar()

        assertTrue(viewModel.uiState.value is SeleccionButacasUiState.Inicial)
        assertTrue(viewModel.seleccion.value.isEmpty())
    }
}
