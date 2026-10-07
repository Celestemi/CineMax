package com.cinemax.cliente.data.seed

import com.cinemax.cliente.data.local.ButacaEntity
import com.cinemax.cliente.data.local.GeneroConverter
import com.cinemax.cliente.data.local.EstadoReservaConverter
import com.cinemax.cliente.model.EstadoReserva
import com.cinemax.cliente.model.Genero
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedDataTest {

    private val sedeIds = SeedData.sedes.indices.map { (it + 1).toLong() }
    private val peliculaIds = SeedData.peliculas.indices.map { (it + 101).toLong() }
    private val salas = SeedData.salas(sedeIds)
    private val salaIds = salas.indices.map { (it + 201).toLong() }

    @Test
    fun `volumenes minimos del seed`() {
        assertEquals(12, SeedData.peliculas.size)
        assertEquals(4, SeedData.sedes.size)
        assertEquals(8, salas.size)
        assertEquals(80, SeedData.BUTACAS_POR_SALA)
        assertEquals(80, SeedData.butacasDeSala(999).size)
        assertEquals(34, SeedData.funcionesProgramadas)
        assertEquals(34, funciones().size)
        assertTrue(SeedData.usuarios().size >= 2)
    }

    @Test
    fun `las 8 salas tienen 80 butacas y 640 en total`() {
        val total = salas.indices.sumOf { SeedData.butacasDeSala(salaIds[it].toInt()).size }
        assertEquals(640, total)
    }

    @Test
    fun `cada sala declara 8 filas por 10 columnas`() {
        salas.forEach { sala ->
            assertEquals(8, sala.filas)
            assertEquals(10, sala.columnas)
            assertEquals(80, sala.capacidad)
        }
    }

    @Test
    fun `las butacas cubren filas A-H y numeros 1-10 sin repetir`() {
        val butacas: List<ButacaEntity> = SeedData.butacasDeSala(7)
        assertEquals(SeedData.FILAS, butacas.map { it.fila }.distinct())
        assertEquals(listOf("A", "B", "C", "D", "E", "F", "G", "H"), SeedData.FILAS)
        assertEquals((1..10).toList(), SeedData.NUMEROS)
        assertEquals(80, butacas.map { it.codigo }.distinct().size)
        assertTrue(butacas.all { it.salaId == 7 })
    }

    @Test
    fun `cada sala cuelga de una sede sembrada`() {
        salas.forEachIndexed { indice, sala ->
            assertEquals(sedeIds[indice / SeedData.SALAS_POR_SEDE].toInt(), sala.sedeId)
        }
    }

    @Test
    fun `las funciones tienen fechas futuras`() {
        val hoy = SeedData.hoyIso()
        funciones().forEach { funcion ->
            assertTrue("Fecha vencida: ${funcion.fecha}", funcion.fecha > hoy)
        }
        val fechas = funciones().map { it.fecha }.distinct()
        assertEquals(SeedData.DIAS_CARTELERA, fechas.size)
        assertEquals(SeedData.fechaIso(1), fechas.min())
        assertEquals(SeedData.fechaIso(SeedData.DIAS_CARTELERA), fechas.max())
    }

    @Test
    fun `ninguna funcion apunta a peliculas sedes o salas inexistentes`() {
        funciones().forEach { funcion ->
            assertTrue(funcion.peliculaId in peliculaIds.map { it.toInt() })
            assertTrue(funcion.sedeId in sedeIds.map { it.toInt() })
            assertTrue(funcion.salaId in salaIds.map { it.toInt() })
        }
    }

    @Test
    fun `la sede de cada funcion coincide con la sede de su sala`() {
        val sedeDeSala = salas.withIndex().associate { (indice, sala) ->
            salaIds[indice].toInt() to sala.sedeId
        }
        funciones().forEach { funcion ->
            assertEquals(sedeDeSala[funcion.salaId], funcion.sedeId)
        }
    }

    @Test
    fun `no hay dos funciones con la misma sala fecha y hora`() {
        val claves = funciones().map { Triple(it.salaId, it.fecha, it.hora) }
        assertEquals(claves.size, claves.distinct().size)
    }

    @Test
    fun `los usuarios demo son validos y unicos`() {
        val usuarios = SeedData.usuarios(creadoEn = 1L)
        assertEquals(usuarios.size, usuarios.map { it.usuario }.distinct().size)
        assertTrue(usuarios.all { it.contrasena == SeedData.CONTRASENA_DEMO })
        assertTrue(usuarios.any { it.usuario == "cliente" && it.rol == "CLIENTE" })
        assertTrue(usuarios.any { it.usuario == "admin" && it.rol == "ADMINISTRADOR" })
    }

    @Test
    fun `las reservas demo referencian funciones y butacas existentes`() {
        val funciones = funciones()
        val butacasPorSala = salas.indices.associate { indice ->
            val salaId = salaIds[indice].toInt()
            salaId to SeedData.butacasDeSala(salaId).associateBy { it.codigo }
        }
        val ocupacion = mutableSetOf<Pair<Int, String>>()

        SeedData.reservasDemo.forEach { demo ->
            val funcion = funciones[demo.funcion]
            val catalogo = butacasPorSala.getValue(funcion.salaId)
            demo.butacas.forEach { codigo ->
                assertTrue("Butaca inexistente $codigo", codigo in catalogo)
                assertTrue("Butaca duplicada $codigo", ocupacion.add(demo.funcion to codigo))
            }
        }
    }

    @Test
    fun `los converters van y vuelven entre enum y TEXT`() {
        val generos = GeneroConverter()
        Genero.entries.forEach { genero ->
            assertEquals(genero, generos.aGenero(generos.deGenero(genero)))
        }
        assertEquals("ACCION", generos.deGenero(Genero.ACCION))

        val estados = EstadoReservaConverter()
        EstadoReserva.entries.forEach { estado ->
            assertEquals(estado, estados.aEstadoReserva(estados.deEstadoReserva(estado)))
        }
        assertEquals("CONFIRMADA", estados.deEstadoReserva(EstadoReserva.CONFIRMADA))
        assertEquals("CANCELADA", estados.deEstadoReserva(EstadoReserva.CANCELADA))
    }

    private fun funciones() = SeedData.funciones(peliculaIds, sedeIds, salaIds)
}
