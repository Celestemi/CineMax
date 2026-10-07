package com.cinemax.admin.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * FASE 8 - Tabla `usuarios` de la base de datos exclusiva de CINEMAX-ADMIN.
 *
 * Es una tabla PROPIA del proyecto ADMIN: no se reutiliza la entidad del proyecto
 * Cliente. `usuario` es UNIQUE por indice, de modo que no puede haber dos cuentas
 * con el mismo nombre. El rol se guarda como texto (ver `RolAdmin`).
 *
 * SEGURIDAD (limitacion academica): `contrasena` se guarda en TEXTO PLANO, tal y
 * como permite la guia del coursework. En produccion habria que almacenar un
 * hash (bcrypt/Argon2) y nunca la contrasena en claro.
 */
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
