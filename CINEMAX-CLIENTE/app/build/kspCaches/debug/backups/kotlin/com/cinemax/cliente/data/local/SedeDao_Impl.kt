package com.cinemax.cliente.`data`.local

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
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
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SedeDao_Impl(
  __db: RoomDatabase,
) : SedeDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSedeEntity: EntityInsertAdapter<SedeEntity>

  private val __deleteAdapterOfSedeEntity: EntityDeleteOrUpdateAdapter<SedeEntity>

  private val __updateAdapterOfSedeEntity: EntityDeleteOrUpdateAdapter<SedeEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSedeEntity = object : EntityInsertAdapter<SedeEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `sedes` (`id`,`nombre`,`distrito`,`direccion`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SedeEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.nombre)
        statement.bindText(3, entity.distrito)
        statement.bindText(4, entity.direccion)
      }
    }
    this.__deleteAdapterOfSedeEntity = object : EntityDeleteOrUpdateAdapter<SedeEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `sedes` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SedeEntity) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfSedeEntity = object : EntityDeleteOrUpdateAdapter<SedeEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `sedes` SET `id` = ?,`nombre` = ?,`distrito` = ?,`direccion` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SedeEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.nombre)
        statement.bindText(3, entity.distrito)
        statement.bindText(4, entity.direccion)
        statement.bindLong(5, entity.id.toLong())
      }
    }
  }

  public override suspend fun insertar(sede: SedeEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfSedeEntity.insertAndReturnId(_connection, sede)
    _result
  }

  public override suspend fun insertarTodas(sedes: List<SedeEntity>): List<Long> = performSuspending(__db, false, true) { _connection ->
    val _result: List<Long> = __insertAdapterOfSedeEntity.insertAndReturnIdsList(_connection, sedes)
    _result
  }

  public override suspend fun eliminar(sede: SedeEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfSedeEntity.handle(_connection, sede)
  }

  public override suspend fun actualizar(sede: SedeEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfSedeEntity.handle(_connection, sede)
  }

  public override fun observarTodas(): Flow<List<SedeEntity>> {
    val _sql: String = "SELECT * FROM sedes ORDER BY nombre"
    return createFlow(__db, false, arrayOf("sedes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfNombre: Int = getColumnIndexOrThrow(_stmt, "nombre")
        val _columnIndexOfDistrito: Int = getColumnIndexOrThrow(_stmt, "distrito")
        val _columnIndexOfDireccion: Int = getColumnIndexOrThrow(_stmt, "direccion")
        val _result: MutableList<SedeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SedeEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpNombre: String
          _tmpNombre = _stmt.getText(_columnIndexOfNombre)
          val _tmpDistrito: String
          _tmpDistrito = _stmt.getText(_columnIndexOfDistrito)
          val _tmpDireccion: String
          _tmpDireccion = _stmt.getText(_columnIndexOfDireccion)
          _item = SedeEntity(_tmpId,_tmpNombre,_tmpDistrito,_tmpDireccion)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observarPorId(id: Int): Flow<SedeEntity?> {
    val _sql: String = "SELECT * FROM sedes WHERE id = ? LIMIT 1"
    return createFlow(__db, false, arrayOf("sedes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfNombre: Int = getColumnIndexOrThrow(_stmt, "nombre")
        val _columnIndexOfDistrito: Int = getColumnIndexOrThrow(_stmt, "distrito")
        val _columnIndexOfDireccion: Int = getColumnIndexOrThrow(_stmt, "direccion")
        val _result: SedeEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpNombre: String
          _tmpNombre = _stmt.getText(_columnIndexOfNombre)
          val _tmpDistrito: String
          _tmpDistrito = _stmt.getText(_columnIndexOfDistrito)
          val _tmpDireccion: String
          _tmpDireccion = _stmt.getText(_columnIndexOfDireccion)
          _result = SedeEntity(_tmpId,_tmpNombre,_tmpDistrito,_tmpDireccion)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun porId(id: Int): SedeEntity? {
    val _sql: String = "SELECT * FROM sedes WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfNombre: Int = getColumnIndexOrThrow(_stmt, "nombre")
        val _columnIndexOfDistrito: Int = getColumnIndexOrThrow(_stmt, "distrito")
        val _columnIndexOfDireccion: Int = getColumnIndexOrThrow(_stmt, "direccion")
        val _result: SedeEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpNombre: String
          _tmpNombre = _stmt.getText(_columnIndexOfNombre)
          val _tmpDistrito: String
          _tmpDistrito = _stmt.getText(_columnIndexOfDistrito)
          val _tmpDireccion: String
          _tmpDireccion = _stmt.getText(_columnIndexOfDireccion)
          _result = SedeEntity(_tmpId,_tmpNombre,_tmpDistrito,_tmpDireccion)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun contar(): Int {
    val _sql: String = "SELECT COUNT(*) FROM sedes"
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

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
