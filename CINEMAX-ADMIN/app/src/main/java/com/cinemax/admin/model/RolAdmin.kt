package com.cinemax.admin.model

/**
 * FASE 8 - Roles que puede tener una cuenta de CINEMAX-ADMIN.
 *
 * El proyecto ADMIN es independiente del CLIENTE, pero comparte vocabulario: el
 * unico rol con acceso es [ADMINISTRADOR]. Se incluye [CLIENTE] para poder
 * representar y RECHAZAR cuentas de cliente que, por error, acabaran en la base
 * de datos de administracion; asi el login no depende de que la base solo
 * contenga administradores.
 *
 * `name` es el texto que se persiste en la columna `usuarios.rol`.
 */
enum class RolAdmin {

    ADMINISTRADOR,
    CLIENTE;

    companion object {
        /** Convierte el texto guardado en Room al enum, ignorando mayusculas. */
        fun desde(valor: String): RolAdmin? =
            entries.firstOrNull { it.name.equals(valor.trim(), ignoreCase = true) }
    }
}
