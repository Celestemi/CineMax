package com.cinemax.cliente.ui.screens

import com.cinemax.cliente.model.FechaCineMax
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * FASE 7 - Formato de fechas y horas para la UI.
 *
 * ## Por que no `java.time`
 * El proyecto declara `minSdk 24` y `java.time` exige API 26. Por eso se usa
 * `SimpleDateFormat` + `Calendar`, igual que ya hacia `FechaCineMax` (Fase 5), en
 * lugar de anadir core library desugaring.
 *
 * ## Formato en Room
 * `funciones.fecha`, `funciones.hora` y `reservas.fecha_compra` se guardan tal cual
 * los escribio el `SeedData` y `ReservaViewModel`: `yyyy-MM-dd` y `HH:mm`. Estas
 * funciones SOLO traducen ese texto a algo legible; nunca inventan una fecha ni la
 * reescriben, de modo que el valor que se muestra y el que esta en la base son el
 * mismo dato.
 *
 * Si el texto no se puede interpretar se devuelve TAL CUAL: una fecha rara se ve
 * rara, pero nunca se muestra "01/01/1970" ni se lanza una excepcion en pantalla.
 */
object FormatosCliente {

    private val ESPANOL = Locale("es", "PE")

    private const val MESES_CORTOS = "EEE d MMM"
    private const val MESES_LARGOS = "EEEE d 'de' MMMM"
    private const val DIA_MES_ANIO = "d MMM yyyy"
    private const val HORA_MINUTO = "HH:mm"

    /** `"sáb 14 mar"` a partir de `"2026-03-14"`. */
    fun fechaCorta(fechaIso: String): String = reformatear(fechaIso, FechaCineMax.FORMATO_ISO, MESES_CORTOS)

    /** `"sábado 14 de marzo"` a partir de `"2026-03-14"`. */
    fun fechaLarga(fechaIso: String): String = reformatear(fechaIso, FechaCineMax.FORMATO_ISO, MESES_LARGOS)

    /** `"14 mar 2026"` a partir de `"2026-03-14"` (reservas y compras). */
    fun fechaMedia(fechaIso: String): String = reformatear(fechaIso, FechaCineMax.FORMATO_ISO, DIA_MES_ANIO)

    /** Deja `"14:30"` tal cual; solo normaliza el separador si hiciera falta. */
    fun hora(hora: String): String = hora.trim()

    /**
     * `"sáb 14 mar · 14:30"`: la fecha y la hora de una funcion en una sola linea.
     *
     * Es el formato que usan la cartelera, el detalle, el resumen y la
     * confirmacion, para que una funcion se lea igual en las cuatro pantallas.
     */
    fun funcionCuando(fechaIso: String, hora: String): String =
        "${fechaCorta(fechaIso)} · ${hora(hora)}"

    /**
     * Traduce [fechaIso] de `yyyy-MM-dd` al patron [destino].
     *
     * `SimpleDateFormat` no es seguro entre hilos, asi que se crea uno nuevo en
     * cada llamada: la UI puede formatear desde varios hilos a la vez (la
     * recomposicion no es la unica fuente) y un `SimpleDateFormat` compartido
     * devolveria fechas mezcladas.
     */
    private fun reformatear(fechaIso: String, origen: String, destino: String): String {
        val limpia = fechaIso.trim()
        if (limpia.isEmpty()) return limpia
        return try {
            val lectura = SimpleDateFormat(origen, Locale.US)
            lectura.isLenient = false
            SimpleDateFormat(destino, ESPANOL).format(lectura.parse(limpia))
        } catch (invalido: Exception) {
            // Fecha ilegible: se muestra tal cual, sin inventar nada.
            limpia
        }
    }
}

/**
 * FASE 7 - "Hoy" / "manana" / "ayer" para la barra de filtros.
 *
 * El SeedData genera las funciones de los proximos dias, asi que esta es la
 * forma de reconocer las primeras fechas sin que el usuario tenga que leer un
 * `2026-03-14` y compararlo mentalmente con el calendario.
 *
 * Usa `Calendar` (no `java.time`) por el mismo motivo que [FormatosCliente].
 */
fun etiquetaDiaRelativo(fechaIso: String): String {
    val dias = diasHasta(fechaIso) ?: return fechaIso.trim()
    return when (dias) {
        0L -> "Hoy"
        1L -> "Mañana"
        2L -> "Pasado mañana"
        -1L -> "Ayer"
        else -> FormatosCliente.fechaCorta(fechaIso)
    }
}

/**
 * Dias de calendario entre hoy y [fechaIso], o `null` si la fecha no es valida.
 *
 * Compara solo el dia del calendario (mes y anio, no la hora): dos funciones del
 * mismo dia siempre dan 0, da igual a que hora empiecen.
 */
private fun diasHasta(fechaIso: String): Long? {
    if (!FechaCineMax.esIsoValida(fechaIso)) return null
    return try {
        val partes = fechaIso.trim().split("-")
        val objetivo = Calendar.getInstance().apply {
            set(partes[0].toInt(), partes[1].toInt() - 1, partes[2].toInt(), 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diferencia = objetivo.timeInMillis - hoy.timeInMillis
        Math.round(diferencia / 86_400_000.0)
    } catch (invalida: Exception) {
        null
    }
}