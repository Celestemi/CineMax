package com.cinemax.cliente.data.repository

import com.cinemax.cliente.data.local.FuncionCompleta
import com.cinemax.cliente.data.local.FuncionDao
import com.cinemax.cliente.data.local.FuncionEntity
import com.cinemax.cliente.data.local.PeliculaDao
import com.cinemax.cliente.data.local.SalaDao
import com.cinemax.cliente.data.local.SedeDao
import com.cinemax.cliente.data.local.SedeEntity
import com.cinemax.cliente.model.FiltroCartelera
import kotlinx.coroutines.flow.Flow

/**
 * FASE 5 - Repositorio de FUNCIONES del Cliente.
 *
 * Cubre los filtros de la cartelera (genero, fecha, sede y su combinacion) y la
 * consulta de funciones de una pelicula. El filtrado real lo hace
 * `FuncionDao.filtrarCompletas` / `observarActivas`, que ya existian de Fase 3
 * con parametros anulables: aqui solo se traduce el [FiltroCartelera] a esos
 * parametros y se enriquece con la relacion `FuncionCompleta`.
 *
 * Un `FiltroCartelera` con los tres campos a `null` devuelve TODA la cartelera:
 * el DAO usa `(:genero IS NULL OR ...)` precisamente para eso.
 */
class FuncionRepository(
    private val funcionDao: FuncionDao,
    private val peliculaDao: PeliculaDao,
    private val sedeDao: SedeDao,
    private val salaDao: SalaDao
) {

    // ------------------------------------------------------------------
    // Consultas simples
    // ------------------------------------------------------------------

    /** Funciones activas ordenadas por fecha y hora. */
    fun observarActivas(): Flow<List<FuncionEntity>> = funcionDao.observarActivas()

    /**
     * Funcion completa (con pelicula, sede y sala) de una funcion; emite `null`
     * si la funcion no existe.
     */
    fun observarCompletaPorId(id: Int): Flow<FuncionCompleta?> = funcionDao.observarCompletaPorId(id)

    /**
     * Lectura puntual de la funcion completa.
     *
     * Se reconstruye con cuatro consultas `suspend` de una sola fila
     * (`funcion`, `pelicula`, `sede`, `sala`) en lugar de anadir una consulta
     * `@Transaction` nueva al DAO: el DAO de Fase 2/3 no se toca.
     *
     * `null` si la funcion no existe o si le falta alguna relacion, lo que en
     * esta base solo puede ocurrir si la fila quedo corrupta.
     */
    suspend fun obtenerCompletaPorId(id: Int): FuncionCompleta? {
        val funcion = funcionDao.porId(id) ?: return null
        val pelicula = peliculaDao.porId(funcion.peliculaId) ?: return null
        val sede = sedeDao.porId(funcion.sedeId) ?: return null
        val sala = salaDao.porId(funcion.salaId) ?: return null
        return FuncionCompleta(
            funcion = funcion,
            pelicula = pelicula,
            sede = sede,
            sala = sala
        )
    }

    /** `true` si la funcion existe y se puede vender (`funciones.activa = 1`). */
    suspend fun estaActiva(id: Int): Boolean = funcionDao.porId(id)?.activa == true

    // ------------------------------------------------------------------
    // Filtros de cartelera
    // ------------------------------------------------------------------

    /**
     * Filtro combinado de la cartelera.
     *
     * Los tres criterios son independientes y combinables:
     * - solo genero  -> `genero = X`
     * - solo fecha   -> `fecha = 'yyyy-MM-dd'`
     * - solo sede    -> `sede_id = N`
     * - los tres     -> interseccion de las tres condiciones
     *
     * Devuelve [FuncionCompleta] para que la pantalla de cartelera pueda pintar
     * titulo, genero, sede y sala sin volver a consultar.
     */
    fun observarFiltradas(filtro: FiltroCartelera): Flow<List<FuncionCompleta>> {
        val normalizado = filtro.normalizado()
        return funcionDao.filtrarCompletas(
            genero = normalizado.genero,
            fecha = normalizado.fecha,
            sedeId = normalizado.sedeId
        )
    }

    /**
     * Filtro por un unico genero. Reutiliza `FuncionDao.filtrar` con `fecha` y
     * `sedeId` a `null`, que es la forma de preguntar "de este genero, sin mas
     * restricciones".
     */
    fun observarPorGenero(genero: String): Flow<List<FuncionEntity>> =
        funcionDao.filtrar(
            genero = genero.trim(),
            fecha = null,
            sedeId = null,
            soloActivas = true
        )

    /** Funciones de una sala en una fecha concreta. */
    fun observarPorSalaYFecha(salaId: Int, fecha: String): Flow<List<FuncionEntity>> =
        funcionDao.porSalaYFecha(salaId, fecha.trim())

    /** Funciones de una pelicula, con su relacion completa (sede y sala incluidas). */
    fun observarPorPelicula(peliculaId: Int): Flow<List<FuncionCompleta>> =
        funcionDao.observarCompletasPorPelicula(peliculaId)

    // ------------------------------------------------------------------
    // Opciones de los filtros
    // ------------------------------------------------------------------

    /** Fechas con funciones, ordenadas: alimenta el selector de fecha. */
    fun observarFechas(): Flow<List<String>> = funcionDao.observarFechas()

    /** Sedes del circuito: alimenta el selector de sede. */
    fun observarSedes(): Flow<List<SedeEntity>> = sedeDao.observarTodas()

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    suspend fun contar(): Int = funcionDao.contar()

    suspend fun contarActivas(): Int = funcionDao.contarActivas()
}
