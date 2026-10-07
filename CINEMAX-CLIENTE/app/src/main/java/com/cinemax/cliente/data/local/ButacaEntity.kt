package com.cinemax.cliente.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "butacas",
    foreignKeys = [
        ForeignKey(
            entity = SalaEntity::class,
            parentColumns = ["id"],
            childColumns = ["sala_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sala_id"]),
        Index(value = ["sala_id", "fila", "numero"], unique = true)
    ]
)
data class ButacaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "sala_id")
    val salaId: Int,
    val fila: String,
    val numero: Int
) {
    val codigo: String
        get() = fila + numero.toString()
}
