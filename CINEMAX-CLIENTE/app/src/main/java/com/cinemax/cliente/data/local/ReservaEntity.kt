package com.cinemax.cliente.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reservas",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuario_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FuncionEntity::class,
            parentColumns = ["id"],
            childColumns = ["funcion_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["codigo"], unique = true),
        Index(value = ["usuario_id"]),
        Index(value = ["funcion_id"])
    ]
)
data class ReservaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val codigo: String,
    @ColumnInfo(name = "usuario_id")
    val usuarioId: Int,
    @ColumnInfo(name = "funcion_id")
    val funcionId: Int,
    @ColumnInfo(name = "fecha_compra")
    val fechaCompra: String,
    val total: Double,
    val estado: String = ESTADO_CONFIRMADA
) {
    companion object {
        const val ESTADO_CONFIRMADA = "CONFIRMADA"
        const val ESTADO_CANCELADA = "CANCELADA"
    }
}
