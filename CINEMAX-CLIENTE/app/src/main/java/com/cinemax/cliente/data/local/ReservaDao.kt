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
interface ReservaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(reserva: ReservaEntity): Long

    @Update
    suspend fun actualizar(reserva: ReservaEntity)

    @Delete
    suspend fun eliminar(reserva: ReservaEntity)

    @Query("SELECT * FROM reservas WHERE usuario_id = :usuarioId ORDER BY id DESC")
    fun porUsuario(usuarioId: Int): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM reservas WHERE usuario_id = :usuarioId AND estado = :estado ORDER BY id DESC")
    fun porUsuarioYEstado(usuarioId: Int, estado: String): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM reservas WHERE codigo = :codigo LIMIT 1")
    fun porCodigo(codigo: String): Flow<ReservaEntity?>

    @Query("SELECT * FROM reservas WHERE codigo = :codigo LIMIT 1")
    suspend fun buscarPorCodigo(codigo: String): ReservaEntity?

    @Query("SELECT * FROM reservas WHERE funcion_id = :funcionId ORDER BY id")
    fun porFuncion(funcionId: Int): Flow<List<ReservaEntity>>

    @Query(
        """
        SELECT b.* FROM butacas b
        INNER JOIN butaca_reservas br ON br.butaca_id = b.id
        WHERE br.reserva_id = :reservaId
        ORDER BY b.fila, b.numero
        """
    )
    fun butacasDeReserva(reservaId: Int): Flow<List<ButacaEntity>>

    @Query("SELECT COUNT(*) FROM reservas")
    suspend fun contar(): Int

    @Query("SELECT COUNT(*) FROM butaca_reservas WHERE reserva_id = :reservaId")
    suspend fun contarButacasDeReserva(reservaId: Int): Int

    @Transaction
    @Query("SELECT * FROM reservas WHERE usuario_id = :usuarioId ORDER BY id DESC")
    fun porUsuarioConDetalle(usuarioId: Int): Flow<List<ReservaConDetalle>>

    @Transaction
    @Query("SELECT * FROM reservas WHERE codigo = :codigo LIMIT 1")
    fun porCodigoConDetalle(codigo: String): Flow<ReservaConDetalle?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarDetalleButacas(detalles: List<ButacaReservaEntity>)

    @Transaction
    suspend fun registrarReserva(
        reserva: ReservaEntity,
        butacas: List<ButacaEntity>
    ): Int {
        val reservaId = insertar(reserva).toInt()
        insertarDetalleButacas(
            butacas.map { butaca ->
                ButacaReservaEntity(
                    reservaId = reservaId,
                    funcionId = reserva.funcionId,
                    butacaId = butaca.id
                )
            }
        )
        return reservaId
    }

    @Transaction
    suspend fun cancelarReserva(reserva: ReservaEntity) {
        eliminarButacasDeReserva(reserva.id)
        actualizar(reserva.copy(estado = ReservaEntity.ESTADO_CANCELADA))
    }

    @Query("DELETE FROM butaca_reservas WHERE reserva_id = :reservaId")
    suspend fun eliminarButacasDeReserva(reservaId: Int)
}
