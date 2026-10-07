package com.cinemax.cliente.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cinemax.cliente.data.BaseDePrueba
import com.cinemax.cliente.data.Catalogo
import com.cinemax.cliente.data.aSesionDePrueba
import com.cinemax.cliente.data.clienteSinReservas
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.FuncionEntity
import com.cinemax.cliente.data.local.UsuarioEntity
import com.cinemax.cliente.data.ocupacionSembrada
import com.cinemax.cliente.data.repository.RepositoriosCliente
import com.cinemax.cliente.data.repository.ResultadoReserva
import com.cinemax.cliente.data.repository.SolicitudReserva
import com.cinemax.cliente.ui.screens.EtiquetasPrueba
import com.cinemax.cliente.ui.screens.MisReservasScreen
import com.cinemax.cliente.ui.theme.CineMaxTheme
import com.cinemax.cliente.viewmodel.MisReservasViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * FASE 7 - Pruebas de UI de "MIS RESERVAS".
 *
 * Comprueba los cuatro estados que la pantalla puede mostrar (cargando, vacio,
 * exito con reservas y los dos botones) sobre datos REALES de Room. El
 * `usuarioId` lo pone la sesion del ViewModel, asi que la pantalla no recibe por
 * parametro nada que pueda traer el historial de otra cuenta.
 *
 * ## Ojo con el seed
 * El catalogo demo siembra 3 reservas de ejemplo: dos del primer cliente y una
 * CANCELADA del segundo. Asi que el estado vacio se prueba con
 * [Catalogo.clienteSinReservas], las compras se hacen en funciones que el seed deja
 * libres, y los botones de cabecera y pie (`< Volver`, "Ver la cartelera") NO usan
 * `performScrollTo()` porque viven fuera del contenedor con scroll.
 */
@RunWith(AndroidJUnit4::class)
class MisReservasScreenTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: CineMaxClienteDatabase
    private lateinit var catalogo: Catalogo
    private lateinit var ocupadas: Map<Int, Int>

    @Before
    fun setUp() = runBlocking<Unit> {
        database = BaseDePrueba.abrirEnMemoria()
        catalogo = BaseDePrueba.sembrarYLeer(database)
        ocupadas = ocupacionSembrada(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun pintar(
        usuario: UsuarioEntity,
        onVolver: () -> Unit = {},
        onIrACartelera: () -> Unit = {}
    ) {
        val viewModel = MisReservasViewModel(
            reservas = RepositoriosCliente.reservas(database),
            peliculas = RepositoriosCliente.peliculas(database),
            sesion = MutableStateFlow(usuario.aSesionDePrueba())
        )
        compose.setContent {
            CineMaxTheme {
                MisReservasScreen(
                    viewModel = viewModel,
                    onVolver = onVolver,
                    onIrACartelera = onIrACartelera
                )
            }
        }
    }

    /** Compra en una funcion que el seed deja libre, para que la reserva se acepte. */
    private suspend fun comprar(
        usuario: UsuarioEntity,
        funcion: FuncionEntity,
        cuantas: Int
    ): String {
        val resultado = RepositoriosCliente.reservas(database).registrar(
            SolicitudReserva(
                usuarioId = usuario.id,
                funcionId = funcion.id,
                butacaIds = catalogo.butacas(funcion.salaId).take(cuantas).map { it.id },
                total = funcion.precioEntrada * cuantas,
                fechaCompra = "2026-03-10"
            )
        )
        assertTrue(
            "la compra de prueba debe aceptarse, fue $resultado",
            resultado is ResultadoReserva.Exito
        )
        return (resultado as ResultadoReserva.Exito).reserva.codigo
    }

    /** Funciones del seed que aun no tienen ninguna butaca ocupada. */
    private fun funcionesLibres(): List<FuncionEntity> =
        catalogo.funciones.filter { (ocupadas[it.id] ?: 0) == 0 }

    @Test
    fun unUsuarioSinComprasVeElEstadoVacioYUnaInvitacionAComprar() = runBlocking<Unit> {
        pintar(usuario = catalogo.clienteSinReservas(database))

        compose.nodo(EtiquetasPrueba.PANTALLA_MIS_RESERVAS).assertIsDisplayed()
        compose.nodoConTexto(EtiquetasPrueba.ESTADO, "Todavía no tienes reservas")
            .assertIsDisplayed()
    }

    @Test
    fun unaReservaConfirmadaApareceConSuCodigoYSuTotal() = runBlocking<Unit> {
        val usuario = catalogo.clienteSinReservas(database)
        val funcion = funcionesLibres().first()
        val codigo = comprar(usuario, funcion, cuantas = 2)
        pintar(usuario)

        val tarjeta = compose.nodo(EtiquetasPrueba.tarjetaReserva(codigo))
        tarjeta.assert(hasText(codigo))
        tarjeta.assert(
            hasText("S/ " + String.format(java.util.Locale.US, "%.2f", funcion.precioEntrada * 2))
        )
    }

    @Test
    fun laTarjetaMuestraElTituloYLaFechaDeLaPeliculaReservada() = runBlocking<Unit> {
        val usuario = catalogo.clienteSinReservas(database)
        val funcion = funcionesLibres().first()
        val titulo = catalogo.pelicula(funcion.peliculaId).titulo
        val codigo = comprar(usuario, funcion, cuantas = 1)
        pintar(usuario)

        val tarjeta = compose.nodo(EtiquetasPrueba.tarjetaReserva(codigo))
        tarjeta.assert(hasText(titulo))
        // La hora va dentro de "fecha · hora": hay que buscar como subcadena.
        tarjeta.assert(hasText(funcion.hora, substring = true))
    }

    @Test
    fun elContadorDiceCuantasReservasHay() = runBlocking<Unit> {
        val usuario = catalogo.clienteSinReservas(database)
        val libres = funcionesLibres()
        comprar(usuario, libres[0], cuantas = 1)
        comprar(usuario, libres[1], cuantas = 1)
        pintar(usuario)

        compose.nodo(EtiquetasPrueba.CONTADOR_FUNCIONES)
            .assert(hasText("2 reservas en tu historial"))
    }

    @Test
    fun elHistorialDelSeedTambienAparece() = runBlocking<Unit> {
        // El usuario "cliente" del seed ya tiene reservas: el historial las enseña
        // tal cual, sin inventarse un "aun no has reservado nada". Ojo: `usuarios`
        // viene ordenado por nombre completo, asi que `first()` NO es "cliente".
        pintar(catalogo.usuario("cliente"))

        compose.nodo(EtiquetasPrueba.tarjetaReserva("CINEMAX-0001")).assertIsDisplayed()
    }

    @Test
    fun cadaUsuarioVeSoloSusPropiasReservas() = runBlocking<Unit> {
        val primero = catalogo.usuario("cliente")
        val codigoAjeno = comprar(primero, funcionesLibres().first(), cuantas = 1)
        pintar(usuario = catalogo.clienteSinReservas(database))

        compose.nodoConTexto(EtiquetasPrueba.ESTADO, "Todavía no tienes reservas")
            .assertIsDisplayed()
        assertTrue(
            "El historial del segundo usuario no puede contener la reserva del primero",
            compose.onAllNodesWithTag(EtiquetasPrueba.tarjetaReserva(codigoAjeno))
                .fetchSemanticsNodes()
                .isEmpty()
        )
    }

    @Test
    fun elBotonVolverCierraElHistorial() = runBlocking<Unit> {
        var salidas = 0
        pintar(usuario = catalogo.usuario("cliente"), onVolver = { salidas++ })

        compose.nodo(EtiquetasPrueba.VOLVER).performClick()
        compose.waitForIdle()

        assertEquals(1, salidas)
    }

    @Test
    fun elBotonIrACarteleraFunciona() = runBlocking<Unit> {
        var destino = 0
        pintar(
            usuario = catalogo.clienteSinReservas(database),
            onIrACartelera = { destino++ }
        )

        compose.nodo("misReservas:irACartelera").performClick()
        compose.waitForIdle()

        assertEquals(1, destino)
    }

    @Test
    fun elEstadoVacioTambienOfreceVerLaCartelera() = runBlocking<Unit> {
        var destino = 0
        pintar(
            usuario = catalogo.clienteSinReservas(database),
            onIrACartelera = { destino++ }
        )

        compose.nodo("misReservas:primeraReserva").performClick()
        compose.waitForIdle()

        assertEquals(1, destino)
    }
}
