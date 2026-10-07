package com.cinemax.cliente.ui.navigation

import android.net.Uri

/**
 * FASE 6 - UNICA fuente de verdad de las rutas de CINEMAX-CLIENTE.
 *
 * Ningun Composable escribe un literal de ruta: todas pasan por los metodos
 * [detalle], [butacas] y [confirmacion], de modo que un cambio de patron se
 * resuelve en este fichero y no repartido por la aplicacion.
 *
 * ```
 *  splash
 *    |
 *  login <--> registro
 *    |  (Autenticado)
 *    v
 *  home / cartelera <--> misReservas
 *    |
 *  detalle/{peliculaId}
 *    |
 *  butacas/{funcionId}
 *    |
 *  resumen
 *    |
 *  confirmacion/{codigo}
 * ```
 *
 * ## Solo viajan identificadores
 * Por los argumentos pasan un `peliculaId`, un `funcionId` y un `codigo`: nunca
 * una entidad de Room. Cada pantalla vuelve a obtener sus datos desde su
 * ViewModel, que a su vez los pide a los repositorios.
 *
 * ## `resumen` no lleva argumentos a proposito
 * La seleccion de butacas es TEMPORAL y vive en `SeleccionButacasViewModel`
 * (Fase 5). Pasarla por la ruta obligaria a serializarla; en su lugar la pantalla
 * de resumen la lee del ViewModel y se la entrega a `ReservaViewModel.preparar`.
 */
object DestinosCliente {

    // ------------------------------------------------------------------
    // Rutas publicas
    // ------------------------------------------------------------------

    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTRO = "registro"

    // ------------------------------------------------------------------
    // Rutas protegidas
    // ------------------------------------------------------------------

    const val HOME = "home"
    const val CARTELERA = "cartelera"
    const val RESUMEN = "resumen"

    /**
     * FASE 7 - Historial del usuario.
     *
     * No lleva argumentos a proposito, y por la MISMA razon que [RESUMEN]: el
     * `usuarioId` sale de la sesion de `AuthViewModel` dentro del ViewModel, no de
     * la ruta. Ponerlo en la URL permitiria que alguien abriera el historial de
     * otra cuenta cambiando un numero.
     */
    const val MIS_RESERVAS = "misReservas"

    // ------------------------------------------------------------------
    // Argumentos
    // ------------------------------------------------------------------

    const val ARG_PELICULA_ID = "peliculaId"
    const val ARG_FUNCION_ID = "funcionId"
    const val ARG_CODIGO = "codigo"

    /** Patron con el que se DECLARA la pantalla de detalle. */
    const val DETALLE = "detalle/{$ARG_PELICULA_ID}"

    /** Patron con el que se DECLARA la seleccion de butacas. */
    const val BUTACAS = "butacas/{$ARG_FUNCION_ID}"

    /** Patron con el que se DECLARA la confirmacion. */
    const val CONFIRMACION = "confirmacion/{$ARG_CODIGO}"

    // ------------------------------------------------------------------
    // Constructores de ruta
    // ------------------------------------------------------------------

    fun detalle(peliculaId: Int): String = "detalle/$peliculaId"

    fun butacas(funcionId: Int): String = "butacas/$funcionId"

    fun confirmacion(codigo: String): String =
        "confirmacion/${Uri.encode(codigo.trim())}"

    // ------------------------------------------------------------------
    // Proteccion
    // ------------------------------------------------------------------

    /**
     * Patrones que exigen sesion valida.
     *
     * Se comparan contra `NavDestination.route`, que devuelve el PATRON
     * (`detalle/{peliculaId}`) y no la ruta concreta (`detalle/7`): por eso
     * [esProtegida] no necesita recibir la ruta ya resuelta.
     */
    val patronesProtegidos: Set<String> = setOf(
        HOME,
        CARTELERA,
        DETALLE,
        BUTACAS,
        RESUMEN,
        CONFIRMACION,
        MIS_RESERVAS
    )

    /** `true` si [patronRuta] exige sesion. `null` (aun sin destino) no exige nada. */
    fun esProtegida(patronRuta: String?): Boolean = patronRuta != null && patronRuta in patronesProtegidos

    /** `true` solo para las tres pantallas publicas de arranque. */
    fun esPublica(patronRuta: String?): Boolean =
        patronRuta != null && patronRuta in setOf(SPLASH, LOGIN, REGISTRO)
}