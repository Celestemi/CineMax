package com.cinemax.cliente.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FASE 5 - Semantica de los filtros de la cartelera.
 *
 * `null` significa SIEMPRE "sin restriccion en este criterio", nunca "sin
 * resultados": de eso depende el `(:genero IS NULL OR ...)` de
 * `FuncionDao.filtrarCompletas`.
 */
class FiltroCarteleraTest {

    @Test
    fun sinFiltrosNoTieneNingunCriterio() {
        val filtro = FiltroCartelera.SIN_FILTROS

        assertNull(filtro.genero)
        assertNull(filtro.fecha)
        assertNull(filtro.sedeId)
        assertFalse(filtro.hayAlguno)
        assertFalse(filtro.hayCombinacionCompleta)
    }

    @Test
    fun unSoloCriterioCuentaComoFiltro() {
        assertTrue(FiltroCartelera(genero = "ACCION").hayAlguno)
        assertTrue(FiltroCartelera(fecha = "2026-01-01").hayAlguno)
        assertTrue(FiltroCartelera(sedeId = 1).hayAlguno)
    }

    @Test
    fun laCombinacionCompletaExigeLosTresCriterios() {
        val completa = FiltroCartelera(genero = "ACCION", fecha = "2026-01-01", sedeId = 1)

        assertTrue(completa.hayAlguno)
        assertTrue(completa.hayCombinacionCompleta)

        assertFalse(FiltroCartelera(genero = "ACCION", fecha = "2026-01-01").hayCombinacionCompleta)
        assertFalse(FiltroCartelera(genero = "ACCION", sedeId = 1).hayCombinacionCompleta)
        assertFalse(FiltroCartelera(fecha = "2026-01-01", sedeId = 1).hayCombinacionCompleta)
    }

    @Test
    fun normalizadoConvierteLosTextosVaciosEnSinFiltro() {
        // Un `TextField` vacio llega como "" y compararlo en SQL ("") no encontraria nada.
        val normalizado = FiltroCartelera(genero = "  ", fecha = "", sedeId = null).normalizado()

        assertNull(normalizado.genero)
        assertNull(normalizado.fecha)
        assertNull(normalizado.sedeId)
        assertFalse(normalizado.hayAlguno)
    }

    @Test
    fun normalizadoQuitaLosEspaciosSobrantes() {
        val normalizado = FiltroCartelera(
            genero = "  ACCION  ",
            fecha = " 2026-01-01 ",
            sedeId = 3
        ).normalizado()

        assertEquals("ACCION", normalizado.genero)
        assertEquals("2026-01-01", normalizado.fecha)
        assertEquals(3, normalizado.sedeId)
    }

    @Test
    fun normalizadoIgnoraUnIdDeSedeNoPositivo() {
        assertNull(FiltroCartelera(sedeId = 0).normalizado().sedeId)
        assertNull(FiltroCartelera(sedeId = -7).normalizado().sedeId)
    }

    @Test
    fun normalizadoEsIdempotente() {
        val una = FiltroCartelera(genero = " DRAMA ", fecha = " 2026-02-02 ", sedeId = 2)
        val dos = una.normalizado()

        assertEquals(dos, dos.normalizado())
    }

    @Test
    fun dosFiltrosDistintosNoSonIguales() {
        assertTrue(FiltroCartelera(genero = "ACCION") != FiltroCartelera(genero = "DRAMA"))
        assertTrue(FiltroCartelera(genero = "ACCION") != FiltroCartelera(fecha = "2026-01-01"))
        assertTrue(FiltroCartelera(sedeId = 1) != FiltroCartelera(sedeId = 2))
    }
}
