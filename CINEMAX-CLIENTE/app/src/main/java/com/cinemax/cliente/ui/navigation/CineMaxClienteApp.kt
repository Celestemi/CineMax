package com.cinemax.cliente.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cinemax.cliente.state.AuthUiState
import com.cinemax.cliente.state.SeleccionButacasUiState
import com.cinemax.cliente.ui.screens.CarteleraScreen
import com.cinemax.cliente.ui.screens.ConfirmacionScreen
import com.cinemax.cliente.ui.screens.DetallePeliculaScreen
import com.cinemax.cliente.ui.screens.EtiquetasPrueba
import com.cinemax.cliente.ui.screens.LoginScreen
import com.cinemax.cliente.ui.screens.MisReservasScreen
import com.cinemax.cliente.ui.screens.RegistroScreen
import com.cinemax.cliente.ui.screens.ResumenCompraScreen
import com.cinemax.cliente.ui.screens.SeleccionButacasScreen
import com.cinemax.cliente.ui.screens.SeleccionDeButacas
import com.cinemax.cliente.ui.screens.SplashScreen
import com.cinemax.cliente.viewmodel.AuthViewModel
import com.cinemax.cliente.viewmodel.AuthViewModelFactory
import com.cinemax.cliente.viewmodel.CarteleraViewModel
import com.cinemax.cliente.viewmodel.CarteleraViewModelFactory
import com.cinemax.cliente.viewmodel.DetallePeliculaViewModel
import com.cinemax.cliente.viewmodel.DetallePeliculaViewModelFactory
import com.cinemax.cliente.viewmodel.MisReservasViewModel
import com.cinemax.cliente.viewmodel.MisReservasViewModelFactory
import com.cinemax.cliente.viewmodel.ReservaViewModel
import com.cinemax.cliente.viewmodel.ReservaViewModelFactory
import com.cinemax.cliente.viewmodel.SeleccionButacasViewModel
import com.cinemax.cliente.viewmodel.SeleccionButacasViewModelFactory

/**
 * FASE 6 - Grafo de navegacion de CINEMAX-CLIENTE.
 *
 * ```
 *  splash
 *    |
 *  login <------> registro
 *    |  (Autenticado)
 *    v
 *  home
 *    |  (tambien se puede volver por `cartelera`)
 *    v
 *  cartelera -> detalle/{peliculaId} -> butacas/{funcionId} -> resumen -> confirmacion/{codigo}
 * ```
 *
 * ## Una sola fuente de verdad para la sesion
 * El unico estado que decide si una pantalla protegida es accesible es
 * [AuthViewModel.sesion] (Fase 4). Ningun `composable` de este fichero comprueba
 * credenciales ni roles: eso ya lo hizo el ViewModel al crear la sesion, y todas
 * las pantallas protegidas se envuelven en [PantallaProtegida].
 *
 * ## Un solo ViewModel por pantalla
 * No se ha creado ningun ViewModel nuevo: se reutilizan los de Fase 5
 * ([CarteleraViewModel], [DetallePeliculaViewModel],
 * [SeleccionButacasViewModel], [ReservaViewModel]) y el de Fase 4
 * ([AuthViewModel]). La UI nunca abre un DAO: pide el ViewModel a la fabrica, que
 * resuelve los repositorios a partir de `DatabaseProvider`.
 */
@Composable
fun CineMaxClienteApp(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current

    // Dueño de los ViewModels de negocio: la Activity. Se captura FUERA del NavHost
    // porque dentro de un destino `LocalViewModelStoreOwner` es el NavBackStackEntry,
    // y eso destruiria el ViewModel al cambiar de pantalla. Aqui la instancia se
    // crea la primera vez que una pantalla la pide y se REUTILIZA al volver atras,
    // que es lo que permite que la seleccion de butacas y la cotizacion sobrevivan
    // al flujo.
    val propietario: ViewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current) {
        "CineMaxClienteApp debe ejecutarse dentro de una Activity (setContent)"
    }

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context.applicationContext)
    )

    val sesion by authViewModel.sesion.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Unica fuente de verdad para saber si el usuario esta autenticado.
    val haySesion = sesion != null

    // La seleccion que viaja de BUTACAS a RESUMEN. No va por la ruta: vive en el
    // `SeleccionButacasViewModel` y se lee aqui para entregarsela al de compra.
    var seleccionPendiente by remember { mutableStateOf(SeleccionDeButacas(0, emptyList())) }

    val entrada by navController.currentBackStackEntryAsState()
    VigilaRutaProtegida(
        rutaActual = entrada?.destination?.route,
        haySesion = haySesion,
        navController = navController
    )

    // Login correcto -> Home (el rol ya fue validado en el ViewModel).
    LaunchedEffect(authUiState) {
        if (authUiState is AuthUiState.Autenticado) {
            NavegacionCliente.irAInicioDeCliente(navController)
        }
    }

    // Registro correcto -> Login, para que la persona entre con su cuenta nueva.
    // NO se inicia sesion automaticamente tras el alta.
    LaunchedEffect(authUiState) {
        if (authUiState is AuthUiState.RegistroExitoso) {
            authViewModel.limpiarEstado()
            NavegacionCliente.volverAlLogin(navController)
        }
    }

    NavHost(
        navController = navController,
        startDestination = DestinosCliente.SPLASH
    ) {
        // ==============================================================
        // PUBLICAS
        // ==============================================================

        composable(DestinosCliente.SPLASH) {
            SplashScreen(
                onNavegarAlLogin = {
                    if (haySesion) {
                        NavegacionCliente.irAInicioDeCliente(navController)
                    } else {
                        navController.navigate(DestinosCliente.LOGIN) {
                            popUpTo(DestinosCliente.SPLASH) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                modifier = Modifier.testTag(EtiquetasPrueba.PANTALLA_SPLASH)
            )
        }

        composable(DestinosCliente.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                // La navegacion la dispara el `LaunchedEffect(authUiState)` de este
                // grafo: hay UN solo autor de la transicion a Home.
                onLoginExitoso = { },
                onIrARegistro = {
                    authViewModel.limpiarEstado()
                    navController.navigate(DestinosCliente.REGISTRO)
                },
                modifier = Modifier.testTag(EtiquetasPrueba.PANTALLA_LOGIN)
            )
        }

        composable(DestinosCliente.REGISTRO) {
            RegistroScreen(
                viewModel = authViewModel,
                onRegistroExitoso = { /* la navegacion la dispara el LaunchedEffect */ },
                onVolverAlLogin = {
                    authViewModel.limpiarEstado()
                    authViewModel.limpiarFormulario()
                    NavegacionCliente.volverAlLogin(navController)
                },
                modifier = Modifier.testTag(EtiquetasPrueba.PANTALLA_REGISTRO)
            )
        }

        // ==============================================================
        // PROTEGIDAS
        // ==============================================================

        composable(DestinosCliente.HOME) {
            PantallaProtegida(haySesion = haySesion, navController = navController) {
                val usuario = sesion ?: return@PantallaProtegida
                val carteleraViewModel: CarteleraViewModel = viewModel(
                    viewModelStoreOwner = propietario,
                    factory = CarteleraViewModelFactory.desde(context)
                )
                CarteleraScreen(
                    viewModel = carteleraViewModel,
                    sesion = usuario,
                    onPeliculaSeleccionada = { navController.navigate(DestinosCliente.detalle(it)) },
                    onCerrarSesion = { cerrarSesion(authViewModel, navController) },
                    onMisReservas = { navController.navigate(DestinosCliente.MIS_RESERVAS) },
                    nombrePrueba = EtiquetasPrueba.PANTALLA_HOME
                )
            }
        }

        composable(DestinosCliente.CARTELERA) {
            PantallaProtegida(haySesion = haySesion, navController = navController) {
                val usuario = sesion ?: return@PantallaProtegida
                val carteleraViewModel: CarteleraViewModel = viewModel(
                    viewModelStoreOwner = propietario,
                    factory = CarteleraViewModelFactory.desde(context)
                )
                CarteleraScreen(
                    viewModel = carteleraViewModel,
                    sesion = usuario,
                    onPeliculaSeleccionada = { navController.navigate(DestinosCliente.detalle(it)) },
                    onCerrarSesion = { cerrarSesion(authViewModel, navController) },
                    onMisReservas = { navController.navigate(DestinosCliente.MIS_RESERVAS) }
                )
            }
        }

        composable(DestinosCliente.MIS_RESERVAS) {
            PantallaProtegida(haySesion = haySesion, navController = navController) {
                val misReservasViewModel: MisReservasViewModel = viewModel(
                    viewModelStoreOwner = propietario,
                    factory = MisReservasViewModelFactory.desde(context, authViewModel.sesion)
                )
                MisReservasScreen(
                    viewModel = misReservasViewModel,
                    onVolver = { navController.popBackStack() },
                    onIrACartelera = {
                        NavegacionCliente.irACarteleraDesdeMisReservas(navController)
                    }
                )
            }
        }

        composable(
            route = DestinosCliente.DETALLE,
            arguments = listOf(
                navArgument(DestinosCliente.ARG_PELICULA_ID) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val peliculaId = backStackEntry.arguments?.getInt(DestinosCliente.ARG_PELICULA_ID) ?: 0
            PantallaProtegida(haySesion = haySesion, navController = navController) {
                val detalleViewModel: DetallePeliculaViewModel = viewModel(
                    viewModelStoreOwner = propietario,
                    factory = DetallePeliculaViewModelFactory.desde(context)
                )
                DetallePeliculaScreen(
                    viewModel = detalleViewModel,
                    peliculaId = peliculaId,
                    onFuncionSeleccionada = { navController.navigate(DestinosCliente.butacas(it)) },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = DestinosCliente.BUTACAS,
            arguments = listOf(
                navArgument(DestinosCliente.ARG_FUNCION_ID) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val funcionId = backStackEntry.arguments?.getInt(DestinosCliente.ARG_FUNCION_ID) ?: 0
            PantallaProtegida(haySesion = haySesion, navController = navController) {
                val butacasViewModel: SeleccionButacasViewModel = viewModel(
                    viewModelStoreOwner = propietario,
                    factory = SeleccionButacasViewModelFactory.desde(context)
                )
                // Se lee el estado aqui, y no dentro de la pantalla, para poder
                // adjuntar sede y sala a la seleccion que viaja al resumen.
                val estadoButacas by butacasViewModel.uiState.collectAsStateWithLifecycle()

                SeleccionButacasScreen(
                    viewModel = butacasViewModel,
                    funcionId = funcionId,
                    onContinuar = { idSeleccionada, butacas ->
                        val exito = estadoButacas as? SeleccionButacasUiState.Exito
                        seleccionPendiente = SeleccionDeButacas(
                            funcionId = idSeleccionada,
                            butacaIds = butacas,
                            nombreSede = exito?.sede?.nombre.orEmpty(),
                            nombreSala = exito?.sala?.nombre.orEmpty()
                        )
                        navController.navigate(DestinosCliente.RESUMEN)
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable(DestinosCliente.RESUMEN) {
            PantallaProtegida(haySesion = haySesion, navController = navController) {
                val reservaViewModel: ReservaViewModel = viewModel(
                    viewModelStoreOwner = propietario,
                    factory = ReservaViewModelFactory.desde(context, authViewModel.sesion)
                )
                ResumenCompraScreen(
                    viewModel = reservaViewModel,
                    seleccion = seleccionPendiente,
                    onReservaConfirmada = { codigo ->
                        NavegacionCliente.irAConfirmacion(navController, codigo)
                    },
                    onVolverAButacas = {
                        val funcionElegida = seleccionPendiente.funcionId
                        if (funcionElegida > 0) {
                            navController.navigate(DestinosCliente.butacas(funcionElegida)) {
                                popUpTo(DestinosCliente.RESUMEN) { inclusive = true }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = DestinosCliente.CONFIRMACION,
            arguments = listOf(
                navArgument(DestinosCliente.ARG_CODIGO) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString(DestinosCliente.ARG_CODIGO).orEmpty()
            PantallaProtegida(haySesion = haySesion, navController = navController) {
                val reservaViewModel: ReservaViewModel = viewModel(
                    viewModelStoreOwner = propietario,
                    factory = ReservaViewModelFactory.desde(context, authViewModel.sesion)
                )
                ConfirmacionScreen(
                    viewModel = reservaViewModel,
                    codigo = codigo,
                    onVolverACartelera = {
                        // El flujo de compra se cierra aqui: la seleccion temporal y la
                        // cotizacion dejan de ser utiles.
                        seleccionPendiente = SeleccionDeButacas(0, emptyList())
                        reservaViewModel.limpiar()
                        NavegacionCliente.irACarteleraDesdeConfirmacion(navController)
                    },
                    onMisReservas = {
                        // `resumen` ya no esta en el back stack y la cotizacion sigue viva
                        // solo por si se vuelve atras: la seleccion se limpia para que no
                        // pueda reutilizarse.
                        seleccionPendiente = SeleccionDeButacas(0, emptyList())
                        navController.navigate(DestinosCliente.MIS_RESERVAS) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

/**
 * LOGOUT.
 *
 * 1. [AuthViewModel.cerrarSesion] deja `UsuarioSesion` en `null`.
 * 2. [NavegacionCliente.irAInicioDeSesion] va a `login` con `popUpTo` INCLUSIVO
 *    sobre todo el grafo, de modo que ninguna pantalla protegida queda en el back
 *    stack.
 *
 * No se borra ningun usuario, pelicula ni reserva de Room: la sesion vive solo en
 * memoria (Fase 4) y la base de datos es independiente de ella.
 */
private fun cerrarSesion(authViewModel: AuthViewModel, navController: NavHostController) {
    authViewModel.cerrarSesion()
    NavegacionCliente.irAInicioDeSesion(navController)
}