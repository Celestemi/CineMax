package com.cinemax.cliente.data.repository

import com.cinemax.cliente.data.local.CineMaxClienteDatabase

/**
 * FASE 5 - Fabrica unica de repositorios del Cliente.
 *
 * Evita que cada `ViewModelFactory` reconstruya a mano las mismas dependencias
 * (y que dos pantallas construyan un repositorio con DAOs distintos por error).
 * La UI nunca llama a un DAO: siempre pasa por aqui.
 *
 * Todos los repositorios son de SOLO LECTURA sobre el catalogo, salvo
 * [reservas], que es el unico que escribe. No hay ningun repositorio
 * administrativo: el CRUD de peliculas, salas, sedes y funciones pertenece a
 * CINEMAX-ADMIN, que no se crea en esta fase.
 */
object RepositoriosCliente {

    fun peliculas(database: CineMaxClienteDatabase): PeliculaRepository =
        PeliculaRepository(database.peliculaDao())

    fun funciones(database: CineMaxClienteDatabase): FuncionRepository =
        FuncionRepository(
            funcionDao = database.funcionDao(),
            peliculaDao = database.peliculaDao(),
            sedeDao = database.sedeDao(),
            salaDao = database.salaDao()
        )

    fun salas(database: CineMaxClienteDatabase): SalaButacaRepository =
        SalaButacaRepository(
            salaDao = database.salaDao(),
            butacaDao = database.butacaDao(),
            sedeDao = database.sedeDao(),
            funcionDao = database.funcionDao()
        )

    fun reservas(database: CineMaxClienteDatabase): ReservaRepository =
        ReservaRepository(database)
}
