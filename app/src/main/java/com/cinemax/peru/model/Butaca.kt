package com.cinemax.peru.model

data class Butaca(
    val id: Int,
    val fila: Char,
    val numero: Int,
    val estado: EstadoButaca
)

enum class EstadoButaca {
    DISPONIBLE,
    SELECCIONADA,
    OCUPADA
}