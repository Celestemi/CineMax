package com.cinemax.cliente.state

import com.cinemax.cliente.data.local.ButacaEntity
import com.cinemax.cliente.data.local.FuncionEntity
import com.cinemax.cliente.data.local.PeliculaEntity
import com.cinemax.cliente.data.local.SalaEntity
import com.cinemax.cliente.data.local.SedeEntity
import com.cinemax.cliente.model.ButacaSeleccionable
import com.cinemax.cliente.model.EstadoButaca
import com.cinemax.cliente.viewmodel.CalculoReserva

/**
 * FASE 5 - Estados de [SeleccionButacasViewModel].
 *
 * ## La seleccion es temporal
 * `seleccionadas` vive SOLO en memoria del ViewModel. Ningun metodo de este
 * estado escribe en Room: las butacas se ocupan unicamente cuando
 * `ReservaViewModel.confirmar` registra la reserva. Por eso aqui no hay ningun
 * `insert`.
 *
 * ## El total ya viene calculado
 * [total] es el resultado de `CalculoReserva.calcularTotal`, no una multiplicacion
 * hecha en el Composable.
 */
sealed class SeleccionButacasUiState {

    /** Todavia no se ha pedido ninguna funcion. */
    data object Inicial : SeleccionButacasUiState()

    /** Leyendo funcion, sala y mapa de butacas desde Room. */
    data object Cargando : SeleccionButacasUiState()

    /** La funcion no existe o esta inactiva. */
    data object FuncionNoEncontrada : SeleccionButacasUiState()

    /** Mapa de butacas listo. */
    data class Exito(
        val funcion: FuncionEntity,
        val pelicula: PeliculaEntity,
        val sede: SedeEntity,
        val sala: SalaEntity,
        val butacas: List<ButacaSeleccionable> = emptyList(),
        val precioEntrada: Double = 0.0,
        val total: CalculoReserva.Resultado = CalculoReserva.Resultado.Invalido(
            CalculoReserva.ErrorCalculo.CANTIDAD_INVALIDA
        ),
        /** Aviso puntual para la UI (por ejemplo, una butaca ya ocupada). */
        val aviso: String? = null
    ) : SeleccionButacasUiState() {

        /** Ids de las butacas elegidas por el usuario, en orden de sala. */
        val seleccionadas: List<ButacaEntity>
            get() = butacas.filter { it.seleccionada }.map { it.butaca }

        val seleccionadasIds: Set<Int>
            get() = butacas.filter { it.seleccionada }.map { it.id }.toSet()

        val codigosSeleccionadas: List<String>
            get() = butacas.filter { it.seleccionada }.map { it.codigo }

        /** `true` si hay al menos una butaca seleccionada. */
        val haySeleccion: Boolean
            get() = butacas.any { it.seleccionada }

        /** Capacidad de la sala (= numero de butacas del mapa). */
        val capacidad: Int
            get() = butacas.size

        val ocupadas: Int
            get() = butacas.count { it.estado == EstadoButaca.OCUPADA }

        val libres: Int
            get() = butacas.count { it.estado == EstadoButaca.LIBRE }

        /** Importe a pagar, o `null` si la seleccion aun no es valida. */
        val importe: Double?
            get() = (total as? CalculoReserva.Resultado.Valido)?.total

        /** `true` si se puede pasar a la pantalla de compra. */
        val puedeContinuar: Boolean
            get() = haySeleccion && (importe ?: 0.0) > 0.0
    }

    /** Fallo de lectura. */
    data class Error(val mensaje: String) : SeleccionButacasUiState()
}
