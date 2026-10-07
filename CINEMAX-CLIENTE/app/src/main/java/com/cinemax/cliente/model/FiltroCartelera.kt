package com.cinemax.cliente.model

/**
 * FASE 5 - Filtros de la cartelera.
 *
 * Los tres filtros son independientes y combinables, de modo que `null` significa
 * "sin restriccion en este criterio" y nunca "sin resultados". La combinacion
 * genero + fecha + sede la resuelve `FuncionDao.filtrarCompletas`, que ya estaba
 * preparado en Fase 3 con parametros anulables.
 *
 * Objeto de datos puro: no depende de Room ni de Android, por lo que se puede
 * construir desde un Composable, desde un ViewModel y desde un test JVM.
 */
data class FiltroCartelera(
    val genero: String? = null,
    val fecha: String? = null,
    val sedeId: Int? = null
) {

    /** `true` si se aplica al menos un criterio; la UI lo usa para mostrar "quitar filtros". */
    val hayAlguno: Boolean
        get() = genero != null || fecha != null || sedeId != null

    /** `true` solo cuando se estan usando los TRES criterios a la vez. */
    val hayCombinacionCompleta: Boolean
        get() = genero != null && fecha != null && sedeId != null

    /**
     * Limpia los criterios que llegaron vacios desde la UI (un `TextField` vacio
     * llega como `""`, no como `null`). Sin esto, un `""` no encontraria nada
     * porque la consulta compararia `p.genero = ''`.
     */
    fun normalizado(): FiltroCartelera = copy(
        genero = genero?.trim()?.takeIf { it.isNotEmpty() },
        fecha = fecha?.trim()?.takeIf { it.isNotEmpty() },
        sedeId = sedeId?.takeIf { it > 0 }
    )

    companion object {
        /** Estado inicial: cartelera completa, sin filtros. */
        val SIN_FILTROS: FiltroCartelera = FiltroCartelera()
    }
}
