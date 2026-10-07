package com.cinemax.cliente.state

import com.cinemax.cliente.data.local.FuncionCompleta
import com.cinemax.cliente.data.local.FuncionEntity
import com.cinemax.cliente.data.local.PeliculaEntity
import com.cinemax.cliente.data.local.SalaEntity
import com.cinemax.cliente.data.local.SedeEntity
import com.cinemax.cliente.model.FiltroCartelera
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FASE 5 - Reglas derivadas del estado de la cartelera y del detalle.
 *
 * La cartelera expone la lista YA filtrada (el filtro se aplico en SQL), asi que
 * estos tests fijan que la UI puede deducir "vacio" y "hay filtros" sin volver a
 * preguntar nada.
 */
class CarteleraUiStateTest {

    private val peliculaAccion = PeliculaEntity(
        id = 1, titulo = "Cerro Veloz", genero = "ACCION", clasificacionEdad = "14+",
        duracionMinutos = 128, sinopsis = "", posterUrl = "", trailerUrl = ""
    )
    private val peliculaTerror = PeliculaEntity(
        id = 2, titulo = "Caceria Nocturna", genero = "TERROR", clasificacionEdad = "18+",
        duracionMinutos = 104, sinopsis = "", posterUrl = "", trailerUrl = ""
    )
    private val sede = SedeEntity(id = 1, nombre = "CineMax Miraflores", distrito = "Miraflores", direccion = "")
    private val sala = SalaEntity(id = 1, sedeId = 1, nombre = "Sala 1", filas = 8, columnas = 10)

    private fun completa(id: Int, pelicula: PeliculaEntity) = FuncionCompleta(
        funcion = FuncionEntity(
            id = id, peliculaId = pelicula.id, sedeId = 1, salaId = 1,
            fecha = "2026-05-01", hora = "20:00", precioEntrada = 15.0, activa = true
        ),
        pelicula = pelicula,
        sede = sede,
        sala = sala
    )

    @Test
    fun sinFiltroNiFiltroAplicado() {
        val estado = CarteleraUiState.Exito()

        assertFalse(estado.hayFiltros)
        assertTrue(estado.vacia)
        assertEquals(0, estado.totalFunciones)
    }

    @Test
    fun conFuncionesLaCarteleraNoEstaVacia() {
        val estado = CarteleraUiState.Exito(
            funciones = listOf(completa(1, peliculaAccion), completa(2, peliculaTerror)),
            filtro = FiltroCartelera(genero = "ACCION")
        )

        assertTrue(estado.hayFiltros)
        assertFalse(estado.vacia)
        assertEquals(2, estado.totalFunciones)
    }

    @Test
    fun lasPeliculasDeLaCarteleraEstanSinDuplicar() {
        // Dos funciones de la misma pelicula no deben aparecer dos veces en el
        // carrusel de peliculas.
        val estado = CarteleraUiState.Exito(
            funciones = listOf(completa(1, peliculaAccion), completa(2, peliculaAccion), completa(3, peliculaTerror))
        )

        assertEquals(3, estado.totalFunciones)
        assertEquals(listOf(1, 2), estado.peliculas.map { it.id })
    }

    @Test
    fun unFiltroSinCoincidenciasEsVaciaPeroConservaElFiltro() {
        val filtro = FiltroCartelera(genero = "MUSICAL", fecha = "2026-05-01", sedeId = 9)
        val estado = CarteleraUiState.Exito(funciones = emptyList(), filtro = filtro)

        assertTrue(estado.vacia)
        assertTrue(estado.hayFiltros)
        assertEquals(filtro, estado.filtro)
    }

    @Test
    fun soloExitoTieneFunciones() {
        assertFalse(CarteleraUiState.Inicial is CarteleraUiState.Exito)
        assertFalse(CarteleraUiState.Cargando() is CarteleraUiState.Exito)
        assertFalse(CarteleraUiState.Error("fallo") is CarteleraUiState.Exito)
    }

    @Test
    fun cargandoRecuerdaElFiltroQueSeEstaAplicando() {
        val filtro = FiltroCartelera(genero = "DRAMA")
        val estado = CarteleraUiState.Cargando(filtro)

        assertEquals(filtro, estado.filtro)
    }

    // ------------------------------------------------------------------
    // Detalle
    // ------------------------------------------------------------------

    @Test
    fun elDetalleConsolidaLosPreciosDeSusFunciones() {
        val funciones = listOf(
            completa(1, peliculaAccion).copy(
                funcion = FuncionEntity(1, 1, 1, 1, "2026-05-01", "18:00", 12.0, true)
            ),
            completa(2, peliculaAccion).copy(
                funcion = FuncionEntity(2, 1, 1, 1, "2026-05-02", "20:00", 18.0, true)
            )
        )

        val estado = DetallePeliculaUiState.Exito(pelicula = peliculaAccion, funciones = funciones)

        assertEquals("Cerro Veloz", estado.titulo)
        assertEquals("ACCION", estado.genero)
        assertEquals(2, estado.totalFunciones)
        assertFalse(estado.sinFunciones)
        assertEquals(12.0, estado.precioMinimo!!, 0.0001)
        assertEquals(18.0, estado.precioMaximo!!, 0.0001)
    }

    @Test
    fun unDetalleSinFuncionesNoTienePrecios() {
        val estado = DetallePeliculaUiState.Exito(pelicula = peliculaTerror)

        assertTrue(estado.sinFunciones)
        assertEquals(0, estado.totalFunciones)
        assertEquals(null, estado.precioMinimo)
        assertEquals(null, estado.precioMaximo)
    }
}
