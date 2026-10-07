package com.cinemax.cliente.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "peliculas",
    indices = [Index(value = ["titulo"]), Index(value = ["genero"])]
)
data class PeliculaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val genero: String,
    @ColumnInfo(name = "clasificacion_edad")
    val clasificacionEdad: String,
    @ColumnInfo(name = "duracion_minutos")
    val duracionMinutos: Int,
    val sinopsis: String,
    @ColumnInfo(name = "poster_url")
    val posterUrl: String,
    @ColumnInfo(name = "trailer_url")
    val trailerUrl: String,
    val activo: Boolean = true
)
