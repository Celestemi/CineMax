package com.cinemax.cliente.data.local

import androidx.room.TypeConverter
import com.cinemax.cliente.model.EstadoReserva

/**
 * Conversion Enumerado -> TEXT para Room.
 *
 * `reservas.estado` permanece TEXT ("CONFIRMADA" / "CANCELADA"), igual que en la
 * Fase 2, por lo que no hay migracion. El converter queda disponible para DAOs,
 * consultas y mapeos de dominio de la Fase 4/5.
 */
class EstadoReservaConverter {

    @TypeConverter
    fun deEstadoReserva(estado: EstadoReserva?): String? = estado?.valorPersistido

    @TypeConverter
    fun aEstadoReserva(valor: String?): EstadoReserva? = valor?.let { EstadoReserva.desdeTexto(it) }
}
