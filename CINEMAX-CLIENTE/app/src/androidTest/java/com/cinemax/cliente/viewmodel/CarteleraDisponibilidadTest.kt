package com.cinemax.cliente.viewmodel

import com.cinemax.cliente.data.BaseDePrueba
import com.cinemax.cliente.data.Catalogo
import com.cinemax.cliente.data.funcionLibre
import com.cinemax.cliente.data.local.ButacaEntity
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.FuncionEntity
import com.cinemax.cliente.data.ocupacionSembrada
import com.cinemax.cliente.data.repository.RepositoriosCliente
import com.cinemax.cliente.data.repository.ResultadoReserva
import com.cinemax.cliente.data.repository.SolicitudReserva
import com.cinemax.cliente.state.CarteleraUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * FASE 7 - Disponibilidad de la cartelera: de `butaca_reservas` a "quedan 78 de 80".
 *
 * Comprueba las tres cosas que la pantalla necesita del ViewModel:
 * - la disponibilidad inicial sale de lo que hay REALMENTE ocupado en Room;
 * - cuando alguien compra, SOLO baja el contador de esa funcion;
 * - el mapa se actualiza por si solo, sin volver a pedir la cartelera.
 *
 * ## Ojo con el seed
 * El catalogo demo siembra 3 reservas de ejemplo ([BaseDePrueba.ocupacionSembrada]):
 * dos CONFIRMADAS que ocupan butacas y una CANCELADA que no. Estas pruebas NO pueden
 * suponer "0 ocupadas en todas partes": comprarian butacas que el seed ya vendio y el
 * repositorio las rechazaria. Por eso las que compran usan [funcionLibre].
 */
class CarteleraDisponibilidadTest {

    private lateinit var database: CineMaxClienteDatabase
    private lateinit var alcance: CoroutineScope
    private lateinit var viewModel: CarteleraViewModel
    private lateinit var catalogo: Catalogo
    private lateinit var ocupadas: Map<Int, Int>

    @Before
    fun setUp() = runBlocking<Unit> {
        database = BaseDePrueba.abrirEnMemoria()
        catalogo = BaseDePrueba.sembrarYLeer(database)
        ocupadas = ocupacionSembrada(database)
        alcance = CoroutineScope(Dispatchers.IO)
        viewModel = CarteleraViewModel(
            peliculas = RepositoriosCliente.peliculas(database),
            funciones = RepositoriosCliente.funciones(database),
            salas = RepositoriosCliente.salas(database),
            alcance = alcance
        )
    }

    @After
    fun tearDown() {
        alcance.cancel()
        database.close()
    }

    private suspend fun esperarExito(
        delFiltro: CarteleraUiState.Exito.() -> Boolean = { true }
    ): CarteleraUiState.Exito = withTimeout(10_000) {
        viewModel.uiState.first { it is CarteleraUiState.Exito && it.delFiltro() }
    } as CarteleraUiState.Exito

    private suspend fun esperarOcupadas(
        funcionId: Int,
        ocupadas: Int
    ): Map<Int, Int> = withTimeout(10_000) {
        viewModel.ocupadasPorFuncion.first { mapa -> (mapa[funcionId] ?: 0) == ocupadas }
    }

    private suspend fun comprar(
        usuarioId: Int,
        funcion: FuncionEntity,
        butacas: List<ButacaEntity>
    ): ResultadoReserva =
        RepositoriosCliente.reservas(database).registrar(
            SolicitudReserva(
                usuarioId = usuarioId,
                funcionId = funcion.id,
                butacaIds = butacas.map { it.id },
                total = funcion.precioEntrada * butacas.size,
                fechaCompra = "2026-03-10"
            )
        )

    @Test
    fun laDisponibilidadInicialReflejaLoQueElSeedYaVendi() = runBlocking<Unit> {
        esperarExito()
        val mapa = esperarOcupadas(catalogo.funcionDePrograma(0).id, 2)

        val esperada = mapOf(
            catalogo.funcionDePrograma(0).id to 2,
            catalogo.funcionDePrograma(6).id to 3
        )

        assertEquals(esperada, mapa)
        // La tercera reserva del seed esta CANCELADA: no ocupa butaca, asi que su
        // funcion no aparece en el mapa de ocupacion.
        assertNull(mapa[catalogo.funcionDePrograma(12).id])
    }

    @Test
    fun unaFuncionSinReservasEmpiezaConTodaLaCapacidadDeLaSala() = runBlocking<Unit> {
        esperarExito()
        val funcion = catalogo.funcionLibre(ocupadas)
        val capacidad = catalogo.sala(funcion.salaId).capacidad

        val disponibilidad = viewModel.disponibilidadDe(capacidad, funcion.id)

        assertEquals(capacidad, disponibilidad.libres)
        assertEquals(0, disponibilidad.ocupadas)
        assertFalse(disponibilidad.agotada)
    }

    @Test
    fun unaFuncionConReservasDelSeedArrancaYaDescontadas() = runBlocking<Unit> {
        esperarExito()
        val funcion = catalogo.funcionDePrograma(0)
        val capacidad = catalogo.sala(funcion.salaId).capacidad
        val yaOcupadas = ocupadas.getValue(funcion.id)

        val disponibilidad = viewModel.disponibilidadDe(capacidad, funcion.id)

        assertEquals(yaOcupadas, disponibilidad.ocupadas)
        assertEquals(capacidad - yaOcupadas, disponibilidad.libres)
    }

    @Test
    fun unaCompraReduceSoloLaDisponibilidadDeSuFuncion() = runBlocking<Unit> {
        esperarExito()
        val usuario = catalogo.usuarios.first()
        val libres = catalogo.funciones.filter { (ocupadas[it.id] ?: 0) == 0 }
        val comprada = libres.first()
        val intacta = libres[1]
        val butacas = catalogo.butacas(comprada.salaId).take(3)

        val resultado = comprar(usuario.id, comprada, butacas)
        assertTrue(resultado.toString(), resultado is ResultadoReserva.Exito)

        val mapa = esperarOcupadas(comprada.id, butacas.size)

        assertEquals(0, mapa[intacta.id] ?: 0)
        assertEquals(
            catalogo.sala(intacta.salaId).capacidad,
            viewModel.disponibilidadDe(catalogo.sala(intacta.salaId).capacidad, intacta.id).libres
        )
    }

    @Test
    fun laOcupacionSeActualizaEnVivoSinVolverAPedirLaCartelera() = runBlocking<Unit> {
        esperarExito()
        val usuario = catalogo.usuarios.first()
        val funcion = catalogo.funcionLibre(ocupadas)
        val butacas = catalogo.butacas(funcion.salaId).take(1)

        // Antes de comprar se espera a que el flujo de ocupacion este emitiendo, para
        // que la afirmacion sea sobre la REACTIVIDAD y no sobre un caso afortunado.
        esperarOcupadas(funcion.id, 0)

        val resultado = comprar(usuario.id, funcion, butacas)
        assertTrue(resultado.toString(), resultado is ResultadoReserva.Exito)

        val mapa = esperarOcupadas(funcion.id, 1)
        assertEquals(1, mapa[funcion.id])
    }

    @Test
    fun unaFuncionAgotadaSeMarcaComoTal() = runBlocking<Unit> {
        esperarExito()
        val usuario = catalogo.usuarios.first()
        val funcion = catalogo.funcionLibre(ocupadas)
        val capacidad = catalogo.sala(funcion.salaId).capacidad
        val todas = catalogo.butacas(funcion.salaId)

        val resultado = comprar(usuario.id, funcion, todas)
        assertTrue(resultado.toString(), resultado is ResultadoReserva.Exito)
        esperarOcupadas(funcion.id, capacidad)

        val disponibilidad = viewModel.disponibilidadDe(capacidad, funcion.id)

        assertEquals(0, disponibilidad.libres)
        assertTrue(disponibilidad.agotada)
    }

    @Test
    fun elCambioDeFiltroNoMeteLaCarteleraEnCargando() = runBlocking<Unit> {
        esperarExito()
        val ocupadasAntes = esperarOcupadas(catalogo.funcionDePrograma(0).id, 2)
        val sede = catalogo.sedes.first()

        viewModel.filtrarPorSede(sede.id)
        val exito = esperarExito { filtro.sedeId == sede.id }

        // La disponibilidad sobrevive al cambio de filtro: es un `StateFlow` propio,
        // no parte del estado de la consulta, para que filtrar no la haga parpadear.
        assertEquals(ocupadasAntes, viewModel.ocupadasPorFuncion.value)
        assertTrue(exito.hayFiltros)
        assertEquals(sede.id, exito.filtro.sedeId)
        assertTrue(exito.funciones.isNotEmpty())
        assertTrue(exito.funciones.all { it.funcion.sedeId == sede.id })
    }
}
