package com.cinemax.peru.data.repository

import com.cinemax.peru.data.fake.FakeData
import com.cinemax.peru.model.Genero
import com.cinemax.peru.model.Pelicula

interface PeliculaRepository {
    fun obtenerTodas(): List<Pelicula>
    fun obtenerPorId(peliculaId: Int): Pelicula?
    fun obtenerPorGenero(genero: Genero): List<Pelicula>
    fun registrarPelicula(pelicula: Pelicula): Pelicula
    fun actualizarPelicula(pelicula: Pelicula): Pelicula?
    fun desactivarPelicula(peliculaId: Int): Boolean
    fun activarPelicula(peliculaId: Int): Boolean
}

class FakePeliculaRepository(
    iniciales: List<Pelicula> = FakeData.peliculas
) : PeliculaRepository {

    private val peliculasEnMemoria: MutableList<Pelicula> = iniciales.toMutableList()
    private var siguienteId: Int = (iniciales.maxOfOrNull { it.id } ?: 0) + 1

    override fun obtenerTodas(): List<Pelicula> = peliculasEnMemoria.toList()

    override fun obtenerPorId(peliculaId: Int): Pelicula? =
        peliculasEnMemoria.firstOrNull { it.id == peliculaId }

    override fun obtenerPorGenero(genero: Genero): List<Pelicula> =
        peliculasEnMemoria.filter { it.genero == genero }

    override fun registrarPelicula(pelicula: Pelicula): Pelicula {
        val id = siguienteId++
        val nueva = pelicula.copy(id = id)
        peliculasEnMemoria += nueva
        return nueva
    }

    override fun actualizarPelicula(pelicula: Pelicula): Pelicula? {
        val indice = peliculasEnMemoria.indexOfFirst { it.id == pelicula.id }
        if (indice == -1) return null
        peliculasEnMemoria[indice] = pelicula
        return pelicula
    }

    override fun desactivarPelicula(peliculaId: Int): Boolean {
        val indice = peliculasEnMemoria.indexOfFirst { it.id == peliculaId }
        if (indice == -1) return false
        peliculasEnMemoria[indice] = peliculasEnMemoria[indice].copy(activo = false)
        return true
    }

    override fun activarPelicula(peliculaId: Int): Boolean {
        val indice = peliculasEnMemoria.indexOfFirst { it.id == peliculaId }
        if (indice == -1) return false
        peliculasEnMemoria[indice] = peliculasEnMemoria[indice].copy(activo = true)
        return true
    }
}