package com.cinemax.cliente.model

/**
 * Estado de una reserva.
 *
 * FASE 3: los valores coinciden con las constantes que ya vivian en
 * `ReservaEntity` (Fase 2: `ESTADO_CONFIRMADA` / `ESTADO_CANCELADA`), por lo que
 * la columna `reservas.estado` sigue siendo TEXT con los mismos valores y no
 * requiere migracion.
 */
enum class EstadoReserva(val nombre: String) {
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada");

    val valorPersistido: String
        get() = name

    companion object {
        val POR_DEFECTO: EstadoReserva = CONFIRMADA

        fun desdeTexto(texto: String): EstadoReserva? =
            entries.firstOrNull { it.name == texto.trim().uppercase() }
    }
}
