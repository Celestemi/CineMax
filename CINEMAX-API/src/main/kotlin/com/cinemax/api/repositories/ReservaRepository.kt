package com.cinemax.api.repositories

import com.cinemax.api.db.DatabaseFactory
import com.cinemax.api.dto.ButacaDto
import com.cinemax.api.dto.CrearReservaRequest
import com.cinemax.api.dto.FuncionDto
import com.cinemax.api.dto.ReservaDto
import com.cinemax.api.plugins.AppException
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.sql.Connection
import java.util.UUID

object ReservaRepository {

    private val json = Json { ignoreUnknownKeys = true }

    fun crear(usuarioId: Long, request: CrearReservaRequest): Pair<ReservaDto, Boolean> {
        val uuid = runCatching { UUID.fromString(request.uuidOperacion) }.getOrNull()
            ?: throw AppException(HttpStatusCode.BadRequest, "UUID_INVALIDO", "uuidOperacion debe ser un UUID valido", uuidOperacion = request.uuidOperacion)

        if (request.butacasIds.isEmpty() || request.butacasIds.distinct().size != request.butacasIds.size) {
            throw AppException(HttpStatusCode.BadRequest, "VALIDACION", "butacasIds no puede estar vacio ni repetido", uuidOperacion = request.uuidOperacion)
        }
        if (request.total < 0) {
            throw AppException(HttpStatusCode.BadRequest, "VALIDACION", "total no puede ser negativo", uuidOperacion = request.uuidOperacion)
        }

        DatabaseFactory.connection().use { conn ->
            conn.autoCommit = false
            try {
                // 1) Idempotencia: mismo uuidOperacion ya procesado -> devolver resultado anterior
                val yaProcesado = buscarOperacion(conn, request.uuidOperacion)
                if (yaProcesado != null) {
                    if (yaProcesado.first != usuarioId) {
                        conn.rollback()
                        throw AppException(HttpStatusCode.Forbidden, "UUID_AJENO", "Este uuidOperacion pertenece a otro usuario", uuidOperacion = request.uuidOperacion)
                    }
                    conn.commit()
                    return json.decodeFromString<ReservaDto>(yaProcesado.second) to true
                }

                val funcion = buscarFuncion(conn, request.funcionId)
                    ?: throw AppException(HttpStatusCode.NotFound, "FUNCION_NO_ENCONTRADA", "La funcion no existe", uuidOperacion = request.uuidOperacion)
                if (!funcion.activa) {
                    throw AppException(HttpStatusCode.UnprocessableEntity, "FUNCION_INACTIVA", "La funcion no esta activa", uuidOperacion = request.uuidOperacion)
                }
                val fechaHora = try {
                    java.time.LocalDateTime.of(java.time.LocalDate.parse(funcion.fecha), java.time.LocalTime.parse(funcion.hora))
                } catch (e: Exception) { null }
                if (fechaHora != null && fechaHora.isBefore(java.time.LocalDateTime.now())) {
                    throw AppException(HttpStatusCode.UnprocessableEntity, "FUNCION_VENCIDA", "La funcion ya no esta vigente", uuidOperacion = request.uuidOperacion)
                }

                val salaId = funcion.salaId
                val butacas = ButacaRepository.porIds(request.butacasIds).associateBy { it.id }
                if (butacas.size != request.butacasIds.size) {
                    throw AppException(HttpStatusCode.BadRequest, "BUTACAS_INVALIDAS", "Una o mas butacas no existen", uuidOperacion = request.uuidOperacion)
                }
                val fueraDeSala = request.butacasIds.filter { butacas[it]?.salaId != salaId }
                if (fueraDeSala.isNotEmpty()) {
                    throw AppException(HttpStatusCode.UnprocessableEntity, "BUTACA_FUERA_DE_SALA", "Todas las butacas deben pertenecer a la sala de la funcion", uuidOperacion = request.uuidOperacion)
                }

                val ocupadas = ButacaRepository.ocupadasEn(request.funcionId, request.butacasIds)
                if (ocupadas.isNotEmpty()) {
                    conn.rollback()
                    throw AppException(
                        HttpStatusCode.Conflict, "CONFLICTO_BUTACA",
                        "Una o mas butacas ya no estan disponibles",
                        detalles = mapOf(
                            "funcionId" to kotlinx.serialization.json.JsonPrimitive(request.funcionId),
                            "butacasOcupadas" to kotlinx.serialization.json.JsonArray(ocupadas.map { kotlinx.serialization.json.JsonPrimitive(it) })
                        ),
                        uuidOperacion = request.uuidOperacion
                    )
                }

                val esperado = funcion.precioEntrada * request.butacasIds.size
                if (kotlin.math.abs(esperado - request.total) > 0.01) {
                    throw AppException(HttpStatusCode.UnprocessableEntity, "TOTAL_INVALIDO", "El total no coincide con el precio de las butacas", uuidOperacion = request.uuidOperacion)
                }

                val codigo = "CM-" + request.uuidOperacion.replace("-", "").substring(0, 8).uppercase()
                val reservaId = insertarReserva(conn, codigo, usuarioId, request, request.total)
                insertarButacaReserva(conn, reservaId, request.funcionId, request.butacasIds)

                val butacasSeleccionadas = request.butacasIds.map { butacas.getValue(it) }
                val reserva = ReservaDto(
                    id = reservaId, codigo = codigo, usuarioId = usuarioId, funcionId = request.funcionId,
                    fechaCompra = java.time.Instant.now().toString(), total = request.total,
                    estado = "CONFIRMADA", uuidOperacion = request.uuidOperacion, version = 1,
                    butacas = butacasSeleccionadas
                )
                guardarOperacion(conn, request.uuidOperacion, "CREAR_RESERVA", reservaId, usuarioId, json.encodeToString(ReservaDto.serializer(), reserva))
                conn.commit()
                return reserva to false
            } catch (e: java.sql.SQLException) {
                runCatching { conn.rollback() }
                if (e.sqlState == "23505") {
                    // Carrera concurrente con el mismo uuidOperacion o butaca: devolver el
                    // resultado ya persistido si pertenece al mismo usuario; si no, conflicto.
                    val almacenado = DatabaseFactory.connection().use { c2 ->
                        buscarOperacion(c2, request.uuidOperacion)
                    }
                    if (almacenado != null && almacenado.first == usuarioId) {
                        return json.decodeFromString<ReservaDto>(almacenado.second) to true
                    }
                    if (almacenado != null) {
                        throw AppException(HttpStatusCode.Forbidden, "UUID_AJENO", "Este uuidOperacion pertenece a otro usuario", uuidOperacion = request.uuidOperacion)
                    }
                    throw AppException(
                        HttpStatusCode.Conflict, "CONFLICTO_BUTACA",
                        "Una o mas butacas ya no estan disponibles",
                        detalles = mapOf(
                            "funcionId" to kotlinx.serialization.json.JsonPrimitive(request.funcionId),
                            "butacasOcupadas" to kotlinx.serialization.json.JsonArray(emptyList())
                        ),
                        uuidOperacion = request.uuidOperacion
                    )
                }
                throw e
            } catch (e: AppException) {
                runCatching { conn.rollback() }
                throw e
            } catch (e: SerializationException) {
                runCatching { conn.rollback() }
                throw AppException(HttpStatusCode.BadRequest, "JSON_INVALIDO", "Cuerpo de solicitud invalido", uuidOperacion = request.uuidOperacion)
            } catch (e: Throwable) {
                runCatching { conn.rollback() }
                throw e
            }
        }
    }

    fun listar(usuarioIdSesion: Long, rol: String): List<ReservaDto> {
        val sql = if (rol == "ADMINISTRADOR")
            "SELECT id, codigo, usuario_id, funcion_id, fecha_compra, total, estado, uuid_operacion, version FROM reservas ORDER BY id DESC"
        else
            "SELECT id, codigo, usuario_id, funcion_id, fecha_compra, total, estado, uuid_operacion, version FROM reservas WHERE usuario_id = ? ORDER BY id DESC"
        return DatabaseFactory.connection().use { conn ->
            conn.prepareStatement(sql).use { ps ->
                if (rol != "ADMINISTRADOR") ps.setLong(1, usuarioIdSesion)
                ps.executeQuery().use { rs ->
                    generateSequence { if (rs.next()) rs else null }.map { rsToDto(it, conn) }.toList()
                }
            }
        }
    }

    fun porId(id: Long, usuarioIdSesion: Long, rol: String): ReservaDto? {
        return DatabaseFactory.connection().use { conn ->
            conn.prepareStatement("SELECT id, codigo, usuario_id, funcion_id, fecha_compra, total, estado, uuid_operacion, version FROM reservas WHERE id = ?").use { ps ->
                ps.setLong(1, id)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return@use null
                    val dueño = rs.getLong("usuario_id")
                    if (rol != "ADMINISTRADOR" && dueño != usuarioIdSesion) {
                        throw AppException(HttpStatusCode.Forbidden, "PROHIBIDO", "No puedes ver esta reserva")
                    }
                    rsToDto(rs, conn)
                }
            }
        }
    }

    private fun rsToDto(rs: java.sql.ResultSet, conn: Connection): ReservaDto {
        val id = rs.getLong("id")
        val funcionId = rs.getLong("funcion_id")
        val butacas = conn.prepareStatement(
            "SELECT b.id, b.sala_id, b.fila, b.numero FROM butaca_reserva br JOIN butacas b ON b.id = br.butaca_id WHERE br.reserva_id = ? ORDER BY b.fila, b.numero"
        ).use { ps ->
            ps.setLong(1, id)
            ps.executeQuery().use { r ->
                generateSequence { if (r.next()) r else null }.map {
                    val f = it.getString("fila"); val n = it.getInt("numero")
                    ButacaDto(it.getLong("id"), it.getLong("sala_id"), f, n, "$f$n", "OCUPADA")
                }.toList()
            }
        }
        return ReservaDto(
            id = id, codigo = rs.getString("codigo"), usuarioId = rs.getLong("usuario_id"),
            funcionId = funcionId, fechaCompra = rs.getTimestamp("fecha_compra").toInstant().toString(),
            total = rs.getBigDecimal("total").toDouble(), estado = rs.getString("estado"),
            uuidOperacion = rs.getString("uuid_operacion"), version = rs.getLong("version"), butacas = butacas
        )
    }

    private fun buscarOperacion(conn: Connection, uuid: String): Pair<Long, String>? =
        conn.prepareStatement("SELECT usuario_id, resultado_json FROM operaciones_procesadas WHERE uuid_operacion = ?::uuid").use { ps ->
            ps.setString(1, uuid)
            ps.executeQuery().use { rs -> if (rs.next()) rs.getLong(1) to rs.getString(2) else null }
        }

    private fun buscarFuncion(conn: Connection, id: Long): FuncionDto? =
        conn.prepareStatement("SELECT id, pelicula_id, sede_id, sala_id, fecha, hora, precio_entrada, activa, version, actualizado_en FROM funciones WHERE id = ?").use { ps ->
            ps.setLong(1, id)
            ps.executeQuery().use { rs ->
                if (!rs.next()) null else FuncionDto(
                    id = rs.getLong("id"), peliculaId = rs.getLong("pelicula_id"), sedeId = rs.getLong("sede_id"),
                    salaId = rs.getLong("sala_id"), fecha = rs.getDate("fecha").toString(),
                    hora = rs.getTime("hora").toString().substring(0, 5),
                    precioEntrada = rs.getBigDecimal("precio_entrada").toDouble(), activa = rs.getBoolean("activa"),
                    version = rs.getLong("version"), actualizadoEn = rs.getTimestamp("actualizado_en").toInstant().toString()
                )
            }
        }

    private fun insertarReserva(conn: Connection, codigo: String, usuarioId: Long, req: CrearReservaRequest, total: Double): Long =
        conn.prepareStatement(
            "INSERT INTO reservas (codigo, usuario_id, funcion_id, total, uuid_operacion) VALUES (?,?,?,?,?::uuid) RETURNING id"
        ).use { ps ->
            ps.setString(1, codigo); ps.setLong(2, usuarioId); ps.setLong(3, req.funcionId)
            ps.setBigDecimal(4, total.toBigDecimal()); ps.setString(5, req.uuidOperacion)
            ps.executeQuery().use { rs -> if (rs.next()) rs.getLong(1) else error("sin id") }
        }

    private fun insertarButacaReserva(conn: Connection, reservaId: Long, funcionId: Long, butacasIds: List<Long>) {
        conn.prepareStatement("INSERT INTO butaca_reserva (reserva_id, funcion_id, butaca_id) VALUES (?,?,?)").use { ps ->
            for (id in butacasIds) {
                ps.setLong(1, reservaId); ps.setLong(2, funcionId); ps.setLong(3, id)
                ps.addBatch()
            }
            try {
                ps.executeBatch()
            } catch (e: java.sql.SQLException) {
                if (e.sqlState == "23505") {
                    throw AppException(HttpStatusCode.Conflict, "CONFLICTO_BUTACA", "Una o mas butacas ya no estan disponibles")
                }
                throw e
            }
        }
    }

    private fun guardarOperacion(conn: Connection, uuid: String, tipo: String, idRecurso: Long, usuarioId: Long, resultadoJson: String) {
        conn.prepareStatement("INSERT INTO operaciones_procesadas (uuid_operacion, tipo, id_recurso, usuario_id, resultado_json) VALUES (?::uuid,?,?,?,?::jsonb)").use { ps ->
            ps.setString(1, uuid); ps.setString(2, tipo); ps.setLong(3, idRecurso); ps.setLong(4, usuarioId); ps.setString(5, resultadoJson)
            ps.executeUpdate()
        }
    }
}
