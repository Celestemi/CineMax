package com.cinemax.cliente.model

import com.cinemax.cliente.data.local.ButacaEntity

/**
 * FASE 5 - Estado visual de una butaca dentro de la pantalla de seleccion.
 *
 * La logica de "que butacas puede tocar el usuario" NO vive aqui: la decide
 * `SeleccionButacasViewModel`. Este tipo solo transporta el resultado de esa
 * decision para que el Composable no tenga que recalcular nada.
 *
 * - [LIBRE]        -> seleccionable.
 * - [OCUPADA]      -> ya vendida en ESTA funcion; nunca seleccionable.
 * - [SELECCIONADA] -> libre y elegida por el usuario en esta pantalla.
 *
 * NOTA: la seleccion es temporal. Nada se escribe en Room hasta confirmar la
 * compra (`ReservaViewModel.confirmar`).
 */
enum class EstadoButaca {
    LIBRE,
    OCUPADA,
    SELECCIONADA
}

/** FASE 5 - Butaca concreta con su estado actual en la pantalla de seleccion. */
data class ButacaSeleccionable(
    val butaca: ButacaEntity,
    val estado: EstadoButaca
) {
    val id: Int get() = butaca.id
    val codigo: String get() = butaca.codigo
    val fila: String get() = butaca.fila
    val numero: Int get() = butaca.numero

    /** `true` si el usuario puede pulsarla. */
    val seleccionable: Boolean
        get() = estado != EstadoButaca.OCUPADA

    /** `true` si ya forma parte de la seleccion actual. */
    val seleccionada: Boolean
        get() = estado == EstadoButaca.SELECCIONADA
}
