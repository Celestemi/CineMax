package com.cinemax.cliente.model

import com.cinemax.cliente.data.local.ButacaEntity
import com.cinemax.cliente.data.local.FuncionEntity
import com.cinemax.cliente.data.local.PeliculaEntity

/**
 * FASE 5 - Cotizacion de una compra, ya calculada y validada por el ViewModel.
 *
 * Es el unico objeto que `ReservaViewModel` entrega a la pantalla de compra: la
 * operacion de negocio
 *
 *     TOTAL = precioEntrada x cantidad
 *
 * ocurre en `CalculoReserva.calcularTotal`, NO en el Composable. La UI solo
 * pinta [total].
 */
data class CotizacionReserva(
    val funcion: FuncionEntity,
    val pelicula: PeliculaEntity,
    val butacas: List<ButacaEntity>,
    val precioEntrada: Double,
    val cantidad: Int,
    val total: Double,
    /**
     * FASE 7 - Nombre de la sede y de la sala de [funcion].
     *
     * Se anaden CON VALOR POR DEFECIDO para no romper ninguna construccion
     * anterior: `FuncionEntity` solo guarda `sedeId` y `salaId`, y la pantalla de
     * confirmacion tiene que poder mostrar "CineMax Miraflores" y no un numero.
     * Los rellena `ReservaViewModel` desde la misma `FuncionCompleta` que ya trae
     * sede y sala de Room, de modo que la UI no hace ninguna consulta nueva.
     */
    val nombreSede: String = "",
    val nombreSala: String = ""
) {
    /** "A1", "A2", ... en orden de sala. */
    val codigosButacas: List<String>
        get() = butacas.map { it.codigo }

    /** `true` si la seleccion tiene al menos una butaca. */
    val hayButacas: Boolean
        get() = butacas.isNotEmpty()
}
