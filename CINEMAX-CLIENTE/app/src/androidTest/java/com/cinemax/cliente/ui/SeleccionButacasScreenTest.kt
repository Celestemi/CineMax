package com.cinemax.cliente.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cinemax.cliente.data.BaseDePrueba
import com.cinemax.cliente.data.Catalogo
import com.cinemax.cliente.data.funcionLibre
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.FuncionEntity
import com.cinemax.cliente.data.ocupacionSembrada
import com.cinemax.cliente.data.repository.RepositoriosCliente
import com.cinemax.cliente.data.repository.SolicitudReserva
import com.cinemax.cliente.ui.screens.EtiquetasPrueba
import com.cinemax.cliente.ui.screens.SeleccionButacasScreen
import com.cinemax.cliente.ui.theme.CineMaxTheme
import com.cinemax.cliente.viewmodel.SeleccionButacasViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * FASE 7 - Pruebas de UI del mapa de butacas.
 *
 * Se comprueba lo que el usuario ve y puede hacer sobre el mapa real de 80
 * butacas: la leyenda, la seleccion, el total, y el caso importante de una butaca
 * que se ocupa mientras se esta eligiendo.
 *
 * ## Ojo con el seed
 * El catalogo demo ya trae 3 reservas de ejemplo, y dos son CONFIRMADAS: la funcion
 * [catalogo.funcionDePrograma] 0 arranca con 2 butacas ocupadas (A1 y A2). Por eso
 * este fichero tiene dos mapas de referencia:
 * - [funcionConSeed] es la primera del seed, con "78 libres · 2 ocupadas";
 * - [funcionLibre] es la primera que el seed deja vacia, con "80 libres · 0 ocupadas".
 * Los contadores se comparan contra el numero REAL de ocupadas de la funcion, no
 * contra un 0 inventado.
 */
@RunWith(AndroidJUnit4::class)
class SeleccionButacasScreenTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: CineMaxClienteDatabase
    private lateinit var catalogo: Catalogo
    private lateinit var ocupadas: Map<Int, Int>

    /** Funcion del seed con 2 butacas ya vendidas: sirve para comprobar el contador real. */
    private fun funcionConSeed(): FuncionEntity = catalogo.funcionDePrograma(0)

    /** Funcion del seed sin ninguna butaca ocupada: la usan las pruebas que compran. */
    private fun funcionLibre(): FuncionEntity = catalogo.funcionLibre(ocupadas)

    /**
     * "79 libres · 1 ocupada · 0 elegidas de 80" con los numeros que de verdad hay.
     *
     * `libres` EXCLUYE las elegidas: el estado cuenta las butacas realmente
     * disponibles para el usuario (capacidad - ocupadas - elegidas).
     */
    private fun contador(ocupadas: Int, elegidas: Int, capacidad: Int = 80): String =
        "${capacidad - ocupadas - elegidas} libres · $ocupadas ocupadas · " +
            "$elegidas elegidas de $capacidad"

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
        funcion: FuncionEntity,
        onContinuar: (Int, List<Int>) -> Unit = { _, _ -> },
        onVolver: () -> Unit = {}
    ) {
        val viewModel = SeleccionButacasViewModel(
            funciones = RepositoriosCliente.funciones(database),
            salas = RepositoriosCliente.salas(database)
        )
        compose.setContent {
            CineMaxTheme {
                SeleccionButacasScreen(
                    viewModel = viewModel,
                    funcionId = funcion.id,
                    onContinuar = onContinuar,
                    onVolver = onVolver
                )
            }
        }
    }

    @Test
    fun elMapaMuestraLaLeyendaConLosTresEstados() {
        pintar(funcionConSeed())

        compose.nodo(EtiquetasPrueba.leyenda("LIBRE")).assertIsDisplayed()
        compose.nodo(EtiquetasPrueba.leyenda("SELECCIONADA")).assertIsDisplayed()
        compose.nodo(EtiquetasPrueba.leyenda("OCUPADA")).assertIsDisplayed()
    }

    @Test
    fun elMapaEmpiezaConLasOchentaButacasDeLaSala() {
        val funcion = funcionLibre()
        pintar(funcion)

        compose.nodo(EtiquetasPrueba.CONTADOR_FUNCIONES)
            .assert(hasText(contador(ocupadas = 0, elegidas = 0)))
    }

    @Test
    fun elMapaDescuentaLasButacasQueElSeedYaVendi() {
        val funcion = funcionConSeed()
        pintar(funcion)

        compose.nodo(EtiquetasPrueba.CONTADOR_FUNCIONES)
            .assert(hasText(contador(ocupadas = 2, elegidas = 0)))
    }

    @Test
    fun lasButacasSeIdentificanPorSuCodigoReal() {
        pintar(funcionLibre())

        compose.nodo(EtiquetasPrueba.butaca("A1")).performScrollTo().assertIsDisplayed()
        compose.nodo(EtiquetasPrueba.butaca("A10")).performScrollTo().assertIsDisplayed()
        compose.nodo(EtiquetasPrueba.butaca("H10")).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun alPulsarUnaButacaSeSeleccionaYApareceEnElTotal() {
        val funcion = funcionLibre()
        pintar(funcion)

        compose.nodo(EtiquetasPrueba.butaca("C1")).performScrollTo().performClick()
        compose.nodo(EtiquetasPrueba.butaca("C2")).performScrollTo().performClick()
        compose.waitForIdle()

        compose.nodo(EtiquetasPrueba.CONTADOR_FUNCIONES)
            .assert(hasText(contador(ocupadas = 0, elegidas = 2)))
        compose.nodo(EtiquetasPrueba.TOTAL)
            .assert(hasText("Total: " + String.format(java.util.Locale.US, "S/ %.2f", funcion.precioEntrada * 2)))
    }

    @Test
    fun alQuitarUnaButacaVuelveALibre() {
        val funcion = funcionLibre()
        pintar(funcion)

        compose.nodo(EtiquetasPrueba.butaca("B3")).performScrollTo().performClick()
        compose.waitForIdle()
        compose.nodo(EtiquetasPrueba.butaca("B3")).performScrollTo().performClick()
        compose.waitForIdle()

        compose.nodo(EtiquetasPrueba.CONTADOR_FUNCIONES)
            .assert(hasText(contador(ocupadas = 0, elegidas = 0)))
    }

    @Test
    fun unaButacaOcupadaPorRoomNoSePuedeSeleccionar() = runBlocking<Unit> {
        val usuario = catalogo.usuarios.first()
        val funcion = funcionLibre()
        val ocupada = catalogo.butacas(funcion.salaId).first { it.codigo == "C5" }
        RepositoriosCliente.reservas(database).registrar(
            SolicitudReserva(
                usuarioId = usuario.id,
                funcionId = funcion.id,
                butacaIds = listOf(ocupada.id),
                total = funcion.precioEntrada,
                fechaCompra = "2026-03-10"
            )
        )
        pintar(funcion)

        compose.nodo(EtiquetasPrueba.CONTADOR_FUNCIONES)
            .assert(hasText(contador(ocupadas = 1, elegidas = 0)))

        // Se "pulsa" la ocupada: `clickable` esta desactivado, asi que el estado no
        // cambia. Es la garantia de que la UI no puede vender una butaca vendida.
        compose.nodo(EtiquetasPrueba.butaca("C5")).performScrollTo().performClick()
        compose.waitForIdle()

        compose.nodo(EtiquetasPrueba.CONTADOR_FUNCIONES)
            .assert(hasText(contador(ocupadas = 1, elegidas = 0)))
    }

    @Test
    fun unaButacaQueSeOcupaDesapareceDeLaSeleccionEnVivo() = runBlocking<Unit> {
        val usuario = catalogo.usuarios.first()
        val funcion = funcionLibre()
        pintar(funcion)

        val butaca = catalogo.butacas(funcion.salaId).first { it.codigo == "D2" }
        compose.nodo(EtiquetasPrueba.butaca(butaca.codigo)).performScrollTo().performClick()
        compose.waitForIdle()
        compose.nodo(EtiquetasPrueba.CONTADOR_FUNCIONES)
            .assert(hasText(contador(ocupadas = 0, elegidas = 1)))

        // Otra persona compra esa misma butaca: el mapa se repinta solo.
        RepositoriosCliente.reservas(database).registrar(
            SolicitudReserva(
                usuarioId = usuario.id,
                funcionId = funcion.id,
                butacaIds = listOf(butaca.id),
                total = funcion.precioEntrada,
                fechaCompra = "2026-03-10"
            )
        )
        compose.waitForIdle()

        compose.nodo(EtiquetasPrueba.CONTADOR_FUNCIONES)
            .assert(hasText(contador(ocupadas = 1, elegidas = 0)))
    }

    @Test
    fun continuarEntregaElIdDeLaFuncionYLasButacasElegidas() {
        val funcion = funcionLibre()
        var entregada = 0
        var butacasElegidas = 0
        pintar(
            funcion = funcion,
            onContinuar = { id, butacas ->
                entregada = id
                butacasElegidas = butacas.size
            }
        )

        compose.nodo(EtiquetasPrueba.butaca("E1")).performScrollTo().performClick()
        compose.nodo(EtiquetasPrueba.butaca("E2")).performScrollTo().performClick()
        compose.waitForIdle()
        // "Continuar al resumen" arranca deshabilitado: hay que esperar a que la
        // seleccion lo habilite, no a que Compose este simplemente quieto.
        compose.nodoHabilitado(EtiquetasPrueba.BOTON).performClick()
        compose.waitForIdle()

        assertEquals(funcion.id, entregada)
        assertEquals(2, butacasElegidas)
    }

    @Test
    fun unaFuncionInexistentePideVolverEnLugarDeMostrarUnMapaVacio() {
        pintar(FuncionEntity(id = 9999, peliculaId = 1, sedeId = 1, salaId = 1, fecha = "2026-03-14", hora = "19:00", precioEntrada = 15.0))

        compose.nodo(EtiquetasPrueba.ESTADO)
            .assert(hasText("La funcion seleccionada ya no esta disponible"))
    }
}
