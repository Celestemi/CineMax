package com.cinemax.admin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cinemax.admin.state.AuthUiState
import com.cinemax.admin.ui.EtiquetasPrueba
import com.cinemax.admin.ui.auth.LoginScreen
import com.cinemax.admin.ui.auth.RegistroScreen
import com.cinemax.admin.ui.auth.SplashScreen
import com.cinemax.admin.ui.home.HomeAdminScreen
import com.cinemax.admin.viewmodel.AuthViewModel
import com.cinemax.admin.viewmodel.AuthViewModelFactory

/**
 * FASE 8 - Grafo de navegacion de CINEMAX-ADMIN.
 *
 * La unica fuente de verdad para saber si el usuario puede entrar al panel es
 * [AuthViewModel.sesion] (Fase 8). Ninguna pantalla valida credenciales: eso ya
 * ocurrio en el ViewModel al crear la sesion.
 */
@Composable
fun CineMaxAdminApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context.applicationContext)
    )

    val sesion by authViewModel.sesion.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()

    // Login correcto -> Home (el rol ya fue validado en el ViewModel).
    LaunchedEffect(authUiState) {
        if (authUiState is AuthUiState.Autenticado) {
            navController.navigate(DestinosAdmin.HOME) {
                popUpTo(DestinosAdmin.LOGIN) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Registro correcto -> Login: la cuenta ya existe, ahora se inicia sesion.
    LaunchedEffect(authUiState) {
        if (authUiState is AuthUiState.RegistroExitoso) {
            authViewModel.limpiarEstado()
            authViewModel.limpiarFormulario()
            navController.navigate(DestinosAdmin.LOGIN) {
                popUpTo(DestinosAdmin.SPLASH) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = DestinosAdmin.SPLASH
    ) {
        composable(DestinosAdmin.SPLASH) {
            SplashScreen(
                onNavegarAlLogin = {
                    if (sesion != null) {
                        navController.navigate(DestinosAdmin.HOME) {
                            popUpTo(DestinosAdmin.SPLASH) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(DestinosAdmin.LOGIN) {
                            popUpTo(DestinosAdmin.SPLASH) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                modifier = Modifier.testTag(EtiquetasPrueba.PANTALLA_SPLASH)
            )
        }

        composable(DestinosAdmin.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onIrARegistro = {
                    authViewModel.limpiarEstado()
                    navController.navigate(DestinosAdmin.REGISTRO)
                },
                modifier = Modifier.testTag(EtiquetasPrueba.PANTALLA_LOGIN)
            )
        }

        composable(DestinosAdmin.REGISTRO) {
            RegistroScreen(
                viewModel = authViewModel,
                onVolverAlLogin = {
                    authViewModel.limpiarEstado()
                    authViewModel.limpiarFormulario()
                    navController.popBackStack()
                },
                modifier = Modifier.testTag(EtiquetasPrueba.PANTALLA_REGISTRO)
            )
        }

        composable(DestinosAdmin.HOME) {
            val usuario = sesion
            if (usuario != null) {
                HomeAdminScreen(
                    sesion = usuario,
                    onCerrarSesion = {
                        authViewModel.cerrarSesion()
                        navController.navigate(DestinosAdmin.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            } else {
                // Sin sesion no hay panel: se devuelve al login.
                LaunchedEffect(Unit) {
                    navController.navigate(DestinosAdmin.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}
