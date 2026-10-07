package com.cinemax.cliente.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sedes",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class SedeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val distrito: String,
    val direccion: String
)
