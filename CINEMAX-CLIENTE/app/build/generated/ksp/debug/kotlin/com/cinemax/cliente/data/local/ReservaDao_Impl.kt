package com.cinemax.cliente.`data`.local

import androidx.collection.LongSparseArray
import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndex
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performInTransactionSuspending
import androidx.room.util.performSuspending
import androidx.room.util.recursiveFetchLongSparseArray
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ReservaDao_Impl(
  __db: RoomDatabase,
) : ReservaDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfReservaEntity: EntityInsertAdapter<ReservaEntity>

  private val __insertAdapterOfButacaReservaEntity: EntityInsertAdapter<ButacaReservaEntity>

  private val __deleteAdapterOfReservaEntity: EntityDeleteOrUpdateAdapter<ReservaEntity>

  private val __updateAdapterOfReservaEntity: EntityDeleteOrUpdateAdapter<ReservaEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfReservaEntity = object : EntityInsertAdapter<ReservaEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `reservas` (`id`,`codigo`,`usuario_id`,`funcion_id`,`fecha_compra`,`total`,`estado`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ReservaEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.codigo)
        statement.bindLong(3, entity.usuarioId.toLong())
        statement.bindLong(4, entity.funcionId.toLong())
        statement.bindText(5, entity.fechaCompra)
        statement.bindDouble(6, entity.total)
        statement.bindText(7, entity.estado)
      }
    }
    this.__insertAdapterOfButacaReservaEntity = object : EntityInsertAdapter<ButacaReservaEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `butaca_reservas` (`reserva_id`,`funcion_id`,`butaca_id`) VALUES (?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ButacaReservaEntity) {
        statement.bindLong(1, entity.reservaId.toLong())
        statement.bindLong(2, entity.funcionId.toLong())
        statement.bindLong(3, entity.butacaId.toLong())
      }
    }
    this.__deleteAdapterOfReservaEntity = object : EntityDeleteOrUpdateAdapter<ReservaEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `reservas` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ReservaEntity) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfReservaEntity = object : EntityDeleteOrUpdateAdapter<ReservaEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `reservas` SET `id` = ?,`codigo` = ?,`usuario_id` = ?,`funcion_id` = ?,`fecha_compra` = ?,`total` = ?,`estado` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ReservaEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.codigo)
        statement.bindLong(3, entity.usuarioId.toLong())
        statement.bindLong(4, entity.funcionId.toLong())
        statement.bindText(5, entity.fechaCompra)
        statement.bindDouble(6, entity.total)
        statement.bindText(7, entity.estado)
        statement.bindLong(8, entity.id.toLong())
      }
    }
  }

  public override suspend fun insertar(reserva: ReservaEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfReservaEntity.insertAndReturnId(_connection, reserva)
    _result
  }

  public override suspend fun insertarDetalleButacas(detalles: List<ButacaReservaEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfButacaReservaEntity.insert(_connection, detalles)
  }

  public override suspend fun eliminar(reserva: ReservaEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfReservaEntity.handle(_connection, reserva)
  }

  public override suspend fun actualizar(reserva: ReservaEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfReservaEntity.handle(_connection, reserva)
  }

  public override suspend fun registrarReserva(reserva: ReservaEntity, butacas: List<ButacaEntity>): Int = performInTransactionSuspending(__db) {
    super@ReservaDao_Impl.registrarReserva(reserva, butacas)
  }

  public override suspend fun cancelarReserva(reserva: ReservaEntity): Unit = performInTransactionSuspending(__db) {
    super@ReservaDao_Impl.cancelarReserva(reserva)
  }

  public override fun porUsuario(usuarioId: Int): Flow<List<ReservaEntity>> {
    val _sql: String = "SELECT * FROM reservas WHERE usuario_id = ? ORDER BY id DESC"
    return createFlow(__db, false, arrayOf("reservas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, usuarioId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCodigo: Int = getColumnIndexOrThrow(_stmt, "codigo")
        val _columnIndexOfUsuarioId: Int = getColumnIndexOrThrow(_stmt, "usuario_id")
        val _columnIndexOfFuncionId: Int = getColumnIndexOrThrow(_stmt, "funcion_id")
        val _columnIndexOfFechaCompra: Int = getColumnIndexOrThrow(_stmt, "fecha_compra")
        val _columnIndexOfTotal: Int = getColumnIndexOrThrow(_stmt, "total")
        val _columnIndexOfEstado: Int = getColumnIndexOrThrow(_stmt, "estado")
        val _result: MutableList<ReservaEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ReservaEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpCodigo: String
          _tmpCodigo = _stmt.getText(_columnIndexOfCodigo)
          val _tmpUsuarioId: Int
          _tmpUsuarioId = _stmt.getLong(_columnIndexOfUsuarioId).toInt()
          val _tmpFuncionId: Int
          _tmpFuncionId = _stmt.getLong(_columnIndexOfFuncionId).toInt()
          val _tmpFechaCompra: String
          _tmpFechaCompra = _stmt.getText(_columnIndexOfFechaCompra)
          val _tmpTotal: Double
          _tmpTotal = _stmt.getDouble(_columnIndexOfTotal)
          val _tmpEstado: String
          _tmpEstado = _stmt.getText(_columnIndexOfEstado)
          _item = ReservaEntity(_tmpId,_tmpCodigo,_tmpUsuarioId,_tmpFuncionId,_tmpFechaCompra,_tmpTotal,_tmpEstado)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun porUsuarioYEstado(usuarioId: Int, estado: String): Flow<List<ReservaEntity>> {
    val _sql: String = "SELECT * FROM reservas WHERE usuario_id = ? AND estado = ? ORDER BY id DESC"
    return createFlow(__db, false, arrayOf("reservas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, usuarioId.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, estado)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCodigo: Int = getColumnIndexOrThrow(_stmt, "codigo")
        val _columnIndexOfUsuarioId: Int = getColumnIndexOrThrow(_stmt, "usuario_id")
        val _columnIndexOfFuncionId: Int = getColumnIndexOrThrow(_stmt, "funcion_id")
        val _columnIndexOfFechaCompra: Int = getColumnIndexOrThrow(_stmt, "fecha_compra")
        val _columnIndexOfTotal: Int = getColumnIndexOrThrow(_stmt, "total")
        val _columnIndexOfEstado: Int = getColumnIndexOrThrow(_stmt, "estado")
        val _result: MutableList<ReservaEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ReservaEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpCodigo: String
          _tmpCodigo = _stmt.getText(_columnIndexOfCodigo)
          val _tmpUsuarioId: Int
          _tmpUsuarioId = _stmt.getLong(_columnIndexOfUsuarioId).toInt()
          val _tmpFuncionId: Int
          _tmpFuncionId = _stmt.getLong(_columnIndexOfFuncionId).toInt()
          val _tmpFechaCompra: String
          _tmpFechaCompra = _stmt.getText(_columnIndexOfFechaCompra)
          val _tmpTotal: Double
          _tmpTotal = _stmt.getDouble(_columnIndexOfTotal)
          val _tmpEstado: String
          _tmpEstado = _stmt.getText(_columnIndexOfEstado)
          _item = ReservaEntity(_tmpId,_tmpCodigo,_tmpUsuarioId,_tmpFuncionId,_tmpFechaCompra,_tmpTotal,_tmpEstado)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun porCodigo(codigo: String): Flow<ReservaEntity?> {
    val _sql: String = "SELECT * FROM reservas WHERE codigo = ? LIMIT 1"
    return createFlow(__db, false, arrayOf("reservas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, codigo)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCodigo: Int = getColumnIndexOrThrow(_stmt, "codigo")
        val _columnIndexOfUsuarioId: Int = getColumnIndexOrThrow(_stmt, "usuario_id")
        val _columnIndexOfFuncionId: Int = getColumnIndexOrThrow(_stmt, "funcion_id")
        val _columnIndexOfFechaCompra: Int = getColumnIndexOrThrow(_stmt, "fecha_compra")
        val _columnIndexOfTotal: Int = getColumnIndexOrThrow(_stmt, "total")
        val _columnIndexOfEstado: Int = getColumnIndexOrThrow(_stmt, "estado")
        val _result: ReservaEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpCodigo: String
          _tmpCodigo = _stmt.getText(_columnIndexOfCodigo)
          val _tmpUsuarioId: Int
          _tmpUsuarioId = _stmt.getLong(_columnIndexOfUsuarioId).toInt()
          val _tmpFuncionId: Int
          _tmpFuncionId = _stmt.getLong(_columnIndexOfFuncionId).toInt()
          val _tmpFechaCompra: String
          _tmpFechaCompra = _stmt.getText(_columnIndexOfFechaCompra)
          val _tmpTotal: Double
          _tmpTotal = _stmt.getDouble(_columnIndexOfTotal)
          val _tmpEstado: String
          _tmpEstado = _stmt.getText(_columnIndexOfEstado)
          _result = ReservaEntity(_tmpId,_tmpCodigo,_tmpUsuarioId,_tmpFuncionId,_tmpFechaCompra,_tmpTotal,_tmpEstado)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun buscarPorCodigo(codigo: String): ReservaEntity? {
    val _sql: String = "SELECT * FROM reservas WHERE codigo = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, codigo)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCodigo: Int = getColumnIndexOrThrow(_stmt, "codigo")
        val _columnIndexOfUsuarioId: Int = getColumnIndexOrThrow(_stmt, "usuario_id")
        val _columnIndexOfFuncionId: Int = getColumnIndexOrThrow(_stmt, "funcion_id")
        val _columnIndexOfFechaCompra: Int = getColumnIndexOrThrow(_stmt, "fecha_compra")
        val _columnIndexOfTotal: Int = getColumnIndexOrThrow(_stmt, "total")
        val _columnIndexOfEstado: Int = getColumnIndexOrThrow(_stmt, "estado")
        val _result: ReservaEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpCodigo: String
          _tmpCodigo = _stmt.getText(_columnIndexOfCodigo)
          val _tmpUsuarioId: Int
          _tmpUsuarioId = _stmt.getLong(_columnIndexOfUsuarioId).toInt()
          val _tmpFuncionId: Int
          _tmpFuncionId = _stmt.getLong(_columnIndexOfFuncionId).toInt()
          val _tmpFechaCompra: String
          _tmpFechaCompra = _stmt.getText(_columnIndexOfFechaCompra)
          val _tmpTotal: Double
          _tmpTotal = _stmt.getDouble(_columnIndexOfTotal)
          val _tmpEstado: String
          _tmpEstado = _stmt.getText(_columnIndexOfEstado)
          _result = ReservaEntity(_tmpId,_tmpCodigo,_tmpUsuarioId,_tmpFuncionId,_tmpFechaCompra,_tmpTotal,_tmpEstado)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun porFuncion(funcionId: Int): Flow<List<ReservaEntity>> {
    val _sql: String = "SELECT * FROM reservas WHERE funcion_id = ? ORDER BY id"
    return createFlow(__db, false, arrayOf("reservas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, funcionId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCodigo: Int = getColumnIndexOrThrow(_stmt, "codigo")
        val _columnIndexOfUsuarioId: Int = getColumnIndexOrThrow(_stmt, "usuario_id")
        val _columnIndexOfFuncionId: Int = getColumnIndexOrThrow(_stmt, "funcion_id")
        val _columnIndexOfFechaCompra: Int = getColumnIndexOrThrow(_stmt, "fecha_compra")
        val _columnIndexOfTotal: Int = getColumnIndexOrThrow(_stmt, "total")
        val _columnIndexOfEstado: Int = getColumnIndexOrThrow(_stmt, "estado")
        val _result: MutableList<ReservaEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ReservaEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpCodigo: String
          _tmpCodigo = _stmt.getText(_columnIndexOfCodigo)
          val _tmpUsuarioId: Int
          _tmpUsuarioId = _stmt.getLong(_columnIndexOfUsuarioId).toInt()
          val _tmpFuncionId: Int
          _tmpFuncionId = _stmt.getLong(_columnIndexOfFuncionId).toInt()
          val _tmpFechaCompra: String
          _tmpFechaCompra = _stmt.getText(_columnIndexOfFechaCompra)
          val _tmpTotal: Double
          _tmpTotal = _stmt.getDouble(_columnIndexOfTotal)
          val _tmpEstado: String
          _tmpEstado = _stmt.getText(_columnIndexOfEstado)
          _item = ReservaEntity(_tmpId,_tmpCodigo,_tmpUsuarioId,_tmpFuncionId,_tmpFechaCompra,_tmpTotal,_tmpEstado)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun butacasDeReserva(reservaId: Int): Flow<List<ButacaEntity>> {
    val _sql: String = """
        |
        |        SELECT b.* FROM butacas b
        |        INNER JOIN butaca_reservas br ON br.butaca_id = b.id
        |        WHERE br.reserva_id = ?
        |        ORDER BY b.fila, b.numero
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("butacas", "butaca_reservas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, reservaId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSalaId: Int = getColumnIndexOrThrow(_stmt, "sala_id")
        val _columnIndexOfFila: Int = getColumnIndexOrThrow(_stmt, "fila")
        val _columnIndexOfNumero: Int = getColumnIndexOrThrow(_stmt, "numero")
        val _result: MutableList<ButacaEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ButacaEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpSalaId: Int
          _tmpSalaId = _stmt.getLong(_columnIndexOfSalaId).toInt()
          val _tmpFila: String
          _tmpFila = _stmt.getText(_columnIndexOfFila)
          val _tmpNumero: Int
          _tmpNumero = _stmt.getLong(_columnIndexOfNumero).toInt()
          _item = ButacaEntity(_tmpId,_tmpSalaId,_tmpFila,_tmpNumero)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun contar(): Int {
    val _sql: String = "SELECT COUNT(*) FROM reservas"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun contarButacasDeReserva(reservaId: Int): Int {
    val _sql: String = "SELECT COUNT(*) FROM butaca_reservas WHERE reserva_id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, reservaId.toLong())
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun porUsuarioConDetalle(usuarioId: Int): Flow<List<ReservaConDetalle>> {
    val _sql: String = "SELECT * FROM reservas WHERE usuario_id = ? ORDER BY id DESC"
    return createFlow(__db, true, arrayOf("usuarios", "funciones", "reservas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, usuarioId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCodigo: Int = getColumnIndexOrThrow(_stmt, "codigo")
        val _columnIndexOfUsuarioId: Int = getColumnIndexOrThrow(_stmt, "usuario_id")
        val _columnIndexOfFuncionId: Int = getColumnIndexOrThrow(_stmt, "funcion_id")
        val _columnIndexOfFechaCompra: Int = getColumnIndexOrThrow(_stmt, "fecha_compra")
        val _columnIndexOfTotal: Int = getColumnIndexOrThrow(_stmt, "total")
        val _columnIndexOfEstado: Int = getColumnIndexOrThrow(_stmt, "estado")
        val _collectionUsuario: LongSparseArray<UsuarioEntity?> = LongSparseArray<UsuarioEntity?>()
        val _collectionFuncion: LongSparseArray<FuncionEntity?> = LongSparseArray<FuncionEntity?>()
        while (_stmt.step()) {
          val _tmpKey: Long
          _tmpKey = _stmt.getLong(_columnIndexOfUsuarioId)
          _collectionUsuario.put(_tmpKey, null)
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfFuncionId)
          _collectionFuncion.put(_tmpKey_1, null)
        }
        _stmt.reset()
        __fetchRelationshipusuariosAscomCinemaxClienteDataLocalUsuarioEntity(_connection, _collectionUsuario)
        __fetchRelationshipfuncionesAscomCinemaxClienteDataLocalFuncionEntity(_connection, _collectionFuncion)
        val _result: MutableList<ReservaConDetalle> = mutableListOf()
        while (_stmt.step()) {
          val _item: ReservaConDetalle
          val _tmpReserva: ReservaEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpCodigo: String
          _tmpCodigo = _stmt.getText(_columnIndexOfCodigo)
          val _tmpUsuarioId: Int
          _tmpUsuarioId = _stmt.getLong(_columnIndexOfUsuarioId).toInt()
          val _tmpFuncionId: Int
          _tmpFuncionId = _stmt.getLong(_columnIndexOfFuncionId).toInt()
          val _tmpFechaCompra: String
          _tmpFechaCompra = _stmt.getText(_columnIndexOfFechaCompra)
          val _tmpTotal: Double
          _tmpTotal = _stmt.getDouble(_columnIndexOfTotal)
          val _tmpEstado: String
          _tmpEstado = _stmt.getText(_columnIndexOfEstado)
          _tmpReserva = ReservaEntity(_tmpId,_tmpCodigo,_tmpUsuarioId,_tmpFuncionId,_tmpFechaCompra,_tmpTotal,_tmpEstado)
          val _tmpUsuario: UsuarioEntity?
          val _tmpKey_2: Long
          _tmpKey_2 = _stmt.getLong(_columnIndexOfUsuarioId)
          _tmpUsuario = _collectionUsuario.get(_tmpKey_2)
          if (_tmpUsuario == null) {
            error("Relationship item 'usuario' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'usuario_id' and entityColumn named 'id'.")
          }
          val _tmpFuncion: FuncionEntity?
          val _tmpKey_3: Long
          _tmpKey_3 = _stmt.getLong(_columnIndexOfFuncionId)
          _tmpFuncion = _collectionFuncion.get(_tmpKey_3)
          if (_tmpFuncion == null) {
            error("Relationship item 'funcion' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'funcion_id' and entityColumn named 'id'.")
          }
          _item = ReservaConDetalle(_tmpReserva,_tmpUsuario,_tmpFuncion)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun porCodigoConDetalle(codigo: String): Flow<ReservaConDetalle?> {
    val _sql: String = "SELECT * FROM reservas WHERE codigo = ? LIMIT 1"
    return createFlow(__db, true, arrayOf("usuarios", "funciones", "reservas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, codigo)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCodigo: Int = getColumnIndexOrThrow(_stmt, "codigo")
        val _columnIndexOfUsuarioId: Int = getColumnIndexOrThrow(_stmt, "usuario_id")
        val _columnIndexOfFuncionId: Int = getColumnIndexOrThrow(_stmt, "funcion_id")
        val _columnIndexOfFechaCompra: Int = getColumnIndexOrThrow(_stmt, "fecha_compra")
        val _columnIndexOfTotal: Int = getColumnIndexOrThrow(_stmt, "total")
        val _columnIndexOfEstado: Int = getColumnIndexOrThrow(_stmt, "estado")
        val _collectionUsuario: LongSparseArray<UsuarioEntity?> = LongSparseArray<UsuarioEntity?>()
        val _collectionFuncion: LongSparseArray<FuncionEntity?> = LongSparseArray<FuncionEntity?>()
        while (_stmt.step()) {
          val _tmpKey: Long
          _tmpKey = _stmt.getLong(_columnIndexOfUsuarioId)
          _collectionUsuario.put(_tmpKey, null)
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfFuncionId)
          _collectionFuncion.put(_tmpKey_1, null)
        }
        _stmt.reset()
        __fetchRelationshipusuariosAscomCinemaxClienteDataLocalUsuarioEntity(_connection, _collectionUsuario)
        __fetchRelationshipfuncionesAscomCinemaxClienteDataLocalFuncionEntity(_connection, _collectionFuncion)
        val _result: ReservaConDetalle?
        if (_stmt.step()) {
          val _tmpReserva: ReservaEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpCodigo: String
          _tmpCodigo = _stmt.getText(_columnIndexOfCodigo)
          val _tmpUsuarioId: Int
          _tmpUsuarioId = _stmt.getLong(_columnIndexOfUsuarioId).toInt()
          val _tmpFuncionId: Int
          _tmpFuncionId = _stmt.getLong(_columnIndexOfFuncionId).toInt()
          val _tmpFechaCompra: String
          _tmpFechaCompra = _stmt.getText(_columnIndexOfFechaCompra)
          val _tmpTotal: Double
          _tmpTotal = _stmt.getDouble(_columnIndexOfTotal)
          val _tmpEstado: String
          _tmpEstado = _stmt.getText(_columnIndexOfEstado)
          _tmpReserva = ReservaEntity(_tmpId,_tmpCodigo,_tmpUsuarioId,_tmpFuncionId,_tmpFechaCompra,_tmpTotal,_tmpEstado)
          val _tmpUsuario: UsuarioEntity?
          val _tmpKey_2: Long
          _tmpKey_2 = _stmt.getLong(_columnIndexOfUsuarioId)
          _tmpUsuario = _collectionUsuario.get(_tmpKey_2)
          if (_tmpUsuario == null) {
            error("Relationship item 'usuario' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'usuario_id' and entityColumn named 'id'.")
          }
          val _tmpFuncion: FuncionEntity?
          val _tmpKey_3: Long
          _tmpKey_3 = _stmt.getLong(_columnIndexOfFuncionId)
          _tmpFuncion = _collectionFuncion.get(_tmpKey_3)
          if (_tmpFuncion == null) {
            error("Relationship item 'funcion' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'funcion_id' and entityColumn named 'id'.")
          }
          _result = ReservaConDetalle(_tmpReserva,_tmpUsuario,_tmpFuncion)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun eliminarButacasDeReserva(reservaId: Int) {
    val _sql: String = "DELETE FROM butaca_reservas WHERE reserva_id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, reservaId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __fetchRelationshipusuariosAscomCinemaxClienteDataLocalUsuarioEntity(_connection: SQLiteConnection, _map: LongSparseArray<UsuarioEntity?>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, false) { _tmpMap ->
        __fetchRelationshipusuariosAscomCinemaxClienteDataLocalUsuarioEntity(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`usuario`,`contrasena`,`nombre_completo`,`email`,`telefono`,`rol`,`activo`,`creado_en` FROM `usuarios` WHERE `id` IN (")
    val _inputSize: Int = _map.size()
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    val _stmt: SQLiteStatement = _connection.prepare(_sql)
    var _argIndex: Int = 1
    for (i in 0 until _map.size()) {
      val _item: Long = _map.keyAt(i)
      _stmt.bindLong(_argIndex, _item)
      _argIndex++
    }
    try {
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "id")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfUsuario: Int = 1
      val _columnIndexOfContrasena: Int = 2
      val _columnIndexOfNombreCompleto: Int = 3
      val _columnIndexOfEmail: Int = 4
      val _columnIndexOfTelefono: Int = 5
      val _columnIndexOfRol: Int = 6
      val _columnIndexOfActivo: Int = 7
      val _columnIndexOfCreadoEn: Int = 8
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_itemKeyIndex)
        if (_map.containsKey(_tmpKey)) {
          val _item_1: UsuarioEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUsuario: String
          _tmpUsuario = _stmt.getText(_columnIndexOfUsuario)
          val _tmpContrasena: String
          _tmpContrasena = _stmt.getText(_columnIndexOfContrasena)
          val _tmpNombreCompleto: String
          _tmpNombreCompleto = _stmt.getText(_columnIndexOfNombreCompleto)
          val _tmpEmail: String
          _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          val _tmpTelefono: String
          _tmpTelefono = _stmt.getText(_columnIndexOfTelefono)
          val _tmpRol: String
          _tmpRol = _stmt.getText(_columnIndexOfRol)
          val _tmpActivo: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfActivo).toInt()
          _tmpActivo = _tmp != 0
          val _tmpCreadoEn: Long
          _tmpCreadoEn = _stmt.getLong(_columnIndexOfCreadoEn)
          _item_1 = UsuarioEntity(_tmpId,_tmpUsuario,_tmpContrasena,_tmpNombreCompleto,_tmpEmail,_tmpTelefono,_tmpRol,_tmpActivo,_tmpCreadoEn)
          _map.put(_tmpKey, _item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  private fun __fetchRelationshipfuncionesAscomCinemaxClienteDataLocalFuncionEntity(_connection: SQLiteConnection, _map: LongSparseArray<FuncionEntity?>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, false) { _tmpMap ->
        __fetchRelationshipfuncionesAscomCinemaxClienteDataLocalFuncionEntity(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`pelicula_id`,`sede_id`,`sala_id`,`fecha`,`hora`,`precio_entrada`,`activa` FROM `funciones` WHERE `id` IN (")
    val _inputSize: Int = _map.size()
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    val _stmt: SQLiteStatement = _connection.prepare(_sql)
    var _argIndex: Int = 1
    for (i in 0 until _map.size()) {
      val _item: Long = _map.keyAt(i)
      _stmt.bindLong(_argIndex, _item)
      _argIndex++
    }
    try {
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "id")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfPeliculaId: Int = 1
      val _columnIndexOfSedeId: Int = 2
      val _columnIndexOfSalaId: Int = 3
      val _columnIndexOfFecha: Int = 4
      val _columnIndexOfHora: Int = 5
      val _columnIndexOfPrecioEntrada: Int = 6
      val _columnIndexOfActiva: Int = 7
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_itemKeyIndex)
        if (_map.containsKey(_tmpKey)) {
          val _item_1: FuncionEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpPeliculaId: Int
          _tmpPeliculaId = _stmt.getLong(_columnIndexOfPeliculaId).toInt()
          val _tmpSedeId: Int
          _tmpSedeId = _stmt.getLong(_columnIndexOfSedeId).toInt()
          val _tmpSalaId: Int
          _tmpSalaId = _stmt.getLong(_columnIndexOfSalaId).toInt()
          val _tmpFecha: String
          _tmpFecha = _stmt.getText(_columnIndexOfFecha)
          val _tmpHora: String
          _tmpHora = _stmt.getText(_columnIndexOfHora)
          val _tmpPrecioEntrada: Double
          _tmpPrecioEntrada = _stmt.getDouble(_columnIndexOfPrecioEntrada)
          val _tmpActiva: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfActiva).toInt()
          _tmpActiva = _tmp != 0
          _item_1 = FuncionEntity(_tmpId,_tmpPeliculaId,_tmpSedeId,_tmpSalaId,_tmpFecha,_tmpHora,_tmpPrecioEntrada,_tmpActiva)
          _map.put(_tmpKey, _item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
