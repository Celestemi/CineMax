package com.cinemax.peru.model

data class Reserva(
    val id: Int,
    val codigo: String,
    val fechaCompra: String,
    val funcion: Funcion,
    val butacas: List<Butaca>,
    val total: Double
)