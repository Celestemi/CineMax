package com.cinemax.peru.state

import com.cinemax.peru.model.Funcion
import com.cinemax.peru.model.Pelicula

data class DetallePeliculaUiState(
    val estado: EstadoDetalle = EstadoDetalle.INICIAL,
    val funcionSeleccionada: Funcion? = null,
    val pelicula: Pelicula? = null,
    val funcionesDePelicula: List<Funcion> = emptyList(),
    val mensajeError: String? = null
)

enum class EstadoDetalle {
    INICIAL,
    CARGANDO,
    CARGADO,
    VACIO,
    ERROR
}