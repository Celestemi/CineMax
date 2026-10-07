package com.cinemax.admin.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cinemax.admin.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * FASE 8 - Acceso a la tabla `usuarios` de la base ADMIN.
 *
 * Ofrece el CRUD basico de usuarios y las consultas de autenticacion. El
 * `OnConflictStrategy.ABORT` de [insertar] es deliberado: el indice UNIQUE de
 * `usuario` lanza excepcion y `AuthRepository` la traduce a "usuario duplicado".
 */
@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Update
    suspend fun actualizar(usuario: UsuarioEntity)

    @Delete
    suspend fun eliminar(usuario: UsuarioEntity)

    /** Devuelve `null` si no existe o si la contrasena no coincide. */
    @Query("SELECT * FROM usuarios WHERE usuario = :usuario AND contrasena = :contrasena LIMIT 1")
    suspend fun login(usuario: String, contrasena: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE usuario = :usuario LIMIT 1")
    suspend fun buscarPorUsuario(usuario: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE usuario = :usuario LIMIT 1")
    fun observarPorUsuario(usuario: String): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun porId(id: Int): UsuarioEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE usuario = :usuario)")
    suspend fun existeUsuario(usuario: String): Boolean

    @Query("SELECT * FROM usuarios WHERE rol = :rol AND activo = 1 ORDER BY nombre_completo")
    fun observarPorRol(rol: String): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios ORDER BY nombre_completo")
    fun observarTodos(): Flow<List<UsuarioEntity>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int
}
