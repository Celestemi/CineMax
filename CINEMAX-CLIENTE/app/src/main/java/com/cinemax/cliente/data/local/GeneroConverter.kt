package com.cinemax.cliente.data.local

import androidx.room.TypeConverter
import com.cinemax.cliente.model.Genero

/**
 * Conversion Enumerado -> TEXT para Room.
 *
 * `peliculas.genero` permanece TEXT (nombre estable del enum) y la entidad de la
 * Fase 2 no cambia, por lo que no hay migracion. El converter queda disponible
 * para DAOs, consultas y mapeos de dominio de la Fase 4/5.
 */
class GeneroConverter {

    @TypeConverter
    fun deGenero(genero: Genero?): String? = genero?.valorPersistido

    @TypeConverter
    fun aGenero(valor: String?): Genero? = valor?.let { Genero.desdeTexto(it) }
}
