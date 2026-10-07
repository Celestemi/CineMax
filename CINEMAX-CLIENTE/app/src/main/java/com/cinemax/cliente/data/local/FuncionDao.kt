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
interface FuncionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(funcion: FuncionEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarTodas(funciones: List<FuncionEntity>): List<Long>

    @Update
    suspend fun actualizar(funcion: FuncionEntity)

    @Delete
    suspend fun eliminar(funcion: FuncionEntity)

    @Query("DELETE FROM funciones WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    @Query("SELECT * FROM funciones ORDER BY fecha, hora")
    fun observarTodas(): Flow<List<FuncionEntity>>

    @Query("SELECT * FROM funciones WHERE activa = 1 ORDER BY fecha, hora")
    fun observarActivas(): Flow<List<FuncionEntity>>

    @Query("SELECT * FROM funciones WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<FuncionEntity?>

    @Query("SELECT * FROM funciones WHERE id = :id LIMIT 1")
    suspend fun porId(id: Int): FuncionEntity?

    @Query("SELECT * FROM funciones WHERE pelicula_id = :peliculaId ORDER BY fecha, hora")
    fun porPelicula(peliculaId: Int): Flow<List<FuncionEntity>>

    @Query("SELECT * FROM funciones WHERE pelicula_id = :peliculaId ORDER BY fecha, hora")
    suspend fun porPeliculaSuspend(peliculaId: Int): List<FuncionEntity>

    @Query("SELECT * FROM funciones WHERE sala_id = :salaId AND fecha = :fecha ORDER BY hora")
    fun porSalaYFecha(salaId: Int, fecha: String): Flow<List<FuncionEntity>>

    @Query(
        """
        SELECT f.* FROM funciones f
        INNER JOIN peliculas p ON p.id = f.pelicula_id
        WHERE (:genero IS NULL OR p.genero = :genero)
          AND (:fecha IS NULL OR f.fecha = :fecha)
          AND (:sedeId IS NULL OR f.sede_id = :sedeId)
          AND (:soloActivas = 0 OR f.activa = 1)
        ORDER BY f.fecha, f.hora
        """
    )
    fun filtrar(
        genero: String?,
        fecha: String?,
        sedeId: Int?,
        soloActivas: Boolean
    ): Flow<List<FuncionEntity>>

    @Query("SELECT DISTINCT fecha FROM funciones ORDER BY fecha")
    fun observarFechas(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM funciones")
    suspend fun contar(): Int

    @Query("SELECT COUNT(*) FROM funciones WHERE activa = 1")
    suspend fun contarActivas(): Int

    @Query("DELETE FROM funciones WHERE fecha < :fecha")
    suspend fun eliminarNoVigentes(fecha: String): Int

    @Transaction
    @Query("SELECT * FROM funciones ORDER BY fecha, hora")
    fun observarTodasCompletas(): Flow<List<FuncionCompleta>>

    /**
     * FASE 5 - Cartelera filtrada CON sus relaciones.
     *
     * Se anaden `f.activa = 1` y `p.activo = 1` al SQL de Fase 2/3: la app
     * Cliente no puede ofrecer una funcion desactivada ni una pelicula dada de
     * baja, y el filtro tiene que caer en la base de datos para no traer filas
     * que luego habria que descartar en memoria.
     */
    @Transaction
    @Query(
        """
        SELECT f.* FROM funciones f
        INNER JOIN peliculas p ON p.id = f.pelicula_id
        WHERE f.activa = 1
          AND p.activo = 1
          AND (:genero IS NULL OR p.genero = :genero)
          AND (:fecha IS NULL OR f.fecha = :fecha)
          AND (:sedeId IS NULL OR f.sede_id = :sedeId)
        ORDER BY f.fecha, f.hora
        """
    )
    fun filtrarCompletas(
        genero: String?,
        fecha: String?,
        sedeId: Int?
    ): Flow<List<FuncionCompleta>>

    @Transaction
    @Query("SELECT * FROM funciones WHERE id = :id LIMIT 1")
    fun observarCompletaPorId(id: Int): Flow<FuncionCompleta?>

    /**
     * FASE 5 - Funciones de una pelicula CON sus relaciones (pelicula, sede y sala).
     *
     * Es la consulta que necesita la pantalla de detalle: antes solo existia
     * `porPelicula`, que devuelve la funcion suelta y obligaba a la capa superior
     * a reconstruir la relacion a mano. Se declara `@Transaction` porque
     * [FuncionCompleta] usa `@Relation`, igual que `filtrarCompletas`.
     *
     * FASE 5 anade `activa = 1` y el filtro de pelicula activa para que el
     * detalle no ofrezca funciones retiradas. `DetallePeliculaViewModel` ya
     * resuelve [com.cinemax.cliente.model.ErrorDeCarga.NoEncontrado] cuando la
     * pelicula no esta vigente, asi que el criterio se mantiene aqui en SQL.
     *
     * No altera el esquema: es una consulta de solo lectura sobre `funciones`.
     */
    @Transaction
    @Query(
        """
        SELECT f.* FROM funciones f
        INNER JOIN peliculas p ON p.id = f.pelicula_id
        WHERE f.pelicula_id = :peliculaId
          AND f.activa = 1
          AND p.activo = 1
        ORDER BY f.fecha, f.hora
        """
    )
    fun observarCompletasPorPelicula(peliculaId: Int): Flow<List<FuncionCompleta>>
}
