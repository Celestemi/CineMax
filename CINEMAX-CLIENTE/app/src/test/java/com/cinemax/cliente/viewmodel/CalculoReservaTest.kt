package com.cinemax.cliente.viewmodel

import com.cinemax.cliente.viewmodel.CalculoReserva.ErrorCalculo
import com.cinemax.cliente.viewmodel.CalculoReserva.Resultado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FASE 5 - Caso K: la operacion de negocio `calcularTotal`.
 *
 *     TOTAL = precioEntrada x cantidadDeButacas
 *
 * Pruebas JVM puras: no se abre Room, no se toca el looper y no hace falta un
 * emulador. Es exactamente la ventaja de tener el calculo en una funcion sin
 * dependencias.
 */
class CalculoReservaTest {

    // ------------------------------------------------------------------
    // K.1) Precio valido + cantidad valida
    // ------------------------------------------------------------------

    @Test
    fun k1_precioYCantidadValidosDevuelvenElTotal() {
        val resultado = CalculoReserva.calcularTotal(precioEntrada = 15.0, cantidad = 3)

        assertEquals(Resultado.Valido(45.0), resultado)
    }

    @Test
    fun k1_elEjemploDeLaGuiaDaCuarentaYCinco() {
        // S/ 15 x 3 butacas = S/ 45
        val resultado = CalculoReserva.calcularTotal(15.0, 3) as Resultado.Valido

        assertEquals(45.0, resultado.total, 0.0001)
        assertEquals(45.0, resultado.total * 3 / 3, 0.0001)
        assertTrue(resultado.esValido)
    }

    @Test
    fun k1_unaSolaButacaDevuelveElPrecioDeEntrada() {
        assertEquals(Resultado.Valido(12.5), CalculoReserva.calcularTotal(12.50, 1))
    }

    @Test
    fun k1_preciosConDecimalesSeRedondeanADosCifras() {
        // 12.50 x 3 = 37.5 exacto en binario
        assertEquals(Resultado.Valido(37.5), CalculoReserva.calcularTotal(12.50, 3))
        // 11.00 x 3 = 33.0
        assertEquals(Resultado.Valido(33.0), CalculoReserva.calcularTotal(11.00, 3))
        // 14.50 x 4 = 58.0
        assertEquals(Resultado.Valido(58.0), CalculoReserva.calcularTotal(14.50, 4))
    }

    @Test
    fun k1_laComaFlotanteNoSeEscapaAlRedondeo() {
        // 33.333 x 3 = 99.999 -> debe quedar en 100.0 y no en 99.998999999999995
        val resultado = CalculoReserva.calcularTotal(33.333, 3) as Resultado.Valido

        assertEquals(100.0, resultado.total, 0.0001)
    }

    // ------------------------------------------------------------------
    // K.2) Precio cero (y negativo / NaN / infinito)
    // ------------------------------------------------------------------

    @Test
    fun k2_precioCeroSeRechaza() {
        val resultado = CalculoReserva.calcularTotal(precioEntrada = 0.0, cantidad = 3)

        assertEquals(Resultado.Invalido(ErrorCalculo.PRECIO_INVALIDO), resultado)
        assertTrue(!resultado.esValido)
    }

    @Test
    fun k2_precioNegativoSeRechaza() {
        assertEquals(
            Resultado.Invalido(ErrorCalculo.PRECIO_INVALIDO),
            CalculoReserva.calcularTotal(-15.0, 3)
        )
    }

    @Test
    fun k2_unPrecioNoNumeroSeRechaza() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { precio ->
            assertEquals(
                "El precio $precio deberia rechazarse",
                Resultado.Invalido(ErrorCalculo.PRECIO_INVALIDO),
                CalculoReserva.calcularTotal(precio, 1)
            )
        }
    }

    // ------------------------------------------------------------------
    // K.3) Cantidad cero (y negativa)
    // ------------------------------------------------------------------

    @Test
    fun k3_cantidadCeroSeRechaza() {
        val resultado = CalculoReserva.calcularTotal(precioEntrada = 15.0, cantidad = 0)

        assertEquals(Resultado.Invalido(ErrorCalculo.CANTIDAD_INVALIDA), resultado)
    }

    @Test
    fun k3_cantidadNegativaSeRechaza() {
        assertEquals(
            Resultado.Invalido(ErrorCalculo.CANTIDAD_INVALIDA),
            CalculoReserva.calcularTotal(15.0, -3)
        )
    }

    // ------------------------------------------------------------------
    // Combinaciones y mensajes
    // ------------------------------------------------------------------

    @Test
    fun siFalloElPrecioSeCompruebaAntesQueLaCantidad() {
        // Con precio 0 y cantidad 0 los dos son invalidos: el motivo que se informa
        // debe ser el del precio, para que la UI muestre siempre el mismo error
        // cuando el catalogo esta mal configurado.
        val resultado = CalculoReserva.calcularTotal(0.0, 0)

        assertEquals(Resultado.Invalido(ErrorCalculo.PRECIO_INVALIDO), resultado)
    }

    @Test
    fun todoMotivoDeRechazoTieneUnMensajeParaLaUI() {
        ErrorCalculo.entries.forEach { error ->
            assertTrue("Falta mensaje para $error", error.mensaje.isNotBlank())
        }
    }

    @Test
    fun esTotalValidoRechazaCeroNegativoYNoNumeros() {
        assertTrue(CalculoReserva.esTotalValido(0.01))
        assertTrue(CalculoReserva.esTotalValido(45.0))

        assertTrue(!CalculoReserva.esTotalValido(0.0))
        assertTrue(!CalculoReserva.esTotalValido(-45.0))
        assertTrue(!CalculoReserva.esTotalValido(Double.NaN))
        assertTrue(!CalculoReserva.esTotalValido(Double.POSITIVE_INFINITY))
    }

    @Test
    fun redondearMontoUsaDosDecimales() {
        assertEquals(0.0, CalculoReserva.redondearMonto(0.001), 0.00001)
        assertEquals(1.24, CalculoReserva.redondearMonto(1.2351), 0.00001)
        assertEquals(100.0, CalculoReserva.redondearMonto(99.999), 0.00001)
    }

    @Test
    fun elCalculoEsDeterminista() {
        // Misma entrada, misma salida: sin estado interno ni azar.
        repeat(5) {
            assertEquals(
                CalculoReserva.calcularTotal(13.50, 2),
                CalculoReserva.calcularTotal(13.50, 2)
            )
        }
    }
}
