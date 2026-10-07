package com.cinemax.admin.ui.navigation

/**
 * FASE 8 - Rutas de CINEMAX-ADMIN.
 *
 * Unica fuente de verdad de los destinos del grafo de navegacion. Ningun
 * Composable escribe un literal de ruta: todas las transiciones pasan por aqui.
 *
 * ```
 *  splash
 *    |
 *  login <--> registro
 *    |  (Autenticado)
 *    v
 *  home
 * ```
 */
object DestinosAdmin {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val HOME = "home"
}
