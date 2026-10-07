package com.cinemax.cliente.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class SalaConButacas(
    @Embedded val sala: SalaEntity,
    @Relation(parentColumn = "id", entityColumn = "sala_id")
    val butacas: List<ButacaEntity>
)

data class FuncionCompleta(
    @Embedded val funcion: FuncionEntity,
    @Relation(parentColumn = "pelicula_id", entityColumn = "id")
    val pelicula: PeliculaEntity,
    @Relation(parentColumn = "sede_id", entityColumn = "id")
    val sede: SedeEntity,
    @Relation(parentColumn = "sala_id", entityColumn = "id")
    val sala: SalaEntity
)

data class ReservaConDetalle(
    @Embedded val reserva: ReservaEntity,
    @Relation(parentColumn = "usuario_id", entityColumn = "id")
    val usuario: UsuarioEntity,
    @Relation(parentColumn = "funcion_id", entityColumn = "id")
    val funcion: FuncionEntity
)
