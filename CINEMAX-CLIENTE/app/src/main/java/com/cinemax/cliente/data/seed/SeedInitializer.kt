package com.cinemax.cliente.data.seed

import androidx.room.withTransaction
import com.cinemax.cliente.data.local.ButacaReservaEntity
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.ReservaEntity
import com.cinemax.cliente.model.EstadoReserva
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * FASE 3 - Carga inicial de datos de CineMax Cliente.
 *
 * Idempotente por diseno:
 * - [programar] solo encola una vez por proceso.
 * - [sembrar] solo inserta cuando el catalogo esta vacio y lo hace dentro de una
 *   unica transaccion, por lo que ejecutarlo N veces nunca duplica informacion.
 *
 * PROVISIONAL: no se guarda ninguna preferencia de "ya sembrado". Es suficiente
 * porque el sembrado solo ocurre si la base esta vacia; en Fase 4/5 se
 * complementara con la inicializacion real de sesion y datos.
 */
object SeedInitializer {

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var programado = false

    /** Encola el sembrado en segundo plano, como maximo una vez por proceso. */
    fun programar(database: CineMaxClienteDatabase) {
        synchronized(this) {
            if (programado) return
            programado = true
        }
        alcance.launch { sembrar(database) }
    }

    /**
     * Inserta todo el catalogo demo en una unica transaccion.
     * @return `true` si se sembro, `false` si ya habia datos.
     */
    suspend fun sembrar(database: CineMaxClienteDatabase): Boolean = database.withTransaction {
        if (yaSembrada(database)) return@withTransaction false

        val peliculaIds = database.peliculaDao().insertarTodas(SeedData.peliculas)
        val sedeIds = database.sedeDao().insertarTodas(SeedData.sedes)

        val salas = SeedData.salas(sedeIds)
        val salaIds = database.salaDao().insertarTodas(salas)

        val butacasPorSala = mutableMapOf<Int, Map<String, Int>>()
        salas.forEachIndexed { indice, sala ->
            val salaId = salaIds[indice].toInt()
            val butacas = SeedData.butacasDeSala(salaId)
            require(butacas.size == SeedData.BUTACAS_POR_SALA)
            val butacaIds = database.butacaDao().insertarTodas(butacas)
            require(butacaIds.size == butacas.size)
            butacasPorSala[salaId] = butacas
                .mapIndexed { posicion, butaca -> butaca.codigo to butacaIds[posicion].toInt() }
                .toMap()
        }

        val funciones = SeedData.funciones(peliculaIds, sedeIds, salaIds)
        val funcionIds = database.funcionDao().insertarTodas(funciones)

        val usuarioIds = SeedData.usuarios().map { database.usuarioDao().insertar(it) }

        val ocupacion = mutableSetOf<Pair<Int, Int>>()
        SeedData.reservasDemo.forEach { demo ->
            require(demo.usuario in usuarioIds.indices)
            require(demo.funcion in funciones.indices)

            val funcion = funciones[demo.funcion]
            val funcionId = funcionIds[demo.funcion].toInt()
            val catalogo = butacasPorSala[funcion.salaId]
                ?: error("No hay butacas sembradas para la sala ${funcion.salaId}")

            val butacasReserva = demo.butacas.map { codigo ->
                catalogo[codigo] ?: error("La butaca $codigo no existe en la sala ${funcion.salaId}")
            }
            require(ocupacion.addAll(butacasReserva.map { funcionId to it })) {
                "Butaca duplicada para la misma funcion en ${demo.codigo}"
            }

            val reservaId = database.reservaDao().insertar(
                ReservaEntity(
                    codigo = demo.codigo,
                    usuarioId = usuarioIds[demo.usuario].toInt(),
                    funcionId = funcionId,
                    fechaCompra = SeedData.hoyIso(),
                    total = butacasReserva.size * funcion.precioEntrada,
                    estado = demo.estado.valorPersistido
                )
            ).toInt()

            if (demo.estado == EstadoReserva.CONFIRMADA) {
                database.reservaDao().insertarDetalleButacas(
                    butacasReserva.map { butacaId ->
                        ButacaReservaEntity(
                            reservaId = reservaId,
                            funcionId = funcionId,
                            butacaId = butacaId
                        )
                    }
                )
            }
        }

        true
    }

    /** `true` si ya existe cualquier dato de catalogo. */
    suspend fun yaSembrada(database: CineMaxClienteDatabase): Boolean =
        database.peliculaDao().contar() > 0 ||
            database.sedeDao().contar() > 0 ||
            database.salaDao().contar() > 0 ||
            database.butacaDao().contar() > 0 ||
            database.funcionDao().contar() > 0 ||
            database.usuarioDao().contar() > 0
}
