package com.cinemax.peru.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cinemax.peru.state.AuthUiState
import com.cinemax.peru.state.RolCineMax
import com.cinemax.peru.ui.screens.AdminDashboardScreen
import com.cinemax.peru.ui.screens.AdminFuncionesScreen
import com.cinemax.peru.ui.screens.AdminPeliculasScreen
import com.cinemax.peru.ui.screens.AdminSalasScreen
import com.cinemax.peru.ui.screens.CarteleraScreen
import com.cinemax.peru.ui.screens.CompraScreen
import com.cinemax.peru.ui.screens.ConfirmacionScreen
import com.cinemax.peru.ui.screens.DetallePeliculaScreen
import com.cinemax.peru.ui.screens.LoginScreen
import com.cinemax.peru.ui.screens.SeleccionButacasScreen
import com.cinemax.peru.ui.screens.SplashScreen
import com.cinemax.peru.viewmodel.AdminCineViewModel
import com.cinemax.peru.viewmodel.AuthViewModel
import com.cinemax.peru.viewmodel.CarteleraViewModel
import com.cinemax.peru.viewmodel.ReservaViewModel

enum class Destino(val ruta: String) {
    SPLASH("splash"),
    LOGIN("login"),
    CARTELERA("cartelera"),
    DETALLE_PELICULA("detalle_pelicula"),
    SELECCION_BUTACAS("seleccion_butacas/{funcionId}"),
    COMPRA("compra/{funcionId}"),
    CONFIRMACION("confirmacion"),
    ADMIN_DASHBOARD("admin_dashboard"),
    ADMIN_PELICULAS("admin_peliculas"),
    ADMIN_FUNCIONES("admin_funciones"),
    ADMIN_SALAS("admin_salas");

    companion object {
        fun desdeRuta(ruta: String): Destino? {
            if (ruta.startsWith("seleccion_butacas/")) {
                return SELECCION_BUTACAS
            }
            if (ruta.startsWith("compra/")) {
                return COMPRA
            }
            return entries.firstOrNull { it.ruta == ruta }
        }
    }
}

@Composable
fun CineMaxApp() {
    val authViewModel = remember { AuthViewModel() }
    val carteleraViewModel = remember { CarteleraViewModel() }
    val reservaViewModel = remember { ReservaViewModel() }
    val adminCineViewModel = remember { AdminCineViewModel() }

    val authState by authViewModel.uiState.collectAsState()
    val rolActual = (authState as? AuthUiState.Autenticado)?.rol
    val esAdministrador = rolActual == RolCineMax.ADMINISTRADOR

    var rutaActual by rememberSaveable { mutableStateOf(Destino.SPLASH.ruta) }
    var funcionSeleccionadaId by rememberSaveable { mutableStateOf(0) }
    val destino = Destino.desdeRuta(rutaActual) ?: Destino.SPLASH
    val volverAlLogin = { rutaActual = Destino.LOGIN.ruta }

    when (destino) {
        Destino.SPLASH -> SplashScreen(
            onNavegarAlLogin = { rutaActual = Destino.LOGIN.ruta }
        )

        Destino.LOGIN -> LoginScreen(
            viewModel = authViewModel,
            onLoginExitoso = { rol ->
                rutaActual = if (rol == RolCineMax.ADMINISTRADOR) {
                    Destino.ADMIN_DASHBOARD.ruta
                } else {
                    Destino.CARTELERA.ruta
                }
            }
        )

        Destino.CARTELERA -> CarteleraScreen(
            viewModel = carteleraViewModel,
            onSeleccionarFuncion = { funcion ->
                funcionSeleccionadaId = funcion.id
                rutaActual = Destino.DETALLE_PELICULA.ruta
            },
            onCerrarSesion = {
                authViewModel.cerrarSesion()
                rutaActual = Destino.LOGIN.ruta
            }
        )

        Destino.DETALLE_PELICULA -> DetallePeliculaScreen(
            funcionId = funcionSeleccionadaId,
            onSeleccionarFuncion = { funcion ->
                rutaActual = Destino.SELECCION_BUTACAS.ruta.replace(
                    "{funcionId}",
                    funcion.id.toString()
                )
            },
            onVolver = { rutaActual = Destino.CARTELERA.ruta }
        )

        Destino.SELECCION_BUTACAS -> SeleccionButacasScreen(
            viewModel = reservaViewModel,
            funcionId = rutaActual.substringAfterLast('/').toIntOrNull() ?: 0,
            onContinuar = { funcionId ->
                rutaActual = Destino.COMPRA.ruta.replace("{funcionId}", funcionId.toString())
            },
            onVolver = { rutaActual = Destino.DETALLE_PELICULA.ruta }
        )

        Destino.COMPRA -> {
            val funcionId = rutaActual.substringAfterLast('/').toIntOrNull() ?: 0
            CompraScreen(
                viewModel = reservaViewModel,
                onCompraExitosa = { rutaActual = Destino.CONFIRMACION.ruta },
                onVolver = {
                    rutaActual = Destino.SELECCION_BUTACAS.ruta.replace("{funcionId}", funcionId.toString())
                }
            )
        }

        Destino.CONFIRMACION -> ConfirmacionScreen(
            viewModel = reservaViewModel,
            onVolverACartelera = { rutaActual = Destino.CARTELERA.ruta }
        )

        Destino.ADMIN_DASHBOARD -> AdminRoute(
            esAdministrador = esAdministrador,
            onVolverLogin = volverAlLogin
        ) {
            AdminDashboardScreen(
                viewModel = adminCineViewModel,
                onGestionarPeliculas = { rutaActual = Destino.ADMIN_PELICULAS.ruta },
                onGestionarFunciones = { rutaActual = Destino.ADMIN_FUNCIONES.ruta },
                onRevisarSalas = { rutaActual = Destino.ADMIN_SALAS.ruta },
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    rutaActual = Destino.LOGIN.ruta
                }
            )
        }

        Destino.ADMIN_PELICULAS -> AdminRoute(
            esAdministrador = esAdministrador,
            onVolverLogin = volverAlLogin
        ) {
            AdminPeliculasScreen(
                viewModel = adminCineViewModel,
                onVolver = { rutaActual = Destino.ADMIN_DASHBOARD.ruta }
            )
        }

        Destino.ADMIN_FUNCIONES -> AdminRoute(
            esAdministrador = esAdministrador,
            onVolverLogin = volverAlLogin
        ) {
            AdminFuncionesScreen(
                viewModel = adminCineViewModel,
                onVolver = { rutaActual = Destino.ADMIN_DASHBOARD.ruta }
            )
        }

        Destino.ADMIN_SALAS -> AdminRoute(
            esAdministrador = esAdministrador,
            onVolverLogin = volverAlLogin
        ) {
            AdminSalasScreen(
                viewModel = adminCineViewModel,
                onVolver = { rutaActual = Destino.ADMIN_DASHBOARD.ruta }
            )
        }
    }
}

@Composable
private fun AdminRoute(
    esAdministrador: Boolean,
    onVolverLogin: () -> Unit,
    contenido: @Composable () -> Unit
) {
    if (esAdministrador) {
        contenido()
    } else {
        AccesoRestringido(onVolverLogin)
    }
}

@Composable
private fun AccesoRestringido(onVolverLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Acceso restringido",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Solo el personal administrador puede ingresar a este módulo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Button(
            onClick = onVolverLogin,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Ir al login")
        }
    }
}