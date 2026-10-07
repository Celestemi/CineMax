package com.cinemax.cliente.state

/**
 * FASE 4 - Estado del formulario de registro.
 *
 * Los valores viven en el ViewModel (no en `remember`) para que las validaciones
 * sean comprobables sin Compose y para que el boton de enviar pueda deshabilitarse
 * con datos reales.
 */
data class RegistroUiState(
    val usuario: String = "",
    val contrasena: String = "",
    val nombreCompleto: String = "",
    val email: String = "",
    val telefono: String = "",
    val errores: ErroresFormulario = ErroresFormulario(),
    val enviando: Boolean = false
)

/** Error por campo; `null` significa que el campo es valido. */
data class ErroresFormulario(
    val usuario: String? = null,
    val contrasena: String? = null,
    val nombreCompleto: String? = null,
    val email: String? = null,
    val telefono: String? = null
) {
    val hayAlguno: Boolean
        get() = usuario != null ||
            contrasena != null ||
            nombreCompleto != null ||
            email != null ||
            telefono != null

    companion object {
        val NINGUNO = ErroresFormulario()
    }
}
