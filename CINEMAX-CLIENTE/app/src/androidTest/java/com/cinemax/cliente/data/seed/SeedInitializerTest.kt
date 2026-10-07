package com.cinemax.cliente.data.seed

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * FASE 3 - Verifica el sembrado real contra Room (SQLite de verdad):
 * cantidades, relaciones exigidas por las FKs y no duplicacion al repetir.
 */
@RunWith(AndroidJUnit4::class)
class SeedInitializerTest {

    private lateinit var database: CineMaxClienteDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, CineMaxClienteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun siembraConLasCantidadesAcordadas() = runBlocking {
        assertTrue(SeedInitializer.sembrar(database))

        assertEquals(12, database.peliculaDao().contar())
        assertEquals(4, database.sedeDao().contar())
        assertEquals(8, database.salaDao().contar())
        assertEquals(640, database.butacaDao().contar())
        assertEquals(34, database.funcionDao().contar())
        assertEquals(6, database.usuarioDao().contar())
        assertEquals(3, database.reservaDao().contar())
        assertEquals(2, database.reservaDao().contarButacasDeReserva(1))
        assertEquals(3, database.reservaDao().contarButacasDeReserva(2))
        assertEquals(0, database.reservaDao().contarButacasDeReserva(3))
    }

    @Test
    fun cadaSalaTiene80Butacas() = runBlocking {
        SeedInitializer.sembrar(database)
        (1..8).forEach { salaId ->
            assertEquals(80, database.butacaDao().contarPorSala(salaId))
        }
    }

    @Test
    fun lasFuncionesNoEstanVencidas() = runBlocking {
        SeedInitializer.sembrar(database)
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            .format(Calendar.getInstance().time)

        val funciones = database.funcionDao().observarTodas().first()
        assertEquals(34, funciones.size)
        assertTrue(funciones.all { it.fecha > hoy })
        assertEquals(34, database.funcionDao().contarActivas())
    }

    @Test
    fun lasReservasRespetanRelacionesButacasSinDuplicar() = runBlocking {
        SeedInitializer.sembrar(database)

        // Las FKs de SQLite ya validan que no haya funciones ni butacas huerfanas:
        // el sembrado habria fallado en la transaccion.
        val reservasCliente = database.reservaDao().porUsuario(1).first()
        assertEquals(listOf(1, 7), reservasCliente.map { it.funcionId }.sorted())
        assertTrue(reservasCliente.all { it.total > 0 })

        // (funcion_id, butaca_id) es UNIQUE: ningun asiento se puede vender dos veces.
        assertEquals(2, database.butacaDao().observarOcupadasEnFuncion(1).first())
        assertEquals(3, database.butacaDao().observarOcupadasEnFuncion(7).first())
        assertEquals(0, database.butacaDao().observarOcupadasEnFuncion(13).first())

        val disponibles = database.butacaDao().disponiblesEnFuncion(1, 1).first()
        assertEquals(78, disponibles.size)
    }

    @Test
    fun noDuplicaDatosAlEjecutarseMasDeUnaVez() = runBlocking {
        assertTrue(SeedInitializer.sembrar(database))
        assertFalse(SeedInitializer.sembrar(database))
        assertFalse(SeedInitializer.sembrar(database))

        assertEquals(12, database.peliculaDao().contar())
        assertEquals(4, database.sedeDao().contar())
        assertEquals(8, database.salaDao().contar())
        assertEquals(640, database.butacaDao().contar())
        assertEquals(34, database.funcionDao().contar())
        assertEquals(6, database.usuarioDao().contar())
        assertEquals(3, database.reservaDao().contar())
    }

    @Test
    fun elGeneroYElEstadoSeGuardanComoTexto() = runBlocking {
        SeedInitializer.sembrar(database)

        val peliculas = database.peliculaDao().observarTodas().first()
        assertTrue(peliculas.all { it.genero.isNotBlank() })
        assertTrue(peliculas.any { it.genero == "ACCION" })

        val reservasCliente = database.reservaDao().porUsuario(1).first()
        assertEquals(2, reservasCliente.size)
        assertTrue(reservasCliente.all { it.estado == "CONFIRMADA" })

        val reservaCancelada = database.reservaDao().porCodigo("CINEMAX-0003").first()
        assertEquals("CANCELADA", reservaCancelada?.estado)
    }
}
