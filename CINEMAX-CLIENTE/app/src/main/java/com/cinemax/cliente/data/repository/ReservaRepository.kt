package com.cinemax.cliente.data.repository

import androidx.room.withTransaction
import com.cinemax.cliente.data.local.ButacaDao
import com.cinemax.cliente.data.local.ButacaEntity
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.FuncionDao
import com.cinemax.cliente.data.local.ReservaDao
import com.cinemax.cliente.data.local.ReservaConDetalle
import com.cinemax.cliente.data.local.ReservaEntity
import com.cinemax.cliente.data.local.UsuarioDao
import com.cinemax.cliente.model.EstadoReserva
import com.cinemax.cliente.viewmodel.CalculoReserva
import com.cinemax.cliente.viewmodel.CodigoReserva
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * FASE 5 - Datos que `ReservaRepository.registrar` necesita para escribir una
 * reserva.
 *
 * IMPORTANTE: el `usuarioId` NO lo elige la UI. `ReservaViewModel` lo obtiene de
 * `AuthViewModel.sesion` (la sesion en memoria aprobada en Fase 4), de modo que
 * una reserva siempre queda asociada al usuario realmente autenticado.
 *
 * El `total` lo calcula el ViewModel con `CalculoReserva.calcularTotal`; aqui
 * solo se vuelve a comprobar que sea un importe valido.
 */
data class SolicitudReserva(
    val usuarioId: Int,
    val funcionId: Int,
    val butacaIds: List<Int>,
    val total: Double,
    val fechaCompra: String
)

/** FASE 5 - Por que se rechaza una reserva. La UI solo muestra [mensaje]. */
enum class MotivoReserva(val mensaje: String) {
    SIN_FUNCION("Selecciona una funcion antes de reservar"),
    FUNCION_INEXISTENTE("La funcion seleccionada ya no existe"),
    FUNCION_INACTIVA("La funcion seleccionada ya no esta disponible"),
    SIN_SESION("Inicia sesion para reservar"),
    USUARIO_INEXISTENTE("Tu cuenta ya no esta registrada en la base de datos"),
    SIN_BUTACAS("Selecciona al menos una butaca"),
    BUTACAS_DESCONOCIDAS("Alguna de las butacas seleccionadas no existe"),
    BUTACAS_DE_OTRA_SALA("Alguna de las butacas seleccionadas no pertenece a esta sala"),
    BUTACAS_DUPLICADAS("Hay butacas repetidas en la seleccion"),
    BUTACAS_YA_OCUPADAS("Algunas butacas ya fueron ocupadas. Elige otras"),
    TOTAL_INVALIDO("El total de la reserva no es valido"),
    CODIGO_NO_DISPONIBLE("No se pudo generar un codigo de reserva. Intentalo de nuevo")
}

/** FASE 5 - Resultado de registrar una reserva. */
sealed class ResultadoReserva {
    /** Reserva escrita; [reserva] lleva ya el `id` asignado por Room. */
    data class Exito(val reserva: ReservaEntity, val butacas: List<ButacaEntity>) : ResultadoReserva()

    /** Regla de negocio incumplida: NO se ha escrito nada. */
    data class Rechazada(val motivo: MotivoReserva) : ResultadoReserva() {
        val mensaje: String get() = motivo.mensaje
    }

    /** Fallo tecnico de escritura. La transaccion se ha revertido. */
    data class Error(val causa: Throwable) : ResultadoReserva()
}

/** FASE 5 - Resultado de cancelar una reserva. */
sealed class ResultadoCancelacion {
    data class Exito(val reserva: ReservaEntity) : ResultadoCancelacion()
    data object NoEncontrada : ResultadoCancelacion()
    data object NoPerteneceAlUsuario : ResultadoCancelacion()
    data object YaCancelada : ResultadoCancelacion()
    data class Error(val causa: Throwable) : ResultadoCancelacion()
}

/**
 * FASE 5 - Repositorio de RESERVAS del Cliente.
 *
 * Es el unico punto del proyecto que escribe en `reservas` y `butaca_reservas`, y
 * por tanto el unico que puede garantizar que una butaca no se vende dos veces.
 *
 * ## COMO SE PROTEGE CONTRA LA DOBLE VENTA
 *
 * Se combinan tres mecanismos, del mas fuerte al mas debil:
 *
 * 1. **Indice UNIQUE (`butaca_reservas(funcion_id, butaca_id)`)** - heredado de
 *    Fase 2 y NO eliminado. Es la garantia real: aunque dos personas confirmen a
 *    la vez la misma butaca, SQLite no puede llegar a tener las dos filas.
 * 2. **Transaccion unica** - `database.withTransaction { ... }` envuelve la
 *    comprobacion y las dos inserciones. `ReservaDao.registrarReserva` es a su vez
 *    `@Transaction`. Si algo falla, no queda ni la reserva ni sus butacas: nunca
 *    hay una reserva parcial.
 * 3. **Comprobacion dentro de la transaccion** - `butacasOcupadas` se consulta con
 *    la MISMA transaccion abierta, de modo que el usuario recibe un mensaje claro
 *    ("algunas butacas ya fueron ocupadas") en vez de un error de SQLite.
 *
 * La UI (punto 4.F de la guia) tambien revalida antes de confirmar, pero esa es
 * solo la version-visible de la garantia: la definitiva es el indice UNIQUE.
 *
 * @param database se inyecta (y no solo los DAOs) porque la operacion necesita una
 *        transaccion que abarque `reservas` y `butaca_reservas`.
 */
class ReservaRepository(
    private val database: CineMaxClienteDatabase,
    private val reservaDao: ReservaDao = database.reservaDao(),
    private val butacaDao: ButacaDao = database.butacaDao(),
    private val funcionDao: FuncionDao = database.funcionDao(),
    private val usuarioDao: UsuarioDao = database.usuarioDao()
) {

    // ------------------------------------------------------------------
    // Registrar reserva
    // ------------------------------------------------------------------

    /**
     * Registra la reserva completa de forma atomica.
     *
     * Orden de operaciones (punto 5 de la guia):
     * 1. validar en memoria lo que no depende de Room (sesion, funcion, butacas, total);
     * 2. abrir la transaccion;
     * 3. verificar que la funcion existe y esta activa;
     * 4. verificar que el usuario autenticado existe;
     * 5. verificar que las butacas existen y son de la sala de esa funcion;
     * 6. verificar que siguen disponibles;
     * 7. insertar `ReservaEntity` y `ButacaReservaEntity` (una sola transaccion);
     * 8. si algo falla, se revierte TODO.
     *
     * @return [ResultadoReserva.Exito] con la reserva ya persistida,
     *         [ResultadoReserva.Rechazada] si se incumple una regla de negocio
     *         (en cuyo caso no se ha escrito nada) o [ResultadoReserva.Error] si
     *         fallo la escritura.
     */
    suspend fun registrar(solicitud: SolicitudReserva): ResultadoReserva {
        // --- Fase 1: validaciones puras, sin tocar la base de datos ---
        if (solicitud.usuarioId <= 0) return ResultadoReserva.Rechazada(MotivoReserva.SIN_SESION)
        if (solicitud.funcionId <= 0) return ResultadoReserva.Rechazada(MotivoReserva.SIN_FUNCION)
        if (solicitud.butacaIds.isEmpty()) return ResultadoReserva.Rechazada(MotivoReserva.SIN_BUTACAS)
        if (!CalculoReserva.esTotalValido(solicitud.total)) {
            return ResultadoReserva.Rechazada(MotivoReserva.TOTAL_INVALIDO)
        }

        val butacasUnicas = solicitud.butacaIds.distinct()
        if (butacasUnicas.size != solicitud.butacaIds.size) {
            return ResultadoReserva.Rechazada(MotivoReserva.BUTACAS_DUPLICADAS)
        }

        // --- Fase 2: escritura transaccional ---
        // Solo se reintenta por una colision de `codigo` (indice UNIQUE). Un
        // rechazo de negocio o una butaca ya ocupada se devuelven tal cual.
        var ultimoFallo: ResultadoReserva.Error? = null
        val consecutivoInicial = reservaDao.contar() + 1
        for (intento in 0 until CodigoReserva.MAX_INTENTOS) {
            val codigo = siguienteCodigoLibre(consecutivoInicial + intento)
                ?: return ResultadoReserva.Rechazada(MotivoReserva.CODIGO_NO_DISPONIBLE)

            when (val resultado = registrarEnTransaccion(codigo, solicitud, butacasUnicas)) {
                is ResultadoReserva.Error -> {
                    ultimoFallo = resultado
                }

                else -> return resultado
            }
        }
        return ultimoFallo ?: ResultadoReserva.Rechazada(MotivoReserva.CODIGO_NO_DISPONIBLE)
    }

    /**
     * Cuerpo transaccional. Puede devolver [ResultadoReserva.Rechazada] sin haber
     * escrito nada (todos los rechazos se comprueban ANTES del primer `insert`).
     */
    private suspend fun registrarEnTransaccion(
        codigo: String,
        solicitud: SolicitudReserva,
        butacasUnicas: List<Int>
    ): ResultadoReserva = try {
        database.withTransaction<ResultadoReserva> {
            // 3) la funcion existe y esta activa
            val funcion = funcionDao.porId(solicitud.funcionId)
                ?: return@withTransaction ResultadoReserva.Rechazada(MotivoReserva.FUNCION_INEXISTENTE)
            if (!funcion.activa) {
                return@withTransaction ResultadoReserva.Rechazada(MotivoReserva.FUNCION_INACTIVA)
            }

            // 4) el usuario autenticado sigue existiendo
            if (usuarioDao.porId(solicitud.usuarioId) == null) {
                return@withTransaction ResultadoReserva.Rechazada(MotivoReserva.USUARIO_INEXISTENTE)
            }

            // 5) las butacas existen y pertenecen a la sala de la funcion
            val butacas = butacaDao.porIds(butacasUnicas)
            if (butacas.size != butacasUnicas.size) {
                return@withTransaction ResultadoReserva.Rechazada(MotivoReserva.BUTACAS_DESCONOCIDAS)
            }
            if (butacas.any { it.salaId != funcion.salaId }) {
                return@withTransaction ResultadoReserva.Rechazada(MotivoReserva.BUTACAS_DE_OTRA_SALA)
            }

            // 6) siguen disponibles (comprobacion DENTRO de la transaccion)
            val ocupadas = butacaDao.ocupadasDeButacasEnFuncion(solicitud.funcionId, butacasUnicas)
            if (ocupadas.isNotEmpty()) {
                return@withTransaction ResultadoReserva.Rechazada(MotivoReserva.BUTACAS_YA_OCUPADAS)
            }

            // 7) escritura atomica de la reserva y de sus butacas
            val entidad = ReservaEntity(
                codigo = codigo,
                usuarioId = solicitud.usuarioId,
                funcionId = solicitud.funcionId,
                fechaCompra = solicitud.fechaCompra,
                total = solicitud.total,
                estado = ReservaEntity.ESTADO_CONFIRMADA
            )
            val reservaId = reservaDao.registrarReserva(entidad, butacas)
            ResultadoReserva.Exito(entidad.copy(id = reservaId), butacas)
        }
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (fallo: Exception) {
        clasificarFallo(fallo, solicitud.funcionId, butacasUnicas)
    }

    /**
     * Convierte una violacion de constraint en un motivo de negocio.
     *
     * Se vuelve a consultar la ocupacion DESPUES de la transaccion (ya revertida):
     * - si alguna butaca acaba de ocuparse, otra persona se llevo la butaca entre
     *   la comprobacion y el `insert`; el mensaje correcto es "ya esta ocupada";
     * - si no, el fallo es de `codigo` (indice UNIQUE) o de escritura, y se
     *   devuelve como [ResultadoReserva.Error] para que [registrar] reintente.
     */
    private suspend fun clasificarFallo(
        causa: Throwable,
        funcionId: Int,
        butacasUnicas: List<Int>
    ): ResultadoReserva {
        val ocupadas = try {
            butacaDao.ocupadasDeButacasEnFuncion(funcionId, butacasUnicas)
        } catch (ignorado: Exception) {
            emptyList()
        }
        return if (ocupadas.isNotEmpty()) {
            ResultadoReserva.Rechazada(MotivoReserva.BUTACAS_YA_OCUPADAS)
        } else {
            ResultadoReserva.Error(causa)
        }
    }

    /** Primer codigo libre a partir de [consecutivoInicial], o `null` si no hay ninguno. */
    private suspend fun siguienteCodigoLibre(consecutivoInicial: Int): String? {
        var consecutivo = consecutivoInicial
        repeat(CodigoReserva.MAX_INTENTOS) {
            val codigo = CodigoReserva.generar(consecutivo)
            if (reservaDao.buscarPorCodigo(codigo) == null) return codigo
            consecutivo = CodigoReserva.siguiente(consecutivo)
        }
        return null
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    fun observarPorCodigo(codigo: String): Flow<ReservaEntity?> = reservaDao.porCodigo(codigo.trim())

    suspend fun obtenerPorCodigo(codigo: String): ReservaEntity? =
        reservaDao.buscarPorCodigo(codigo.trim())

    fun observarPorUsuario(usuarioId: Int): Flow<List<ReservaEntity>> = reservaDao.porUsuario(usuarioId)

    /**
     * FASE 7 - Historial del usuario CON la funcion de cada reserva.
     *
     * Usa `ReservaDao.porUsuarioConDetalle`, una consulta `@Transaction` que ya
     * existia desde Fase 2/3 y que solo faltaba consumir: trae el
     * `UsuarioEntity` y el `FuncionEntity` de cada fila sin N+1.
     *
     * El `usuarioId` lo decide la sesion, no la UI: la pantalla de "Mis Reservas"
     * lo recibe del `AuthViewModel`, igual que `ReservaViewModel` para comprar.
     */
    fun observarPorUsuarioConDetalle(usuarioId: Int): Flow<List<ReservaConDetalle>> =
        reservaDao.porUsuarioConDetalle(usuarioId)

    fun observarPorUsuarioYEstado(usuarioId: Int, estado: EstadoReserva): Flow<List<ReservaEntity>> =
        reservaDao.porUsuarioYEstado(usuarioId, estado.valorPersistido)

    fun observarPorFuncion(funcionId: Int): Flow<List<ReservaEntity>> = reservaDao.porFuncion(funcionId)

    /** Butacas de una reserva, en `Flow` (se actualiza si la reserva se cancela). */
    fun observarButacasDeReserva(reservaId: Int): Flow<List<ButacaEntity>> =
        reservaDao.butacasDeReserva(reservaId)

    suspend fun contarButacasDeReserva(reservaId: Int): Int = reservaDao.contarButacasDeReserva(reservaId)

    suspend fun contar(): Int = reservaDao.contar()

    // ------------------------------------------------------------------
    // Cancelar
    // ------------------------------------------------------------------

    /**
     * Cancela una reserva y libera sus butacas.
     *
     * `ReservaDao.cancelarReserva` es `@Transaction`: borra las filas de
     * `butaca_reservas` y marca la reserva como CANCELADA, de modo que la butaca
     * vuelve a estar libre para esa misma funcion sin tocar el indice UNIQUE.
     *
     * @param usuarioId el usuario de la SESION: no se puede cancelar la reserva
     *        de otra persona.
     */
    suspend fun cancelar(codigo: String, usuarioId: Int): ResultadoCancelacion {
        val reserva = reservaDao.buscarPorCodigo(codigo.trim())
            ?: return ResultadoCancelacion.NoEncontrada
        if (reserva.usuarioId != usuarioId) return ResultadoCancelacion.NoPerteneceAlUsuario
        if (reserva.estado == ReservaEntity.ESTADO_CANCELADA) return ResultadoCancelacion.YaCancelada

        return try {
            database.withTransaction {
                reservaDao.cancelarReserva(reserva)
            }
            ResultadoCancelacion.Exito(reserva.copy(estado = ReservaEntity.ESTADO_CANCELADA))
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (fallo: Exception) {
            ResultadoCancelacion.Error(fallo)
        }
    }
}
