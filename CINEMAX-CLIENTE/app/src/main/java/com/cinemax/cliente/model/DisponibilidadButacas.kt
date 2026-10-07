package com.cinemax.cliente.model

/**
 * FASE 7 - Disponibilidad de butacas de UNA funcion, tal y como la muestra la
 * cartelera.
 *
 * La cartelera necesita un unico dato por tarjeta ("quedan 78 de 80"). Sacarlo
 * con la capacidad de la sala y el recuento de `butaca_reservas` de ESA funcion
 * cumple la regla de negocio de siempre: una butaca se ocupa para una FUNCION,
 * nunca para una sala.
 *
 * Es codigo PURO, sin Room ni Android, para poder comprobarlo desde un test JVM.
 */
data class DisponibilidadButacas(
    val capacidad: Int,
    val ocupadas: Int
) {
    /**
     * Butacas que todavia se pueden elegir.
     *
     * Recortado a cero: si el catalogo se editase a mano y una funcion tuviera mas
     * `butaca_reservas` que butacas, la UI debe mostrar 0 y no un numero negativo.
     */
    val libres: Int
        get() = (capacidad - ocupadas).coerceAtLeast(0)

    /** `true` si ya no queda ninguna butaca: la cartelera lo marca como "agotada". */
    val agotada: Boolean
        get() = libres <= 0

    /** `true` si la funcion tiene poche vacia. */
    val vacia: Boolean
        get() = capacidad <= 0

    companion object {
        /**
         * Construye la disponibilidad de la funcion [funcionId] a partir del mapa
         * `funcionId -> ocupadas` que publica `SalaButacaRepository`.
         *
         * Si la funcion no aparece en el mapa es que nadie ha comprado en ella, de
         * modo que [ocupadas] es 0.
         */
        fun de(capacidadSala: Int, ocupadasPorFuncion: Map<Int, Int>, funcionId: Int):
            DisponibilidadButacas =
            DisponibilidadButacas(
                capacidad = capacidadSala.coerceAtLeast(0),
                ocupadas = (ocupadasPorFuncion[funcionId] ?: 0).coerceAtLeast(0)
            )
    }
}