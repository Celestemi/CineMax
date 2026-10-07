package com.cinemax.cliente.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CineMaxClienteDatabase_Impl : CineMaxClienteDatabase() {
  private val _usuarioDao: Lazy<UsuarioDao> = lazy {
    UsuarioDao_Impl(this)
  }

  private val _peliculaDao: Lazy<PeliculaDao> = lazy {
    PeliculaDao_Impl(this)
  }

  private val _sedeDao: Lazy<SedeDao> = lazy {
    SedeDao_Impl(this)
  }

  private val _salaDao: Lazy<SalaDao> = lazy {
    SalaDao_Impl(this)
  }

  private val _butacaDao: Lazy<ButacaDao> = lazy {
    ButacaDao_Impl(this)
  }

  private val _funcionDao: Lazy<FuncionDao> = lazy {
    FuncionDao_Impl(this)
  }

  private val _reservaDao: Lazy<ReservaDao> = lazy {
    ReservaDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "4b9319409d1c0afbc9cfca6d9b12301a", "63c3fd4983842f25c35c90cc59f67645") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `usuarios` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `usuario` TEXT NOT NULL, `contrasena` TEXT NOT NULL, `nombre_completo` TEXT NOT NULL, `email` TEXT NOT NULL, `telefono` TEXT NOT NULL, `rol` TEXT NOT NULL, `activo` INTEGER NOT NULL, `creado_en` INTEGER NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_usuarios_usuario` ON `usuarios` (`usuario`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `peliculas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `titulo` TEXT NOT NULL, `genero` TEXT NOT NULL, `clasificacion_edad` TEXT NOT NULL, `duracion_minutos` INTEGER NOT NULL, `sinopsis` TEXT NOT NULL, `poster_url` TEXT NOT NULL, `trailer_url` TEXT NOT NULL, `activo` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_peliculas_titulo` ON `peliculas` (`titulo`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_peliculas_genero` ON `peliculas` (`genero`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sedes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nombre` TEXT NOT NULL, `distrito` TEXT NOT NULL, `direccion` TEXT NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_sedes_nombre` ON `sedes` (`nombre`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `salas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sede_id` INTEGER NOT NULL, `nombre` TEXT NOT NULL, `filas` INTEGER NOT NULL, `columnas` INTEGER NOT NULL, FOREIGN KEY(`sede_id`) REFERENCES `sedes`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_salas_sede_id` ON `salas` (`sede_id`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `butacas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sala_id` INTEGER NOT NULL, `fila` TEXT NOT NULL, `numero` INTEGER NOT NULL, FOREIGN KEY(`sala_id`) REFERENCES `salas`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_butacas_sala_id` ON `butacas` (`sala_id`)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_butacas_sala_id_fila_numero` ON `butacas` (`sala_id`, `fila`, `numero`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `funciones` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `pelicula_id` INTEGER NOT NULL, `sede_id` INTEGER NOT NULL, `sala_id` INTEGER NOT NULL, `fecha` TEXT NOT NULL, `hora` TEXT NOT NULL, `precio_entrada` REAL NOT NULL, `activa` INTEGER NOT NULL, FOREIGN KEY(`pelicula_id`) REFERENCES `peliculas`(`id`) ON UPDATE CASCADE ON DELETE RESTRICT , FOREIGN KEY(`sede_id`) REFERENCES `sedes`(`id`) ON UPDATE CASCADE ON DELETE RESTRICT , FOREIGN KEY(`sala_id`) REFERENCES `salas`(`id`) ON UPDATE CASCADE ON DELETE RESTRICT )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_funciones_pelicula_id` ON `funciones` (`pelicula_id`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_funciones_sede_id` ON `funciones` (`sede_id`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_funciones_sala_id` ON `funciones` (`sala_id`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_funciones_fecha_activa` ON `funciones` (`fecha`, `activa`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `reservas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `codigo` TEXT NOT NULL, `usuario_id` INTEGER NOT NULL, `funcion_id` INTEGER NOT NULL, `fecha_compra` TEXT NOT NULL, `total` REAL NOT NULL, `estado` TEXT NOT NULL, FOREIGN KEY(`usuario_id`) REFERENCES `usuarios`(`id`) ON UPDATE CASCADE ON DELETE CASCADE , FOREIGN KEY(`funcion_id`) REFERENCES `funciones`(`id`) ON UPDATE CASCADE ON DELETE RESTRICT )")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_reservas_codigo` ON `reservas` (`codigo`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_reservas_usuario_id` ON `reservas` (`usuario_id`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_reservas_funcion_id` ON `reservas` (`funcion_id`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `butaca_reservas` (`reserva_id` INTEGER NOT NULL, `funcion_id` INTEGER NOT NULL, `butaca_id` INTEGER NOT NULL, PRIMARY KEY(`reserva_id`, `funcion_id`, `butaca_id`), FOREIGN KEY(`reserva_id`) REFERENCES `reservas`(`id`) ON UPDATE CASCADE ON DELETE CASCADE , FOREIGN KEY(`funcion_id`) REFERENCES `funciones`(`id`) ON UPDATE CASCADE ON DELETE CASCADE , FOREIGN KEY(`butaca_id`) REFERENCES `butacas`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_butaca_reservas_funcion_id_butaca_id` ON `butaca_reservas` (`funcion_id`, `butaca_id`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_butaca_reservas_butaca_id` ON `butaca_reservas` (`butaca_id`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '4b9319409d1c0afbc9cfca6d9b12301a')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `usuarios`")
        connection.execSQL("DROP TABLE IF EXISTS `peliculas`")
        connection.execSQL("DROP TABLE IF EXISTS `sedes`")
        connection.execSQL("DROP TABLE IF EXISTS `salas`")
        connection.execSQL("DROP TABLE IF EXISTS `butacas`")
        connection.execSQL("DROP TABLE IF EXISTS `funciones`")
        connection.execSQL("DROP TABLE IF EXISTS `reservas`")
        connection.execSQL("DROP TABLE IF EXISTS `butaca_reservas`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsUsuarios: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsUsuarios.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsuarios.put("usuario", TableInfo.Column("usuario", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsuarios.put("contrasena", TableInfo.Column("contrasena", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsuarios.put("nombre_completo", TableInfo.Column("nombre_completo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsuarios.put("email", TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsuarios.put("telefono", TableInfo.Column("telefono", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsuarios.put("rol", TableInfo.Column("rol", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsuarios.put("activo", TableInfo.Column("activo", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsuarios.put("creado_en", TableInfo.Column("creado_en", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUsuarios: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesUsuarios: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesUsuarios.add(TableInfo.Index("index_usuarios_usuario", true, listOf("usuario"), listOf("ASC")))
        val _infoUsuarios: TableInfo = TableInfo("usuarios", _columnsUsuarios, _foreignKeysUsuarios, _indicesUsuarios)
        val _existingUsuarios: TableInfo = read(connection, "usuarios")
        if (!_infoUsuarios.equals(_existingUsuarios)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |usuarios(com.cinemax.cliente.data.local.UsuarioEntity).
              | Expected:
              |""".trimMargin() + _infoUsuarios + """
              |
              | Found:
              |""".trimMargin() + _existingUsuarios)
        }
        val _columnsPeliculas: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPeliculas.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPeliculas.put("titulo", TableInfo.Column("titulo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPeliculas.put("genero", TableInfo.Column("genero", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPeliculas.put("clasificacion_edad", TableInfo.Column("clasificacion_edad", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPeliculas.put("duracion_minutos", TableInfo.Column("duracion_minutos", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPeliculas.put("sinopsis", TableInfo.Column("sinopsis", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPeliculas.put("poster_url", TableInfo.Column("poster_url", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPeliculas.put("trailer_url", TableInfo.Column("trailer_url", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPeliculas.put("activo", TableInfo.Column("activo", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPeliculas: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPeliculas: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesPeliculas.add(TableInfo.Index("index_peliculas_titulo", false, listOf("titulo"), listOf("ASC")))
        _indicesPeliculas.add(TableInfo.Index("index_peliculas_genero", false, listOf("genero"), listOf("ASC")))
        val _infoPeliculas: TableInfo = TableInfo("peliculas", _columnsPeliculas, _foreignKeysPeliculas, _indicesPeliculas)
        val _existingPeliculas: TableInfo = read(connection, "peliculas")
        if (!_infoPeliculas.equals(_existingPeliculas)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |peliculas(com.cinemax.cliente.data.local.PeliculaEntity).
              | Expected:
              |""".trimMargin() + _infoPeliculas + """
              |
              | Found:
              |""".trimMargin() + _existingPeliculas)
        }
        val _columnsSedes: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSedes.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSedes.put("nombre", TableInfo.Column("nombre", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSedes.put("distrito", TableInfo.Column("distrito", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSedes.put("direccion", TableInfo.Column("direccion", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSedes: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSedes: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSedes.add(TableInfo.Index("index_sedes_nombre", true, listOf("nombre"), listOf("ASC")))
        val _infoSedes: TableInfo = TableInfo("sedes", _columnsSedes, _foreignKeysSedes, _indicesSedes)
        val _existingSedes: TableInfo = read(connection, "sedes")
        if (!_infoSedes.equals(_existingSedes)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |sedes(com.cinemax.cliente.data.local.SedeEntity).
              | Expected:
              |""".trimMargin() + _infoSedes + """
              |
              | Found:
              |""".trimMargin() + _existingSedes)
        }
        val _columnsSalas: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSalas.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSalas.put("sede_id", TableInfo.Column("sede_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSalas.put("nombre", TableInfo.Column("nombre", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSalas.put("filas", TableInfo.Column("filas", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSalas.put("columnas", TableInfo.Column("columnas", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSalas: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysSalas.add(TableInfo.ForeignKey("sedes", "CASCADE", "CASCADE", listOf("sede_id"), listOf("id")))
        val _indicesSalas: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSalas.add(TableInfo.Index("index_salas_sede_id", false, listOf("sede_id"), listOf("ASC")))
        val _infoSalas: TableInfo = TableInfo("salas", _columnsSalas, _foreignKeysSalas, _indicesSalas)
        val _existingSalas: TableInfo = read(connection, "salas")
        if (!_infoSalas.equals(_existingSalas)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |salas(com.cinemax.cliente.data.local.SalaEntity).
              | Expected:
              |""".trimMargin() + _infoSalas + """
              |
              | Found:
              |""".trimMargin() + _existingSalas)
        }
        val _columnsButacas: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsButacas.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsButacas.put("sala_id", TableInfo.Column("sala_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsButacas.put("fila", TableInfo.Column("fila", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsButacas.put("numero", TableInfo.Column("numero", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysButacas: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysButacas.add(TableInfo.ForeignKey("salas", "CASCADE", "CASCADE", listOf("sala_id"), listOf("id")))
        val _indicesButacas: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesButacas.add(TableInfo.Index("index_butacas_sala_id", false, listOf("sala_id"), listOf("ASC")))
        _indicesButacas.add(TableInfo.Index("index_butacas_sala_id_fila_numero", true, listOf("sala_id", "fila", "numero"), listOf("ASC", "ASC", "ASC")))
        val _infoButacas: TableInfo = TableInfo("butacas", _columnsButacas, _foreignKeysButacas, _indicesButacas)
        val _existingButacas: TableInfo = read(connection, "butacas")
        if (!_infoButacas.equals(_existingButacas)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |butacas(com.cinemax.cliente.data.local.ButacaEntity).
              | Expected:
              |""".trimMargin() + _infoButacas + """
              |
              | Found:
              |""".trimMargin() + _existingButacas)
        }
        val _columnsFunciones: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFunciones.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFunciones.put("pelicula_id", TableInfo.Column("pelicula_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFunciones.put("sede_id", TableInfo.Column("sede_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFunciones.put("sala_id", TableInfo.Column("sala_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFunciones.put("fecha", TableInfo.Column("fecha", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFunciones.put("hora", TableInfo.Column("hora", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFunciones.put("precio_entrada", TableInfo.Column("precio_entrada", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFunciones.put("activa", TableInfo.Column("activa", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFunciones: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysFunciones.add(TableInfo.ForeignKey("peliculas", "RESTRICT", "CASCADE", listOf("pelicula_id"), listOf("id")))
        _foreignKeysFunciones.add(TableInfo.ForeignKey("sedes", "RESTRICT", "CASCADE", listOf("sede_id"), listOf("id")))
        _foreignKeysFunciones.add(TableInfo.ForeignKey("salas", "RESTRICT", "CASCADE", listOf("sala_id"), listOf("id")))
        val _indicesFunciones: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesFunciones.add(TableInfo.Index("index_funciones_pelicula_id", false, listOf("pelicula_id"), listOf("ASC")))
        _indicesFunciones.add(TableInfo.Index("index_funciones_sede_id", false, listOf("sede_id"), listOf("ASC")))
        _indicesFunciones.add(TableInfo.Index("index_funciones_sala_id", false, listOf("sala_id"), listOf("ASC")))
        _indicesFunciones.add(TableInfo.Index("index_funciones_fecha_activa", false, listOf("fecha", "activa"), listOf("ASC", "ASC")))
        val _infoFunciones: TableInfo = TableInfo("funciones", _columnsFunciones, _foreignKeysFunciones, _indicesFunciones)
        val _existingFunciones: TableInfo = read(connection, "funciones")
        if (!_infoFunciones.equals(_existingFunciones)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |funciones(com.cinemax.cliente.data.local.FuncionEntity).
              | Expected:
              |""".trimMargin() + _infoFunciones + """
              |
              | Found:
              |""".trimMargin() + _existingFunciones)
        }
        val _columnsReservas: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsReservas.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReservas.put("codigo", TableInfo.Column("codigo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReservas.put("usuario_id", TableInfo.Column("usuario_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReservas.put("funcion_id", TableInfo.Column("funcion_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReservas.put("fecha_compra", TableInfo.Column("fecha_compra", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReservas.put("total", TableInfo.Column("total", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReservas.put("estado", TableInfo.Column("estado", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysReservas: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysReservas.add(TableInfo.ForeignKey("usuarios", "CASCADE", "CASCADE", listOf("usuario_id"), listOf("id")))
        _foreignKeysReservas.add(TableInfo.ForeignKey("funciones", "RESTRICT", "CASCADE", listOf("funcion_id"), listOf("id")))
        val _indicesReservas: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesReservas.add(TableInfo.Index("index_reservas_codigo", true, listOf("codigo"), listOf("ASC")))
        _indicesReservas.add(TableInfo.Index("index_reservas_usuario_id", false, listOf("usuario_id"), listOf("ASC")))
        _indicesReservas.add(TableInfo.Index("index_reservas_funcion_id", false, listOf("funcion_id"), listOf("ASC")))
        val _infoReservas: TableInfo = TableInfo("reservas", _columnsReservas, _foreignKeysReservas, _indicesReservas)
        val _existingReservas: TableInfo = read(connection, "reservas")
        if (!_infoReservas.equals(_existingReservas)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |reservas(com.cinemax.cliente.data.local.ReservaEntity).
              | Expected:
              |""".trimMargin() + _infoReservas + """
              |
              | Found:
              |""".trimMargin() + _existingReservas)
        }
        val _columnsButacaReservas: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsButacaReservas.put("reserva_id", TableInfo.Column("reserva_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsButacaReservas.put("funcion_id", TableInfo.Column("funcion_id", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsButacaReservas.put("butaca_id", TableInfo.Column("butaca_id", "INTEGER", true, 3, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysButacaReservas: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysButacaReservas.add(TableInfo.ForeignKey("reservas", "CASCADE", "CASCADE", listOf("reserva_id"), listOf("id")))
        _foreignKeysButacaReservas.add(TableInfo.ForeignKey("funciones", "CASCADE", "CASCADE", listOf("funcion_id"), listOf("id")))
        _foreignKeysButacaReservas.add(TableInfo.ForeignKey("butacas", "CASCADE", "CASCADE", listOf("butaca_id"), listOf("id")))
        val _indicesButacaReservas: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesButacaReservas.add(TableInfo.Index("index_butaca_reservas_funcion_id_butaca_id", true, listOf("funcion_id", "butaca_id"), listOf("ASC", "ASC")))
        _indicesButacaReservas.add(TableInfo.Index("index_butaca_reservas_butaca_id", false, listOf("butaca_id"), listOf("ASC")))
        val _infoButacaReservas: TableInfo = TableInfo("butaca_reservas", _columnsButacaReservas, _foreignKeysButacaReservas, _indicesButacaReservas)
        val _existingButacaReservas: TableInfo = read(connection, "butaca_reservas")
        if (!_infoButacaReservas.equals(_existingButacaReservas)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |butaca_reservas(com.cinemax.cliente.data.local.ButacaReservaEntity).
              | Expected:
              |""".trimMargin() + _infoButacaReservas + """
              |
              | Found:
              |""".trimMargin() + _existingButacaReservas)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "usuarios", "peliculas", "sedes", "salas", "butacas", "funciones", "reservas", "butaca_reservas")
  }

  public override fun clearAllTables() {
    super.performClear(true, "usuarios", "peliculas", "sedes", "salas", "butacas", "reservas", "funciones", "butaca_reservas")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(UsuarioDao::class, UsuarioDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(PeliculaDao::class, PeliculaDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SedeDao::class, SedeDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SalaDao::class, SalaDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ButacaDao::class, ButacaDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(FuncionDao::class, FuncionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ReservaDao::class, ReservaDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun usuarioDao(): UsuarioDao = _usuarioDao.value

  public override fun peliculaDao(): PeliculaDao = _peliculaDao.value

  public override fun sedeDao(): SedeDao = _sedeDao.value

  public override fun salaDao(): SalaDao = _salaDao.value

  public override fun butacaDao(): ButacaDao = _butacaDao.value

  public override fun funcionDao(): FuncionDao = _funcionDao.value

  public override fun reservaDao(): ReservaDao = _reservaDao.value
}
