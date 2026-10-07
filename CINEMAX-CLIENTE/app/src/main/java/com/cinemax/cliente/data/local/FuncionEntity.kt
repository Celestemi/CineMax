package com.cinemax.cliente.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "funciones",
    foreignKeys = [
        ForeignKey(
            entity = PeliculaEntity::class,
            parentColumns = ["id"],
            childColumns = ["pelicula_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SedeEntity::class,
            parentColumns = ["id"],
            childColumns = ["sede_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SalaEntity::class,
            parentColumns = ["id"],
            childColumns = ["sala_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["pelicula_id"]),
        Index(value = ["sede_id"]),
        Index(value = ["sala_id"]),
        Index(value = ["fecha", "activa"])
    ]
)
data class FuncionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "pelicula_id")
    val peliculaId: Int,
    @ColumnInfo(name = "sede_id")
    val sedeId: Int,
    @ColumnInfo(name = "sala_id")
    val salaId: Int,
    val fecha: String,
    val hora: String,
    @ColumnInfo(name = "precio_entrada")
    val precioEntrada: Double,
    val activa: Boolean = true
)
