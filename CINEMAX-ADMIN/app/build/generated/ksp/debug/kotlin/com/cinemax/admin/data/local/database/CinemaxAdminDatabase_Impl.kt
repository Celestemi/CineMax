package com.cinemax.admin.`data`.local.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.cinemax.admin.`data`.local.dao.UsuarioDao
import com.cinemax.admin.`data`.local.dao.UsuarioDao_Impl
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
public class CinemaxAdminDatabase_Impl : CinemaxAdminDatabase() {
  private val _usuarioDao: Lazy<UsuarioDao> = lazy {
    UsuarioDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "9ec487add92126e5ee6b88b2009dbb3e", "27d3e1c3279d8c3009508e70e581099b") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `usuarios` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `usuario` TEXT NOT NULL, `contrasena` TEXT NOT NULL, `nombre_completo` TEXT NOT NULL, `email` TEXT NOT NULL, `telefono` TEXT NOT NULL, `rol` TEXT NOT NULL, `activo` INTEGER NOT NULL, `creado_en` INTEGER NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_usuarios_usuario` ON `usuarios` (`usuario`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '9ec487add92126e5ee6b88b2009dbb3e')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `usuarios`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
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
              |usuarios(com.cinemax.admin.data.local.entity.UsuarioEntity).
              | Expected:
              |""".trimMargin() + _infoUsuarios + """
              |
              | Found:
              |""".trimMargin() + _existingUsuarios)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "usuarios")
  }

  public override fun clearAllTables() {
    super.performClear(false, "usuarios")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(UsuarioDao::class, UsuarioDao_Impl.getRequiredConverters())
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
}
