package com.cinemax.cliente.data.repository

import com.cinemax.cliente.data.local.PeliculaDao
import com.cinemax.cliente.data.local.PeliculaEntity
import kotlinx.coroutines.flow.Flow

/**
 * FASE 5 - Repositorio de PELICULAS del Cliente.
 *
 * Unica puerta de la capa de datos al `PeliculaDao`. Ningun Composable ni
 * ViewModel consulta Room directamente: ambos consumen este repositorio, de modo
 * que las queries podran cambiarse mas adelante (red, cache) sin tocar la UI.
 *
 * Solo operaciones de LECTURA orientadas al cliente: el alta y la edicion de
 * peliculas pertenecen a CINEMAX-ADMIN, que no se crea en esta fase.
 *
 * NOTA: se respetan los `Flow` del DAO. Ningun metodo convierte un `Flow` en una
 * lista estatica, de modo que si la tabla cambia la pantalla se entera sola.
 */
class PeliculaRepository(private val peliculaDao: PeliculaDao) {

    /** Todas las peliculas en cartelera, ordenadas por titulo. */
    fun observarActivas(): Flow<List<PeliculaEntity>> = peliculaDao.observarActivas()

    /** Pelicula concreta; emite `null` si no existe o si se desactiva. */
    fun observarPorId(id: Int): Flow<PeliculaEntity?> = peliculaDao.observarPorId(id)

    /** Pelicula concreta en una sola lectura. `null` si no existe. */
    suspend fun obtenerPorId(id: Int): PeliculaEntity? = peliculaDao.porId(id)

    /** Peliculas de un genero. El genero se compara contra `peliculas.genero`. */
    fun observarPorGenero(genero: String): Flow<List<PeliculaEntity>> =
        peliculaDao.porGenero(genero.trim())

    /** Generos que tienen al menos una pelicula, para el selector de la cartelera. */
    fun observarGeneros(): Flow<List<String>> = peliculaDao.observarGeneros()

    /** `true` si la pelicula existe y esta activa (la app Cliente no muestra las dadas de baja). */
    suspend fun estaActiva(id: Int): Boolean = peliculaDao.porId(id)?.activo == true

    suspend fun contar(): Int = peliculaDao.contar()
}
