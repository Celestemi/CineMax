package com.cinemax.cliente.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["usuario"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuario: String,
    val contrasena: String,
    @ColumnInfo(name = "nombre_completo")
    val nombreCompleto: String,
    val email: String,
    val telefono: String,
    val rol: String,
    val activo: Boolean = true,
    @ColumnInfo(name = "creado_en")
    val creadoEn: Long
)
