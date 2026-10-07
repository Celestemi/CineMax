package com.cinemax.cliente.viewmodel

import com.cinemax.cliente.data.BaseDePrueba
import com.cinemax.cliente.data.Catalogo
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.repository.RepositoriosCliente
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * FASE 5 - Casos A a E en la capa de presentacion: los filtros de la cartelera
 * llegan desde la UI hasta el SQL y vuelven como un estado listo para pintar.
 *
 * Se usa un `CoroutineScope` propio con [Dispatchers.IO] para que las colecciones
 * de `Flow` funcionen igual que en la app, sin depender de un ViewModel real.
 */
class CarteleraViewModelTest {

    private lateinit var database: CineMaxClienteDatabase
    private lateinit var alcance: CoroutineScope
    private lateinit var viewModel: CarteleraViewModel
    private lateinit var catalogo: Catalogo

    @Before
    fun setUp() = runBlocking<Unit> {
        database = BaseDePrueba.abrirEnMemoria()
        catalogo = BaseDePrueba.sembrarYLeer(database)
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

    private suspend fun esperarEstado(
        objetivo: (CarteleraUiState) -> Boolean,
        descripcion: String
    ): CarteleraUiState = withTimeout(10_000) {
        viewModel.uiState.first { objetivo(it) }
    }

    private val esExito: (CarteleraUiState) -> Boolean = { it is CarteleraUiState.Exito }

    @Test
    fun a_laCarteleraArrancaCargandoYTerminaEnExito() = runBlocking<Unit> {
        val estado = esperarEstado(esExito, "carga inicial") as CarteleraUiState.Exito

        assertEquals(catalogo.funciones.size, estado.funciones.size)
        assertFalse(estado.vacia)
        assertFalse(estado.hayFiltros)
    }

    @Test
    fun b_filtrarPorGeneroReduceLaCartelera() = runBlocking<Unit> {
        esperarEstado(esExito, "carga inicial")
        val genero = catalogo.unGeneroConFunciones()
        val esperadas = catalogo.funcionesDeGenero(genero).size

        viewModel.filtrarPorGenero(genero)
        val estado = esperarEstado({ it is CarteleraUiState.Exito && it.hayFiltros }, "filtro por genero") as CarteleraUiState.Exito

        assertEquals(esperadas, estado.funciones.size)
        assertTrue(estado.funciones.all { it.pelicula.genero == genero })
        assertEquals(genero, viewModel.filtros.value.genero)
    }

    @Test
    fun c_filtrarPorFechaReduceLaCartelera() = runBlocking<Unit> {
        esperarEstado(esExito, "carga inicial")
        val fecha = catalogo.fechas().first()

        viewModel.filtrarPorFecha(fecha)
        val estado = esperarEstado({ it is CarteleraUiState.Exito && it.hayFiltros }, "filtro por fecha") as CarteleraUiState.Exito

        assertEquals(catalogo.funcionesDeFecha(fecha).size, estado.funciones.size)
        assertTrue(estado.funciones.all { it.funcion.fecha == fecha })
    }

    @Test
    fun d_filtrarPorSedeReduceLaCartelera() = runBlocking<Unit> {
        esperarEstado(esExito, "carga inicial")
        val sede = catalogo.sedes.first()

        viewModel.filtrarPorSede(sede.id)
        val estado = esperarEstado({ it is CarteleraUiState.Exito && it.hayFiltros }, "filtro por sede") as CarteleraUiState.Exito

        assertEquals(catalogo.funcionesDeSede(sede.id).size, estado.funciones.size)
        assertTrue(estado.funciones.all { it.funcion.sedeId == sede.id })
    }

    @Test
    fun e_losTresFiltrosSeCombinan() = runBlocking<Unit> {
        esperarEstado(esExito, "carga inicial")
        val genero = catalogo.unGeneroConFunciones()
        val fechas = catalogo.fechas()

        // Se busca la primera combinacion sede x fecha que tenga funciones del genero.
        var combinacion: Pair<Int, String>? = null
        for (sede in catalogo.sedes) {
            for (fecha in fechas) {
                if (catalogo.funcionesDeGenero(genero).any { it.sedeId == sede.id && it.fecha == fecha }) {
                    combinacion = sede.id to fecha
                    break
                }
            }
            if (combinacion != null) break
        }
        val (sedeId, fecha) = requireNotNull(combinacion) { "El seed no permite combinar los tres filtros" }

        viewModel.filtrar(genero = genero, fecha = fecha, sedeId = sedeId)
        val estado = esperarEstado(
            { it is CarteleraUiState.Exito && it.hayFiltros && it.filtro.sedeId == sedeId },
            "filtro combinado"
        ) as CarteleraUiState.Exito

        assertTrue(estado.funciones.isNotEmpty())
        assertTrue(estado.funciones.all { it.pelicula.genero == genero && it.funcion.fecha == fecha && it.funcion.sedeId == sedeId })
        assertEquals(genero, viewModel.filtros.value.genero)
        assertEquals(fecha, viewModel.filtros.value.fecha)
        assertEquals(sedeId, viewModel.filtros.value.sedeId)
    }

    @Test
    fun e_losFiltrosSeAcumulanYSeLimpian() = runBlocking<Unit> {
        esperarEstado(esExito, "carga inicial")
        val genero = catalogo.unGeneroConFunciones()
        val sede = catalogo.sedes.first()

        viewModel.filtrarPorGenero(genero)
        esperarEstado({ it is CarteleraUiState.Exito && it.hayFiltros }, "filtro por genero")
        viewModel.filtrarPorSede(sede.id)
        esperarEstado({ it is CarteleraUiState.Exito && it.hayFiltros && it.filtro.sedeId == sede.id }, "filtro por sede")

        assertEquals(genero, viewModel.filtros.value.genero)
        assertEquals(sede.id, viewModel.filtros.value.sedeId)

        viewModel.limpiarFiltros()
        val estado = esperarEstado({ it is CarteleraUiState.Exito && !it.hayFiltros }, "filtros limpiados") as CarteleraUiState.Exito

        assertEquals(catalogo.funciones.size, estado.funciones.size)
    }

    @Test
    fun unFiltroSinCoincidenciasMuestraLaCarteleraVacia() = runBlocking<Unit> {
        esperarEstado(esExito, "carga inicial")

        viewModel.filtrarPorFecha("1999-01-01")
        val estado = esperarEstado({ it is CarteleraUiState.Exito && it.vacia }, "sin coincidencias") as CarteleraUiState.Exito

        assertTrue(estado.vacia)
        assertTrue(estado.hayFiltros)
        assertEquals(0, estado.totalFunciones)
    }

    @Test
    fun quitarElGeneroDejaElRestoDelFiltro() = runBlocking<Unit> {
        esperarEstado(esExito, "carga inicial")
        val sede = catalogo.sedes.first()

        viewModel.filtrar(genero = catalogo.unGeneroConFunciones(), fecha = null, sedeId = sede.id)
        esperarEstado({ it is CarteleraUiState.Exito && it.filtro.genero != null }, "filtro con genero")

        viewModel.filtrarPorGenero(null)
        val estado = esperarEstado(
            { it is CarteleraUiState.Exito && it.filtro.genero == null && it.filtro.sedeId == sede.id },
            "genero quitado"
        ) as CarteleraUiState.Exito

        assertEquals(catalogo.funcionesDeSede(sede.id).size, estado.funciones.size)
    }

    @Test
    fun lasOpcionesDeFiltroVienenDeLoQueEstaEnCartelera() = runBlocking<Unit> {
        esperarEstado(esExito, "carga inicial")
        val opciones = withTimeout(10_000) { viewModel.opciones.first { it.fechas.isNotEmpty() && it.sedes.isNotEmpty() } }

        assertEquals(catalogo.fechas(), opciones.fechas)
        assertEquals(catalogo.sedes.size, opciones.sedes.size)
        assertEquals(catalogo.peliculas.map { it.genero }.distinct().sorted(), opciones.generos)
    }

    @Test
    fun unaFuncionInactivaDesapareceDeLaCarteleraEnVivo() = runBlocking<Unit> {
        esperarEstado(esExito, "carga inicial")
        val objetivo = catalogo.funciones.first()

        database.funcionDao().actualizar(objetivo.copy(activa = false))
        val estado = esperarEstado(
            { it is CarteleraUiState.Exito && it.funciones.none { f -> f.funcion.id == objetivo.id } },
            "funcion desactivada"
        ) as CarteleraUiState.Exito

        assertEquals(catalogo.funciones.size - 1, estado.funciones.size)
    }
}
