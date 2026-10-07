package com.cinemax.admin.state

import com.cinemax.admin.model.UsuarioSesion

/**
 * FASE 8 - Estados de la autenticacion de CINEMAX-ADMIN.
 *
 * Cada causa de rechazo tiene su propio estado para que la UI responda distinto
 * y para que los tests puedan afirmar exactamente que ocurrio (contraseña
 * incorrecta, usuario inexistente, rol no administrador, etc.).
 */
sealed class AuthUiState {

    /** Todavia no se ha intentado nada: la app arranca en Splash -> Login. */
    data object Inicial : AuthUiState()

    /** Consulta a Room en curso: los botones se deshabilitan. */
    data object Cargando : AuthUiState()

    /** Login correcto Y rol ADMINISTRADOR. Es el unico estado que abre la app. */
    data class Autenticado(val usuario: UsuarioSesion) : AuthUiState()

    /** No existe el usuario o la contrasena no coincide. */
    data class LoginIncorrecto(val mensaje: String) : AuthUiState()

    /** Credenciales correctas pero `activo = 0`. */
    data class UsuarioInactivo(val mensaje: String) : AuthUiState()

    /** Credenciales correctas pero el rol no es ADMINISTRADOR. */
    data class RolNoPermitido(val mensaje: String) : AuthUiState()

    /** Alta completada: el usuario ya existe en Room con rol ADMINISTRADOR. */
    data class RegistroExitoso(val mensaje: String) : AuthUiState()

    /** El nombre de usuario ya estaba ocupado. */
    data class UsuarioDuplicado(val mensaje: String) : AuthUiState()

    /** Fallo al escribir en Room (constraint violada, disco lleno, etc.). */
    data class ErrorRegistro(val mensaje: String) : AuthUiState()

    /** Faltan campos obligatorios o el formato no es valido. */
    data class ValidacionInvalida(val mensaje: String) : AuthUiState()

    /** `true` para los estados que impiden el acceso al panel de administracion. */
    val esAccesoDenegado: Boolean
        get() = this is LoginIncorrecto ||
            this is UsuarioInactivo ||
            this is RolNoPermitido

    /** `true` para los estados que la pantalla de Registro debe mostrar. */
    val esFalloDeRegistro: Boolean
        get() = this is UsuarioDuplicado ||
            this is ErrorRegistro ||
            this is ValidacionInvalida

    /** `true` solo para [Autenticado]. */
    val esAccesoPermitido: Boolean
        get() = this is Autenticado
}
