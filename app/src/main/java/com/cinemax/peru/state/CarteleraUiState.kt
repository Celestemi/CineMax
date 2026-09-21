package com.cinemax.peru.state

import com.cinemax.peru.model.Funcion
import com.cinemax.peru.model.Genero
import com.cinemax.peru.model.Pelicula
import com.cinemax.peru.model.Sede

data class CarteleraUiState(
    val estado: EstadoCartelera = EstadoCartelera.INICIAL,
    val peliculas: List<Pelicula> = emptyList(),
    val sedes: List<Sede> = emptyList(),
    val funciones: List<Funcion> = emptyList(),
    val funcionesFiltradas: List<Funcion> = emptyList(),
    val generoSeleccionado: Genero? = null,
    val fechaSeleccionada: String? = null,
    val sedeSeleccionada: Int? = null,
    val textoBusqueda: String = "",
    val mensajeError: String? = null
)

enum class EstadoCartelera {
    INICIAL,
    CARGANDO,
    CARGADO,
    VACIO,
    ERROR
}