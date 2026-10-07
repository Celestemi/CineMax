package com.cinemax.cliente.state

import com.cinemax.cliente.data.local.ButacaEntity
import com.cinemax.cliente.data.local.ReservaEntity
import com.cinemax.cliente.model.CotizacionReserva
import com.cinemax.cliente.viewmodel.CalculoReserva

/**
 * FASE 5 - Motivo por el que `ReservaViewModel` no deja confirmar.
 *
 * Cada causa tiene su propio tipo porque la UI responde de forma distinta: un
 * [SinSesion] manda al login, unas [ButacasNoDisponibles] devuelven al mapa de
 * butacas y un [CalculoInvalido] solo muestran un aviso.
 */
sealed class MotivoValidacionReserva(val mensaje: String) {

    /** Punto 4.O: no hay sesion abierta, luego no se puede comprar. */
    data object SinSesion : MotivoValidacionReserva("Inicia sesion para reservar")

    /** Punto 4.A: no se ha elegido funcion. */
    data object SinFuncion : MotivoValidacionReserva("Selecciona una funcion antes de reservar")

    /** Punto 4.N: la funcion no existe o ya no esta activa. */
    data object FuncionInvalida : MotivoValidacionReserva("La funcion seleccionada ya no esta disponible")

    /** Punto 4.B: no se ha elegido ninguna butaca. */
    data object SinButacas : MotivoValidacionReserva("Selecciona al menos una butaca")

    /** Punto 4.D / 4.E: el calculo del total no es valido. */
    data class CalculoInvalido(val error: CalculoReserva.ErrorCalculo) :
        MotivoValidacionReserva(error.mensaje)

    /** Punto 4.F / 4.C: al revalidar contra Room, alguna butaca ya no esta libre. */
    data class ButacasNoDisponibles(val codigos: List<String>) : MotivoValidacionReserva(
        "Estas butacas ya fueron ocupadas: ${codigos.joinToString(", ")}. Elige otras"
    )

    /** El repositorio veto la escritura. */
    data class Rechazada(val motivo: com.cinemax.cliente.data.repository.MotivoReserva) :
        MotivoValidacionReserva(motivo.mensaje)
}

/**
 * FASE 5 - Estados de [ReservaViewModel].
 *
 * ```
 * Inicial
 *   -> Cargando        (leyendo funcion y butacas de Room)
 *   -> Calculando      (total ya calculado en el ViewModel)
 *        -> ValidacionInvalida  (falta funcion / butacas / total no valido)
 *        -> Registrando          (transaccion abierta en el repositorio)
 *             -> Exito
 *             -> ValidacionInvalida (butaca ocupada / veto del repositorio)
 *             -> Error
 * ```
 *
 * `Cargando` y `Calculando` se han separado aunque la guia los reuna: mientras se
 * lee de Room no hay ningun dato que pintar, y un unico estado que mezcla "no se
 * sabe todavia el total" con "ya se sabe el total" obligaria a la UI a comprobar
 * nulls. Es la adaptacion de nombres que la propia guia permite.
 */
sealed class ReservaUiState {

    /** Pantalla de compra recien abierta, sin nada preparado. */
    data object Inicial : ReservaUiState()

    /** Leyendo la funcion y las butacas de Room para poder cotizar. */
    data object Cargando : ReservaUiState()

    /** Cotizacion lista: el total ya esta calculado por el ViewModel. */
    data class Calculando(val cotizacion: CotizacionReserva) : ReservaUiState() {
        val total: Double get() = cotizacion.total
        val cantidad: Int get() = cotizacion.cantidad
        val codigosButacas: List<String> get() = cotizacion.codigosButacas
    }

    /** No se ha podido confirmar: la compra se puede reintentar tras corregir. */
    data class ValidacionInvalida(
        val motivo: MotivoValidacionReserva,
        val mensaje: String = motivo.mensaje
    ) : ReservaUiState() {
        /** `true` si el problema es la sesion y hay que volver al login. */
        val requiereLogin: Boolean get() = motivo is MotivoValidacionReserva.SinSesion

        /** `true` si hay que volver al mapa de butacas. */
        val requiereButacas: Boolean
            get() = motivo is MotivoValidacionReserva.ButacasNoDisponibles ||
                motivo is MotivoValidacionReserva.SinButacas
    }

    /** Transaccion de registro en curso: el boton de confirmar esta deshabilitado. */
    data object Registrando : ReservaUiState()

    /** Reserva confirmada y persistida en Room. */
    data class Exito(
        val reserva: ReservaEntity,
        val butacas: List<ButacaEntity>,
        val cotizacion: CotizacionReserva
    ) : ReservaUiState() {
        val codigo: String get() = reserva.codigo
        val total: Double get() = reserva.total
        val cantidad: Int get() = butacas.size
        val codigosButacas: List<String> get() = butacas.map { it.codigo }
    }

    /** Fallo tecnico. La transaccion se ha revertido: no hay reserva parcial. */
    data class Error(val mensaje: String) : ReservaUiState()
}
