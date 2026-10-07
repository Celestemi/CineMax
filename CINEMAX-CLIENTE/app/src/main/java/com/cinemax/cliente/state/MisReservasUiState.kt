package com.cinemax.cliente.state

import com.cinemax.cliente.data.local.ReservaConDetalle
import com.cinemax.cliente.model.EstadoReserva

/**
 * FASE 7 - Una linea del historial "Mis Reservas".
 *
 * Es un modelo de PRESENTACION, no una entidad de Room: la pantalla no puede
 * pintar una [ReservaConDetalle] directamente porque necesita el TITULO de la
 * pelicula y la etiqueta legible del ESTADO, que en Room son respectivamente el
 * `pelicula_id` de otra tabla y el texto del `TypeConverter`.
 *
 * Todo lo que se muestra sale de la reserva REAL que se leyo de Room: el codigo
 * es el mismo que se escribio al confirmar y el total es el que quedo guardado.
 */
data class ReservaListada(
    val codigo: String,
    val fechaCompra: String,
    val estado: EstadoReserva,
    val total: Double,
    /** Titulo de la pelicula de la funcion reservada. */
    val tituloPelicula: String,
    /** `fecha` de la funcion, tal cual la guarda Room (ISO `yyyy-MM-dd`). */
    val fechaFuncion: String,
    /** `hora` de la funcion, tal cual la guarda Room. */
    val horaFuncion: String
) {
    /** "`fecha` · `hora`" de la funcion reservada. */
    val cuandoFuncion: String
        get() = "$fechaFuncion · $horaFuncion"

    val confirmada: Boolean
        get() = estado == EstadoReserva.CONFIRMADA
}

/**
 * FASE 7 - Estados de [com.cinemax.cliente.viewmodel.MisReservasViewModel].
 *
 * Mismo esquema que el resto de pantallas (cargando / error / vacio / exito) para
 * que "Mis Reservas" nunca se quede en blanco: un usuario recien registrado que
 * todavia no ha comprado ve un mensaje explicativo, no un hueco vacio.
 */
sealed class MisReservasUiState {

    /** Pantalla abierta, consulta todavia sin empezar. */
    data object Inicial : MisReservasUiState()

    /** Leyendo el historial desde Room. */
    data object Cargando : MisReservasUiState()

    /** Historial leido. [reservas] vacio significa "aun no has reservado nada". */
    data class Exito(val reservas: List<ReservaListada> = emptyList()) : MisReservasUiState() {
        val vacio: Boolean get() = reservas.isEmpty()
        val total: Int get() = reservas.size
    }

    /** Fallo de lectura. */
    data class Error(val mensaje: String) : MisReservasUiState()
}

/**
 * FASE 7 - Traduce una fila de [ReservaConDetalle] a [ReservaListada].
 *
 * Se deja como funcion suelta (y no como metodo del ViewModel) para poder
 * comprobar el mapeo desde un test JVM sin Room. Dos decisiones importantes:
 * - un estado desconocido en Room cae en `CONFIRMADA` en vez de romper la pantalla;
 * - si la pelicula ya no esta en el catalogo se muestra su id, no un hueco.
 */
fun ReservaConDetalle.aReservaListada(titulosPelicula: Map<Int, String>): ReservaListada =
    ReservaListada(
        codigo = reserva.codigo,
        fechaCompra = reserva.fechaCompra,
        estado = EstadoReserva.desdeTexto(reserva.estado) ?: EstadoReserva.POR_DEFECTO,
        total = reserva.total,
        tituloPelicula = titulosPelicula[funcion.peliculaId] ?: "Pelicula #${funcion.peliculaId}",
        fechaFuncion = funcion.fecha,
        horaFuncion = funcion.hora
    )