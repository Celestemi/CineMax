package com.cinemax.cliente.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Update
    suspend fun actualizar(usuario: UsuarioEntity)

    @Delete
    suspend fun eliminar(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE usuario = :usuario LIMIT 1")
    suspend fun buscarPorUsuario(usuario: String): UsuarioEntity?

    /**
     * FASE 4 - Autenticacion contra la tabla `usuarios`.
     *
     * Devuelve `null` tanto si el usuario no existe como si la contrasena no
     * coincide: [AuthRepository] usa ademas `buscarPorUsuario` para distinguir
     * "usuario inexistente" de "contrasena incorrecta" y poder informar bien.
     */
    @Query("SELECT * FROM usuarios WHERE usuario = :usuario AND contrasena = :contrasena LIMIT 1")
    suspend fun login(usuario: String, contrasena: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE usuario = :usuario LIMIT 1")
    fun observarPorUsuario(usuario: String): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun porId(id: Int): UsuarioEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE usuario = :usuario)")
    suspend fun existeUsuario(usuario: String): Boolean

    @Query("SELECT * FROM usuarios WHERE rol = :rol AND activo = 1 ORDER BY nombre_completo")
    fun observarPorRol(rol: String): Flow<List<UsuarioEntity>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int
}
