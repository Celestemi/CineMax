package com.cinemax.cliente.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SedeDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(sede: SedeEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarTodas(sedes: List<SedeEntity>): List<Long>

    @Update
    suspend fun actualizar(sede: SedeEntity)

    @Delete
    suspend fun eliminar(sede: SedeEntity)

    @Query("SELECT * FROM sedes ORDER BY nombre")
    fun observarTodas(): Flow<List<SedeEntity>>

    @Query("SELECT * FROM sedes WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<SedeEntity?>

    @Query("SELECT * FROM sedes WHERE id = :id LIMIT 1")
    suspend fun porId(id: Int): SedeEntity?

    @Query("SELECT COUNT(*) FROM sedes")
    suspend fun contar(): Int
}
