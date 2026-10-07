package com.cinemax.cliente.state

import com.cinemax.cliente.data.local.FuncionCompleta
import com.cinemax.cliente.data.local.PeliculaEntity

/**
 * FASE 5 - Estados de [DetallePeliculaViewModel].
 *
 * `NoEncontrado` es un estado propio y no un `Error`: que la pelicula se haya
 * dado de baja o no exista nunca es un fallo tecnico, es una situacion normal en la
 * que la app debe mostrar "pelicula no disponible" y dejar volver a la cartelera.
 *
 * ```
 * Inicial -> Cargando -> Exito | NoEncontrado | Error
 * ```
 */
sealed class DetallePeliculaUiState {

    /** Todavia no se ha pedido ninguna pelicula. */
    data object Inicial : DetallePeliculaUiState()

    /** Leyendo pelicula y funciones desde Room. */
    data object Cargando : DetallePeliculaUiState()

    /** Pelicula cargada con su programacion completa. */
    data class Exito(
        val pelicula: PeliculaEntity,
        val funciones: List<FuncionCompleta> = emptyList()
    ) : DetallePeliculaUiState() {
        val titulo: String get() = pelicula.titulo
        val genero: String get() = pelicula.genero
        val totalFunciones: Int get() = funciones.size

        /** `true` si la pelicula esta en cartelera pero todavia no tiene funciones. */
        val sinFunciones: Boolean get() = funciones.isEmpty()

        /** Precios distintos de la funcion mas barata a la mas cara. */
        val precioMinimo: Double? get() = funciones.minOfOrNull { it.funcion.precioEntrada }
        val precioMaximo: Double? get() = funciones.maxOfOrNull { it.funcion.precioEntrada }
    }

    /** La pelicula no existe o esta dada de baja (`peliculas.activo = 0`). */
    data object NoEncontrado : DetallePeliculaUiState()

    /** Fallo de lectura. */
    data class Error(val mensaje: String) : DetallePeliculaUiState()
}
