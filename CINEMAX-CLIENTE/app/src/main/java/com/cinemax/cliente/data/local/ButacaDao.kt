package com.cinemax.cliente.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ButacaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(butaca: ButacaEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarTodas(butacas: List<ButacaEntity>): List<Long>

    @Update
    suspend fun actualizar(butaca: ButacaEntity)

    @Delete
    suspend fun eliminar(butaca: ButacaEntity)

    @Query("DELETE FROM butacas WHERE sala_id = :salaId")
    suspend fun eliminarPorSala(salaId: Int)

    @Query("SELECT * FROM butacas WHERE sala_id = :salaId ORDER BY fila, numero")
    fun porSala(salaId: Int): Flow<List<ButacaEntity>>

    @Query("SELECT * FROM butacas WHERE id = :id LIMIT 1")
    suspend fun porId(id: Int): ButacaEntity?

    @Query("SELECT * FROM butacas WHERE id IN (:ids)")
    suspend fun porIds(ids: List<Int>): List<ButacaEntity>

    @Query(
        """
        SELECT b.* FROM butacas b
        INNER JOIN butaca_reservas br ON br.butaca_id = b.id
        WHERE br.funcion_id = :funcionId
        ORDER BY b.fila, b.numero
        """
    )
    fun ocupadasEnFuncion(funcionId: Int): Flow<List<ButacaEntity>>

    @Query(
        """
        SELECT b.* FROM butacas b
        WHERE b.sala_id = :salaId
          AND NOT EXISTS (
            SELECT 1 FROM butaca_reservas br
            WHERE br.butaca_id = b.id AND br.funcion_id = :funcionId
          )
        ORDER BY b.fila, b.numero
        """
    )
    fun disponiblesEnFuncion(funcionId: Int, salaId: Int): Flow<List<ButacaEntity>>

    @Query("SELECT COUNT(*) FROM butacas WHERE sala_id = :salaId")
    suspend fun contarPorSala(salaId: Int): Int

    @Query("SELECT COUNT(*) FROM butaca_reservas WHERE funcion_id = :funcionId")
    fun observarOcupadasEnFuncion(funcionId: Int): Flow<Int>

    /**
     * FASE 5 - Variante `suspend` (NO `Flow`) de la ocupacion por funcion.
     *
     * Existe por un motivo concreto: `ReservaRepository` necesita comprobar la
     * disponibilidad DENTRO de `database.withTransaction`, y un `Flow` de Room se
     * recolecta en el hilo de consultas, que es distinto del hilo de la
     * transaccion, con lo que bloquearia la escritura. Estas consultas `suspend`
     * se ejecutan en el mismo hilo de la transaccion y por tanto ven el estado
     * no confirmado.
     *
     * Devuelve solo los `butaca_id` ocupados de entre los solicitados, para no
     * traer el mapa entero de la sala.
     */
    @Query(
        """
        SELECT br.butaca_id FROM butaca_reservas br
        WHERE br.funcion_id = :funcionId AND br.butaca_id IN (:butacaIds)
        """
    )
    suspend fun ocupadasDeButacasEnFuncion(funcionId: Int, butacaIds: List<Int>): List<Int>

    /** FASE 5 - Recuento puntual de butacas ocupadas en la funcion. */
    @Query("SELECT COUNT(*) FROM butaca_reservas WHERE funcion_id = :funcionId")
    suspend fun contarOcupadasEnFuncion(funcionId: Int): Int

    /**
     * FASE 7 - Ocupacion agrupada por funcion, para la cartelera.
     *
     * Es UNA lectura para todas las funciones en vez de una por tarjeta, y al ser
     * un `Flow` de Room la cartelera se repinta sola en cuanto alguien compra en
     * cualquier funcion.
     *
     * Solo consulta `butaca_reservas`: no anade ninguna tabla ni columna, asi que
     * el esquema de la version 1 no cambia y no hay migracion.
     */
    @Query(
        """
        SELECT funcion_id AS funcionId, COUNT(*) AS ocupadas
        FROM butaca_reservas
        GROUP BY funcion_id
        """
    )
    fun contarOcupadasPorFuncion(): Flow<List<OcupacionPorFuncion>>

    @Query("SELECT COUNT(*) FROM butacas")
    suspend fun contar(): Int
}
