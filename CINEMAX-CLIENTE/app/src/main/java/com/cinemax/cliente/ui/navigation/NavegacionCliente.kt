package com.cinemax.cliente.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController

/**
 * FASE 6 - Politica de navegacion del Cliente.
 *
 * Todas las opciones de `popUpTo` del proyecto viven aqui, de modo que el
 * back stack se comporta igual desde cualquier pantalla y no depende de lo que
 * cada Composable recuerde.
 *
 * ## Reglas que implementa
 * - **Login -> Home**: al entrar se borran `splash` y `login`, de modo que "atras"
 *   desde Home cierra la aplicacion en lugar de volver a una pantalla ya cumplida.
 * - **Login <-> Registro**: se usa `popBackStack`, que conserva el login debajo.
 * - **Confirmacion**: se borra `resumen` de forma INCLUSIVA, asi el boton "atras"
 *   nunca devuelve al formulario de compra (que volveria a permitir registrar la
 *   misma operacion por segunda vez).
 * - **Logout**: se borra el grafo completo, de modo que ninguna pantalla protegida
 *   queda en el back stack.
 */
object NavegacionCliente {

    /** Vuelve a `login` dejando el back stack vacio (logout, sesion caducada). */
    fun irAInicioDeSesion(navController: NavHostController) {
        navController.navigate(DestinosCliente.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    /** Entra a la aplicacion: borra `splash` y `login` del back stack. */
    fun irAInicioDeCliente(navController: NavHostController) {
        navController.navigate(DestinosCliente.HOME) {
            // `splash` ya se ha borrado a si mismo al ir a `login`, asi que apuntar a
            // `SPLASH` no vaciaria nada y `login` quedaria debajo de `home`. Se limpia
            // el grafo entero (mismo idioma que [irAInicioDeSesion]) para que entrar
            // deje una sola pantalla en el back stack.
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    /** Vuelve de `registro` a `login` conservando el login debajo. */
    fun volverAlLogin(navController: NavHostController) {
        navController.popBackStack()
    }

    /** Cierra la compra: `resumen` desaparece, de modo que "atras" no repite la compra. */
    fun irAConfirmacion(navController: NavHostController, codigo: String) {
        navController.navigate(DestinosCliente.confirmacion(codigo)) {
            popUpTo(DestinosCliente.RESUMEN) { inclusive = true }
            launchSingleTop = true
        }
    }

    /** Regresa a la cartelera desde la confirmacion, cerrando todo el flujo de compra. */
    fun irACarteleraDesdeConfirmacion(navController: NavHostController) {
        navController.navigate(DestinosCliente.CARTELERA) {
            popUpTo(DestinosCliente.SPLASH) { inclusive = true }
            launchSingleTop = true
        }
    }

    /**
     * FASE 7 - "Mis reservas" -> cartelera.
     *
     * Se borra `misReservas` de forma INCLUSIVA, igual que se hace con `resumen`
     * en [irAConfirmacion]: el historial es una pantalla de solo lectura y no debe
     * quedar bajo la cartelera, de modo que "atras" desde la cartelera no vuelve a
     * ella ni se puede reentrar con el mismo boton.
     */
    fun irACarteleraDesdeMisReservas(navController: NavHostController) {
        navController.navigate(DestinosCliente.CARTELERA) {
            popUpTo(DestinosCliente.MIS_RESERVAS) { inclusive = true }
            launchSingleTop = true
        }
    }
}

/**
 * FASE 6 - Unica comprobacion de sesion de la aplicacion.
 *
 * Ninguna pantalla protegida vuelve a mirar si el usuario esta autenticado: todas
 * delegan en este composable, que a su vez lee el `sesion` de `AuthViewModel`
 * (la sesion en memoria aprobada en Fase 4).
 *
 * - Con sesion: dibuja el contenido.
 * - Sin sesion: no dibuja nada y encadena la redireccion a `login`, cuyo
 *   `popUpTo` deja el back stack sin ninguna pantalla protegida.
 *
 * No hay una copia de esta comprobacion en `Login`, `Home` o `Detalle`: por eso
 * cerrar sesion desde cualquier punto surte efecto en todas partes.
 */
@Composable
fun PantallaProtegida(
    haySesion: Boolean,
    navController: NavHostController,
    contenido: @Composable () -> Unit
) {
    LaunchedEffect(haySesion) {
        if (!haySesion) NavegacionCliente.irAInicioDeSesion(navController)
    }

    if (haySesion) {
        contenido()
    }
}

/**
 * FASE 6 - Red de seguridad del back stack.
 *
 * Se observa la ruta del destino actual y, en cuanto aparece una protegida sin
 * sesion, se corrige. Cubre los casos que un `popUpTo` no puede: por ejemplo,
 * que Android restaure el back stack tras recrear el proceso (la sesion es solo
 * en memoria, asi que tras un giro con `rememberSaveable` la ruta protegida
 * seguiria en pantalla sin nadie con quien iniciar sesion).
 */
@Composable
fun VigilaRutaProtegida(
    rutaActual: String?,
    haySesion: Boolean,
    navController: NavHostController
) {
    val debeExpulsar = remember(rutaActual, haySesion) {
        !haySesion && DestinosCliente.esProtegida(rutaActual)
    }
    LaunchedEffect(debeExpulsar) {
        if (debeExpulsar) NavegacionCliente.irAInicioDeSesion(navController)
    }
}