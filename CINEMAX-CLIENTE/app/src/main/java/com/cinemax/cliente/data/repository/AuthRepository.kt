package com.cinemax.cliente.data.repository

import com.cinemax.cliente.data.local.UsuarioDao
import com.cinemax.cliente.data.local.UsuarioEntity
import com.cinemax.cliente.state.RolCineMax
import com.cinemax.cliente.state.UsuarioSesion
import java.util.Locale

/**
 * FASE 4 - Resultado de [AuthRepository.login].
 *
 * Se modela con una clase sellada en lugar de un `Boolean` porque la UI necesita
 * distinguir cuatro situaciones distintas: usuario inexistente, contrasena
 * incorrecta, usuario inactivo y exito.
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
 * FASE 4 - Unica puerta de la capa de datos a la autenticacion.
 *
 * Ningun Composable ni ViewModel habla SQL o `UsuarioDao` directamente: ambos
 * consumen este repositorio, de modo que Room queda aislado aqui y las consultas
 * podran cambiarse (hashing, red, etc.) sin tocar la capa superior.
 *
 * SEGURIDAD (limitación academica): la contrasena se compara y se guarda en
 * TEXTO PLANO, tal y como permite la guia del coursework. En produccion habria
 * que storing un hash (bcrypt/Argon2) y nunca la contrasena en claro.
 */
class AuthRepository(private val usuarioDao: UsuarioDao) {

    /**
     * Autentica contra Room. El orden de las consultas permite diferenciar
     * "usuario inexistente" de "contrasena incorrecta" sin exponer informacion
     * innecesaria, algo innecesario para el coursework pero barato de hacer.
     */
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
     * Da de alta un usuario.
     *
     * El rol NO es un parametro: esta aplicacion es exclusivamente Cliente, asi
     * que siempre se graba [RolCineMax.CLIENTE] y el usuario no puede elegirlo.
     * `activo` se fija en `true` y `creadoEn` recibe el reloj real.
     */
    suspend fun registrar(
        usuario: String,
        contrasena: String,
        nombreCompleto: String,
        email: String,
        telefono: String,
        creadoEn: Long = System.currentTimeMillis()
    ): ResultadoRegistro {
        if (usuarioDao.existeUsuario(usuario)) return ResultadoRegistro.UsuarioDuplicado

        val entidad = UsuarioEntity(
            usuario = usuario.trim(),
            contrasena = contrasena,
            nombreCompleto = nombreCompleto.trim(),
            email = email.trim().lowercase(Locale.US),
            telefono = telefono.trim(),
            rol = RolCineMax.CLIENTE.name,
            activo = true,
            creadoEn = creadoEn
        )

        return try {
            ResultadoRegistro.Exito(usuarioDao.insertar(entidad))
        } catch (causa: Exception) {
            // `insertar` usa OnConflictStrategy.ABORT, de modo que un nombre
            // repetido lanza la excepcion del indice UNIQUE `usuarios.usuario`.
            // Se distingue de un fallo real de escritura comprobando si la fila
            // llego a existir, en vez de referenciar clases de Android aqui.
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

    /** Baja logica: mantiene el historial de reservas y desactiva el acceso. */
    suspend fun desactivar(usuario: UsuarioEntity) =
        usuarioDao.actualizar(usuario.copy(activo = false))

    /** Reactiva una cuenta dada de baja. */
    suspend fun activar(usuario: UsuarioEntity) =
        usuarioDao.actualizar(usuario.copy(activo = true))
}

/** Traduce la entidad de Room al modelo de sesion que consume la UI. */
fun UsuarioEntity.aSesion(inicioSesionEn: Long = System.currentTimeMillis()): UsuarioSesion =
    UsuarioSesion(
        id = id,
        usuario = usuario,
        nombreCompleto = nombreCompleto,
        email = email,
        telefono = telefono,
        rol = RolCineMax.desde(rol) ?: RolCineMax.ADMINISTRADOR,
        activo = activo,
        inicioSesionEn = inicioSesionEn
    )
