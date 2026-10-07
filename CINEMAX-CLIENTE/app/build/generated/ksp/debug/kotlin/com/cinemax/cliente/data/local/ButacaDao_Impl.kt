package com.cinemax.cliente.`data`.local

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
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
public class ButacaDao_Impl(
  __db: RoomDatabase,
) : ButacaDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfButacaEntity: EntityInsertAdapter<ButacaEntity>

  private val __deleteAdapterOfButacaEntity: EntityDeleteOrUpdateAdapter<ButacaEntity>

  private val __updateAdapterOfButacaEntity: EntityDeleteOrUpdateAdapter<ButacaEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfButacaEntity = object : EntityInsertAdapter<ButacaEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `butacas` (`id`,`sala_id`,`fila`,`numero`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ButacaEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.salaId.toLong())
        statement.bindText(3, entity.fila)
        statement.bindLong(4, entity.numero.toLong())
      }
    }
    this.__deleteAdapterOfButacaEntity = object : EntityDeleteOrUpdateAdapter<ButacaEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `butacas` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ButacaEntity) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfButacaEntity = object : EntityDeleteOrUpdateAdapter<ButacaEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `butacas` SET `id` = ?,`sala_id` = ?,`fila` = ?,`numero` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ButacaEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.salaId.toLong())
        statement.bindText(3, entity.fila)
        statement.bindLong(4, entity.numero.toLong())
        statement.bindLong(5, entity.id.toLong())
      }
    }
  }

  public override suspend fun insertar(butaca: ButacaEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfButacaEntity.insertAndReturnId(_connection, butaca)
    _result
  }

  public override suspend fun insertarTodas(butacas: List<ButacaEntity>): List<Long> = performSuspending(__db, false, true) { _connection ->
    val _result: List<Long> = __insertAdapterOfButacaEntity.insertAndReturnIdsList(_connection, butacas)
    _result
  }

  public override suspend fun eliminar(butaca: ButacaEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfButacaEntity.handle(_connection, butaca)
  }

  public override suspend fun actualizar(butaca: ButacaEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfButacaEntity.handle(_connection, butaca)
  }

  public override fun porSala(salaId: Int): Flow<List<ButacaEntity>> {
    val _sql: String = "SELECT * FROM butacas WHERE sala_id = ? ORDER BY fila, numero"
    return createFlow(__db, false, arrayOf("butacas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, salaId.toLong())
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

  public override suspend fun porId(id: Int): ButacaEntity? {
    val _sql: String = "SELECT * FROM butacas WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSalaId: Int = getColumnIndexOrThrow(_stmt, "sala_id")
        val _columnIndexOfFila: Int = getColumnIndexOrThrow(_stmt, "fila")
        val _columnIndexOfNumero: Int = getColumnIndexOrThrow(_stmt, "numero")
        val _result: ButacaEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpSalaId: Int
          _tmpSalaId = _stmt.getLong(_columnIndexOfSalaId).toInt()
          val _tmpFila: String
          _tmpFila = _stmt.getText(_columnIndexOfFila)
          val _tmpNumero: Int
          _tmpNumero = _stmt.getLong(_columnIndexOfNumero).toInt()
          _result = ButacaEntity(_tmpId,_tmpSalaId,_tmpFila,_tmpNumero)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun porIds(ids: List<Int>): List<ButacaEntity> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT * FROM butacas WHERE id IN (")
    val _inputSize: Int = ids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: Int in ids) {
          _stmt.bindLong(_argIndex, _item.toLong())
          _argIndex++
        }
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSalaId: Int = getColumnIndexOrThrow(_stmt, "sala_id")
        val _columnIndexOfFila: Int = getColumnIndexOrThrow(_stmt, "fila")
        val _columnIndexOfNumero: Int = getColumnIndexOrThrow(_stmt, "numero")
        val _result: MutableList<ButacaEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: ButacaEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpSalaId: Int
          _tmpSalaId = _stmt.getLong(_columnIndexOfSalaId).toInt()
          val _tmpFila: String
          _tmpFila = _stmt.getText(_columnIndexOfFila)
          val _tmpNumero: Int
          _tmpNumero = _stmt.getLong(_columnIndexOfNumero).toInt()
          _item_1 = ButacaEntity(_tmpId,_tmpSalaId,_tmpFila,_tmpNumero)
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun ocupadasEnFuncion(funcionId: Int): Flow<List<ButacaEntity>> {
    val _sql: String = """
        |
        |        SELECT b.* FROM butacas b
        |        INNER JOIN butaca_reservas br ON br.butaca_id = b.id
        |        WHERE br.funcion_id = ?
        |        ORDER BY b.fila, b.numero
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("butacas", "butaca_reservas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, funcionId.toLong())
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

  public override fun disponiblesEnFuncion(funcionId: Int, salaId: Int): Flow<List<ButacaEntity>> {
    val _sql: String = """
        |
        |        SELECT b.* FROM butacas b
        |        WHERE b.sala_id = ?
        |          AND NOT EXISTS (
        |            SELECT 1 FROM butaca_reservas br
        |            WHERE br.butaca_id = b.id AND br.funcion_id = ?
        |          )
        |        ORDER BY b.fila, b.numero
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("butacas", "butaca_reservas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, salaId.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, funcionId.toLong())
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

  public override suspend fun contarPorSala(salaId: Int): Int {
    val _sql: String = "SELECT COUNT(*) FROM butacas WHERE sala_id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, salaId.toLong())
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

  public override fun observarOcupadasEnFuncion(funcionId: Int): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM butaca_reservas WHERE funcion_id = ?"
    return createFlow(__db, false, arrayOf("butaca_reservas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, funcionId.toLong())
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

  public override suspend fun ocupadasDeButacasEnFuncion(funcionId: Int, butacaIds: List<Int>): List<Int> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("""
        |
        |""".trimMargin())
    _stringBuilder.append("        SELECT br.butaca_id FROM butaca_reservas br")
    _stringBuilder.append("""
        |
        |""".trimMargin())
    _stringBuilder.append("        WHERE br.funcion_id = ")
    _stringBuilder.append("?")
    _stringBuilder.append(" AND br.butaca_id IN (")
    val _inputSize: Int = butacaIds.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    _stringBuilder.append("""
        |
        |""".trimMargin())
    _stringBuilder.append("        ")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, funcionId.toLong())
        _argIndex = 2
        for (_item: Int in butacaIds) {
          _stmt.bindLong(_argIndex, _item.toLong())
          _argIndex++
        }
        val _result: MutableList<Int> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: Int
          _item_1 = _stmt.getLong(0).toInt()
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun contarOcupadasEnFuncion(funcionId: Int): Int {
    val _sql: String = "SELECT COUNT(*) FROM butaca_reservas WHERE funcion_id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, funcionId.toLong())
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

  public override fun contarOcupadasPorFuncion(): Flow<List<OcupacionPorFuncion>> {
    val _sql: String = """
        |
        |        SELECT funcion_id AS funcionId, COUNT(*) AS ocupadas
        |        FROM butaca_reservas
        |        GROUP BY funcion_id
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("butaca_reservas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfFuncionId: Int = 0
        val _columnIndexOfOcupadas: Int = 1
        val _result: MutableList<OcupacionPorFuncion> = mutableListOf()
        while (_stmt.step()) {
          val _item: OcupacionPorFuncion
          val _tmpFuncionId: Int
          _tmpFuncionId = _stmt.getLong(_columnIndexOfFuncionId).toInt()
          val _tmpOcupadas: Int
          _tmpOcupadas = _stmt.getLong(_columnIndexOfOcupadas).toInt()
          _item = OcupacionPorFuncion(_tmpFuncionId,_tmpOcupadas)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun contar(): Int {
    val _sql: String = "SELECT COUNT(*) FROM butacas"
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

  public override suspend fun eliminarPorSala(salaId: Int) {
    val _sql: String = "DELETE FROM butacas WHERE sala_id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, salaId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
