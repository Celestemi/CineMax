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
public class UsuarioDao_Impl(
  __db: RoomDatabase,
) : UsuarioDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfUsuarioEntity: EntityInsertAdapter<UsuarioEntity>

  private val __deleteAdapterOfUsuarioEntity: EntityDeleteOrUpdateAdapter<UsuarioEntity>

  private val __updateAdapterOfUsuarioEntity: EntityDeleteOrUpdateAdapter<UsuarioEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfUsuarioEntity = object : EntityInsertAdapter<UsuarioEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `usuarios` (`id`,`usuario`,`contrasena`,`nombre_completo`,`email`,`telefono`,`rol`,`activo`,`creado_en`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: UsuarioEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.usuario)
        statement.bindText(3, entity.contrasena)
        statement.bindText(4, entity.nombreCompleto)
        statement.bindText(5, entity.email)
        statement.bindText(6, entity.telefono)
        statement.bindText(7, entity.rol)
        val _tmp: Int = if (entity.activo) 1 else 0
        statement.bindLong(8, _tmp.toLong())
        statement.bindLong(9, entity.creadoEn)
      }
    }
    this.__deleteAdapterOfUsuarioEntity = object : EntityDeleteOrUpdateAdapter<UsuarioEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `usuarios` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: UsuarioEntity) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfUsuarioEntity = object : EntityDeleteOrUpdateAdapter<UsuarioEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `usuarios` SET `id` = ?,`usuario` = ?,`contrasena` = ?,`nombre_completo` = ?,`email` = ?,`telefono` = ?,`rol` = ?,`activo` = ?,`creado_en` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: UsuarioEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.usuario)
        statement.bindText(3, entity.contrasena)
        statement.bindText(4, entity.nombreCompleto)
        statement.bindText(5, entity.email)
        statement.bindText(6, entity.telefono)
        statement.bindText(7, entity.rol)
        val _tmp: Int = if (entity.activo) 1 else 0
        statement.bindLong(8, _tmp.toLong())
        statement.bindLong(9, entity.creadoEn)
        statement.bindLong(10, entity.id.toLong())
      }
    }
  }

  public override suspend fun insertar(usuario: UsuarioEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfUsuarioEntity.insertAndReturnId(_connection, usuario)
    _result
  }

  public override suspend fun eliminar(usuario: UsuarioEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfUsuarioEntity.handle(_connection, usuario)
  }

  public override suspend fun actualizar(usuario: UsuarioEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfUsuarioEntity.handle(_connection, usuario)
  }

  public override suspend fun buscarPorUsuario(usuario: String): UsuarioEntity? {
    val _sql: String = "SELECT * FROM usuarios WHERE usuario = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, usuario)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUsuario: Int = getColumnIndexOrThrow(_stmt, "usuario")
        val _columnIndexOfContrasena: Int = getColumnIndexOrThrow(_stmt, "contrasena")
        val _columnIndexOfNombreCompleto: Int = getColumnIndexOrThrow(_stmt, "nombre_completo")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfTelefono: Int = getColumnIndexOrThrow(_stmt, "telefono")
        val _columnIndexOfRol: Int = getColumnIndexOrThrow(_stmt, "rol")
        val _columnIndexOfActivo: Int = getColumnIndexOrThrow(_stmt, "activo")
        val _columnIndexOfCreadoEn: Int = getColumnIndexOrThrow(_stmt, "creado_en")
        val _result: UsuarioEntity?
        if (_stmt.step()) {
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
          _result = UsuarioEntity(_tmpId,_tmpUsuario,_tmpContrasena,_tmpNombreCompleto,_tmpEmail,_tmpTelefono,_tmpRol,_tmpActivo,_tmpCreadoEn)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun login(usuario: String, contrasena: String): UsuarioEntity? {
    val _sql: String = "SELECT * FROM usuarios WHERE usuario = ? AND contrasena = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, usuario)
        _argIndex = 2
        _stmt.bindText(_argIndex, contrasena)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUsuario: Int = getColumnIndexOrThrow(_stmt, "usuario")
        val _columnIndexOfContrasena: Int = getColumnIndexOrThrow(_stmt, "contrasena")
        val _columnIndexOfNombreCompleto: Int = getColumnIndexOrThrow(_stmt, "nombre_completo")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfTelefono: Int = getColumnIndexOrThrow(_stmt, "telefono")
        val _columnIndexOfRol: Int = getColumnIndexOrThrow(_stmt, "rol")
        val _columnIndexOfActivo: Int = getColumnIndexOrThrow(_stmt, "activo")
        val _columnIndexOfCreadoEn: Int = getColumnIndexOrThrow(_stmt, "creado_en")
        val _result: UsuarioEntity?
        if (_stmt.step()) {
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
          _result = UsuarioEntity(_tmpId,_tmpUsuario,_tmpContrasena,_tmpNombreCompleto,_tmpEmail,_tmpTelefono,_tmpRol,_tmpActivo,_tmpCreadoEn)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observarPorUsuario(usuario: String): Flow<UsuarioEntity?> {
    val _sql: String = "SELECT * FROM usuarios WHERE usuario = ? LIMIT 1"
    return createFlow(__db, false, arrayOf("usuarios")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, usuario)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUsuario: Int = getColumnIndexOrThrow(_stmt, "usuario")
        val _columnIndexOfContrasena: Int = getColumnIndexOrThrow(_stmt, "contrasena")
        val _columnIndexOfNombreCompleto: Int = getColumnIndexOrThrow(_stmt, "nombre_completo")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfTelefono: Int = getColumnIndexOrThrow(_stmt, "telefono")
        val _columnIndexOfRol: Int = getColumnIndexOrThrow(_stmt, "rol")
        val _columnIndexOfActivo: Int = getColumnIndexOrThrow(_stmt, "activo")
        val _columnIndexOfCreadoEn: Int = getColumnIndexOrThrow(_stmt, "creado_en")
        val _result: UsuarioEntity?
        if (_stmt.step()) {
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
          _result = UsuarioEntity(_tmpId,_tmpUsuario,_tmpContrasena,_tmpNombreCompleto,_tmpEmail,_tmpTelefono,_tmpRol,_tmpActivo,_tmpCreadoEn)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun porId(id: Int): UsuarioEntity? {
    val _sql: String = "SELECT * FROM usuarios WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUsuario: Int = getColumnIndexOrThrow(_stmt, "usuario")
        val _columnIndexOfContrasena: Int = getColumnIndexOrThrow(_stmt, "contrasena")
        val _columnIndexOfNombreCompleto: Int = getColumnIndexOrThrow(_stmt, "nombre_completo")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfTelefono: Int = getColumnIndexOrThrow(_stmt, "telefono")
        val _columnIndexOfRol: Int = getColumnIndexOrThrow(_stmt, "rol")
        val _columnIndexOfActivo: Int = getColumnIndexOrThrow(_stmt, "activo")
        val _columnIndexOfCreadoEn: Int = getColumnIndexOrThrow(_stmt, "creado_en")
        val _result: UsuarioEntity?
        if (_stmt.step()) {
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
          _result = UsuarioEntity(_tmpId,_tmpUsuario,_tmpContrasena,_tmpNombreCompleto,_tmpEmail,_tmpTelefono,_tmpRol,_tmpActivo,_tmpCreadoEn)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun existeUsuario(usuario: String): Boolean {
    val _sql: String = "SELECT EXISTS(SELECT 1 FROM usuarios WHERE usuario = ?)"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, usuario)
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observarPorRol(rol: String): Flow<List<UsuarioEntity>> {
    val _sql: String = "SELECT * FROM usuarios WHERE rol = ? AND activo = 1 ORDER BY nombre_completo"
    return createFlow(__db, false, arrayOf("usuarios")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, rol)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUsuario: Int = getColumnIndexOrThrow(_stmt, "usuario")
        val _columnIndexOfContrasena: Int = getColumnIndexOrThrow(_stmt, "contrasena")
        val _columnIndexOfNombreCompleto: Int = getColumnIndexOrThrow(_stmt, "nombre_completo")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfTelefono: Int = getColumnIndexOrThrow(_stmt, "telefono")
        val _columnIndexOfRol: Int = getColumnIndexOrThrow(_stmt, "rol")
        val _columnIndexOfActivo: Int = getColumnIndexOrThrow(_stmt, "activo")
        val _columnIndexOfCreadoEn: Int = getColumnIndexOrThrow(_stmt, "creado_en")
        val _result: MutableList<UsuarioEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: UsuarioEntity
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
          _item = UsuarioEntity(_tmpId,_tmpUsuario,_tmpContrasena,_tmpNombreCompleto,_tmpEmail,_tmpTelefono,_tmpRol,_tmpActivo,_tmpCreadoEn)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun contar(): Int {
    val _sql: String = "SELECT COUNT(*) FROM usuarios"
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
