package com.cinemax.admin.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cinemax.admin.data.local.dao.UsuarioDao
import com.cinemax.admin.data.local.entity.UsuarioEntity

/**
 * FASE 8 - Base de datos Room EXCLUSIVA de CINEMAX-ADMIN.
 *
 * Es independiente de la base del proyecto Cliente y de la del proyecto original:
 * su fichero es `cinemax_admin.db` y solo registra la entidad [UsuarioEntity] en
 * esta fase. Las tablas de funciones/salas/ocupacion se anadiran en la FASE 9.
 */
@Database(
    entities = [UsuarioEntity::class],
    version = 1,
    exportSchema = true
)
abstract class CinemaxAdminDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
}
