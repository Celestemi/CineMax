package com.cinemax.cliente.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FASE 7 - Disponibilidad de butacas tal y como la muestra la cartelera.
 *
 * Es codigo PURO (no Room, no Android), asi que se comprueba aqui en un test JVM
 * en vez de con un test de instrumentacion. La regla que se protege es la de
 * negocio: la disponibilidad es la de UNA FUNCION, no la de la sala, y nunca
 * puede ser negativa.
 */
class DisponibilidadButacasTest {

    @Test
    fun unaFuncionSinComprasTieneTodaLaCapacidadLibre() {
        val disponibilidad = DisponibilidadButacas.de(
            capacidadSala = 80,
            ocupadasPorFuncion = emptyMap(),
            funcionId = 7
        )

        assertEquals(80, disponibilidad.capacidad)
        assertEquals(0, disponibilidad.ocupadas)
        assertEquals(80, disponibilidad.libres)
        assertFalse(disponibilidad.agotada)
        assertFalse(disponibilidad.vacia)
    }

    @Test
    fun laDisponibilidadSeTomaDeSuPropiaFuncionYNoDeLasVecinas() {
        val ocupadas = mapOf(7 to 30, 8 to 5)

        val siete = DisponibilidadButacas.de(80, ocupadas, funcionId = 7)
        val ocho = DisponibilidadButacas.de(80, ocupadas, funcionId = 8)
        val nueve = DisponibilidadButacas.de(80, ocupadas, funcionId = 9)

        assertEquals(50, siete.libres)
        assertEquals(75, ocho.libres)
        assertEquals(80, nueve.libres)
    }

    @Test
    fun unaFuncionAgotadaEsLaQueNoDejaNiUnaButacaLibre() {
        val disponibilidad = DisponibilidadButacas.de(80, mapOf(7 to 80), funcionId = 7)

        assertEquals(0, disponibilidad.libres)
        assertTrue(disponibilidad.agotada)
        assertFalse(disponibilidad.vacia)
    }

    @Test
    fun masOcupadasQueCapacidadSeRecortanACeroYNoDaNumeroNegativo() {
        // Caso defensivo: solo posible si el catalogo se editase a mano. La UI
        // debe mostrar 0, nunca -5, que se leeria como un error de calculo.
        val disponibilidad = DisponibilidadButacas.de(80, mapOf(7 to 85), funcionId = 7)

        assertEquals(0, disponibilidad.libres)
        assertTrue(disponibilidad.agotada)
    }

    @Test
    fun unaSalaSinButacasSeMarcaVaciaYNoAgotada() {
        val disponibilidad = DisponibilidadButacas.de(0, emptyMap(), funcionId = 7)

        assertTrue(disponibilidad.vacia)
        assertEquals(0, disponibilidad.libres)
        // "Agotada" y "vacia" son cosas distintas: una sala de 0 butacas es un
        // dato del catalogo, una funcion agotada es un dato de la venta.
        assertTrue(disponibilidad.agotada)
    }

    @Test
    fun capacidadYNegativasNormalizadasACero() {
        val disponibilidad = DisponibilidadButacas.de(-10, mapOf(7 to -3), funcionId = 7)

        assertEquals(0, disponibilidad.capacidad)
        assertEquals(0, disponibilidad.ocupadas)
        assertEquals(0, disponibilidad.libres)
    }
}