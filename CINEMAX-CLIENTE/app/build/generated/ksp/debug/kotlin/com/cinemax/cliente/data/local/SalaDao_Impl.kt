package com.cinemax.cliente.`data`.local

import androidx.collection.LongSparseArray
import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndex
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.room.util.recursiveFetchLongSparseArray
import androidx.sqlite.SQLiteConnection
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
public class SalaDao_Impl(
  __db: RoomDatabase,
) : SalaDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSalaEntity: EntityInsertAdapter<SalaEntity>

  private val __deleteAdapterOfSalaEntity: EntityDeleteOrUpdateAdapter<SalaEntity>

  private val __updateAdapterOfSalaEntity: EntityDeleteOrUpdateAdapter<SalaEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSalaEntity = object : EntityInsertAdapter<SalaEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `salas` (`id`,`sede_id`,`nombre`,`filas`,`columnas`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SalaEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.sedeId.toLong())
        statement.bindText(3, entity.nombre)
        statement.bindLong(4, entity.filas.toLong())
        statement.bindLong(5, entity.columnas.toLong())
      }
    }
    this.__deleteAdapterOfSalaEntity = object : EntityDeleteOrUpdateAdapter<SalaEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `salas` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SalaEntity) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfSalaEntity = object : EntityDeleteOrUpdateAdapter<SalaEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `salas` SET `id` = ?,`sede_id` = ?,`nombre` = ?,`filas` = ?,`columnas` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SalaEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.sedeId.toLong())
        statement.bindText(3, entity.nombre)
        statement.bindLong(4, entity.filas.toLong())
        statement.bindLong(5, entity.columnas.toLong())
        statement.bindLong(6, entity.id.toLong())
      }
    }
  }

  public override suspend fun insertar(sala: SalaEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfSalaEntity.insertAndReturnId(_connection, sala)
    _result
  }

  public override suspend fun insertarTodas(salas: List<SalaEntity>): List<Long> = performSuspending(__db, false, true) { _connection ->
    val _result: List<Long> = __insertAdapterOfSalaEntity.insertAndReturnIdsList(_connection, salas)
    _result
  }

  public override suspend fun eliminar(sala: SalaEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfSalaEntity.handle(_connection, sala)
  }

  public override suspend fun actualizar(sala: SalaEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfSalaEntity.handle(_connection, sala)
  }

  public override fun observarTodas(): Flow<List<SalaEntity>> {
    val _sql: String = "SELECT * FROM salas ORDER BY nombre"
    return createFlow(__db, false, arrayOf("salas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfNombre: Int = getColumnIndexOrThrow(_stmt, "nombre")
        val _columnIndexOfFilas: Int = getColumnIndexOrThrow(_stmt, "filas")
        val _columnIndexOfColumnas: Int = getColumnIndexOrThrow(_stmt, "columnas")
        val _result: MutableList<SalaEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SalaEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpSedeId: Int
          _tmpSedeId = _stmt.getLong(_columnIndexOfSedeId).toInt()
          val _tmpNombre: String
          _tmpNombre = _stmt.getText(_columnIndexOfNombre)
          val _tmpFilas: Int
          _tmpFilas = _stmt.getLong(_columnIndexOfFilas).toInt()
          val _tmpColumnas: Int
          _tmpColumnas = _stmt.getLong(_columnIndexOfColumnas).toInt()
          _item = SalaEntity(_tmpId,_tmpSedeId,_tmpNombre,_tmpFilas,_tmpColumnas)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observarPorId(id: Int): Flow<SalaEntity?> {
    val _sql: String = "SELECT * FROM salas WHERE id = ? LIMIT 1"
    return createFlow(__db, false, arrayOf("salas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfNombre: Int = getColumnIndexOrThrow(_stmt, "nombre")
        val _columnIndexOfFilas: Int = getColumnIndexOrThrow(_stmt, "filas")
        val _columnIndexOfColumnas: Int = getColumnIndexOrThrow(_stmt, "columnas")
        val _result: SalaEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpSedeId: Int
          _tmpSedeId = _stmt.getLong(_columnIndexOfSedeId).toInt()
          val _tmpNombre: String
          _tmpNombre = _stmt.getText(_columnIndexOfNombre)
          val _tmpFilas: Int
          _tmpFilas = _stmt.getLong(_columnIndexOfFilas).toInt()
          val _tmpColumnas: Int
          _tmpColumnas = _stmt.getLong(_columnIndexOfColumnas).toInt()
          _result = SalaEntity(_tmpId,_tmpSedeId,_tmpNombre,_tmpFilas,_tmpColumnas)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun porId(id: Int): SalaEntity? {
    val _sql: String = "SELECT * FROM salas WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfNombre: Int = getColumnIndexOrThrow(_stmt, "nombre")
        val _columnIndexOfFilas: Int = getColumnIndexOrThrow(_stmt, "filas")
        val _columnIndexOfColumnas: Int = getColumnIndexOrThrow(_stmt, "columnas")
        val _result: SalaEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpSedeId: Int
          _tmpSedeId = _stmt.getLong(_columnIndexOfSedeId).toInt()
          val _tmpNombre: String
          _tmpNombre = _stmt.getText(_columnIndexOfNombre)
          val _tmpFilas: Int
          _tmpFilas = _stmt.getLong(_columnIndexOfFilas).toInt()
          val _tmpColumnas: Int
          _tmpColumnas = _stmt.getLong(_columnIndexOfColumnas).toInt()
          _result = SalaEntity(_tmpId,_tmpSedeId,_tmpNombre,_tmpFilas,_tmpColumnas)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun porSede(sedeId: Int): Flow<List<SalaEntity>> {
    val _sql: String = "SELECT * FROM salas WHERE sede_id = ? ORDER BY nombre"
    return createFlow(__db, false, arrayOf("salas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sedeId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfNombre: Int = getColumnIndexOrThrow(_stmt, "nombre")
        val _columnIndexOfFilas: Int = getColumnIndexOrThrow(_stmt, "filas")
        val _columnIndexOfColumnas: Int = getColumnIndexOrThrow(_stmt, "columnas")
        val _result: MutableList<SalaEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SalaEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpSedeId: Int
          _tmpSedeId = _stmt.getLong(_columnIndexOfSedeId).toInt()
          val _tmpNombre: String
          _tmpNombre = _stmt.getText(_columnIndexOfNombre)
          val _tmpFilas: Int
          _tmpFilas = _stmt.getLong(_columnIndexOfFilas).toInt()
          val _tmpColumnas: Int
          _tmpColumnas = _stmt.getLong(_columnIndexOfColumnas).toInt()
          _item = SalaEntity(_tmpId,_tmpSedeId,_tmpNombre,_tmpFilas,_tmpColumnas)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observarTodasConButacas(): Flow<List<SalaConButacas>> {
    val _sql: String = "SELECT * FROM salas ORDER BY nombre"
    return createFlow(__db, true, arrayOf("butacas", "salas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfNombre: Int = getColumnIndexOrThrow(_stmt, "nombre")
        val _columnIndexOfFilas: Int = getColumnIndexOrThrow(_stmt, "filas")
        val _columnIndexOfColumnas: Int = getColumnIndexOrThrow(_stmt, "columnas")
        val _collectionButacas: LongSparseArray<MutableList<ButacaEntity>> = LongSparseArray<MutableList<ButacaEntity>>()
        while (_stmt.step()) {
          val _tmpKey: Long
          _tmpKey = _stmt.getLong(_columnIndexOfId)
          if (!_collectionButacas.containsKey(_tmpKey)) {
            _collectionButacas.put(_tmpKey, mutableListOf())
          }
        }
        _stmt.reset()
        __fetchRelationshipbutacasAscomCinemaxClienteDataLocalButacaEntity(_connection, _collectionButacas)
        val _result: MutableList<SalaConButacas> = mutableListOf()
        while (_stmt.step()) {
          val _item: SalaConButacas
          val _tmpSala: SalaEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpSedeId: Int
          _tmpSedeId = _stmt.getLong(_columnIndexOfSedeId).toInt()
          val _tmpNombre: String
          _tmpNombre = _stmt.getText(_columnIndexOfNombre)
          val _tmpFilas: Int
          _tmpFilas = _stmt.getLong(_columnIndexOfFilas).toInt()
          val _tmpColumnas: Int
          _tmpColumnas = _stmt.getLong(_columnIndexOfColumnas).toInt()
          _tmpSala = SalaEntity(_tmpId,_tmpSedeId,_tmpNombre,_tmpFilas,_tmpColumnas)
          val _tmpButacasCollection: MutableList<ButacaEntity>
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfId)
          _tmpButacasCollection = checkNotNull(_collectionButacas.get(_tmpKey_1))
          _item = SalaConButacas(_tmpSala,_tmpButacasCollection)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observarConButacas(id: Int): Flow<SalaConButacas?> {
    val _sql: String = "SELECT * FROM salas WHERE id = ? LIMIT 1"
    return createFlow(__db, true, arrayOf("butacas", "salas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfNombre: Int = getColumnIndexOrThrow(_stmt, "nombre")
        val _columnIndexOfFilas: Int = getColumnIndexOrThrow(_stmt, "filas")
        val _columnIndexOfColumnas: Int = getColumnIndexOrThrow(_stmt, "columnas")
        val _collectionButacas: LongSparseArray<MutableList<ButacaEntity>> = LongSparseArray<MutableList<ButacaEntity>>()
        while (_stmt.step()) {
          val _tmpKey: Long
          _tmpKey = _stmt.getLong(_columnIndexOfId)
          if (!_collectionButacas.containsKey(_tmpKey)) {
            _collectionButacas.put(_tmpKey, mutableListOf())
          }
        }
        _stmt.reset()
        __fetchRelationshipbutacasAscomCinemaxClienteDataLocalButacaEntity(_connection, _collectionButacas)
        val _result: SalaConButacas?
        if (_stmt.step()) {
          val _tmpSala: SalaEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpSedeId: Int
          _tmpSedeId = _stmt.getLong(_columnIndexOfSedeId).toInt()
          val _tmpNombre: String
          _tmpNombre = _stmt.getText(_columnIndexOfNombre)
          val _tmpFilas: Int
          _tmpFilas = _stmt.getLong(_columnIndexOfFilas).toInt()
          val _tmpColumnas: Int
          _tmpColumnas = _stmt.getLong(_columnIndexOfColumnas).toInt()
          _tmpSala = SalaEntity(_tmpId,_tmpSedeId,_tmpNombre,_tmpFilas,_tmpColumnas)
          val _tmpButacasCollection: MutableList<ButacaEntity>
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfId)
          _tmpButacasCollection = checkNotNull(_collectionButacas.get(_tmpKey_1))
          _result = SalaConButacas(_tmpSala,_tmpButacasCollection)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun porSedeConButacas(sedeId: Int): Flow<List<SalaConButacas>> {
    val _sql: String = "SELECT * FROM salas WHERE sede_id = ? ORDER BY nombre"
    return createFlow(__db, true, arrayOf("butacas", "salas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sedeId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfNombre: Int = getColumnIndexOrThrow(_stmt, "nombre")
        val _columnIndexOfFilas: Int = getColumnIndexOrThrow(_stmt, "filas")
        val _columnIndexOfColumnas: Int = getColumnIndexOrThrow(_stmt, "columnas")
        val _collectionButacas: LongSparseArray<MutableList<ButacaEntity>> = LongSparseArray<MutableList<ButacaEntity>>()
        while (_stmt.step()) {
          val _tmpKey: Long
          _tmpKey = _stmt.getLong(_columnIndexOfId)
          if (!_collectionButacas.containsKey(_tmpKey)) {
            _collectionButacas.put(_tmpKey, mutableListOf())
          }
        }
        _stmt.reset()
        __fetchRelationshipbutacasAscomCinemaxClienteDataLocalButacaEntity(_connection, _collectionButacas)
        val _result: MutableList<SalaConButacas> = mutableListOf()
        while (_stmt.step()) {
          val _item: SalaConButacas
          val _tmpSala: SalaEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpSedeId: Int
          _tmpSedeId = _stmt.getLong(_columnIndexOfSedeId).toInt()
          val _tmpNombre: String
          _tmpNombre = _stmt.getText(_columnIndexOfNombre)
          val _tmpFilas: Int
          _tmpFilas = _stmt.getLong(_columnIndexOfFilas).toInt()
          val _tmpColumnas: Int
          _tmpColumnas = _stmt.getLong(_columnIndexOfColumnas).toInt()
          _tmpSala = SalaEntity(_tmpId,_tmpSedeId,_tmpNombre,_tmpFilas,_tmpColumnas)
          val _tmpButacasCollection: MutableList<ButacaEntity>
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfId)
          _tmpButacasCollection = checkNotNull(_collectionButacas.get(_tmpKey_1))
          _item = SalaConButacas(_tmpSala,_tmpButacasCollection)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun contar(): Int {
    val _sql: String = "SELECT COUNT(*) FROM salas"
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

  private fun __fetchRelationshipbutacasAscomCinemaxClienteDataLocalButacaEntity(_connection: SQLiteConnection, _map: LongSparseArray<MutableList<ButacaEntity>>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, true) { _tmpMap ->
        __fetchRelationshipbutacasAscomCinemaxClienteDataLocalButacaEntity(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`sala_id`,`fila`,`numero` FROM `butacas` WHERE `sala_id` IN (")
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
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "sala_id")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfSalaId: Int = 1
      val _columnIndexOfFila: Int = 2
      val _columnIndexOfNumero: Int = 3
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_itemKeyIndex)
        val _tmpRelation: MutableList<ButacaEntity>? = _map.get(_tmpKey)
        if (_tmpRelation != null) {
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
          _tmpRelation.add(_item_1)
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
