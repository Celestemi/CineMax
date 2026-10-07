package com.cinemax.cliente.state

import com.cinemax.cliente.data.local.FuncionCompleta
import com.cinemax.cliente.data.local.PeliculaEntity
import com.cinemax.cliente.data.local.SedeEntity
import com.cinemax.cliente.model.FiltroCartelera

/**
 * FASE 5 - Estados de [CarteleraViewModel].
 *
 * Sealed en vez de una bolsa de booleanos (`cargando`, `error`, `lista`) para que
 * la UI no pueda mostrar dos estados contradictorios a la vez y para que cada test
 * pueda afirmar exactamente en que situacion esta la pantalla.
 *
 * ```
 * Inicial  -> Cargando -> Exito
 *                  |         |
 *                  |         +-> (nuevo filtro) -> Cargando -> Exito
 *                  +-> Error  -> (reintento)    -> Cargando -> Exito
 * ```
 */
sealed class CarteleraUiState {

    /** La app acaba de abrir la pantalla: aun no se ha consultado nada. */
    data object Inicial : CarteleraUiState()

    /** Consulta a Room en curso. Incluye el filtro con el que se esta cargando. */
    data class Cargando(val filtro: FiltroCartelera = FiltroCartelera.SIN_FILTROS) : CarteleraUiState()

    /**
     * Cartelera cargada.
     *
     * [funciones] es la lista YA filtrada, no la cartelera completa: los filtros
     * se aplican en SQL, no en el Composable.
     */
    data class Exito(
        val funciones: List<FuncionCompleta> = emptyList(),
        val filtro: FiltroCartelera = FiltroCartelera.SIN_FILTROS
    ) : CarteleraUiState() {
        /** `true` si hay al menos un criterio activo. */
        val hayFiltros: Boolean get() = filtro.hayAlguno

        /** `true` si el filtro dejo la cartelera sin resultados. */
        val vacia: Boolean get() = funciones.isEmpty()

        val totalFunciones: Int get() = funciones.size

        /** Peliculas unicas de la cartelera filtrada, en orden de aparicion. */
        val peliculas: List<PeliculaEntity>
            get() = funciones.map { it.pelicula }.distinctBy { it.id }
    }

    /** Fallo de lectura de la cartelera. Se puede reintentar con el mismo filtro. */
    data class Error(val mensaje: String) : CarteleraUiState()
}

/**
 * FASE 5 - Valores disponibles para los selectores de la barra de filtros.
 *
 * Van aparte de [CarteleraUiState] porque salen de tres `Flow` distintos
 * (`peliculas.genero`, `funciones.fecha`, `sedes.nombre`) y porque cambiarlos no
 * debe poner la cartelera en estado "cargando".
 */
data class OpcionesFiltro(
    val generos: List<String> = emptyList(),
    val fechas: List<String> = emptyList(),
    val sedes: List<SedeEntity> = emptyList()
) {
    val vacio: Boolean
        get() = generos.isEmpty() && fechas.isEmpty() && sedes.isEmpty()
}
