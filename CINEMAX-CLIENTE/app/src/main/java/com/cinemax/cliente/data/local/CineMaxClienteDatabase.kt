package com.cinemax.cliente.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UsuarioEntity::class,
        PeliculaEntity::class,
        SedeEntity::class,
        SalaEntity::class,
        ButacaEntity::class,
        FuncionEntity::class,
        ReservaEntity::class,
        ButacaReservaEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class CineMaxClienteDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao

    abstract fun peliculaDao(): PeliculaDao

    abstract fun sedeDao(): SedeDao

    abstract fun salaDao(): SalaDao

    abstract fun butacaDao(): ButacaDao

    abstract fun funcionDao(): FuncionDao

    abstract fun reservaDao(): ReservaDao
}
