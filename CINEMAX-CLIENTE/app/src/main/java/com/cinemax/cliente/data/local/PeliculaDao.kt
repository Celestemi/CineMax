package com.cinemax.cliente.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PeliculaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(pelicula: PeliculaEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarTodas(peliculas: List<PeliculaEntity>): List<Long>

    @Update
    suspend fun actualizar(pelicula: PeliculaEntity)

    @Delete
    suspend fun eliminar(pelicula: PeliculaEntity)

    @Query("SELECT * FROM peliculas ORDER BY titulo")
    fun observarTodas(): Flow<List<PeliculaEntity>>

    @Query("SELECT * FROM peliculas WHERE activo = 1 ORDER BY titulo")
    fun observarActivas(): Flow<List<PeliculaEntity>>

    @Query("SELECT * FROM peliculas WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<PeliculaEntity?>

    @Query("SELECT * FROM peliculas WHERE id = :id LIMIT 1")
    suspend fun porId(id: Int): PeliculaEntity?

    @Query("SELECT * FROM peliculas WHERE genero = :genero ORDER BY titulo")
    fun porGenero(genero: String): Flow<List<PeliculaEntity>>

    @Query("SELECT DISTINCT genero FROM peliculas ORDER BY genero")
    fun observarGeneros(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM peliculas")
    suspend fun contar(): Int
}
