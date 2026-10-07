package com.cinemax.cliente.viewmodel

/**
 * FASE 5 - Traduccion de excepciones de la capa de datos a texto de UI.
 *
 * Objeto compartido por los cuatro ViewModels de negocio para que el texto de
 * error sea el mismo en cartelera, detalle, butacas y reserva, y para no
 * duplicar el `when` en cada uno.
 *
 * No sefiltran mensajes de SQLite a la UI: un `UNIQUE(funcion_id, butaca_id)`
 * violado lo traduce `ReservaRepository` a `MotivoReserva.BUTACAS_YA_OCUPADAS`
 * con un mensaje entendible, nunca a un texto tecnico de la base de datos.
 */
object ErrorDeCarga {

    const val MENSAJE_GENERICO = "No se pudo cargar la informacion. Intentalo de nuevo."

    /**
     * Devuelve SIEMPRE el mismo texto: Room envuelve sus fallos en
     * `SQLiteException` y la base de datos local no tiene una causa util que
     * Unlike a un error de red, no conviene enseñar el detalle tecnico.
     */
    fun mensaje(causa: Throwable): String = MENSAJE_GENERICO
}
