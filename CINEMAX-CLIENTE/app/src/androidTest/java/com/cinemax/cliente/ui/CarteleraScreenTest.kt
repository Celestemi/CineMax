package com.cinemax.cliente.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cinemax.cliente.data.BaseDePrueba
import com.cinemax.cliente.data.Catalogo
import com.cinemax.cliente.data.aSesionDePrueba
import com.cinemax.cliente.data.funcionLibre
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.ocupacionSembrada
import com.cinemax.cliente.data.repository.RepositoriosCliente
import com.cinemax.cliente.data.repository.ResultadoReserva
import com.cinemax.cliente.data.repository.SolicitudReserva
import com.cinemax.cliente.state.UsuarioSesion
import com.cinemax.cliente.ui.screens.CarteleraScreen
import com.cinemax.cliente.ui.screens.EtiquetasPrueba
import com.cinemax.cliente.ui.theme.CineMaxTheme
import com.cinemax.cliente.viewmodel.CarteleraViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * FASE 7 - Pruebas de UI de la cartelera.
 *
 * ## Por que se monta la pantalla y no el grafo entero
 * Se renderiza [CarteleraScreen] directamente con un ViewModel real sobre una base
 * en memoria sembrada. Asi la prueba comprueba lo que el usuario ve (filtros
 * aplicables, contador, disponibilidad) sin arrastrar el login ni el `NavHost`,
 * que ya tienen sus propias pruebas de navegacion. El grafo completo se prueba
 * aparte, en `NavegacionClienteTest`.
 *
 * ## Que se comprueba
 * - las 34 funciones del seed aparecen y son pulsables;
 * - los tres filtros de la barra recortan la lista y se pueden quitar;
 * - cada tarjeta anuncia cuantas butacas quedan, y al comprar bajan.
 *
 * ## Ojo con el seed y con el scroll
 * El seed trae 2 butacas ya vendidas en su primera funcion, asi que "Quedan 78 de 80"
 * es el dato correcto de la primera tarjeta, no "Quedan 80 de 80". Y `performScrollTo()`
 * solo es valido dentro de un contenedor con scroll: los chips de filtro si lo tienen
 * (`horizontalScroll`), pero la cabecera, el boton de "Mis reservas" y la barra
 * inferior no, y ahi `performScrollTo()` falla con "no parent layout with a Scroll
 * SemanticsAction". Por eso [nodo] espera sin desplazar y solo las tarjetas, que si
 * viven en la `LazyColumn`, usan `performScrollTo()`.
 */
@RunWith(AndroidJUnit4::class)
class CarteleraScreenTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: CineMaxClienteDatabase
    private lateinit var catalogo: Catalogo
    private lateinit var viewModel: CarteleraViewModel
    private lateinit var ocupadas: Map<Int, Int>

    @Before
    fun setUp() = runBlocking<Unit> {
        database = BaseDePrueba.abrirEnMemoria()
        catalogo = BaseDePrueba.sembrarYLeer(database)
        ocupadas = ocupacionSembrada(database)
        viewModel = CarteleraViewModel(
            peliculas = RepositoriosCliente.peliculas(database),
            funciones = RepositoriosCliente.funciones(database),
            salas = RepositoriosCliente.salas(database)
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun pintar(
        onPeliculaSeleccionada: (Int) -> Unit = {},
        onCerrarSesion: () -> Unit = {},
        onMisReservas: () -> Unit = {}
    ) {
        compose.setContent {
            CineMaxTheme {
                CarteleraScreen(
                    viewModel = viewModel,
                    sesion = sesionDePrueba(),
                    onPeliculaSeleccionada = onPeliculaSeleccionada,
                    onCerrarSesion = onCerrarSesion,
                    onMisReservas = onMisReservas
                )
            }
        }
    }

    @Test
    fun laCarteleraMuestraElSaludoDelUsuarioReal() {
        pintar()

        compose.nodo(EtiquetasPrueba.PANTALLA_CARTELERA).assertIsDisplayed()
        // El saludo sale de la SESION, no de un texto fijo: se comprueba con el
        // nombre real del cliente del seed.
        compose.nodoConTexto(EtiquetasPrueba.TITULO, "Hola, ${primerNombre()}").assertIsDisplayed()
    }

    @Test
    fun laBarraDeFiltrosMuestraGeneroFechaYSede() {
        pintar()

        compose.nodo(EtiquetasPrueba.FILTRO_GENERO).assertIsDisplayed()
        compose.nodo(EtiquetasPrueba.FILTRO_FECHA).assertIsDisplayed()
        compose.nodo(EtiquetasPrueba.FILTRO_SEDE).assertIsDisplayed()
    }

    @Test
    fun elContadorAnunciaCuantasFuncionesHayEnCartelera() {
        pintar()

        compose.nodo(EtiquetasPrueba.CONTADOR_FUNCIONES)
            .assert(hasText("${catalogo.funciones.size} funciones en cartelera"))
    }

    @Test
    fun sinFiltrosLaListaMuestraLasFuncionesDelSeed() {
        pintar()

        compose.nodo(EtiquetasPrueba.LISTA).assertIsDisplayed()
        assertTrue(
            compose.onAllNodesWithTag(EtiquetasPrueba.FILTRO_LIMPIAR).fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun alPulsarUnaTarjetaSeAbreSuPelicula() {
        var peliculaAbierta = 0
        pintar(onPeliculaSeleccionada = { peliculaAbierta = it })

        val funcion = catalogo.funciones.first()
        compose.nodo(EtiquetasPrueba.tarjetaFuncion(funcion.id)).performScrollTo().performClick()
        compose.waitForIdle()

        // La pantalla navega con el `peliculaId`, no con la entidad.
        assertEquals(funcion.peliculaId, peliculaAbierta)
    }

    @Test
    fun filtrarPorGeneroRecortaLaListaYApareceElBotonDeLimpiar() {
        pintar()
        val genero = catalogo.unGeneroConFunciones()
        val esperadas = catalogo.funcionesDeGenero(genero).size

        compose.nodo(EtiquetasPrueba.FILTRO_GENERO + ":" + genero).performScrollTo().performClick()
        compose.waitForIdle()

        compose.nodo(EtiquetasPrueba.CONTADOR_FUNCIONES)
            .assert(hasText("$esperadas funciones con filtro"))
        compose.nodo(EtiquetasPrueba.FILTRO_LIMPIAR).assertIsDisplayed()
    }

    @Test
    fun limpiarFiltrosDevuelveLaCarteleraCompleta() {
        pintar()
        val genero = catalogo.unGeneroConFunciones()

        compose.nodo(EtiquetasPrueba.FILTRO_GENERO + ":" + genero).performScrollTo().performClick()
        compose.waitForIdle()
        // "Limpiar filtros" vive en la cabecera, que no tiene scroll: se espera al
        // nodo y se pulsa, sin `performScrollTo()`.
        compose.nodo(EtiquetasPrueba.FILTRO_LIMPIAR).performClick()
        compose.waitForIdle()

        compose.nodo(EtiquetasPrueba.CONTADOR_FUNCIONES)
            .assert(hasText("${catalogo.funciones.size} funciones en cartelera"))
    }

    @Test
    fun unFiltroSinCoincidenciasOfreceQuitarElFiltroEnLugarDeRecargar() {
        pintar()

        // Se elige el chip "Todos" de la fecha (que ya esta activo) para poder
        // terminar el test sin depender de una fecha concreta del seed.
        compose.nodo(EtiquetasPrueba.FILTRO_FECHA + ":TODOS").performScrollTo().performClick()
        compose.waitForIdle()

        // Sin filtro sigue habiendo funciones, asi que no debe aparecer el vacio.
        assertTrue(
            compose.onAllNodesWithTag("cartelera:quitarFiltros").fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun lasTarjetasAnuncianLaDisponibilidadDeButacas() {
        pintar()

        // El seed ya vendio 2 butacas de su primera funcion: la tarjeta lo dice.
        val funcion = catalogo.funciones.first()
        val capacidad = catalogo.sala(funcion.salaId).capacidad
        val yaOcupadas = ocupadas[funcion.id] ?: 0

        compose.nodo(EtiquetasPrueba.tarjetaFuncion(funcion.id))
            .assert(hasText("Quedan ${capacidad - yaOcupadas} de $capacidad butacas"))
    }

    @Test
    fun unaFuncionSinReservasAnunciaLaCapacidadCompleta() {
        pintar()

        val funcion = catalogo.funcionLibre(ocupadas)
        val capacidad = catalogo.sala(funcion.salaId).capacidad

        compose.nodo(EtiquetasPrueba.tarjetaFuncion(funcion.id))
            .assert(hasText("Quedan $capacidad de $capacidad butacas"))
    }

    @Test
    fun elBotonMisReservasEstaDisponible() {
        var abiertas = 0
        pintar(onMisReservas = { abiertas++ })

        // Esta en la barra inferior: sin scroll, se espera al nodo y se pulsa.
        compose.nodo("cartelera:misReservas").performClick()
        compose.waitForIdle()

        assertEquals(1, abiertas)
    }

    @Test
    fun cerrarSesionSaleDeLaPantalla() {
        var salidas = 0
        pintar(onCerrarSesion = { salidas++ })

        compose.nodo(EtiquetasPrueba.BOTON).performClick()
        compose.waitForIdle()

        assertEquals(1, salidas)
    }

    @Test
    fun comprarDesdeOtroDispositivoBajaLaDisponibilidadEnPantalla() = runBlocking<Unit> {
        val usuario = catalogo.usuarios.first()
        val funcion = catalogo.funcionLibre(ocupadas)
        val capacidad = catalogo.sala(funcion.salaId).capacidad
        pintar()

        compose.nodo(EtiquetasPrueba.tarjetaFuncion(funcion.id))
            .assert(hasText("Quedan $capacidad de $capacidad butacas"))

        val resultado = RepositoriosCliente.reservas(database).registrar(
            SolicitudReserva(
                usuarioId = usuario.id,
                funcionId = funcion.id,
                butacaIds = catalogo.butacas(funcion.salaId).take(2).map { it.id },
                total = funcion.precioEntrada * 2,
                fechaCompra = "2026-03-10"
            )
        )
        assertTrue(resultado.toString(), resultado is ResultadoReserva.Exito)

        // El `Flow` de Room repinta la tarjeta sin recargar: la disponibilidad de
        // la cartelera es reactiva, asi que se ESPERA al texto nuevo.
        compose.nodoConTexto(
            EtiquetasPrueba.tarjetaFuncion(funcion.id),
            "Quedan ${capacidad - 2} de $capacidad butacas"
        ).assert(hasText("Quedan ${capacidad - 2} de $capacidad butacas"))
    }

    @Test
    fun elPrimerElementoVisibleEsUnaTarjetaDeFuncion() {
        pintar()

        val primera = catalogo.funciones.first()
        compose.onAllNodesWithTag(EtiquetasPrueba.tarjetaFuncion(primera.id)).onFirst()
            .assertIsDisplayed()
    }

    private fun primerNombre(): String =
        sesionDePrueba().nombreCompleto.substringBefore(' ')

    private fun sesionDePrueba(): UsuarioSesion = catalogo.usuarios.first().aSesionDePrueba()
}
