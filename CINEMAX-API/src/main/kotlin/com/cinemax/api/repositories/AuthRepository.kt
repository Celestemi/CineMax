package com.cinemax.api.repositories

import at.favre.lib.crypto.bcrypt.BCrypt
import com.cinemax.api.db.DatabaseFactory
import com.cinemax.api.dto.LoginRequest
import com.cinemax.api.dto.UsuarioDto
import io.ktor.http.HttpStatusCode
import com.cinemax.api.plugins.AppException

object AuthRepository {

    fun login(request: LoginRequest): UsuarioDto {
        if (request.usuario.isBlank() || request.contrasena.isBlank()) {
            throw AppException(HttpStatusCode.BadRequest, "VALIDACION", "usuario y contrasena son obligatorios")
        }
        return DatabaseFactory.connection().use { conn ->
            conn.prepareStatement(
                "SELECT id, usuario, password_hash, nombre_completo, email, telefono, rol, activo FROM usuarios WHERE usuario = ?"
            ).use { ps ->
                ps.setString(1, request.usuario)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) {
                        throw AppException(HttpStatusCode.Unauthorized, "CREDENCIALES_INVALIDAS", "Usuario o contrasena incorrectos")
                    }
                    val hash = rs.getString("password_hash")
                    val verificado = BCrypt.verifyer().verify(request.contrasena.toCharArray(), hash)
                    if (!verificado.verified) {
                        throw AppException(HttpStatusCode.Unauthorized, "CREDENCIALES_INVALIDAS", "Usuario o contrasena incorrectos")
                    }
                    if (!rs.getBoolean("activo")) {
                        throw AppException(HttpStatusCode.Forbidden, "USUARIO_INACTIVO", "Cuenta desactivada")
                    }
                    UsuarioDto(
                        id = rs.getLong("id"),
                        usuario = rs.getString("usuario"),
                        nombreCompleto = rs.getString("nombre_completo"),
                        email = rs.getString("email"),
                        telefono = rs.getString("telefono"),
                        rol = rs.getString("rol"),
                        activo = rs.getBoolean("activo")
                    )
                }
            }
        }
    }
}
