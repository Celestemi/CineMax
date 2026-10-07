package com.cinemax.cliente.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * FASE 5 - Fechas ISO (`yyyy-MM-dd`) usadas por las columnas `funciones.fecha` y
 * `reservas.fecha_compra`.
 *
 * Se usa `java.util.Calendar` en lugar de `java.time.LocalDate` porque el
 * proyecto declara `minSdk 24` y `java.time` exige API 26: `Calendar` funciona en
 * API 24 sin core library desugaring.
 *
 * NO duplica logica de `SeedData` (Fase 3, que no se toca): aqui solo vive el
 * reloj que necesita `ReservaViewModel` para fechar una compra, que es una
 * responsabilidad de negocio y no del sembrado.
 */
object FechaCineMax {

    const val FORMATO_ISO = "yyyy-MM-dd"

    /** Fecha de hoy en ISO. Se usa como `reservas.fecha_compra`. */
    fun hoyIso(): String = iso(0)

    /** Fecha ISO situada [dias] dias despues de hoy (negativo = pasado). */
    fun iso(dias: Int): String {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_MONTH, dias)
        return SimpleDateFormat(FORMATO_ISO, Locale.US).format(calendario.time)
    }

    /**
     * `true` si [fecha] tiene forma `yyyy-MM-dd` y existe en el calendario.
     * Se usa para no dejar pasar un filtro de fecha mal formado.
     */
    fun esIsoValida(fecha: String?): Boolean {
        if (fecha == null) return false
        val partes = fecha.trim().split("-")
        if (partes.size != 3) return false
        if (partes.any { it.isEmpty() || it.any { c -> !c.isDigit() } }) return false
        val (anio, mes, dia) = partes
        if (anio.length != 4 || mes.length != 2 || dia.length != 2) return false
        val calendario = Calendar.getInstance()
        calendario.isLenient = false
        return try {
            // `set` no lanza con `lenient = false`: la fecha se resuelve al pedir el
            // tiempo, y es ahi cuando "31 de febrero" se convierte en excepcion.
            calendario.set(anio.toInt(), mes.toInt() - 1, dia.toInt())
            calendario.timeInMillis
            true
        } catch (invalida: IllegalArgumentException) {
            false
        }
    }
}
