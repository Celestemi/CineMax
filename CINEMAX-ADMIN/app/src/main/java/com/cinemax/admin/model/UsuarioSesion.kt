package com.cinemax.admin.model

/**
 * FASE 8 - Sesion en memoria del administrador autenticado.
 *
 * Se construye a partir del `UsuarioEntity` recuperado de Room tras un login
 * correcto y es lo unico que viaja entre pantallas: ningun Composable vuelve a
 * leer la base de datos. Vive solo en memoria (`AuthViewModel`), igual que en la
 * app Cliente, y el punto de extension para persistirla (DataStore) es este tipo.
 */
data class UsuarioSesion(
    val id: Int,
    val usuario: String,
    val nombreCompleto: String,
    val email: String,
    val telefono: String,
    val rol: RolAdmin,
    val activo: Boolean,
    val inicioSesionEn: Long
) {

    /** Unico rol con acceso a CINEMAX-ADMIN. */
    fun cumpleRolPermitido(): Boolean = activo && rol == ROL_PERMITIDO_EN_ADMIN

    companion object {
        val ROL_PERMITIDO_EN_ADMIN: RolAdmin = RolAdmin.ADMINISTRADOR
    }
}
