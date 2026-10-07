package com.cinemax.cliente.viewmodel

import com.cinemax.cliente.data.BaseDePrueba
import com.cinemax.cliente.data.Catalogo
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.repository.RepositoriosCliente
import com.cinemax.cliente.state.DetallePeliculaUiState
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
 * FASE 5 - Caso F: la ficha de una pelicula con sus funciones, leida de Room.
 */
class DetallePeliculaViewModelTest {

    private lateinit var database: CineMaxClienteDatabase
    private lateinit var alcance: CoroutineScope
    private lateinit var viewModel: DetallePeliculaViewModel
    private lateinit var catalogo: Catalogo

    @Before
    fun setUp() = runBlocking<Unit> {
        database = BaseDePrueba.abrirEnMemoria()
        catalogo = BaseDePrueba.sembrarYLeer(database)
        alcance = CoroutineScope(Dispatchers.IO)
        viewModel = DetallePeliculaViewModel(
            peliculas = RepositoriosCliente.peliculas(database),
            funciones = RepositoriosCliente.funciones(database),
            alcance = alcance
        )
    }

    @After
    fun tearDown() {
        alcance.cancel()
        database.close()
    }

    private suspend fun esperar(objetivo: (DetallePeliculaUiState) -> Boolean): DetallePeliculaUiState =
        withTimeout(10_000) { viewModel.uiState.first { objetivo(it) } }

    @Test
    fun f_laFichaCargaLaPeliculaYSusFunciones() = runBlocking<Unit> {
        val pelicula = catalogo.peliculas.first { candidata -> catalogo.funcionesDe(candidata.id).isNotEmpty() }
        val esperadas = catalogo.funcionesDe(pelicula.id)

        viewModel.cargar(pelicula.id)
        val estado = esperar { it is DetallePeliculaUiState.Exito } as DetallePeliculaUiState.Exito

        assertEquals(pelicula.titulo, estado.titulo)
        assertEquals(pelicula.genero, estado.genero)
        assertEquals(esperadas.size, estado.totalFunciones)
        assertFalse(estado.sinFunciones)
    }

    @Test
    fun f_cadaFuncionTraeSuSedeYSala() = runBlocking<Unit> {
        val pelicula = catalogo.peliculas.first { candidata -> catalogo.funcionesDe(candidata.id).isNotEmpty() }

        viewModel.cargar(pelicula.id)
        val estado = esperar { it is DetallePeliculaUiState.Exito } as DetallePeliculaUiState.Exito

        assertTrue(estado.funciones.all { it.sede.nombre.isNotBlank() })
        assertTrue(estado.funciones.all { it.sala.nombre.isNotBlank() })
        assertTrue(estado.funciones.all { it.pelicula.id == pelicula.id })
    }

    @Test
    fun f_elDetalleConsolidaElRangoDePrecios() = runBlocking<Unit> {
        val pelicula = catalogo.peliculas.first { candidata -> catalogo.funcionesDe(candidata.id).size > 1 }
        val precios = catalogo.funcionesDe(pelicula.id).map { it.precioEntrada }

        viewModel.cargar(pelicula.id)
        val estado = esperar { it is DetallePeliculaUiState.Exito } as DetallePeliculaUiState.Exito

        assertEquals(precios.min(), estado.precioMinimo!!, 0.0001)
        assertEquals(precios.max(), estado.precioMaximo!!, 0.0001)
    }

    @Test
    fun f_unaPeliculaSinFuncionesSeMuestraSinPrecios() = runBlocking<Unit> {
        // En el seed todas las peliculas tienen mas de una funcion, asi que se
        // desactivan TODAS las de una pelicula para dejarla sin cartelera.
        val pelicula = catalogo.peliculas.first { candidata -> catalogo.funcionesDe(candidata.id).isNotEmpty() }
        val funciones = catalogo.funcionesDe(pelicula.id)
        funciones.forEach { database.funcionDao().actualizar(it.copy(activa = false)) }

        viewModel.cargar(pelicula.id)
        val estado = esperar { it is DetallePeliculaUiState.Exito } as DetallePeliculaUiState.Exito

        assertTrue(estado.sinFunciones)
        assertEquals(0, estado.totalFunciones)
        assertEquals(null, estado.precioMinimo)
        assertEquals(null, estado.precioMaximo)
    }

    @Test
    fun f_unaPeliculaInexistenteTerminaEnNoEncontrado() = runBlocking<Unit> {
        viewModel.cargar(999_999)

        val estado = esperar { it is DetallePeliculaUiState.NoEncontrado }

        assertTrue(estado is DetallePeliculaUiState.NoEncontrado)
    }

    @Test
    fun f_unaPeliculaInactivaTerminaEnNoEncontrado() = runBlocking<Unit> {
        val pelicula = catalogo.peliculas.first()
        database.peliculaDao().actualizar(pelicula.copy(activo = false))

        viewModel.cargar(pelicula.id)

        val estado = esperar { it is DetallePeliculaUiState.NoEncontrado }
        assertTrue(estado is DetallePeliculaUiState.NoEncontrado)
    }

    @Test
    fun cargarOtraPeliculaRemplazaElDetalle() = runBlocking<Unit> {
        val primera = catalogo.peliculas.first { candidata -> catalogo.funcionesDe(candidata.id).isNotEmpty() }
        val segunda = catalogo.peliculas.last { candidata -> catalogo.funcionesDe(candidata.id).isNotEmpty() }

        viewModel.cargar(primera.id)
        esperar { it is DetallePeliculaUiState.Exito }
        viewModel.cargar(segunda.id)
        val estado = esperar {
            it is DetallePeliculaUiState.Exito && it.pelicula.id == segunda.id
        } as DetallePeliculaUiState.Exito

        assertEquals(segunda.titulo, estado.titulo)
        assertEquals(catalogo.funcionesDe(segunda.id).size, estado.totalFunciones)
    }

    @Test
    fun limpiarVuelveAlEstadoInicial() = runBlocking<Unit> {
        val pelicula = catalogo.peliculas.first()
        viewModel.cargar(pelicula.id)
        esperar { it is DetallePeliculaUiState.Exito }

        viewModel.limpiar()

        assertTrue(viewModel.uiState.value is DetallePeliculaUiState.Inicial)
    }

    @Test
    fun elDetalleArrancaEnInicial() {
        assertNotNull(viewModel.uiState.value)
        assertTrue(viewModel.uiState.value is DetallePeliculaUiState.Inicial)
    }
}
