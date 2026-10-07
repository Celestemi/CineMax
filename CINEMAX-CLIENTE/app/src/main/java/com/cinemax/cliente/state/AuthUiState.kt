package com.cinemax.cliente.state

/**
 * FASE 4 - Estados de la autenticacion del Cliente.
 *
 * Reemplaza al set minimo de Fase 2 (`Inicial`/`Cargando`/`Autenticado`/`Error`).
 * A diferencia de un unico `Error`, cada causa de rechazo tiene su propio estado
 * para que la UI pueda responder de forma distinta y para que los tests puedan
 * afirmar exactamente que ocurrio.
 */
sealed class AuthUiState {

    /** Todavia no se ha intentado nada: la app arranca en Login. */
    data object Inicial : AuthUiState()

    /** Consulta a Room en curso. Los botones se deshabilitan. */
    data object Cargando : AuthUiState()

    /**
     * Login correcto Y rol permitido. Es el unico estado que abre la aplicacion.
     * La sesion vive en [UsuarioSesion] para poder navegar sin volver a leer Room.
     */
    data class Autenticado(val usuario: UsuarioSesion) : AuthUiState()

    /** No existe un usuario con ese nombre, o la contrasena no coincide. */
    data class LoginIncorrecto(val mensaje: String) : AuthUiState()

    /** Las credenciales son correctas pero el usuario esta dado de baja. */
    data class UsuarioInactivo(val mensaje: String) : AuthUiState()

    /**
     * Credenciales correctas pero el rol no es CLIENTE. En esta aplicacion
     * significa que la cuenta es ADMINISTRADOR y pertenece a CINEMAX-ADMIN.
     */
    data class RolNoPermitido(val mensaje: String) : AuthUiState()

    /** Alta completada: el usuario ya existe en Room con rol CLIENTE. */
    data class RegistroExitoso(val mensaje: String) : AuthUiState()

    /** El nombre de usuario ya estaba ocupado. */
    data class UsuarioDuplicado(val mensaje: String) : AuthUiState()

    /** Fallo al escribir en Room (constraint violada, disco lleno, etc.). */
    data class ErrorRegistro(val mensaje: String) : AuthUiState()

    /** Faltan campos obligatorios o el formato no es razonable. */
    data class ValidacionInvalida(val mensaje: String) : AuthUiState()

    /** `true` para los estados que impiden el acceso a la aplicacion Cliente. */
    val esAccesoDenegado: Boolean
        get() = this is LoginIncorrecto ||
            this is UsuarioInactivo ||
            this is RolNoPermitido

    /** `true` para los estados que la pantalla de Registro debe mostrar. */
    val esFalloDeRegistro: Boolean
        get() = this is UsuarioDuplicado ||
            this is ErrorRegistro ||
            this is ValidacionInvalida

    /** `true` solo para [Autenticado]; un ADMINISTRADOR nunca llega aqui. */
    val esAccesoPermitido: Boolean
        get() = this is Autenticado
}

enum class RolCineMax {
    CLIENTE,
    ADMINISTRADOR;

    companion object {
        /** `true` solo para CLIENTE, que es el unico rol de esta aplicacion. */
        fun desde(valor: String): RolCineMax? =
            entries.firstOrNull { it.name.equals(valor.trim(), ignoreCase = true) }
    }
}
