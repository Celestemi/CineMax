package com.cinemax.cliente.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * FASE 5 - Fechas ISO de la compra.
 *
 * `funciones.fecha` y `reservas.fecha_compra` son TEXT con formato `yyyy-MM-dd`, y
 * los filtros de cartelera llegan de la UI con ese mismo formato, asi que hace
 * falta validarlo antes de mandarlo a la consulta.
 */
class FechaCineMaxTest {

    @Test
    fun hoyIsoRespetaElFormatoIso() {
        val hoy = FechaCineMax.hoyIso()

        assertEquals(10, hoy.length)
        assertTrue("Formato inesperado: $hoy", Regex("""\d{4}-\d{2}-\d{2}""").matches(hoy))
    }

    @Test
    fun elDesplazamientoDeDiasAplicaSobreElCalendarioReal() {
        val hoy = FechaCineMax.iso(0)
        val manana = FechaCineMax.iso(1)
        val ayer = FechaCineMax.iso(-1)

        assertEquals(1, manana.compareTo(hoy))
        assertEquals(1, hoy.compareTo(ayer))
    }

    @Test
    fun unSaltoDeMesSeCalculaConElCalendario() {
        // Determinista para cualquier fecha en que se ejecute: se avanza hasta
        // pasar el ultimo dia del mes en curso y se comprueba que el mes cambia.
        val calendario = Calendar.getInstance()
        val finDeMes = calendario.getActualMaximum(Calendar.DAY_OF_MONTH)
        val diasQueQuedan = finDeMes - calendario.get(Calendar.DAY_OF_MONTH)
        val mesActual = FechaCineMax.iso(0).take(7)

        val diaSiguienteAlFinDeMes = FechaCineMax.iso(diasQueQuedan + 1)

        assertFalse(
            "El dia posterior al fin de mes no puede seguir en $mesActual: $diaSiguienteAlFinDeMes",
            diaSiguienteAlFinDeMes.startsWith(mesActual)
        )
    }

    @Test
    fun esIsoValidaAceptaFechasBienFormadas() {
        assertTrue(FechaCineMax.esIsoValida("2026-01-31"))
        assertTrue(FechaCineMax.esIsoValida(" 2026-12-01 "))
    }

    @Test
    fun esIsoValidaRechazaFormasIncorrectas() {
        assertFalse(FechaCineMax.esIsoValida(null))
        assertFalse(FechaCineMax.esIsoValida(""))
        assertFalse(FechaCineMax.esIsoValida("2026-1-1"))
        assertFalse(FechaCineMax.esIsoValida("26-01-01"))
        assertFalse(FechaCineMax.esIsoValida("2026/01/01"))
        assertFalse(FechaCineMax.esIsoValida("ayer"))
        assertFalse(FechaCineMax.esIsoValida("2026-01"))
        assertFalse(FechaCineMax.esIsoValida("2026-01-01T10:00"))
    }

    @Test
    fun esIsoValidaRechazaFechasQueNoExisten() {
        // 31 de febrero no es una fecha real del calendario.
        assertFalse(FechaCineMax.esIsoValida("2026-02-31"))
        assertFalse(FechaCineMax.esIsoValida("2026-13-01"))
    }

    @Test
    fun hoyIsoCoincideConElCalendarioDelSistema() {
        val calendario = Calendar.getInstance()
        val partes = FechaCineMax.hoyIso().split("-")

        assertEquals(calendario.get(Calendar.YEAR).toString().padStart(4, '0'), partes[0])
        assertEquals((calendario.get(Calendar.MONTH) + 1).toString().padStart(2, '0'), partes[1])
        assertEquals(calendario.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0'), partes[2])
    }
}
