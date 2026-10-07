package com.cinemax.cliente.viewmodel

/**
 * FASE 5 - Generacion del codigo de reserva (`reservas.codigo`).
 *
 * El formato replica el que ya usa `SeedData` ("CINEMAX-0001") para que las
 * reservas creadas en pantalla y las reservas demo sean indistinguibles para el
 * usuario. `reservas.codigo` tiene indice UNIQUE desde Fase 2, asi que el
 * repositorio comprueba que el codigo este libre y reintenta con el siguiente
 * consecutivo.
 *
 * Funcion pura: no lee nada, no escribe nada y no depende de Room.
 */
object CodigoReserva {

    const val PREFIJO = "CINEMAX-"

    /** Digitos del consecutivo; 4 permite `CINEMAX-0001` .. `CINEMAX-9999`. */
    const val DIGITOS = 4

    /**
     * Intentos de busqueda de un codigo libre (uno por cada intento se prueba el
     * consecutivo y el siguiente). Es una garantia de terminacion: si el
     * `ReservaRepository` agota los intentos devuelve
     * `MotivoReserva.CODIGO_NO_DISPONIBLE` en lugar de entrar en un bucle.
     */
    const val MAX_INTENTOS = 5

    /** `CINEMAX-0007` para [consecutivo] = 7. */
    fun generar(consecutivo: Int): String =
        PREFIJO + consecutivo.toString().padStart(DIGITOS, '0')

    /** Siguiente consecutivo a probar tras una colision de codigo. */
    fun siguiente(consecutivo: Int): Int = consecutivo + 1

    /** `true` si el texto tiene el formato `CINEMAX-####`. */
    fun esValido(codigo: String?): Boolean {
        if (codigo == null) return false
        val limpio = codigo.trim()
        if (!limpio.startsWith(PREFIJO)) return false
        val consecutivo = limpio.removePrefix(PREFIJO)
        return consecutivo.length == DIGITOS && consecutivo.all { it.isDigit() }
    }
}
