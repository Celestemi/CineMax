package com.cinemax.peru.state

import com.cinemax.peru.model.Funcion
import com.cinemax.peru.model.Sala

data class OcupacionSala(
    val funcion: Funcion,
    val sala: Sala,
    val capacidad: Int,
    val ocupadas: Int,
    val disponibles: Int,
    val porcentaje: Double
)