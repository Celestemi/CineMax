package com.cinemax.cliente.viewmodel

/**
 * FASE 5 - Operacion de negocio del Cliente.
 *
 * Reune TODO el calculo y TODA la validacion puras de la compra, sin Room, sin
 * Android y sin Compose. Esto cumple la exigencia de la guia: la logica de
 * negocio se ejecuta en el ViewModel y no en los Composables.
 *
 * Es `object` sin estado, por lo que se puede ejercitar desde un test JVM sin
 * levantar la base de datos ni el looper de instrumentacion.
 */
object CalculoReserva {

    /** Motivos por los que una cotizacion NO puede calcularse. */
    enum class ErrorCalculo(val mensaje: String) {
        PRECIO_INVALIDO("El precio de la entrada debe ser mayor a S/ 0.00"),
        CANTIDAD_INVALIDA("Selecciona al menos una butaca para continuar"),
        TOTAL_NO_REPRESENTABLE("El total calculado no es un importe valido")
    }

    /** Resultado de [calcularTotal]: o un importe, o el motivo del rechazo. */
    sealed class Resultado {
        data class Valido(val total: Double) : Resultado()
        data class Invalido(val error: ErrorCalculo) : Resultado()

        /** `true` solo para [Valido]. Permite abreviar en el ViewModel. */
        val esValido: Boolean
            get() = this is Valido
    }

    /**
     * OPERACION DE NEGOCIO CENTRAL DE LA FASE 5.
     *
     *     TOTAL = precioEntrada x cantidadDeButacas
     *
     * Ejemplo: precio 15.0 x 3 butacas = 45.0.
     *
     * Validaciones obligatorias (punto 4.D y 4.E de la guia):
     * - [precioEntrada] debe ser `> 0` (tambien se rechazan `NaN` e infinito).
     * - [cantidad] debe ser `> 0`.
     *
     * Si alguna falla devuelve [Resultado.Invalido] y el ViewModel NO registra la
     * reserva.
     */
    fun calcularTotal(precioEntrada: Double, cantidad: Int): Resultado = when {
        precioEntrada.isNaN() || precioEntrada.isInfinite() || precioEntrada <= 0.0 ->
            Resultado.Invalido(ErrorCalculo.PRECIO_INVALIDO)

        cantidad <= 0 ->
            Resultado.Invalido(ErrorCalculo.CANTIDAD_INVALIDA)

        else -> {
            val bruto = precioEntrada * cantidad
            when {
                bruto.isNaN() || bruto.isInfinite() ->
                    Resultado.Invalido(ErrorCalculo.TOTAL_NO_REPRESENTABLE)

                else -> Resultado.Valido(redondearMonto(bruto))
            }
        }
    }

    /**
     * Defensa adicional del `ReservaRepository` antes de insertar: un `total` que
     * llegue ya calculado desde otra capa debe seguir siendo un importe valido.
     */
    fun esTotalValido(total: Double): Boolean =
        !total.isNaN() && !total.isInfinite() && total > 0.0

    /**
     * Redondeo a dos decimales, que es la precision del sol peruano.
     *
     * Se hace en `Double` de forma explicita (`x * 100`, redondeo, `x / 100`) en
     * lugar de confiar en la coma flotante: `12.50 x 3` debe ser `37.5` y no un
     * `37.499999999999996`.
     */
    fun redondearMonto(valor: Double): Double = Math.round(valor * 100.0) / 100.0
}
