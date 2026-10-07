package com.cinemax.cliente.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FASE 5 - Generacion del codigo de reserva.
 *
 * Formato `CINEMAX-####`, el mismo que usa `SeedData`, porque
 * `reservas.codigo` es UNIQUE desde Fase 2 y el repositorio necesita poder
 * buscar el siguiente consecutivo libre.
 */
class CodigoReservaTest {

    @Test
    fun elCodigoMantieneElFormatoDelSeed() {
        assertEquals("CINEMAX-0001", CodigoReserva.generar(1))
        assertEquals("CINEMAX-0007", CodigoReserva.generar(7))
        assertEquals("CINEMAX-1234", CodigoReserva.generar(1234))
    }

    @Test
    fun elCodigoSeCompletaConCerosHastaCuatroDigitos() {
        assertEquals("CINEMAX-0042", CodigoReserva.generar(42))
        // "CINEMAX-" (8) + 4 digitos
        assertEquals(12, CodigoReserva.generar(42).length)
    }

    @Test
    fun elSiguienteConsecutivoAumentaEnUno() {
        assertEquals(2, CodigoReserva.siguiente(1))
        assertEquals(8, CodigoReserva.siguiente(7))
    }

    @Test
    fun esValidoAceptaSoloElFormatoPropio() {
        assertTrue(CodigoReserva.esValido("CINEMAX-0001"))
        assertTrue(CodigoReserva.esValido("  CINEMAX-0042  "))

        assertTrue(!CodigoReserva.esValido(null))
        assertTrue(!CodigoReserva.esValido(""))
        assertTrue(!CodigoReserva.esValido("CINEMAX-1"))
        assertTrue(!CodigoReserva.esValido("CINEMAX-00001"))
        assertTrue(!CodigoReserva.esValido("CINEMAX-000X"))
        assertTrue(!CodigoReserva.esValido("ADMIN-0001"))
        assertTrue(!CodigoReserva.esValido("cinemax-0001"))
    }

    @Test
    fun losIntentosCubrenUnaVentanaDeCodigosLibres() {
        // El repositorio prueba este numero de consecutivos antes de rendirse, de
        // modo que la busqueda de un codigo libre siempre termina.
        val generados = (0 until CodigoReserva.MAX_INTENTOS).map { CodigoReserva.generar(1 + it) }

        assertEquals(CodigoReserva.MAX_INTENTOS, generados.toSet().size)
        assertEquals("CINEMAX-0001", generados.first())
        assertEquals("CINEMAX-0005", generados.last())
    }
}
