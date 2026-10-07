package com.cinemax.cliente.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FASE 7 - Formato de fechas y horas en pantalla.
 *
 * Se prueban en JVM porque son funciones puras. El punto importante NO es el texto
 * exacto (depende del `Locale` del dispositivo) sino dos garantias: el valor de
 * Room se REESCRIBE de forma interpretable y una fecha ilegible se devuelve tal
 * cual en vez de inventarse un "01/01/1970".
 */
class FormatosClienteTest {

    @Test
    fun unaFechaIsoSeConvierteEnTextoLegible() {
        val resultado = FormatosCliente.fechaCorta("2026-03-14")

        // No se comprueba el dia de la semana exacto (depende del calendario), sino
        // que han desaparecido el ano y los guiones, que es lo ilegible.
        assertTrue(resultado, !resultado.contains("2026"))
        assertTrue(resultado, !resultado.contains("-"))
        assertTrue(resultado, resultado.contains("14"))
    }

    @Test
    fun laFechaLargaIncluyeElNombreDelMes() {
        val resultado = FormatosCliente.fechaLarga("2026-03-14")

        assertTrue(resultado, !resultado.contains("2026"))
        assertTrue(resultado, resultado.contains("14"))
    }

    @Test
    fun laFechaYLaHoraDeUnaFuncionSeLeenJuntas() {
        val resultado = FormatosCliente.funcionCuando("2026-03-14", "14:30")

        assertTrue(resultado, resultado.contains("14"))
        assertTrue(resultado, resultado.contains("14:30"))
        assertTrue(resultado, resultado.contains("·"))
    }

    @Test
    fun laHoraSeDevuelveNormalizadaYNoSeReinterpreta() {
        // La hora de Room ya esta en HH:mm: solo se recorta el espacio, no se
        // convierte a un formato distinto que pudiera descolocar la funcion.
        assertEquals("14:30", FormatosCliente.hora(" 14:30 "))
    }

    @Test
    fun unaFechaIlegibleSeMuestraTalCualEnLugarDeInventarUna() {
        val basura = "no-es-una-fecha"

        assertEquals(basura, FormatosCliente.fechaCorta(basura))
        assertEquals(basura, FormatosCliente.fechaMedia(basura))
    }

    @Test
    fun unaFechaVaciaNoProvocaFallo() {
        // Una fecha vacia o solo con espacios se reduce a cadena vacia: es una
        // cadena de presentacion, y devolver vacio es preferible a devolver los
        // espacios o inventar una fecha.
        assertEquals("", FormatosCliente.fechaCorta(""))
        assertEquals("", FormatosCliente.fechaCorta("   "))
    }

    @Test
    fun lasFechasDeHoyMananaYAyerSeReconocenPorSuNombre() {
        val hoy = com.cinemax.cliente.model.FechaCineMax.hoyIso()
        val manana = com.cinemax.cliente.model.FechaCineMax.iso(1)
        val ayer = com.cinemax.cliente.model.FechaCineMax.iso(-1)

        assertEquals("Hoy", etiquetaDiaRelativo(hoy))
        assertEquals("Mañana", etiquetaDiaRelativo(manana))
        assertEquals("Ayer", etiquetaDiaRelativo(ayer))
    }

    @Test
    fun unaFechaLejanaNoSeEtiquetaComoHoyNiMañana() {
        val lejano = com.cinemax.cliente.model.FechaCineMax.iso(30)

        val etiqueta = etiquetaDiaRelativo(lejano)

        assertTrue(etiqueta, etiqueta != "Hoy")
        assertTrue(etiqueta, etiqueta != "Mañana")
    }

    @Test
    fun unaFechaRelativaInvalidaSeDevuelveSinCambios() {
        assertEquals("basura", etiquetaDiaRelativo("basura"))
    }
}