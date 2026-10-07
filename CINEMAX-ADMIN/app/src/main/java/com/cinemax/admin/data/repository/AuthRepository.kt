package com.cinemax.admin.data.repository

import com.cinemax.admin.data.local.dao.UsuarioDao
import com.cinemax.admin.data.local.entity.UsuarioEntity
import com.cinemax.admin.model.RolAdmin
import com.cinemax.admin.model.UsuarioSesion
import java.util.Locale

/**
 * FASE 8 - Resultado de [AuthRepository.login].
 *
 * Se modela con una clase sellada en lugar de un `Boolean` porque la UI necesita
 * distinguir usuario inexistente, contrasena incorrecta y usuario inactivo. La
 * validacion de ROL (ADMINISTRADOR) no ocurre aqui, sino en `AuthViewModel`.
 */
sealed class ResultadoLogin {

    /** Credenciales correctas. El rol todavia NO ha sido validado. */
    data class Exito(val usuario: UsuarioEntity) : ResultadoLogin()

    /** No existe ningun usuario con ese nombre. */
    data object UsuarioInexistente : ResultadoLogin()

    /** El usuario existe pero la contrasena no coincide. */
    data object ContrasenaIncorrecta : ResultadoLogin()

    /** El usuario existe y la contrasena coincide, pero `activo = 0`. */
    data class UsuarioInactivo(val usuario: UsuarioEntity) : ResultadoLogin()
}

/** Resultado de [AuthRepository.registrar]. */
sealed class ResultadoRegistro {

    data class Exito(val id: Long) : ResultadoRegistro()

    /** `usuarios.usuario` es UNIQUE: el nombre ya esta ocupado. */
    data object UsuarioDuplicado : ResultadoRegistro()

    /** La escritura en Room fallo por cualquier otro motivo. */
    data class Error(val causa: Throwable) : ResultadoRegistro()
}

/**
 * FASE 8 - Unica puerta de la capa de datos a la autenticacion ADMIN.
 *
 * Ningun Composable ni ViewModel habla SQL o `UsuarioDao` directamente: ambos
 * consumen este repositorio, de modo que Room queda aislado aqui.
 *
 * SEGURIDAD (limitacion academica): la contrasena se compara y se guarda en
 * TEXTO PLANO. En produccion habria que almacenar un hash (bcrypt/Argon2).
 */
class AuthRepository(private val usuarioDao: UsuarioDao) {

    /** Autentica contra Room distinguiendo "no existe" de "contrasena mala". */
    suspend fun login(usuario: String, contrasena: String): ResultadoLogin {
        val encontrado = usuarioDao.buscarPorUsuario(usuario)
            ?: return ResultadoLogin.UsuarioInexistente

        val coincide = usuarioDao.login(usuario, contrasena)
            ?: return ResultadoLogin.ContrasenaIncorrecta

        return if (coincide.activo) {
            ResultadoLogin.Exito(coincide)
        } else {
            ResultadoLogin.UsuarioInactivo(encontrado)
        }
    }

    /**
     * Da de alta una cuenta de administracion.
     *
     * El rol NO es un parametro: esta aplicacion es exclusivamente ADMIN, asi que
     * siempre se graba [RolAdmin.ADMINISTRADOR] y el usuario no puede elegirlo
     * desde la UI. `activo` se fija en `true`; `creadoEn` recibe el reloj real.
     */
    suspend fun registrar(
        usuario: String,
        contrasena: String,
        nombreCompleto: String,
        email: String,
        telefono: String,
        creadoEn: Long = System.currentTimeMillis()
    ): ResultadoRegistro {
        if (usuarioDao.existeUsuario(usuario.trim())) return ResultadoRegistro.UsuarioDuplicado

        val entidad = UsuarioEntity(
            usuario = usuario.trim(),
            contrasena = contrasena,
            nombreCompleto = nombreCompleto.trim(),
            email = email.trim().lowercase(Locale.US),
            telefono = telefono.trim(),
            rol = RolAdmin.ADMINISTRADOR.name,
            activo = true,
            creadoEn = creadoEn
        )

        return try {
            ResultadoRegistro.Exito(usuarioDao.insertar(entidad))
        } catch (causa: Exception) {
            // `insertar` usa OnConflictStrategy.ABORT: un nombre repetido lanza la
            // excepcion del indice UNIQUE. Se distingue de un fallo real de
            // escritura comprobando si la fila llego a existir.
            val esDuplicado = try {
                usuarioDao.existeUsuario(entidad.usuario)
            } catch (ignorado: Exception) {
                false
            }
            if (esDuplicado) ResultadoRegistro.UsuarioDuplicado
            else ResultadoRegistro.Error(causa)
        }
    }

    suspend fun buscarPorUsuario(usuario: String): UsuarioEntity? =
        usuarioDao.buscarPorUsuario(usuario.trim())

    suspend fun existeUsuario(usuario: String): Boolean =
        usuarioDao.existeUsuario(usuario.trim())

    suspend fun contar(): Int = usuarioDao.contar()

    suspend fun eliminar(usuario: UsuarioEntity) = usuarioDao.eliminar(usuario)

    /** Baja logica: mantiene el registro y desactiva el acceso. */
    suspend fun desactivar(usuario: UsuarioEntity) =
        usuarioDao.actualizar(usuario.copy(activo = false))

    /** Reactiva una cuenta dada de baja. */
    suspend fun activar(usuario: UsuarioEntity) =
        usuarioDao.actualizar(usuario.copy(activo = true))
}

/**
 * Traduce la entidad de Room al modelo de sesion que consume la UI.
 *
 * Un rol desconocido se mapea al MENOS privilegiado ([RolAdmin.CLIENTE]) y no a
 * ADMINISTRADOR: si por error la columna tuviera un valor inesperado, la sesion
 * no deberia conceder acceso al panel.
 */
fun UsuarioEntity.aSesion(inicioSesionEn: Long = System.currentTimeMillis()): UsuarioSesion =
    UsuarioSesion(
        id = id,
        usuario = usuario,
        nombreCompleto = nombreCompleto,
        email = email,
        telefono = telefono,
        rol = RolAdmin.desde(rol) ?: RolAdmin.CLIENTE,
        activo = activo,
        inicioSesionEn = inicioSesionEn
    )
