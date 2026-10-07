package com.cinemax.cliente.data.repository

import com.cinemax.cliente.data.local.ButacaDao
import com.cinemax.cliente.data.local.ButacaEntity
import com.cinemax.cliente.data.local.FuncionDao
import com.cinemax.cliente.data.local.SalaConButacas
import com.cinemax.cliente.data.local.SalaDao
import com.cinemax.cliente.data.local.SalaEntity
import com.cinemax.cliente.data.local.SedeDao
import com.cinemax.cliente.data.local.SedeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * FASE 5 - Repositorio de RECINTO del Cliente: sedes, salas y butacas.
 *
 * Concentra las tres piezas que hacen falta para pintar el mapa de butacas y,
 * sobre todo, para responder a la pregunta que decide una venta:
 * "¿esta butaca sigue libre en ESTA funcion?".
 *
 * La distincion importante es entre SALA y FUNCION:
 * - una butaca pertenece a una SALA (`butacas.sala_id`);
 * - una butaca se ocupa para una FUNCION (`butaca_reservas.funcion_id`).
 * Por eso la misma butaca puede estar ocupada en la funcion de las 10:00 y libre
 * en la de las 15:00 de la misma sala. Nunca se decide disponibilidad por sala.
 *
 * Solo lectura: la creacion de salas y butacas es tarea de CINEMAX-ADMIN.
 */
class SalaButacaRepository(
    private val salaDao: SalaDao,
    private val butacaDao: ButacaDao,
    private val sedeDao: SedeDao,
    private val funcionDao: FuncionDao
) {

    // ------------------------------------------------------------------
    // Sedes y salas
    // ------------------------------------------------------------------

    fun observarSedes(): Flow<List<SedeEntity>> = sedeDao.observarTodas()

    fun observarSalas(): Flow<List<SalaEntity>> = salaDao.observarTodas()

    fun observarSalaPorId(id: Int): Flow<SalaEntity?> = salaDao.observarPorId(id)

    suspend fun obtenerSalaPorId(id: Int): SalaEntity? = salaDao.porId(id)

    /** Sala de una funcion. `null` si la funcion no existe. */
    suspend fun obtenerSalaDeFuncion(funcionId: Int): SalaEntity? =
        funcionDao.porId(funcionId)?.let { salaDao.porId(it.salaId) }

    // ------------------------------------------------------------------
    // Butacas de una sala
    // ------------------------------------------------------------------

    /** Sala con su mapa de butacas, en un solo `Flow` (`SalaDao.observarConButacas`). */
    fun observarSalaConButacas(id: Int): Flow<SalaConButacas?> = salaDao.observarConButacas(id)

    /** Todas las butacas de la sala, ordenadas por fila y numero. */
    fun observarButacasDeSala(salaId: Int): Flow<List<ButacaEntity>> = butacaDao.porSala(salaId)

    /** Lectura puntual del mapa de butacas de la sala. */
    suspend fun butacasDeSala(salaId: Int): List<ButacaEntity> = butacaDao.porSala(salaId).first()

    // ------------------------------------------------------------------
    // Ocupacion por funcion
    // ------------------------------------------------------------------

    /** Butacas ya vendidas para la funcion. Cambia en vivo si otra persona compra. */
    fun observarOcupadas(funcionId: Int): Flow<List<ButacaEntity>> = butacaDao.ocupadasEnFuncion(funcionId)

    /** Butacas todavia libres para la funcion (sala completa menos las ocupadas). */
    fun observarDisponibles(funcionId: Int, salaId: Int): Flow<List<ButacaEntity>> =
        butacaDao.disponiblesEnFuncion(funcionId, salaId)

    /**
     * REVALIDACION de disponibilidad, en una sola lectura y SIN `Flow`.
     *
     * Es la consulta que `ReservaRepository` ejecuta DENTRO de la transaccion de
     * la reserva y la que `ReservaViewModel` ejecuta justo antes de confirmar.
     * Por eso no puede ser un `Flow`: dentro de `withTransaction` un `Flow` de
     * Room se recolecta en otro hilo y bloquearia la escritura.
     *
     * @return las butacas de [butacaIds] que ya estan ocupadas en [funcionId].
     */
    suspend fun butacasOcupadas(funcionId: Int, butacaIds: List<Int>): List<ButacaEntity> {
        if (butacaIds.isEmpty()) return emptyList()
        val ocupadas = butacaDao.ocupadasDeButacasEnFuncion(funcionId, butacaIds)
        if (ocupadas.isEmpty()) return emptyList()
        return butacaDao.porIds(ocupadas)
    }

    /** Cuantas butacas estan ocupadas en la funcion. */
    suspend fun contarOcupadas(funcionId: Int): Int = butacaDao.contarOcupadasEnFuncion(funcionId)

    /**
     * FASE 7 - Ocupacion de TODAS las funciones en una sola lectura.
     *
     * La necesita la cartelera para mostrar "quedan X de Y butacas" en cada
     * tarjeta sin lanzar una consulta por funcion. Es reactiva: si otra persona
     * compra en cualquier funcion, la cartelera entera se actualiza.
     *
     * @return `funcionId -> butacas ocupadas`.
     */
    fun observarOcupadasPorFuncion(): Flow<Map<Int, Int>> =
        butacaDao.contarOcupadasPorFuncion()
            .map { filas -> filas.associate { it.funcionId to it.ocupadas } }

    /** Capacidad total de la sala. */
    suspend fun contarButacasDeSala(salaId: Int): Int = butacaDao.contarPorSala(salaId)
}
