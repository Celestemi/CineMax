package com.cinemax.cliente.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "butaca_reservas",
    primaryKeys = ["reserva_id", "funcion_id", "butaca_id"],
    foreignKeys = [
        ForeignKey(
            entity = ReservaEntity::class,
            parentColumns = ["id"],
            childColumns = ["reserva_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FuncionEntity::class,
            parentColumns = ["id"],
            childColumns = ["funcion_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ButacaEntity::class,
            parentColumns = ["id"],
            childColumns = ["butaca_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["funcion_id", "butaca_id"], unique = true),
        Index(value = ["butaca_id"])
    ]
)
data class ButacaReservaEntity(
    @ColumnInfo(name = "reserva_id")
    val reservaId: Int,
    @ColumnInfo(name = "funcion_id")
    val funcionId: Int,
    @ColumnInfo(name = "butaca_id")
    val butacaId: Int
)
