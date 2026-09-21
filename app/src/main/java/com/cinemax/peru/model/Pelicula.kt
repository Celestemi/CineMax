package com.cinemax.peru.model

data class Pelicula(
    val id: Int,
    val titulo: String,
    val genero: Genero,
    val clasificacionEdad: String,
    val duracionMinutos: Int,
    val sinopsis: String,
    val posterUrl: String,
    val trailerUrl: String,
    val activo: Boolean = true
)