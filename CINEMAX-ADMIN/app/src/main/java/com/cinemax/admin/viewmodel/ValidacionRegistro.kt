package com.cinemax.admin.viewmodel

import com.cinemax.admin.state.ErroresFormulario

/**
 * FASE 8 - Validaciones del formulario de registro de administrador.
 *
 * Objeto sin dependencias de Android ni de Room: toda la comprobacion ocurre en
 * memoria, por lo que puede ejecutarse en tests JVM y devolver errores por campo
 * para que la UI los muestre bajo el `OutlinedTextField` correspondiente.
 *
 * Campos obligatorios: usuario, contrasena, nombre completo y correo. El telefono
 * es OPCIONAL (la fase no lo exige), pero si se escribe debe ser valido.
 *
 * LIMITACION ACADEMICA: no se valida complejidad de contrasena ni se aplica
 * hashing; la contrasena se trata y se guarda en texto plano.
 */
object ValidacionRegistro {

    const val MENSAJE_USUARIO_VACIO = "El usuario es obligatorio"
    const val MENSAJE_USUARIO_CORTO = "El usuario debe tener al menos 3 caracteres"
    const val MENSAJE_USUARIO_CARACTERES = "El usuario solo admite letras, numeros, punto y guion"
    const val MENSAJE_CONTRASENA_VACIA = "La contrasena es obligatoria"
    const val MENSAJE_CONTRASENA_CORTA = "La contrasena debe tener al menos 4 caracteres"
    const val MENSAJE_NOMBRE_VACIO = "El nombre completo es obligatorio"
    const val MENSAJE_NOMBRE_CORTO = "El nombre completo debe tener al menos 3 caracteres"
    const val MENSAJE_EMAIL_VACIO = "El correo es obligatorio"
    const val MENSAJE_EMAIL_FORMATO = "El correo no tiene un formato valido"
    const val MENSAJE_TELEFONO_FORMATO = "El telefono debe tener entre 9 y 15 digitos"
    const val MENSAJE_TODOS_OBLIGATORIOS = "Completa todos los campos obligatorios"

    private const val USUARIO_MINIMO = 3
    private const val CONTRASENA_MINIMA = 4
    private const val NOMBRE_MINIMO = 3
    private const val TELEFONO_MINIMO = 9
    private const val TELEFONO_MAXIMO = 15

    private val REGEX_EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val REGEX_USUARIO = Regex("^[A-Za-z0-9._-]+$")

    /**
     * @return errores por campo; un [ErroresFormulario] con `hayAlguno == false`
     *         significa que el formulario puede enviarse.
     */
    fun validar(
        usuario: String,
        contrasena: String,
        nombreCompleto: String,
        email: String,
        telefono: String
    ): ErroresFormulario {
        val usuarioLimpio = usuario.trim()
        val nombreLimpio = nombreCompleto.trim()
        val emailLimpio = email.trim()
        val telefonoLimpio = telefono.trim()

        val errorUsuario = when {
            usuarioLimpio.isEmpty() -> MENSAJE_USUARIO_VACIO
            usuarioLimpio.length < USUARIO_MINIMO -> MENSAJE_USUARIO_CORTO
            !REGEX_USUARIO.matches(usuarioLimpio) -> MENSAJE_USUARIO_CARACTERES
            else -> null
        }

        val errorContrasena = when {
            contrasena.isEmpty() -> MENSAJE_CONTRASENA_VACIA
            contrasena.length < CONTRASENA_MINIMA -> MENSAJE_CONTRASENA_CORTA
            else -> null
        }

        val errorNombre = when {
            nombreLimpio.isEmpty() -> MENSAJE_NOMBRE_VACIO
            nombreLimpio.length < NOMBRE_MINIMO -> MENSAJE_NOMBRE_CORTO
            else -> null
        }

        val errorEmail = when {
            emailLimpio.isEmpty() -> MENSAJE_EMAIL_VACIO
            !REGEX_EMAIL.matches(emailLimpio) -> MENSAJE_EMAIL_FORMATO
            else -> null
        }

        val errorTelefono = when {
            telefonoLimpio.isEmpty() -> null
            !telefonoLimpio.all { it.isDigit() } -> MENSAJE_TELEFONO_FORMATO
            telefonoLimpio.length !in TELEFONO_MINIMO..TELEFONO_MAXIMO -> MENSAJE_TELEFONO_FORMATO
            else -> null
        }

        return ErroresFormulario(
            usuario = errorUsuario,
            contrasena = errorContrasena,
            nombreCompleto = errorNombre,
            email = errorEmail,
            telefono = errorTelefono
        )
    }

    /**
     * Validacion del login. Es mas laxa que la del registro: solo exige que haya
     * algo escrito, para no dar pistas sobre las reglas de alta.
     */
    fun validarLogin(usuario: String, contrasena: String): String? = when {
        usuario.trim().isEmpty() -> "Escribe tu usuario"
        contrasena.isEmpty() -> "Escribe tu contrasena"
        else -> null
    }
}
