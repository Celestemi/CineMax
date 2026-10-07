package com.cinemax.cliente.state

import com.cinemax.cliente.data.local.FuncionEntity
import com.cinemax.cliente.data.local.ReservaConDetalle
import com.cinemax.cliente.data.local.ReservaEntity
import com.cinemax.cliente.data.local.UsuarioEntity
import com.cinemax.cliente.model.EstadoReserva
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FASE 7 - Mapeo de una reserva de Room a la linea que ve el usuario.
 *
 * Se comprueba en JVM (no hace falta base de datos) porque lo que se protege es el
 * TRASPASO de datos: que a la pantalla le llegue el codigo y el total REALES, y
 * que un estado desconocido en Room no reviente la pantalla del historial.
 */
class MisReservasUiStateTest {

    @Test
    fun laReservaLlevaElCodigoYElTotalQueEscribioElRepositorio() {
        val listado = filaReal(codigo = "CMX-0007", total = 90.0)
            .aReservaListada(mapaTitulos())

        assertEquals("CMX-0007", listado.codigo)
        assertEquals(90.0, listado.total, 0.0001)
    }

    @Test
    fun laFechaYLaHoraSonLasDeLaFuncionReservada() {
        val listado = filaReal().aReservaListada(mapaTitulos())

        assertEquals("2026-03-14", listado.fechaFuncion)
        assertEquals("19:00", listado.horaFuncion)
        assertEquals("2026-03-14 · 19:00", listado.cuandoFuncion)
    }

    @Test
    fun elTituloSeBuscaEnElCatalogoYNoEnLaRelacion() {
        val listado = filaReal(peliculaId = 3).aReservaListada(mapaTitulos())

        assertEquals("Interstellar", listado.tituloPelicula)
    }

    @Test
    fun unaPeliculaFueraDelCatalogoMuestraSuIdEnLugarDeUnHueco() {
        val listado = filaReal(peliculaId = 99).aReservaListada(mapaTitulos())

        // El id es feo, pero es verdad: una pelicula borrada del catalogo no debe
        // dejar una linea en blanco que parezca un fallo de la pantalla.
        assertTrue(listado.tituloPelicula, listado.tituloPelicula.contains("99"))
    }

    @Test
    fun unEstadoDesconocidoEnRoomNoRompeLaPantalla() {
        val listado = filaReal(estado = "PENDIENTE_POR_INVENCION")
            .aReservaListada(mapaTitulos())

        // Cae en el estado por defecto en vez de lanzar excepcion.
        assertEquals(EstadoReserva.POR_DEFECTO, listado.estado)
    }

    @Test
    fun losEstadosPersistidosSeReconocen() {
        assertEquals(
            EstadoReserva.CONFIRMADA,
            filaReal(estado = "CONFIRMADA").aReservaListada(mapaTitulos()).estado
        )
        assertEquals(
            EstadoReserva.CANCELADA,
            filaReal(estado = "CANCELADA").aReservaListada(mapaTitulos()).estado
        )
    }

    @Test
    fun soloUnaReservaConfirmadaSeMarcaComoTal() {
        assertTrue(filaReal().aReservaListada(mapaTitulos()).confirmada)
        assertFalse(
            filaReal(estado = "CANCELADA")
                .aReservaListada(mapaTitulos())
                .confirmada
        )
    }

    @Test
    fun elHistorialVacioSeDistingueDeUnHistorialConReservas() {
        val vacio = MisReservasUiState.Exito(emptyList())

        assertTrue(vacio.vacio)
        assertEquals(0, vacio.total)

        val conUna = MisReservasUiState.Exito(listOf(filaReal().aReservaListada(mapaTitulos())))

        assertFalse(conUna.vacio)
        assertEquals(1, conUna.total)
    }

    // ------------------------------------------------------------------
    // Ayudas
    // ------------------------------------------------------------------

    private fun mapaTitulos(): Map<Int, String> = mapOf(3 to "Interstellar")

    private fun filaReal(
        codigo: String = "CMX-0001",
        total: Double = 45.0,
        estado: String = ReservaEntity.ESTADO_CONFIRMADA,
        peliculaId: Int = 3
    ): ReservaConDetalle = ReservaConDetalle(
        reserva = ReservaEntity(
            id = 1,
            codigo = codigo,
            usuarioId = 4,
            funcionId = 9,
            fechaCompra = "2026-03-10",
            total = total,
            estado = estado
        ),
        usuario = UsuarioEntity(
            id = 4,
            usuario = "ana",
            nombreCompleto = "Ana Torres",
            email = "ana@correo.com",
            contrasena = "hash",
            telefono = "999",
            rol = "CLIENTE",
            creadoEn = 1
        ),
        funcion = FuncionEntity(
            id = 9,
            peliculaId = peliculaId,
            sedeId = 1,
            salaId = 2,
            fecha = "2026-03-14",
            hora = "19:00",
            precioEntrada = 15.0,
            activa = true
        )
    )
}