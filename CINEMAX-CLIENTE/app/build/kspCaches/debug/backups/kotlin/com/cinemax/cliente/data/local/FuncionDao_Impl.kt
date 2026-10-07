package com.cinemax.cliente.`data`.local

import androidx.collection.LongSparseArray
import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndex
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.getTotalChangedRows
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
public class FuncionDao_Impl(
  __db: RoomDatabase,
) : FuncionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfFuncionEntity: EntityInsertAdapter<FuncionEntity>

  private val __deleteAdapterOfFuncionEntity: EntityDeleteOrUpdateAdapter<FuncionEntity>

  private val __updateAdapterOfFuncionEntity: EntityDeleteOrUpdateAdapter<FuncionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfFuncionEntity = object : EntityInsertAdapter<FuncionEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `funciones` (`id`,`pelicula_id`,`sede_id`,`sala_id`,`fecha`,`hora`,`precio_entrada`,`activa`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FuncionEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.peliculaId.toLong())
        statement.bindLong(3, entity.sedeId.toLong())
        statement.bindLong(4, entity.salaId.toLong())
        statement.bindText(5, entity.fecha)
        statement.bindText(6, entity.hora)
        statement.bindDouble(7, entity.precioEntrada)
        val _tmp: Int = if (entity.activa) 1 else 0
        statement.bindLong(8, _tmp.toLong())
      }
    }
    this.__deleteAdapterOfFuncionEntity = object : EntityDeleteOrUpdateAdapter<FuncionEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `funciones` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: FuncionEntity) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfFuncionEntity = object : EntityDeleteOrUpdateAdapter<FuncionEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `funciones` SET `id` = ?,`pelicula_id` = ?,`sede_id` = ?,`sala_id` = ?,`fecha` = ?,`hora` = ?,`precio_entrada` = ?,`activa` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: FuncionEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.peliculaId.toLong())
        statement.bindLong(3, entity.sedeId.toLong())
        statement.bindLong(4, entity.salaId.toLong())
        statement.bindText(5, entity.fecha)
        statement.bindText(6, entity.hora)
        statement.bindDouble(7, entity.precioEntrada)
        val _tmp: Int = if (entity.activa) 1 else 0
        statement.bindLong(8, _tmp.toLong())
        statement.bindLong(9, entity.id.toLong())
      }
    }
  }

  public override suspend fun insertar(funcion: FuncionEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfFuncionEntity.insertAndReturnId(_connection, funcion)
    _result
  }

  public override suspend fun insertarTodas(funciones: List<FuncionEntity>): List<Long> = performSuspending(__db, false, true) { _connection ->
    val _result: List<Long> = __insertAdapterOfFuncionEntity.insertAndReturnIdsList(_connection, funciones)
    _result
  }

  public override suspend fun eliminar(funcion: FuncionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfFuncionEntity.handle(_connection, funcion)
  }

  public override suspend fun actualizar(funcion: FuncionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfFuncionEntity.handle(_connection, funcion)
  }

  public override fun observarTodas(): Flow<List<FuncionEntity>> {
    val _sql: String = "SELECT * FROM funciones ORDER BY fecha, hora"
    return createFlow(__db, false, arrayOf("funciones")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPeliculaId: Int = getColumnIndexOrThrow(_stmt, "pelicula_id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfSalaId: Int = getColumnIndexOrThrow(_stmt, "sala_id")
        val _columnIndexOfFecha: Int = getColumnIndexOrThrow(_stmt, "fecha")
        val _columnIndexOfHora: Int = getColumnIndexOrThrow(_stmt, "hora")
        val _columnIndexOfPrecioEntrada: Int = getColumnIndexOrThrow(_stmt, "precio_entrada")
        val _columnIndexOfActiva: Int = getColumnIndexOrThrow(_stmt, "activa")
        val _result: MutableList<FuncionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FuncionEntity
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
          _item = FuncionEntity(_tmpId,_tmpPeliculaId,_tmpSedeId,_tmpSalaId,_tmpFecha,_tmpHora,_tmpPrecioEntrada,_tmpActiva)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observarActivas(): Flow<List<FuncionEntity>> {
    val _sql: String = "SELECT * FROM funciones WHERE activa = 1 ORDER BY fecha, hora"
    return createFlow(__db, false, arrayOf("funciones")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPeliculaId: Int = getColumnIndexOrThrow(_stmt, "pelicula_id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfSalaId: Int = getColumnIndexOrThrow(_stmt, "sala_id")
        val _columnIndexOfFecha: Int = getColumnIndexOrThrow(_stmt, "fecha")
        val _columnIndexOfHora: Int = getColumnIndexOrThrow(_stmt, "hora")
        val _columnIndexOfPrecioEntrada: Int = getColumnIndexOrThrow(_stmt, "precio_entrada")
        val _columnIndexOfActiva: Int = getColumnIndexOrThrow(_stmt, "activa")
        val _result: MutableList<FuncionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FuncionEntity
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
          _item = FuncionEntity(_tmpId,_tmpPeliculaId,_tmpSedeId,_tmpSalaId,_tmpFecha,_tmpHora,_tmpPrecioEntrada,_tmpActiva)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observarPorId(id: Int): Flow<FuncionEntity?> {
    val _sql: String = "SELECT * FROM funciones WHERE id = ? LIMIT 1"
    return createFlow(__db, false, arrayOf("funciones")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPeliculaId: Int = getColumnIndexOrThrow(_stmt, "pelicula_id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfSalaId: Int = getColumnIndexOrThrow(_stmt, "sala_id")
        val _columnIndexOfFecha: Int = getColumnIndexOrThrow(_stmt, "fecha")
        val _columnIndexOfHora: Int = getColumnIndexOrThrow(_stmt, "hora")
        val _columnIndexOfPrecioEntrada: Int = getColumnIndexOrThrow(_stmt, "precio_entrada")
        val _columnIndexOfActiva: Int = getColumnIndexOrThrow(_stmt, "activa")
        val _result: FuncionEntity?
        if (_stmt.step()) {
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
          _result = FuncionEntity(_tmpId,_tmpPeliculaId,_tmpSedeId,_tmpSalaId,_tmpFecha,_tmpHora,_tmpPrecioEntrada,_tmpActiva)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun porId(id: Int): FuncionEntity? {
    val _sql: String = "SELECT * FROM funciones WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPeliculaId: Int = getColumnIndexOrThrow(_stmt, "pelicula_id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfSalaId: Int = getColumnIndexOrThrow(_stmt, "sala_id")
        val _columnIndexOfFecha: Int = getColumnIndexOrThrow(_stmt, "fecha")
        val _columnIndexOfHora: Int = getColumnIndexOrThrow(_stmt, "hora")
        val _columnIndexOfPrecioEntrada: Int = getColumnIndexOrThrow(_stmt, "precio_entrada")
        val _columnIndexOfActiva: Int = getColumnIndexOrThrow(_stmt, "activa")
        val _result: FuncionEntity?
        if (_stmt.step()) {
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
          _result = FuncionEntity(_tmpId,_tmpPeliculaId,_tmpSedeId,_tmpSalaId,_tmpFecha,_tmpHora,_tmpPrecioEntrada,_tmpActiva)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun porPelicula(peliculaId: Int): Flow<List<FuncionEntity>> {
    val _sql: String = "SELECT * FROM funciones WHERE pelicula_id = ? ORDER BY fecha, hora"
    return createFlow(__db, false, arrayOf("funciones")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, peliculaId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPeliculaId: Int = getColumnIndexOrThrow(_stmt, "pelicula_id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfSalaId: Int = getColumnIndexOrThrow(_stmt, "sala_id")
        val _columnIndexOfFecha: Int = getColumnIndexOrThrow(_stmt, "fecha")
        val _columnIndexOfHora: Int = getColumnIndexOrThrow(_stmt, "hora")
        val _columnIndexOfPrecioEntrada: Int = getColumnIndexOrThrow(_stmt, "precio_entrada")
        val _columnIndexOfActiva: Int = getColumnIndexOrThrow(_stmt, "activa")
        val _result: MutableList<FuncionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FuncionEntity
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
          _item = FuncionEntity(_tmpId,_tmpPeliculaId,_tmpSedeId,_tmpSalaId,_tmpFecha,_tmpHora,_tmpPrecioEntrada,_tmpActiva)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun porPeliculaSuspend(peliculaId: Int): List<FuncionEntity> {
    val _sql: String = "SELECT * FROM funciones WHERE pelicula_id = ? ORDER BY fecha, hora"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, peliculaId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPeliculaId: Int = getColumnIndexOrThrow(_stmt, "pelicula_id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfSalaId: Int = getColumnIndexOrThrow(_stmt, "sala_id")
        val _columnIndexOfFecha: Int = getColumnIndexOrThrow(_stmt, "fecha")
        val _columnIndexOfHora: Int = getColumnIndexOrThrow(_stmt, "hora")
        val _columnIndexOfPrecioEntrada: Int = getColumnIndexOrThrow(_stmt, "precio_entrada")
        val _columnIndexOfActiva: Int = getColumnIndexOrThrow(_stmt, "activa")
        val _result: MutableList<FuncionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FuncionEntity
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
          _item = FuncionEntity(_tmpId,_tmpPeliculaId,_tmpSedeId,_tmpSalaId,_tmpFecha,_tmpHora,_tmpPrecioEntrada,_tmpActiva)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun porSalaYFecha(salaId: Int, fecha: String): Flow<List<FuncionEntity>> {
    val _sql: String = "SELECT * FROM funciones WHERE sala_id = ? AND fecha = ? ORDER BY hora"
    return createFlow(__db, false, arrayOf("funciones")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, salaId.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, fecha)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPeliculaId: Int = getColumnIndexOrThrow(_stmt, "pelicula_id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfSalaId: Int = getColumnIndexOrThrow(_stmt, "sala_id")
        val _columnIndexOfFecha: Int = getColumnIndexOrThrow(_stmt, "fecha")
        val _columnIndexOfHora: Int = getColumnIndexOrThrow(_stmt, "hora")
        val _columnIndexOfPrecioEntrada: Int = getColumnIndexOrThrow(_stmt, "precio_entrada")
        val _columnIndexOfActiva: Int = getColumnIndexOrThrow(_stmt, "activa")
        val _result: MutableList<FuncionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FuncionEntity
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
          _item = FuncionEntity(_tmpId,_tmpPeliculaId,_tmpSedeId,_tmpSalaId,_tmpFecha,_tmpHora,_tmpPrecioEntrada,_tmpActiva)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun filtrar(
    genero: String?,
    fecha: String?,
    sedeId: Int?,
    soloActivas: Boolean,
  ): Flow<List<FuncionEntity>> {
    val _sql: String = """
        |
        |        SELECT f.* FROM funciones f
        |        INNER JOIN peliculas p ON p.id = f.pelicula_id
        |        WHERE (? IS NULL OR p.genero = ?)
        |          AND (? IS NULL OR f.fecha = ?)
        |          AND (? IS NULL OR f.sede_id = ?)
        |          AND (? = 0 OR f.activa = 1)
        |        ORDER BY f.fecha, f.hora
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("funciones", "peliculas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        if (genero == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, genero)
        }
        _argIndex = 2
        if (genero == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, genero)
        }
        _argIndex = 3
        if (fecha == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, fecha)
        }
        _argIndex = 4
        if (fecha == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, fecha)
        }
        _argIndex = 5
        if (sedeId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, sedeId.toLong())
        }
        _argIndex = 6
        if (sedeId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, sedeId.toLong())
        }
        _argIndex = 7
        val _tmp: Int = if (soloActivas) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPeliculaId: Int = getColumnIndexOrThrow(_stmt, "pelicula_id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfSalaId: Int = getColumnIndexOrThrow(_stmt, "sala_id")
        val _columnIndexOfFecha: Int = getColumnIndexOrThrow(_stmt, "fecha")
        val _columnIndexOfHora: Int = getColumnIndexOrThrow(_stmt, "hora")
        val _columnIndexOfPrecioEntrada: Int = getColumnIndexOrThrow(_stmt, "precio_entrada")
        val _columnIndexOfActiva: Int = getColumnIndexOrThrow(_stmt, "activa")
        val _result: MutableList<FuncionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FuncionEntity
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
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfActiva).toInt()
          _tmpActiva = _tmp_1 != 0
          _item = FuncionEntity(_tmpId,_tmpPeliculaId,_tmpSedeId,_tmpSalaId,_tmpFecha,_tmpHora,_tmpPrecioEntrada,_tmpActiva)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observarFechas(): Flow<List<String>> {
    val _sql: String = "SELECT DISTINCT fecha FROM funciones ORDER BY fecha"
    return createFlow(__db, false, arrayOf("funciones")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun contar(): Int {
    val _sql: String = "SELECT COUNT(*) FROM funciones"
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

  public override suspend fun contarActivas(): Int {
    val _sql: String = "SELECT COUNT(*) FROM funciones WHERE activa = 1"
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

  public override fun observarTodasCompletas(): Flow<List<FuncionCompleta>> {
    val _sql: String = "SELECT * FROM funciones ORDER BY fecha, hora"
    return createFlow(__db, true, arrayOf("peliculas", "sedes", "salas", "funciones")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPeliculaId: Int = getColumnIndexOrThrow(_stmt, "pelicula_id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfSalaId: Int = getColumnIndexOrThrow(_stmt, "sala_id")
        val _columnIndexOfFecha: Int = getColumnIndexOrThrow(_stmt, "fecha")
        val _columnIndexOfHora: Int = getColumnIndexOrThrow(_stmt, "hora")
        val _columnIndexOfPrecioEntrada: Int = getColumnIndexOrThrow(_stmt, "precio_entrada")
        val _columnIndexOfActiva: Int = getColumnIndexOrThrow(_stmt, "activa")
        val _collectionPelicula: LongSparseArray<PeliculaEntity?> = LongSparseArray<PeliculaEntity?>()
        val _collectionSede: LongSparseArray<SedeEntity?> = LongSparseArray<SedeEntity?>()
        val _collectionSala: LongSparseArray<SalaEntity?> = LongSparseArray<SalaEntity?>()
        while (_stmt.step()) {
          val _tmpKey: Long
          _tmpKey = _stmt.getLong(_columnIndexOfPeliculaId)
          _collectionPelicula.put(_tmpKey, null)
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfSedeId)
          _collectionSede.put(_tmpKey_1, null)
          val _tmpKey_2: Long
          _tmpKey_2 = _stmt.getLong(_columnIndexOfSalaId)
          _collectionSala.put(_tmpKey_2, null)
        }
        _stmt.reset()
        __fetchRelationshippeliculasAscomCinemaxClienteDataLocalPeliculaEntity(_connection, _collectionPelicula)
        __fetchRelationshipsedesAscomCinemaxClienteDataLocalSedeEntity(_connection, _collectionSede)
        __fetchRelationshipsalasAscomCinemaxClienteDataLocalSalaEntity(_connection, _collectionSala)
        val _result: MutableList<FuncionCompleta> = mutableListOf()
        while (_stmt.step()) {
          val _item: FuncionCompleta
          val _tmpFuncion: FuncionEntity
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
          _tmpFuncion = FuncionEntity(_tmpId,_tmpPeliculaId,_tmpSedeId,_tmpSalaId,_tmpFecha,_tmpHora,_tmpPrecioEntrada,_tmpActiva)
          val _tmpPelicula: PeliculaEntity?
          val _tmpKey_3: Long
          _tmpKey_3 = _stmt.getLong(_columnIndexOfPeliculaId)
          _tmpPelicula = _collectionPelicula.get(_tmpKey_3)
          if (_tmpPelicula == null) {
            error("Relationship item 'pelicula' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'pelicula_id' and entityColumn named 'id'.")
          }
          val _tmpSede: SedeEntity?
          val _tmpKey_4: Long
          _tmpKey_4 = _stmt.getLong(_columnIndexOfSedeId)
          _tmpSede = _collectionSede.get(_tmpKey_4)
          if (_tmpSede == null) {
            error("Relationship item 'sede' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'sede_id' and entityColumn named 'id'.")
          }
          val _tmpSala: SalaEntity?
          val _tmpKey_5: Long
          _tmpKey_5 = _stmt.getLong(_columnIndexOfSalaId)
          _tmpSala = _collectionSala.get(_tmpKey_5)
          if (_tmpSala == null) {
            error("Relationship item 'sala' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'sala_id' and entityColumn named 'id'.")
          }
          _item = FuncionCompleta(_tmpFuncion,_tmpPelicula,_tmpSede,_tmpSala)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun filtrarCompletas(
    genero: String?,
    fecha: String?,
    sedeId: Int?,
  ): Flow<List<FuncionCompleta>> {
    val _sql: String = """
        |
        |        SELECT f.* FROM funciones f
        |        INNER JOIN peliculas p ON p.id = f.pelicula_id
        |        WHERE f.activa = 1
        |          AND p.activo = 1
        |          AND (? IS NULL OR p.genero = ?)
        |          AND (? IS NULL OR f.fecha = ?)
        |          AND (? IS NULL OR f.sede_id = ?)
        |        ORDER BY f.fecha, f.hora
        |        
        """.trimMargin()
    return createFlow(__db, true, arrayOf("peliculas", "sedes", "salas", "funciones")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        if (genero == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, genero)
        }
        _argIndex = 2
        if (genero == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, genero)
        }
        _argIndex = 3
        if (fecha == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, fecha)
        }
        _argIndex = 4
        if (fecha == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, fecha)
        }
        _argIndex = 5
        if (sedeId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, sedeId.toLong())
        }
        _argIndex = 6
        if (sedeId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, sedeId.toLong())
        }
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPeliculaId: Int = getColumnIndexOrThrow(_stmt, "pelicula_id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfSalaId: Int = getColumnIndexOrThrow(_stmt, "sala_id")
        val _columnIndexOfFecha: Int = getColumnIndexOrThrow(_stmt, "fecha")
        val _columnIndexOfHora: Int = getColumnIndexOrThrow(_stmt, "hora")
        val _columnIndexOfPrecioEntrada: Int = getColumnIndexOrThrow(_stmt, "precio_entrada")
        val _columnIndexOfActiva: Int = getColumnIndexOrThrow(_stmt, "activa")
        val _collectionPelicula: LongSparseArray<PeliculaEntity?> = LongSparseArray<PeliculaEntity?>()
        val _collectionSede: LongSparseArray<SedeEntity?> = LongSparseArray<SedeEntity?>()
        val _collectionSala: LongSparseArray<SalaEntity?> = LongSparseArray<SalaEntity?>()
        while (_stmt.step()) {
          val _tmpKey: Long
          _tmpKey = _stmt.getLong(_columnIndexOfPeliculaId)
          _collectionPelicula.put(_tmpKey, null)
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfSedeId)
          _collectionSede.put(_tmpKey_1, null)
          val _tmpKey_2: Long
          _tmpKey_2 = _stmt.getLong(_columnIndexOfSalaId)
          _collectionSala.put(_tmpKey_2, null)
        }
        _stmt.reset()
        __fetchRelationshippeliculasAscomCinemaxClienteDataLocalPeliculaEntity(_connection, _collectionPelicula)
        __fetchRelationshipsedesAscomCinemaxClienteDataLocalSedeEntity(_connection, _collectionSede)
        __fetchRelationshipsalasAscomCinemaxClienteDataLocalSalaEntity(_connection, _collectionSala)
        val _result: MutableList<FuncionCompleta> = mutableListOf()
        while (_stmt.step()) {
          val _item: FuncionCompleta
          val _tmpFuncion: FuncionEntity
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
          _tmpFuncion = FuncionEntity(_tmpId,_tmpPeliculaId,_tmpSedeId,_tmpSalaId,_tmpFecha,_tmpHora,_tmpPrecioEntrada,_tmpActiva)
          val _tmpPelicula: PeliculaEntity?
          val _tmpKey_3: Long
          _tmpKey_3 = _stmt.getLong(_columnIndexOfPeliculaId)
          _tmpPelicula = _collectionPelicula.get(_tmpKey_3)
          if (_tmpPelicula == null) {
            error("Relationship item 'pelicula' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'pelicula_id' and entityColumn named 'id'.")
          }
          val _tmpSede: SedeEntity?
          val _tmpKey_4: Long
          _tmpKey_4 = _stmt.getLong(_columnIndexOfSedeId)
          _tmpSede = _collectionSede.get(_tmpKey_4)
          if (_tmpSede == null) {
            error("Relationship item 'sede' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'sede_id' and entityColumn named 'id'.")
          }
          val _tmpSala: SalaEntity?
          val _tmpKey_5: Long
          _tmpKey_5 = _stmt.getLong(_columnIndexOfSalaId)
          _tmpSala = _collectionSala.get(_tmpKey_5)
          if (_tmpSala == null) {
            error("Relationship item 'sala' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'sala_id' and entityColumn named 'id'.")
          }
          _item = FuncionCompleta(_tmpFuncion,_tmpPelicula,_tmpSede,_tmpSala)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observarCompletaPorId(id: Int): Flow<FuncionCompleta?> {
    val _sql: String = "SELECT * FROM funciones WHERE id = ? LIMIT 1"
    return createFlow(__db, true, arrayOf("peliculas", "sedes", "salas", "funciones")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPeliculaId: Int = getColumnIndexOrThrow(_stmt, "pelicula_id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfSalaId: Int = getColumnIndexOrThrow(_stmt, "sala_id")
        val _columnIndexOfFecha: Int = getColumnIndexOrThrow(_stmt, "fecha")
        val _columnIndexOfHora: Int = getColumnIndexOrThrow(_stmt, "hora")
        val _columnIndexOfPrecioEntrada: Int = getColumnIndexOrThrow(_stmt, "precio_entrada")
        val _columnIndexOfActiva: Int = getColumnIndexOrThrow(_stmt, "activa")
        val _collectionPelicula: LongSparseArray<PeliculaEntity?> = LongSparseArray<PeliculaEntity?>()
        val _collectionSede: LongSparseArray<SedeEntity?> = LongSparseArray<SedeEntity?>()
        val _collectionSala: LongSparseArray<SalaEntity?> = LongSparseArray<SalaEntity?>()
        while (_stmt.step()) {
          val _tmpKey: Long
          _tmpKey = _stmt.getLong(_columnIndexOfPeliculaId)
          _collectionPelicula.put(_tmpKey, null)
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfSedeId)
          _collectionSede.put(_tmpKey_1, null)
          val _tmpKey_2: Long
          _tmpKey_2 = _stmt.getLong(_columnIndexOfSalaId)
          _collectionSala.put(_tmpKey_2, null)
        }
        _stmt.reset()
        __fetchRelationshippeliculasAscomCinemaxClienteDataLocalPeliculaEntity(_connection, _collectionPelicula)
        __fetchRelationshipsedesAscomCinemaxClienteDataLocalSedeEntity(_connection, _collectionSede)
        __fetchRelationshipsalasAscomCinemaxClienteDataLocalSalaEntity(_connection, _collectionSala)
        val _result: FuncionCompleta?
        if (_stmt.step()) {
          val _tmpFuncion: FuncionEntity
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
          _tmpFuncion = FuncionEntity(_tmpId,_tmpPeliculaId,_tmpSedeId,_tmpSalaId,_tmpFecha,_tmpHora,_tmpPrecioEntrada,_tmpActiva)
          val _tmpPelicula: PeliculaEntity?
          val _tmpKey_3: Long
          _tmpKey_3 = _stmt.getLong(_columnIndexOfPeliculaId)
          _tmpPelicula = _collectionPelicula.get(_tmpKey_3)
          if (_tmpPelicula == null) {
            error("Relationship item 'pelicula' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'pelicula_id' and entityColumn named 'id'.")
          }
          val _tmpSede: SedeEntity?
          val _tmpKey_4: Long
          _tmpKey_4 = _stmt.getLong(_columnIndexOfSedeId)
          _tmpSede = _collectionSede.get(_tmpKey_4)
          if (_tmpSede == null) {
            error("Relationship item 'sede' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'sede_id' and entityColumn named 'id'.")
          }
          val _tmpSala: SalaEntity?
          val _tmpKey_5: Long
          _tmpKey_5 = _stmt.getLong(_columnIndexOfSalaId)
          _tmpSala = _collectionSala.get(_tmpKey_5)
          if (_tmpSala == null) {
            error("Relationship item 'sala' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'sala_id' and entityColumn named 'id'.")
          }
          _result = FuncionCompleta(_tmpFuncion,_tmpPelicula,_tmpSede,_tmpSala)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observarCompletasPorPelicula(peliculaId: Int): Flow<List<FuncionCompleta>> {
    val _sql: String = """
        |
        |        SELECT f.* FROM funciones f
        |        INNER JOIN peliculas p ON p.id = f.pelicula_id
        |        WHERE f.pelicula_id = ?
        |          AND f.activa = 1
        |          AND p.activo = 1
        |        ORDER BY f.fecha, f.hora
        |        
        """.trimMargin()
    return createFlow(__db, true, arrayOf("peliculas", "sedes", "salas", "funciones")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, peliculaId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPeliculaId: Int = getColumnIndexOrThrow(_stmt, "pelicula_id")
        val _columnIndexOfSedeId: Int = getColumnIndexOrThrow(_stmt, "sede_id")
        val _columnIndexOfSalaId: Int = getColumnIndexOrThrow(_stmt, "sala_id")
        val _columnIndexOfFecha: Int = getColumnIndexOrThrow(_stmt, "fecha")
        val _columnIndexOfHora: Int = getColumnIndexOrThrow(_stmt, "hora")
        val _columnIndexOfPrecioEntrada: Int = getColumnIndexOrThrow(_stmt, "precio_entrada")
        val _columnIndexOfActiva: Int = getColumnIndexOrThrow(_stmt, "activa")
        val _collectionPelicula: LongSparseArray<PeliculaEntity?> = LongSparseArray<PeliculaEntity?>()
        val _collectionSede: LongSparseArray<SedeEntity?> = LongSparseArray<SedeEntity?>()
        val _collectionSala: LongSparseArray<SalaEntity?> = LongSparseArray<SalaEntity?>()
        while (_stmt.step()) {
          val _tmpKey: Long
          _tmpKey = _stmt.getLong(_columnIndexOfPeliculaId)
          _collectionPelicula.put(_tmpKey, null)
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfSedeId)
          _collectionSede.put(_tmpKey_1, null)
          val _tmpKey_2: Long
          _tmpKey_2 = _stmt.getLong(_columnIndexOfSalaId)
          _collectionSala.put(_tmpKey_2, null)
        }
        _stmt.reset()
        __fetchRelationshippeliculasAscomCinemaxClienteDataLocalPeliculaEntity(_connection, _collectionPelicula)
        __fetchRelationshipsedesAscomCinemaxClienteDataLocalSedeEntity(_connection, _collectionSede)
        __fetchRelationshipsalasAscomCinemaxClienteDataLocalSalaEntity(_connection, _collectionSala)
        val _result: MutableList<FuncionCompleta> = mutableListOf()
        while (_stmt.step()) {
          val _item: FuncionCompleta
          val _tmpFuncion: FuncionEntity
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
          _tmpFuncion = FuncionEntity(_tmpId,_tmpPeliculaId,_tmpSedeId,_tmpSalaId,_tmpFecha,_tmpHora,_tmpPrecioEntrada,_tmpActiva)
          val _tmpPelicula: PeliculaEntity?
          val _tmpKey_3: Long
          _tmpKey_3 = _stmt.getLong(_columnIndexOfPeliculaId)
          _tmpPelicula = _collectionPelicula.get(_tmpKey_3)
          if (_tmpPelicula == null) {
            error("Relationship item 'pelicula' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'pelicula_id' and entityColumn named 'id'.")
          }
          val _tmpSede: SedeEntity?
          val _tmpKey_4: Long
          _tmpKey_4 = _stmt.getLong(_columnIndexOfSedeId)
          _tmpSede = _collectionSede.get(_tmpKey_4)
          if (_tmpSede == null) {
            error("Relationship item 'sede' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'sede_id' and entityColumn named 'id'.")
          }
          val _tmpSala: SalaEntity?
          val _tmpKey_5: Long
          _tmpKey_5 = _stmt.getLong(_columnIndexOfSalaId)
          _tmpSala = _collectionSala.get(_tmpKey_5)
          if (_tmpSala == null) {
            error("Relationship item 'sala' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'sala_id' and entityColumn named 'id'.")
          }
          _item = FuncionCompleta(_tmpFuncion,_tmpPelicula,_tmpSede,_tmpSala)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun eliminarPorId(id: Int) {
    val _sql: String = "DELETE FROM funciones WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun eliminarNoVigentes(fecha: String): Int {
    val _sql: String = "DELETE FROM funciones WHERE fecha < ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, fecha)
        _stmt.step()
        getTotalChangedRows(_connection)
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __fetchRelationshippeliculasAscomCinemaxClienteDataLocalPeliculaEntity(_connection: SQLiteConnection, _map: LongSparseArray<PeliculaEntity?>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, false) { _tmpMap ->
        __fetchRelationshippeliculasAscomCinemaxClienteDataLocalPeliculaEntity(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`titulo`,`genero`,`clasificacion_edad`,`duracion_minutos`,`sinopsis`,`poster_url`,`trailer_url`,`activo` FROM `peliculas` WHERE `id` IN (")
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
      val _columnIndexOfTitulo: Int = 1
      val _columnIndexOfGenero: Int = 2
      val _columnIndexOfClasificacionEdad: Int = 3
      val _columnIndexOfDuracionMinutos: Int = 4
      val _columnIndexOfSinopsis: Int = 5
      val _columnIndexOfPosterUrl: Int = 6
      val _columnIndexOfTrailerUrl: Int = 7
      val _columnIndexOfActivo: Int = 8
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_itemKeyIndex)
        if (_map.containsKey(_tmpKey)) {
          val _item_1: PeliculaEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTitulo: String
          _tmpTitulo = _stmt.getText(_columnIndexOfTitulo)
          val _tmpGenero: String
          _tmpGenero = _stmt.getText(_columnIndexOfGenero)
          val _tmpClasificacionEdad: String
          _tmpClasificacionEdad = _stmt.getText(_columnIndexOfClasificacionEdad)
          val _tmpDuracionMinutos: Int
          _tmpDuracionMinutos = _stmt.getLong(_columnIndexOfDuracionMinutos).toInt()
          val _tmpSinopsis: String
          _tmpSinopsis = _stmt.getText(_columnIndexOfSinopsis)
          val _tmpPosterUrl: String
          _tmpPosterUrl = _stmt.getText(_columnIndexOfPosterUrl)
          val _tmpTrailerUrl: String
          _tmpTrailerUrl = _stmt.getText(_columnIndexOfTrailerUrl)
          val _tmpActivo: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfActivo).toInt()
          _tmpActivo = _tmp != 0
          _item_1 = PeliculaEntity(_tmpId,_tmpTitulo,_tmpGenero,_tmpClasificacionEdad,_tmpDuracionMinutos,_tmpSinopsis,_tmpPosterUrl,_tmpTrailerUrl,_tmpActivo)
          _map.put(_tmpKey, _item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  private fun __fetchRelationshipsedesAscomCinemaxClienteDataLocalSedeEntity(_connection: SQLiteConnection, _map: LongSparseArray<SedeEntity?>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, false) { _tmpMap ->
        __fetchRelationshipsedesAscomCinemaxClienteDataLocalSedeEntity(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`nombre`,`distrito`,`direccion` FROM `sedes` WHERE `id` IN (")
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
      val _columnIndexOfNombre: Int = 1
      val _columnIndexOfDistrito: Int = 2
      val _columnIndexOfDireccion: Int = 3
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_itemKeyIndex)
        if (_map.containsKey(_tmpKey)) {
          val _item_1: SedeEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpNombre: String
          _tmpNombre = _stmt.getText(_columnIndexOfNombre)
          val _tmpDistrito: String
          _tmpDistrito = _stmt.getText(_columnIndexOfDistrito)
          val _tmpDireccion: String
          _tmpDireccion = _stmt.getText(_columnIndexOfDireccion)
          _item_1 = SedeEntity(_tmpId,_tmpNombre,_tmpDistrito,_tmpDireccion)
          _map.put(_tmpKey, _item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  private fun __fetchRelationshipsalasAscomCinemaxClienteDataLocalSalaEntity(_connection: SQLiteConnection, _map: LongSparseArray<SalaEntity?>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, false) { _tmpMap ->
        __fetchRelationshipsalasAscomCinemaxClienteDataLocalSalaEntity(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`sede_id`,`nombre`,`filas`,`columnas` FROM `salas` WHERE `id` IN (")
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
      val _columnIndexOfSedeId: Int = 1
      val _columnIndexOfNombre: Int = 2
      val _columnIndexOfFilas: Int = 3
      val _columnIndexOfColumnas: Int = 4
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_itemKeyIndex)
        if (_map.containsKey(_tmpKey)) {
          val _item_1: SalaEntity
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
          _item_1 = SalaEntity(_tmpId,_tmpSedeId,_tmpNombre,_tmpFilas,_tmpColumnas)
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
