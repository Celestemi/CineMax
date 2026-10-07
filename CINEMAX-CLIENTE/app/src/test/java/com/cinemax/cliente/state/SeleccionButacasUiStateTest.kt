package com.cinemax.cliente.state

import com.cinemax.cliente.data.local.ButacaEntity
import com.cinemax.cliente.data.local.FuncionEntity
import com.cinemax.cliente.data.local.PeliculaEntity
import com.cinemax.cliente.data.local.SalaEntity
import com.cinemax.cliente.data.local.SedeEntity
import com.cinemax.cliente.model.ButacaSeleccionable
import com.cinemax.cliente.model.EstadoButaca
import com.cinemax.cliente.viewmodel.CalculoReserva
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FASE 5 - Reglas derivadas del estado de la pantalla de butacas.
 *
 * Comprueba que la informacion de la que depende el Composable (seleccion,
 * ocupadas, importe y si se puede continuar) se deriva del estado y NO se
 * recalcula en la UI.
 */
class SeleccionButacasUiStateTest {

    private val funcion = FuncionEntity(
        id = 5,
        peliculaId = 1,
        sedeId = 1,
        salaId = 1,
        fecha = "2026-05-01",
        hora = "20:00",
        precioEntrada = 15.0,
        activa = true
    )

    private val pelicula = PeliculaEntity(
        id = 1,
        titulo = "Cerro Veloz",
        genero = "ACCION",
        clasificacionEdad = "14+",
        duracionMinutos = 128,
        sinopsis = "...",
        posterUrl = "",
        trailerUrl = ""
    )

    private val sede = SedeEntity(id = 1, nombre = "CineMax Miraflores", distrito = "Miraflores", direccion = "")
    private val sala = SalaEntity(id = 1, sedeId = 1, nombre = "Sala 1", filas = 2, columnas = 3)

    private fun butaca(id: Int, fila: String = "A", numero: Int = 1) =
        ButacaEntity(id = id, salaId = 1, fila = fila, numero = numero)

    private fun estado(
        mapa: List<ButacaSeleccionable>,
        total: CalculoReserva.Resultado
    ) = SeleccionButacasUiState.Exito(
        funcion = funcion,
        pelicula = pelicula,
        sede = sede,
        sala = sala,
        butacas = mapa,
        precioEntrada = 15.0,
        total = total
    )

    @Test
    fun unaButacaOcupadaNuncaEsSeleccionable() {
        val ocupada = ButacaSeleccionable(butaca(1), EstadoButaca.OCUPADA)

        assertFalse(ocupada.seleccionable)
        assertFalse(ocupada.seleccionada)
    }

    @Test
    fun unaButacaLibreEsSeleccionableYUnaSeleccionadaTambien() {
        assertTrue(ButacaSeleccionable(butaca(1), EstadoButaca.LIBRE).seleccionable)
        assertTrue(ButacaSeleccionable(butaca(1), EstadoButaca.SELECCIONADA).seleccionable)
    }

    @Test
    fun laSeleccionSeLeeDelMapaDeButacas() {
        val mapa = listOf(
            ButacaSeleccionable(butaca(1, "A", 1), EstadoButaca.OCUPADA),
            ButacaSeleccionable(butaca(2, "A", 2), EstadoButaca.SELECCIONADA),
            ButacaSeleccionable(butaca(3, "A", 3), EstadoButaca.SELECCIONADA),
            ButacaSeleccionable(butaca(4, "B", 1), EstadoButaca.LIBRE)
        )

        val estado = estado(mapa, CalculoReserva.Resultado.Valido(45.0))

        assertEquals(listOf("A2", "A3"), estado.codigosSeleccionadas)
        assertEquals(setOf(2, 3), estado.seleccionadasIds)
        assertEquals(2, estado.seleccionadas.size)
        assertTrue(estado.haySeleccion)
        assertEquals(1, estado.ocupadas)
        assertEquals(1, estado.libres)
        assertEquals(4, estado.capacidad)
    }

    @Test
    fun elImporteSaleDelCalculoYNoDeUnaMultiplicacionEnLaUI() {
        val mapa = listOf(
            ButacaSeleccionable(butaca(1), EstadoButaca.SELECCIONADA),
            ButacaSeleccionable(butaca(2), EstadoButaca.SELECCIONADA),
            ButacaSeleccionable(butaca(3), EstadoButaca.SELECCIONADA)
        )

        val estado = estado(mapa, CalculoReserva.calcularTotal(15.0, 3))

        assertEquals(45.0, estado.importe!!, 0.0001)
        assertTrue(estado.puedeContinuar)
    }

    @Test
    fun sinSeleccionNoHayImporteNiSePuedeContinuar() {
        val mapa = listOf(
            ButacaSeleccionable(butaca(1), EstadoButaca.LIBRE),
            ButacaSeleccionable(butaca(2), EstadoButaca.OCUPADA)
        )

        val estado = estado(mapa, CalculoReserva.calcularTotal(15.0, 0))

        assertNull(estado.importe)
        assertFalse(estado.haySeleccion)
        assertFalse(estado.puedeContinuar)
    }

    @Test
    fun conTotalInvalidoTampocoSePuedeContinuar() {
        val mapa = listOf(ButacaSeleccionable(butaca(1), EstadoButaca.SELECCIONADA))
        val estado = estado(mapa, CalculoReserva.Resultado.Invalido(CalculoReserva.ErrorCalculo.PRECIO_INVALIDO))

        assertNull(estado.importe)
        assertTrue(estado.haySeleccion)
        assertFalse(estado.puedeContinuar)
    }

    @Test
    fun unAvisoDesapareceEnLaSiguienteReconstruccion() {
        val mapa = listOf(ButacaSeleccionable(butaca(1), EstadoButaca.LIBRE))
        val conAviso = estado(mapa, CalculoReserva.calcularTotal(15.0, 0)).copy(aviso = "La butaca A1 ya esta ocupada")

        assertEquals("La butaca A1 ya esta ocupada", conAviso.aviso)
        assertNull(estado(mapa, CalculoReserva.calcularTotal(15.0, 0)).aviso)
    }

    @Test
    fun elEstadoInicialNoEsDeExito() {
        assertFalse(SeleccionButacasUiState.Inicial is SeleccionButacasUiState.Exito)
        assertFalse(SeleccionButacasUiState.Cargando is SeleccionButacasUiState.Exito)
        assertFalse(SeleccionButacasUiState.FuncionNoEncontrada is SeleccionButacasUiState.Exito)
        assertFalse(SeleccionButacasUiState.Error("x") is SeleccionButacasUiState.Exito)
    }
}
