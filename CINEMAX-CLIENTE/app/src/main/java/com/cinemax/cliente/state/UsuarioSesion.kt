package com.cinemax.cliente.state

/**
 * FASE 4 - Sesion minima en memoria de la aplicacion Cliente.
 *
 * Se crea a partir del `UsuarioEntity` recuperado de Room tras un login correcto y
 * es lo unico que viaja entre pantallas: ningun Composable vuelve a leer la base
 * de datos. Contiene el `usuario` y el `rol`, que es lo que habilitara la
 * navegacion de negocio en fases posteriores.
 *
 * ARQUITECTURA: hoy vive solo en memoria (`AuthViewModel`). La intencion es que en
 * una fase posterior se persista con DataStore, de modo que la app pueda saltar
 * el login si la sesion sigue vigente. Para que ese cambio no afecte a la UI,
 * basta con que la sesion se lea siempre desde aqui y no desde el `UsuarioEntity`.
 */
data class UsuarioSesion(
    val id: Int,
    val usuario: String,
    val nombreCompleto: String,
    val email: String,
    val telefono: String,
    val rol: RolCineMax,
    val activo: Boolean,
    val inicioSesionEn: Long
) {

    /**
     * Unico rol con acceso a CINEMAX-CLIENTE. Un ADMINISTRADOR que por error
     * estuviera en `cinemax_cliente.db` es rechazado por [cumpleRolPermitido].
     */
    fun cumpleRolPermitido(): Boolean = activo && rol == ROL_PERMITIDO_EN_CLIENTE

    companion object {
        val ROL_PERMITIDO_EN_CLIENTE: RolCineMax = RolCineMax.CLIENTE
    }
}
