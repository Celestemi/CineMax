package com.cinemax.peru.state

import com.cinemax.peru.model.Butaca
import com.cinemax.peru.model.Funcion
import com.cinemax.peru.model.Reserva

data class ReservaUiState(
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val funcionSeleccionada: Funcion? = null,
    val butacas: List<Butaca> = emptyList(),
    val butacasDisponibles: List<Butaca> = emptyList(),
    val butacasSeleccionadas: List<Butaca> = emptyList(),
    val total: Double = 0.0,
    val estadoCompra: EstadoCompra = EstadoCompra.INICIAL,
    val reservaConfirmada: Reserva? = null,
    val mensaje: String? = null
)

enum class EstadoCompra {
    INICIAL,
    SELECCIONANDO,
    COMPRANDO,
    COMPLETADA,
    ERROR
}