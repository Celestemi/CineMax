package com.cinemax.cliente.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "salas",
    foreignKeys = [
        ForeignKey(
            entity = SedeEntity::class,
            parentColumns = ["id"],
            childColumns = ["sede_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sede_id"])]
)
data class SalaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "sede_id")
    val sedeId: Int,
    val nombre: String,
    val filas: Int,
    val columnas: Int
) {
    val capacidad: Int
        get() = filas * columnas
}
