package com.cinemax.cliente.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SalaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(sala: SalaEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarTodas(salas: List<SalaEntity>): List<Long>

    @Update
    suspend fun actualizar(sala: SalaEntity)

    @Delete
    suspend fun eliminar(sala: SalaEntity)

    @Query("SELECT * FROM salas ORDER BY nombre")
    fun observarTodas(): Flow<List<SalaEntity>>

    @Query("SELECT * FROM salas WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<SalaEntity?>

    @Query("SELECT * FROM salas WHERE id = :id LIMIT 1")
    suspend fun porId(id: Int): SalaEntity?

    @Query("SELECT * FROM salas WHERE sede_id = :sedeId ORDER BY nombre")
    fun porSede(sedeId: Int): Flow<List<SalaEntity>>

    @Transaction
    @Query("SELECT * FROM salas ORDER BY nombre")
    fun observarTodasConButacas(): Flow<List<SalaConButacas>>

    @Transaction
    @Query("SELECT * FROM salas WHERE id = :id LIMIT 1")
    fun observarConButacas(id: Int): Flow<SalaConButacas?>

    @Transaction
    @Query("SELECT * FROM salas WHERE sede_id = :sedeId ORDER BY nombre")
    fun porSedeConButacas(sedeId: Int): Flow<List<SalaConButacas>>

    @Query("SELECT COUNT(*) FROM salas")
    suspend fun contar(): Int
}
