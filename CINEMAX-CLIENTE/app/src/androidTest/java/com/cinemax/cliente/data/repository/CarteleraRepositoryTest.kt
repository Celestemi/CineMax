package com.cinemax.cliente.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cinemax.cliente.data.BaseDePrueba
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.seed.SeedData
import com.cinemax.cliente.model.FiltroCartelera
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * FASE 5 - Casos A a F: la cartelera leida desde Room a traves de los
 * repositorios del Cliente.
 *
 * Todas las consultas van contra una base SQLite en memoria real con la misma
 * clase [CineMaxClienteDatabase] que usa la app, sembrada con `SeedData`, de modo
 * que los filtros se comprueban sobre el mismo SQL (y los mismos indices) que
 * ejecutara la app.
 */
@RunWith(AndroidJUnit4::class)
class CarteleraRepositoryTest {

    private lateinit var database: CineMaxClienteDatabase
    private lateinit var peliculas: PeliculaRepository
    private lateinit var funciones: FuncionRepository
    private lateinit var salas: SalaButacaRepository
    private lateinit var catalogo: com.cinemax.cliente.data.Catalogo

    @Before
    fun setUp() = runBlocking<Unit> {
        database = BaseDePrueba.abrirEnMemoria()
        catalogo = BaseDePrueba.sembrarYLeer(database)
        peliculas = RepositoriosCliente.peliculas(database)
        funciones = RepositoriosCliente.funciones(database)
        salas = RepositoriosCliente.salas(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ------------------------------------------------------------------
    // A) La cartelera carga funciones desde Room
    // ------------------------------------------------------------------

    @Test
    fun a_laCarteleraCargaTodasLasFuncionesSembradas() = runBlocking<Unit> {
        val resultado = funciones.observarFiltradas(FiltroCartelera.SIN_FILTROS).first()

        assertEquals(SeedData.funcionesProgramadas, resultado.size)
        assertEquals(catalogo.funciones.size, resultado.size)
    }

    @Test
    fun a_cadaFuncionLlegaCompletaConPeliculaSedeYSala() = runBlocking<Unit> {
        val resultado = funciones.observarFiltradas(FiltroCartelera.SIN_FILTROS).first()
        val primera = resultado.first()

        assertNotNull(primera.pelicula.titulo)
        assertNotNull(primera.sede.nombre)
        assertNotNull(primera.sala.nombre)
        assertEquals(primera.funcion.peliculaId, primera.pelicula.id)
        assertEquals(primera.funcion.sedeId, primera.sede.id)
        assertEquals(primera.funcion.salaId, primera.sala.id)
    }

    @Test
    fun a_laCarteleraVieneOrdenadaPorFechaYHora() = runBlocking<Unit> {
        val resultado = funciones.observarFiltradas(FiltroCartelera.SIN_FILTROS).first()

        val orden = resultado.map { "${it.funcion.fecha} ${it.funcion.hora}" }

        assertEquals(orden.sorted(), orden)
    }

    @Test
    fun a_lasFuncionesActivasSonTodasLasSembradas() = runBlocking<Unit> {
        // El seed no siembra ninguna funcion inactiva.
        assertEquals(SeedData.funcionesProgramadas, funciones.observarActivas().first().size)
        assertEquals(SeedData.funcionesProgramadas, funciones.contarActivas())
    }

    @Test
    fun a_unaFuncionInactivaDesapareceDeLaCartelera() = runBlocking<Unit> {
        val objetivo = catalogo.funciones.first()
        database.funcionDao().actualizar(objetivo.copy(activa = false))

        val resultado = funciones.observarFiltradas(FiltroCartelera.SIN_FILTROS).first()

        assertEquals(SeedData.funcionesProgramadas - 1, resultado.size)
        assertFalse(resultado.any { it.funcion.id == objetivo.id })
        assertFalse(funciones.estaActiva(objetivo.id))
    }

    // ------------------------------------------------------------------
    // B) Filtro por genero
    // ------------------------------------------------------------------

    @Test
    fun b_filtrarPorGeneroDevuelveSoloSusFunciones() = runBlocking<Unit> {
        val genero = catalogo.unGeneroConFunciones()
        val esperadas = catalogo.funcionesDeGenero(genero)
        assertTrue("El seed debe traer funciones de varios generos", esperadas.size > 0)

        val resultado = funciones.observarFiltradas(FiltroCartelera(genero = genero)).first()

        assertEquals(esperadas.size, resultado.size)
        assertTrue(resultado.all { it.pelicula.genero == genero })
        assertEquals(
            esperadas.map { it.id }.toSet(),
            resultado.map { it.funcion.id }.toSet()
        )
    }

    @Test
    fun b_filtrarPorGeneroSinCoincidenciasDevuelveListaVacia() = runBlocking<Unit> {
        val resultado = funciones.observarFiltradas(FiltroCartelera(genero = "COMEDIA MUSICAL")).first()

        assertTrue(resultado.isEmpty())
    }

    @Test
    fun b_unGeneroNoActivoSeNormalizaAntesDeConsultar() = runBlocking<Unit> {
        val genero = catalogo.unGeneroConFunciones()
        val conEspacios = funciones.observarFiltradas(FiltroCartelera(genero = "  $genero  ")).first()
        val conEspaciosAlFinal = funciones.observarFiltradas(FiltroCartelera(genero = "$genero ")).first()

        assertEquals(catalogo.funcionesDeGenero(genero).size, conEspacios.size)
        assertEquals(catalogo.funcionesDeGenero(genero).size, conEspaciosAlFinal.size)
    }

    // ------------------------------------------------------------------
    // C) Filtro por fecha
    // ------------------------------------------------------------------

    @Test
    fun c_filtrarPorFechaDevuelveSoloLasFuncionesDeEseDia() = runBlocking<Unit> {
        val fecha = catalogo.fechas().first()
        val esperadas = catalogo.funcionesDeFecha(fecha)
        assertTrue(esperadas.isNotEmpty())

        val resultado = funciones.observarFiltradas(FiltroCartelera(fecha = fecha)).first()

        assertEquals(esperadas.size, resultado.size)
        assertTrue(resultado.all { it.funcion.fecha == fecha })
    }

    @Test
    fun c_cadaFechaDeLaCarteleraFiltraPorSeparado() = runBlocking<Unit> {
        val fechas = catalogo.fechas()
        assertTrue("El seed debe traer varios dias", fechas.size > 1)

        val total = fechas.sumOf { fecha ->
            funciones.observarFiltradas(FiltroCartelera(fecha = fecha)).first().size
        }

        assertEquals(catalogo.funciones.size, total)
    }

    @Test
    fun c_unaFechaSinProgramacionDevuelveListaVacia() = runBlocking<Unit> {
        val resultado = funciones.observarFiltradas(FiltroCartelera(fecha = "1999-01-01")).first()

        assertTrue(resultado.isEmpty())
    }

    // ------------------------------------------------------------------
    // D) Filtro por sede
    // ------------------------------------------------------------------

    @Test
    fun d_filtrarPorSedeDevuelveSoloLasFuncionesDeEsaSede() = runBlocking<Unit> {
        val sede = catalogo.sedes.first()
        val esperadas = catalogo.funcionesDeSede(sede.id)
        assertTrue(esperadas.isNotEmpty())

        val resultado = funciones.observarFiltradas(FiltroCartelera(sedeId = sede.id)).first()

        assertEquals(esperadas.size, resultado.size)
        assertTrue(resultado.all { it.funcion.sedeId == sede.id })
        assertTrue(resultado.all { it.sede.id == sede.id })
    }

    @Test
    fun d_cadaSedeFiltraPorSeparado() = runBlocking<Unit> {
        val total = catalogo.sedes.sumOf { sede ->
            funciones.observarFiltradas(FiltroCartelera(sedeId = sede.id)).first().size
        }

        assertEquals(catalogo.funciones.size, total)
    }

    // ------------------------------------------------------------------
    // E) Combinacion de filtros
    // ------------------------------------------------------------------

    @Test
    fun e_laCombinacionEsLaInterseccionDeLosTresCriterios() = runBlocking<Unit> {
        val genero = catalogo.unGeneroConFunciones()
        val fechas = catalogo.fechas()

        // Se recorren todas las combinaciones de sede x fecha buscando al menos una
        // que cruce con el genero, para no depender de un caso concreto del seed.
        var combinacionesConDatos = 0
        for (sede in catalogo.sedes) {
            for (fecha in fechas) {
                val esperadas = catalogo.funcionesDeGenero(genero)
                    .filter { it.sedeId == sede.id && it.fecha == fecha }
                if (esperadas.isEmpty()) continue
                combinacionesConDatos++

                val resultado = funciones.observarFiltradas(
                    FiltroCartelera(genero = genero, fecha = fecha, sedeId = sede.id)
                ).first()

                assertEquals(
                    "Fallo en sede=${sede.id} fecha=$fecha",
                    esperadas.size,
                    resultado.size
                )
                assertTrue(resultado.all { it.pelicula.genero == genero })
                assertTrue(resultado.all { it.funcion.fecha == fecha })
                assertTrue(resultado.all { it.funcion.sedeId == sede.id })
            }
        }
        assertTrue("El seed deberia permitir alguna combinacion con datos", combinacionesConDatos > 0)
    }

    @Test
    fun e_laCombinacionDevuelveMenosQueCadaCriterioPorSeparado() = runBlocking<Unit> {
        val genero = catalogo.unGeneroConFunciones()
        val fecha = catalogo.fechas().first()
        val sede = catalogo.sedes.first { id -> catalogo.funcionesDeSede(id.id).isNotEmpty() }

        val soloGenero = funciones.observarFiltradas(FiltroCartelera(genero = genero)).first().size
        val soloFecha = funciones.observarFiltradas(FiltroCartelera(fecha = fecha)).first().size
        val soloSede = funciones.observarFiltradas(FiltroCartelera(sedeId = sede.id)).first().size
        val combo = funciones.observarFiltradas(
            FiltroCartelera(genero = genero, fecha = fecha, sedeId = sede.id)
        ).first().size

        assertTrue(combo <= soloGenero)
        assertTrue(combo <= soloFecha)
        assertTrue(combo <= soloSede)
    }

    @Test
    fun e_unCriterioQueNoEncajaVaciaLaCombinacion() = runBlocking<Unit> {
        val resultado = funciones.observarFiltradas(
            FiltroCartelera(genero = catalogo.unGeneroConFunciones(), fecha = "1999-01-01", sedeId = 1)
        ).first()

        assertTrue(resultado.isEmpty())
    }

    // ------------------------------------------------------------------
    // Opciones de los filtros
    // ------------------------------------------------------------------

    @Test
    fun lasFechasDisponiblesSonLasQueTienenFunciones() = runBlocking<Unit> {
        val fechas = funciones.observarFechas().first()

        assertEquals(catalogo.fechas(), fechas)
        assertEquals(fechas.sorted(), fechas)
    }

    @Test
    fun losGenerosDisponiblesSonLosDeLasPeliculasActivas() = runBlocking<Unit> {
        val generos = peliculas.observarGeneros().first()

        assertEquals(catalogo.peliculas.map { it.genero }.distinct().sorted(), generos)
    }

    @Test
    fun lasSedesDisponiblesSonTodasLasDelCircuito() = runBlocking<Unit> {
        val sedes = funciones.observarSedes().first()

        assertEquals(catalogo.sedes.size, sedes.size)
        assertEquals(SeedData.sedes.size, sedes.size)
    }

    // ------------------------------------------------------------------
    // F) Cargar pelicula por ID
    // ------------------------------------------------------------------

    @Test
    fun f_unaPeliculaSeCargaPorId() = runBlocking<Unit> {
        val esperada = catalogo.peliculas.first { it.titulo == "Cerro Veloz" }

        val porFlujo = peliculas.observarPorId(esperada.id).first()
        val porLectura = peliculas.obtenerPorId(esperada.id)

        assertEquals(esperada.titulo, porFlujo?.titulo)
        assertEquals(esperada, porLectura)
        assertEquals("ACCION", porLectura?.genero)
        assertEquals(128, porLectura?.duracionMinutos)
    }

    @Test
    fun f_unaPeliculaInexistenteDevuelveNull() = runBlocking<Unit> {
        assertNull(peliculas.obtenerPorId(999_999))
        assertNull(peliculas.observarPorId(999_999).first())
        assertFalse(peliculas.estaActiva(999_999))
    }

    @Test
    fun f_unaPeliculaInactivaNoEstaActiva() = runBlocking<Unit> {
        val pelicula = catalogo.peliculas.first()
        database.peliculaDao().actualizar(pelicula.copy(activo = false))

        assertFalse(peliculas.estaActiva(pelicula.id))
        assertEquals(pelicula.id, peliculas.obtenerPorId(pelicula.id)?.id)
    }

    @Test
    fun f_unaPeliculaSeFiltraPorGenero() = runBlocking<Unit> {
        val genero = catalogo.unGeneroConFunciones()

        val resultado = peliculas.observarPorGenero(genero).first()

        assertTrue(resultado.isNotEmpty())
        assertTrue(resultado.all { it.genero == genero })
        assertEquals(catalogo.peliculas.filter { it.genero == genero }.size, resultado.size)
    }

    @Test
    fun f_lasFuncionesDeUnaPeliculaTraenSedeYSala() = runBlocking<Unit> {
        val pelicula = catalogo.peliculas.first { candidata -> catalogo.funcionesDe(candidata.id).isNotEmpty() }

        val resultado = funciones.observarPorPelicula(pelicula.id).first()

        assertEquals(catalogo.funcionesDe(pelicula.id).size, resultado.size)
        assertTrue(resultado.all { it.pelicula.id == pelicula.id })
        assertTrue(resultado.all { it.sede.nombre.isNotBlank() })
        assertTrue(resultado.all { it.sala.nombre.isNotBlank() })
    }

    // ------------------------------------------------------------------
    // Consultas de una funcion suelta
    // ------------------------------------------------------------------

    @Test
    fun unaFuncionSeLeeCompletaPorId() = runBlocking<Unit> {
        val esperada = catalogo.funciones.first()

        val resultado = requireNotNull(funciones.obtenerCompletaPorId(esperada.id))

        assertEquals(esperada, resultado.funcion)
        assertEquals(esperada.peliculaId, resultado.pelicula.id)
        assertEquals(esperada.sedeId, resultado.sede.id)
        assertEquals(esperada.salaId, resultado.sala.id)
        assertEquals(resultado, funciones.observarCompletaPorId(esperada.id).first())
    }

    @Test
    fun unaFuncionInexistenteDevuelveNull() = runBlocking<Unit> {
        assertNull(funciones.obtenerCompletaPorId(999_999))
        assertNull(funciones.observarCompletaPorId(999_999).first())
        assertFalse(funciones.estaActiva(999_999))
    }

    @Test
    fun lasFuncionesDeUnaSalaYFechaSeLeenSeparadas() = runBlocking<Unit> {
        val objetivo = catalogo.funciones.first()
        val mismaSalaYFecha = catalogo.funciones.filter {
            it.salaId == objetivo.salaId && it.fecha == objetivo.fecha
        }
        assertTrue(mismaSalaYFecha.isNotEmpty())

        val resultado = funciones.observarPorSalaYFecha(objetivo.salaId, objetivo.fecha).first()

        assertEquals(mismaSalaYFecha.size, resultado.size)
        assertTrue(resultado.all { it.salaId == objetivo.salaId && it.fecha == objetivo.fecha })
    }

    @Test
    fun elRepositorioDeSalasExponeElCircuito() = runBlocking<Unit> {
        assertEquals(SeedData.sedes.size * SeedData.SALAS_POR_SEDE, salas.observarSalas().first().size)
        assertEquals(SeedData.sedes.size, salas.observarSedes().first().size)
    }
}
