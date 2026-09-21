package com.cinemax.peru.model

data class Sala(
    val id: Int,
    val sedeId: Int,
    val nombre: String,
    val butacas: List<Butaca>
)