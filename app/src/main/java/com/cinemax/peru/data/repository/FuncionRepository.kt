package com.cinemax.peru.data.repository

import com.cinemax.peru.data.fake.FakeData
import com.cinemax.peru.model.Funcion
import com.cinemax.peru.model.Genero

interface FuncionRepository {
    fun obtenerTodas(): List<Funcion>
    fun obtenerPorPelicula(peliculaId: Int): List<Funcion>
    fun obtenerPorFecha(fecha: String): List<Funcion>
    fun obtenerPorSede(sedeId: Int): List<Funcion>
    fun obtenerPorGenero(genero: Genero): List<Funcion>
    fun filtrar(fecha: String?, sedeId: Int?, genero: Genero?): List<Funcion>
    fun registrarFuncion(funcion: Funcion): Funcion
    fun actualizarFuncion(funcion: Funcion): Funcion?
    fun desactivarFuncion(funcionId: Int): Boolean
    fun activarFuncion(funcionId: Int): Boolean
}

class FakeFuncionRepository(
    iniciales: List<Funcion> = FakeData.funciones
) : FuncionRepository {

    private val funcionesEnMemoria: MutableList<Funcion> = iniciales.toMutableList()
    private var siguienteId: Int = (iniciales.maxOfOrNull { it.id } ?: 0) + 1

    override fun obtenerTodas(): List<Funcion> = funcionesEnMemoria.toList()

    override fun obtenerPorPelicula(peliculaId: Int): List<Funcion> =
        funcionesEnMemoria.filter { it.pelicula.id == peliculaId }

    override fun obtenerPorFecha(fecha: String): List<Funcion> =
        funcionesEnMemoria.filter { it.fecha == fecha }

    override fun obtenerPorSede(sedeId: Int): List<Funcion> =
        funcionesEnMemoria.filter { it.sede.id == sedeId }

    override fun obtenerPorGenero(genero: Genero): List<Funcion> =
        funcionesEnMemoria.filter { it.pelicula.genero == genero }

    override fun filtrar(fecha: String?, sedeId: Int?, genero: Genero?): List<Funcion> =
        funcionesEnMemoria.filter { funcion ->
            (fecha == null || funcion.fecha == fecha) &&
                (sedeId == null || funcion.sede.id == sedeId) &&
                (genero == null || funcion.pelicula.genero == genero)
        }

    override fun registrarFuncion(funcion: Funcion): Funcion {
        val id = siguienteId++
        val nueva = funcion.copy(id = id)
        funcionesEnMemoria += nueva
        return nueva
    }

    override fun actualizarFuncion(funcion: Funcion): Funcion? {
        val indice = funcionesEnMemoria.indexOfFirst { it.id == funcion.id }
        if (indice == -1) return null
        funcionesEnMemoria[indice] = funcion
        return funcion
    }

    override fun desactivarFuncion(funcionId: Int): Boolean {
        val indice = funcionesEnMemoria.indexOfFirst { it.id == funcionId }
        if (indice == -1) return false
        funcionesEnMemoria[indice] = funcionesEnMemoria[indice].copy(activa = false)
        return true
    }

    override fun activarFuncion(funcionId: Int): Boolean {
        val indice = funcionesEnMemoria.indexOfFirst { it.id == funcionId }
        if (indice == -1) return false
        funcionesEnMemoria[indice] = funcionesEnMemoria[indice].copy(activa = true)
        return true
    }
}