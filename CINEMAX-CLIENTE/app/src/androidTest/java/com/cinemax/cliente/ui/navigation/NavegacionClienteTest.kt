package com.cinemax.cliente.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cinemax.cliente.data.BaseDePrueba
import com.cinemax.cliente.data.Catalogo
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.DatabaseProvider
import com.cinemax.cliente.data.seed.SeedData
import com.cinemax.cliente.ui.nodo
import com.cinemax.cliente.ui.nodoHabilitado
import com.cinemax.cliente.ui.screens.EtiquetasPrueba
import com.cinemax.cliente.ui.theme.CineMaxTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * FASE 7 - Pruebas del GRAFO COMPLETO ([CineMaxClienteApp]).
 *
 * Las pantallas tienen sus propias suites (`CarteleraScreenTest`,
 * `SeleccionButacasScreenTest`, `MisReservasScreenTest`). Aqui lo que se prueba es
 * el CABLEADO: que los `onClick` de una pantalla lleven a la ruta correcta y que
 * [`NavegacionCliente`] deje el back stack como dice su contrato.
 *
 * ## Base en memoria
 * El grafo real resuelve sus ViewModels con `DatabaseProvider`, que en produccion
 * apunta al fichero `cinemax_cliente.db`. La prueba lo sustituye por una base en
 * memoria sembrada ([DatabaseProvider.sustituirParaPruebas]) para no depender del
 * estado que dejen otras pruebas ni ensuciar el fichero de la app.
 *
 * ## Casos A-L
 * A. `splash` -> `login` y el splash no queda por debajo.
 * B. login correcto -> `home` sin `login` ni `splash` detras.
 * C. `login` <-> `registro` conservando el login.
 * D. `home` -> `detalle/{id}` -> `butacas/{id}`.
 * E. compra completa: butacas -> `resumen` -> `confirmacion/{codigo}`.
 * F. `confirmacion` -> `misReservas` con la reserva recien creada.
 * G. `misReservas` -> `cartelera` borrando el historial del back stack.
 * H. logout desde `home` -> `login` con el back stack limpio.
 * I. una ruta protegida sin sesion expulsa a `login`.
 * J. login fallido muestra el error y NO navega.
 * K. la politica `esProtegida`/`esPublica` cubre todas las rutas.
 * L. `resumen` no sobrevive a la confirmacion (no se puede recomprar con "atras").
 */
@RunWith(AndroidJUnit4::class)
class NavegacionClienteTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: CineMaxClienteDatabase
    private lateinit var catalogo: Catalogo
    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() = runBlocking<Unit> {
        database = BaseDePrueba.abrirEnMemoria()
        catalogo = BaseDePrueba.sembrarYLeer(database)
        DatabaseProvider.sustituirParaPruebas(database)
    }

    @After
    fun tearDown() {
        DatabaseProvider.sustituirParaPruebas(null)
        database.close()
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------

    /** Monta el grafo real sobre la base en memoria. */
    private fun montarApp() {
        navController = TestNavHostController(compose.activity).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
        }
        compose.setContent {
            CineMaxTheme {
                CineMaxClienteApp(navController = navController)
            }
        }
        compose.waitForIdle()
    }

    /** Rutas (patrones) que hay ahora mismo en el back stack, de abajo arriba. */
    private fun pila(): List<String> =
        navController.currentBackStack.value.mapNotNull { it.destination.route }

    private fun entrarComoCliente() {
        compose.nodo(EtiquetasPrueba.PANTALLA_LOGIN)
        compose.nodo(EtiquetasPrueba.CAMPO_USUARIO).performTextInput("cliente")
        compose.nodo(EtiquetasPrueba.CAMPO_CONTRASENA).performTextInput(SeedData.CONTRASENA_DEMO)
        compose.onNodeWithText("Iniciar sesión").performClick()
        compose.nodo(EtiquetasPrueba.PANTALLA_HOME)
    }

    /** Primera funcion de [ids] que tiene tarjeta montada ahora mismo. */
    private fun primerFuncionVisible(ids: List<Int>): Int =
        ids.first { id ->
            compose.onAllNodesWithTag(EtiquetasPrueba.tarjetaFuncion(id))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

    // ------------------------------------------------------------------
    // A
    // ------------------------------------------------------------------

    @Test
    fun a_splashLlevaAlLoginYNoSeVuelveASplash() {
        montarApp()

        compose.nodo(EtiquetasPrueba.PANTALLA_SPLASH).assertIsDisplayed()
        compose.nodo(EtiquetasPrueba.PANTALLA_LOGIN).assertIsDisplayed()

        assertTrue("login debe estar en el back stack", pila().contains(DestinosCliente.LOGIN))
        assertFalse(
            "splash no puede quedar por debajo tras entrar al login",
            pila().contains(DestinosCliente.SPLASH)
        )
    }

    // ------------------------------------------------------------------
    // B
    // ------------------------------------------------------------------

    @Test
    fun b_loginCorrectoLlevaAlHomeSinDejarLoginDetras() {
        montarApp()
        entrarComoCliente()

        assertTrue(pila().contains(DestinosCliente.HOME))
        assertFalse("login no debe quedar debajo de home", pila().contains(DestinosCliente.LOGIN))
        assertFalse("splash no debe quedar debajo de home", pila().contains(DestinosCliente.SPLASH))
    }

    // ------------------------------------------------------------------
    // C
    // ------------------------------------------------------------------

    @Test
    fun c_loginYRegistroSeAlternanConservandoElLogin() {
        montarApp()
        compose.nodo(EtiquetasPrueba.PANTALLA_LOGIN)

        compose.onNodeWithText("Regístrate").performClick()
        compose.nodo(EtiquetasPrueba.PANTALLA_REGISTRO)
        assertTrue("login debe seguir debajo de registro", pila().contains(DestinosCliente.LOGIN))

        compose.onNodeWithText("Inicia sesión").performClick()
        compose.nodo(EtiquetasPrueba.PANTALLA_LOGIN)
        assertFalse("registro debe desaparecer al volver", pila().contains(DestinosCliente.REGISTRO))
    }

    // ------------------------------------------------------------------
    // D
    // ------------------------------------------------------------------

    @Test
    fun d_homeLlevaAlDetalleYElDetalleAButacas() {
        montarApp()
        entrarComoCliente()

        val peliculaId = catalogo.funciones.first().peliculaId
        val funcionDeCartelera = primerFuncionVisible(catalogo.funciones.map { it.id })
        compose.nodo(EtiquetasPrueba.tarjetaFuncion(funcionDeCartelera))
            .performScrollTo()
            .performClick()
        compose.nodo(EtiquetasPrueba.PANTALLA_DETALLE)
        assertTrue(
            "detalle debe recibir la pelicula de la tarjeta pulsada",
            pila().any { it == DestinosCliente.DETALLE }
        )

        val funcionDeDetalle = primerFuncionVisible(
            catalogo.funcionesDe(peliculaId).map { it.id }
        )
        compose.nodo(EtiquetasPrueba.tarjetaFuncion(funcionDeDetalle))
            .performScrollTo()
            .performClick()
        compose.nodo(EtiquetasPrueba.PANTALLA_BUTACAS)
    }

    // ------------------------------------------------------------------
    // E, F y L: el flujo de compra completo
    // ------------------------------------------------------------------

    @Test
    fun e_flujoCompletoDeCompraHastaLaConfirmacionYElHistorial() {
        montarApp()
        entrarComoCliente()

        // home -> detalle
        val peliculaId = catalogo.funciones.first().peliculaId
        val funcionDeCartelera = primerFuncionVisible(catalogo.funciones.map { it.id })
        compose.nodo(EtiquetasPrueba.tarjetaFuncion(funcionDeCartelera))
            .performScrollTo()
            .performClick()
        compose.nodo(EtiquetasPrueba.PANTALLA_DETALLE)

        // detalle -> butacas
        val funcionId = primerFuncionVisible(catalogo.funcionesDe(peliculaId).map { it.id })
        compose.nodo(EtiquetasPrueba.tarjetaFuncion(funcionId)).performScrollTo().performClick()
        compose.nodo(EtiquetasPrueba.PANTALLA_BUTACAS)

        // butacas: la fila H la deja libre el seed, que solo vende A1,A2 y B3..B5
        compose.nodo(EtiquetasPrueba.butaca("H1")).performScrollTo().performClick()
        compose.nodo(EtiquetasPrueba.butaca("H2")).performScrollTo().performClick()
        compose.nodoHabilitado(EtiquetasPrueba.BOTON).performClick()

        // resumen -> confirmacion
        compose.nodo(EtiquetasPrueba.PANTALLA_RESUMEN)
        compose.nodoHabilitado(EtiquetasPrueba.BOTON).performClick()
        compose.nodo(EtiquetasPrueba.PANTALLA_CONFIRMACION)

        val codigo = navController.currentBackStackEntry
            ?.arguments?.getString(DestinosCliente.ARG_CODIGO).orEmpty()
        assertTrue("la confirmacion debe llevar el codigo real", codigo.startsWith("CINEMAX-"))

        // L: el resumen no sobrevive a la confirmacion
        assertFalse(
            "resumen no puede quedar en el back stack tras confirmar",
            pila().contains(DestinosCliente.RESUMEN)
        )

        // F: confirmacion -> mis reservas, con la reserva recien creada
        compose.nodo("confirmacion:misReservas").performClick()
        compose.nodo(EtiquetasPrueba.PANTALLA_MIS_RESERVAS)
        compose.nodo(EtiquetasPrueba.tarjetaReserva(codigo)).assertIsDisplayed()
    }

    // ------------------------------------------------------------------
    // G
    // ------------------------------------------------------------------

    @Test
    fun g_misReservasVuelveALaCarteleraBorrandoElHistorial() {
        montarApp()
        entrarComoCliente()

        compose.nodo("cartelera:misReservas").performClick()
        compose.nodo(EtiquetasPrueba.PANTALLA_MIS_RESERVAS)

        compose.nodo("misReservas:irACartelera").performClick()
        compose.nodo(EtiquetasPrueba.PANTALLA_CARTELERA)

        assertFalse(
            "el historial no debe quedar en el back stack",
            pila().contains(DestinosCliente.MIS_RESERVAS)
        )
    }

    // ------------------------------------------------------------------
    // H
    // ------------------------------------------------------------------

    @Test
    fun h_cerrarSesionVuelveAlLoginConElBackStackLimpio() {
        montarApp()
        entrarComoCliente()

        compose.nodo("cartelera:cerrarSesion").performClick()
        compose.nodo(EtiquetasPrueba.PANTALLA_LOGIN)

        assertFalse(
            "ninguna ruta protegida puede quedar tras el logout",
            pila().any { DestinosCliente.esProtegida(it) }
        )
    }

    // ------------------------------------------------------------------
    // I
    // ------------------------------------------------------------------

    @Test
    fun i_unaRutaProtegidaSinSesionExpulsaAlLogin() {
        montarApp()
        compose.nodo(EtiquetasPrueba.PANTALLA_LOGIN)

        compose.activity.runOnUiThread {
            navController.navigate(DestinosCliente.MIS_RESERVAS)
        }
        compose.waitUntil(10_000L) {
            pila().none { DestinosCliente.esProtegida(it) }
        }
        compose.nodo(EtiquetasPrueba.PANTALLA_LOGIN)
        assertTrue(pila().contains(DestinosCliente.LOGIN))
    }

    // ------------------------------------------------------------------
    // J
    // ------------------------------------------------------------------

    @Test
    fun j_loginFallidoMuestraErrorYNoNavega() {
        montarApp()
        compose.nodo(EtiquetasPrueba.PANTALLA_LOGIN)

        compose.nodo(EtiquetasPrueba.CAMPO_USUARIO).performTextInput("cliente")
        compose.nodo(EtiquetasPrueba.CAMPO_CONTRASENA).performTextInput("clave-incorrecta")
        compose.onNodeWithText("Iniciar sesión").performClick()

        compose.waitUntil(10_000L) {
            compose.onAllNodesWithText("Usuario o contrasena incorrectos")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        assertFalse(
            "un login fallido no puede dejar rutas protegidas",
            pila().any { DestinosCliente.esProtegida(it) }
        )
    }

    // ------------------------------------------------------------------
    // K
    // ------------------------------------------------------------------

    @Test
    fun k_laPoliticaDeRutasCubreTodasLasPantallas() {
        // Publicas
        assertTrue(DestinosCliente.esPublica(DestinosCliente.SPLASH))
        assertTrue(DestinosCliente.esPublica(DestinosCliente.LOGIN))
        assertTrue(DestinosCliente.esPublica(DestinosCliente.REGISTRO))

        // Protegidas, incluidas las de FASE 7
        assertTrue(DestinosCliente.esProtegida(DestinosCliente.HOME))
        assertTrue(DestinosCliente.esProtegida(DestinosCliente.CARTELERA))
        assertTrue(DestinosCliente.esProtegida(DestinosCliente.DETALLE))
        assertTrue(DestinosCliente.esProtegida(DestinosCliente.BUTACAS))
        assertTrue(DestinosCliente.esProtegida(DestinosCliente.RESUMEN))
        assertTrue(DestinosCliente.esProtegida(DestinosCliente.CONFIRMACION))
        assertTrue(DestinosCliente.esProtegida(DestinosCliente.MIS_RESERVAS))

        // Sin destino no exige nada y no se solapan
        assertFalse(DestinosCliente.esProtegida(null))
        assertFalse(DestinosCliente.esPublica(null))
        assertFalse(DestinosCliente.esProtegida(DestinosCliente.LOGIN))
        assertFalse(DestinosCliente.esPublica(DestinosCliente.MIS_RESERVAS))
    }
}
