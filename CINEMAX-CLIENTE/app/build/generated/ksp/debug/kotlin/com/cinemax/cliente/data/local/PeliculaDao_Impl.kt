package com.cinemax.cliente.`data`.local

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
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
public class PeliculaDao_Impl(
  __db: RoomDatabase,
) : PeliculaDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPeliculaEntity: EntityInsertAdapter<PeliculaEntity>

  private val __deleteAdapterOfPeliculaEntity: EntityDeleteOrUpdateAdapter<PeliculaEntity>

  private val __updateAdapterOfPeliculaEntity: EntityDeleteOrUpdateAdapter<PeliculaEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfPeliculaEntity = object : EntityInsertAdapter<PeliculaEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `peliculas` (`id`,`titulo`,`genero`,`clasificacion_edad`,`duracion_minutos`,`sinopsis`,`poster_url`,`trailer_url`,`activo`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PeliculaEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.titulo)
        statement.bindText(3, entity.genero)
        statement.bindText(4, entity.clasificacionEdad)
        statement.bindLong(5, entity.duracionMinutos.toLong())
        statement.bindText(6, entity.sinopsis)
        statement.bindText(7, entity.posterUrl)
        statement.bindText(8, entity.trailerUrl)
        val _tmp: Int = if (entity.activo) 1 else 0
        statement.bindLong(9, _tmp.toLong())
      }
    }
    this.__deleteAdapterOfPeliculaEntity = object : EntityDeleteOrUpdateAdapter<PeliculaEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `peliculas` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PeliculaEntity) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfPeliculaEntity = object : EntityDeleteOrUpdateAdapter<PeliculaEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `peliculas` SET `id` = ?,`titulo` = ?,`genero` = ?,`clasificacion_edad` = ?,`duracion_minutos` = ?,`sinopsis` = ?,`poster_url` = ?,`trailer_url` = ?,`activo` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PeliculaEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.titulo)
        statement.bindText(3, entity.genero)
        statement.bindText(4, entity.clasificacionEdad)
        statement.bindLong(5, entity.duracionMinutos.toLong())
        statement.bindText(6, entity.sinopsis)
        statement.bindText(7, entity.posterUrl)
        statement.bindText(8, entity.trailerUrl)
        val _tmp: Int = if (entity.activo) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        statement.bindLong(10, entity.id.toLong())
      }
    }
  }

  public override suspend fun insertar(pelicula: PeliculaEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfPeliculaEntity.insertAndReturnId(_connection, pelicula)
    _result
  }

  public override suspend fun insertarTodas(peliculas: List<PeliculaEntity>): List<Long> = performSuspending(__db, false, true) { _connection ->
    val _result: List<Long> = __insertAdapterOfPeliculaEntity.insertAndReturnIdsList(_connection, peliculas)
    _result
  }

  public override suspend fun eliminar(pelicula: PeliculaEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfPeliculaEntity.handle(_connection, pelicula)
  }

  public override suspend fun actualizar(pelicula: PeliculaEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfPeliculaEntity.handle(_connection, pelicula)
  }

  public override fun observarTodas(): Flow<List<PeliculaEntity>> {
    val _sql: String = "SELECT * FROM peliculas ORDER BY titulo"
    return createFlow(__db, false, arrayOf("peliculas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitulo: Int = getColumnIndexOrThrow(_stmt, "titulo")
        val _columnIndexOfGenero: Int = getColumnIndexOrThrow(_stmt, "genero")
        val _columnIndexOfClasificacionEdad: Int = getColumnIndexOrThrow(_stmt, "clasificacion_edad")
        val _columnIndexOfDuracionMinutos: Int = getColumnIndexOrThrow(_stmt, "duracion_minutos")
        val _columnIndexOfSinopsis: Int = getColumnIndexOrThrow(_stmt, "sinopsis")
        val _columnIndexOfPosterUrl: Int = getColumnIndexOrThrow(_stmt, "poster_url")
        val _columnIndexOfTrailerUrl: Int = getColumnIndexOrThrow(_stmt, "trailer_url")
        val _columnIndexOfActivo: Int = getColumnIndexOrThrow(_stmt, "activo")
        val _result: MutableList<PeliculaEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PeliculaEntity
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
          _item = PeliculaEntity(_tmpId,_tmpTitulo,_tmpGenero,_tmpClasificacionEdad,_tmpDuracionMinutos,_tmpSinopsis,_tmpPosterUrl,_tmpTrailerUrl,_tmpActivo)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observarActivas(): Flow<List<PeliculaEntity>> {
    val _sql: String = "SELECT * FROM peliculas WHERE activo = 1 ORDER BY titulo"
    return createFlow(__db, false, arrayOf("peliculas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitulo: Int = getColumnIndexOrThrow(_stmt, "titulo")
        val _columnIndexOfGenero: Int = getColumnIndexOrThrow(_stmt, "genero")
        val _columnIndexOfClasificacionEdad: Int = getColumnIndexOrThrow(_stmt, "clasificacion_edad")
        val _columnIndexOfDuracionMinutos: Int = getColumnIndexOrThrow(_stmt, "duracion_minutos")
        val _columnIndexOfSinopsis: Int = getColumnIndexOrThrow(_stmt, "sinopsis")
        val _columnIndexOfPosterUrl: Int = getColumnIndexOrThrow(_stmt, "poster_url")
        val _columnIndexOfTrailerUrl: Int = getColumnIndexOrThrow(_stmt, "trailer_url")
        val _columnIndexOfActivo: Int = getColumnIndexOrThrow(_stmt, "activo")
        val _result: MutableList<PeliculaEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PeliculaEntity
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
          _item = PeliculaEntity(_tmpId,_tmpTitulo,_tmpGenero,_tmpClasificacionEdad,_tmpDuracionMinutos,_tmpSinopsis,_tmpPosterUrl,_tmpTrailerUrl,_tmpActivo)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observarPorId(id: Int): Flow<PeliculaEntity?> {
    val _sql: String = "SELECT * FROM peliculas WHERE id = ? LIMIT 1"
    return createFlow(__db, false, arrayOf("peliculas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitulo: Int = getColumnIndexOrThrow(_stmt, "titulo")
        val _columnIndexOfGenero: Int = getColumnIndexOrThrow(_stmt, "genero")
        val _columnIndexOfClasificacionEdad: Int = getColumnIndexOrThrow(_stmt, "clasificacion_edad")
        val _columnIndexOfDuracionMinutos: Int = getColumnIndexOrThrow(_stmt, "duracion_minutos")
        val _columnIndexOfSinopsis: Int = getColumnIndexOrThrow(_stmt, "sinopsis")
        val _columnIndexOfPosterUrl: Int = getColumnIndexOrThrow(_stmt, "poster_url")
        val _columnIndexOfTrailerUrl: Int = getColumnIndexOrThrow(_stmt, "trailer_url")
        val _columnIndexOfActivo: Int = getColumnIndexOrThrow(_stmt, "activo")
        val _result: PeliculaEntity?
        if (_stmt.step()) {
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
          _result = PeliculaEntity(_tmpId,_tmpTitulo,_tmpGenero,_tmpClasificacionEdad,_tmpDuracionMinutos,_tmpSinopsis,_tmpPosterUrl,_tmpTrailerUrl,_tmpActivo)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun porId(id: Int): PeliculaEntity? {
    val _sql: String = "SELECT * FROM peliculas WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitulo: Int = getColumnIndexOrThrow(_stmt, "titulo")
        val _columnIndexOfGenero: Int = getColumnIndexOrThrow(_stmt, "genero")
        val _columnIndexOfClasificacionEdad: Int = getColumnIndexOrThrow(_stmt, "clasificacion_edad")
        val _columnIndexOfDuracionMinutos: Int = getColumnIndexOrThrow(_stmt, "duracion_minutos")
        val _columnIndexOfSinopsis: Int = getColumnIndexOrThrow(_stmt, "sinopsis")
        val _columnIndexOfPosterUrl: Int = getColumnIndexOrThrow(_stmt, "poster_url")
        val _columnIndexOfTrailerUrl: Int = getColumnIndexOrThrow(_stmt, "trailer_url")
        val _columnIndexOfActivo: Int = getColumnIndexOrThrow(_stmt, "activo")
        val _result: PeliculaEntity?
        if (_stmt.step()) {
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
          _result = PeliculaEntity(_tmpId,_tmpTitulo,_tmpGenero,_tmpClasificacionEdad,_tmpDuracionMinutos,_tmpSinopsis,_tmpPosterUrl,_tmpTrailerUrl,_tmpActivo)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun porGenero(genero: String): Flow<List<PeliculaEntity>> {
    val _sql: String = "SELECT * FROM peliculas WHERE genero = ? ORDER BY titulo"
    return createFlow(__db, false, arrayOf("peliculas")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, genero)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitulo: Int = getColumnIndexOrThrow(_stmt, "titulo")
        val _columnIndexOfGenero: Int = getColumnIndexOrThrow(_stmt, "genero")
        val _columnIndexOfClasificacionEdad: Int = getColumnIndexOrThrow(_stmt, "clasificacion_edad")
        val _columnIndexOfDuracionMinutos: Int = getColumnIndexOrThrow(_stmt, "duracion_minutos")
        val _columnIndexOfSinopsis: Int = getColumnIndexOrThrow(_stmt, "sinopsis")
        val _columnIndexOfPosterUrl: Int = getColumnIndexOrThrow(_stmt, "poster_url")
        val _columnIndexOfTrailerUrl: Int = getColumnIndexOrThrow(_stmt, "trailer_url")
        val _columnIndexOfActivo: Int = getColumnIndexOrThrow(_stmt, "activo")
        val _result: MutableList<PeliculaEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PeliculaEntity
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
          _item = PeliculaEntity(_tmpId,_tmpTitulo,_tmpGenero,_tmpClasificacionEdad,_tmpDuracionMinutos,_tmpSinopsis,_tmpPosterUrl,_tmpTrailerUrl,_tmpActivo)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observarGeneros(): Flow<List<String>> {
    val _sql: String = "SELECT DISTINCT genero FROM peliculas ORDER BY genero"
    return createFlow(__db, false, arrayOf("peliculas")) { _connection ->
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
    val _sql: String = "SELECT COUNT(*) FROM peliculas"
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
