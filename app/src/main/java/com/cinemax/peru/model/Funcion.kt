package com.cinemax.peru.model

data class Funcion(
    val id: Int,
    val pelicula: Pelicula,
    val sede: Sede,
    val sala: Sala,
    val fecha: String,
    val hora: String,
    val precio: Double,
    val activa: Boolean = true
)