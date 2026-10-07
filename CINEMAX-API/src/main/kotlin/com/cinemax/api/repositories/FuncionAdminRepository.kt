package com.cinemax.api.repositories

import com.cinemax.api.db.DatabaseFactory
import com.cinemax.api.dto.CrearFuncionRequest
import com.cinemax.api.dto.DesactivarFuncionRequest
import com.cinemax.api.dto.EditarFuncionRequest
import com.cinemax.api.dto.FuncionDto
import com.cinemax.api.plugins.AppException
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import java.sql.Connection
import java.util.UUID

object FuncionAdminRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private fun validarUuid(uuid: String) {
        if (runCatching { UUID.fromString(uuid) }.getOrNull() == null) {
            throw AppException(HttpStatusCode.BadRequest, "UUID_INVALIDO", "uuidOperacion debe ser un UUID valido", uuidOperacion = uuid)
        }
    }

    private fun buscarOperacion(conn: Connection, uuid: String): Pair<Long, String>? =
        conn.prepareStatement("SELECT usuario_id, resultado_json FROM operaciones_procesadas WHERE uuid_operacion = ?::uuid").use { ps ->
            ps.setString(1, uuid)
            ps.executeQuery().use { rs -> if (rs.next()) rs.getLong(1) to rs.getString(2) else null }
        }

    private fun guardarOperacion(conn: Connection, uuid: String, tipo: String, idRecurso: Long, usuarioId: Long, resultadoJson: String) {
        conn.prepareStatement("INSERT INTO operaciones_procesadas (uuid_operacion, tipo, id_recurso, usuario_id, resultado_json) VALUES (?::uuid,?,?,?,?::jsonb)").use { ps ->
            ps.setString(1, uuid); ps.setString(2, tipo); ps.setLong(3, idRecurso); ps.setLong(4, usuarioId); ps.setString(5, resultadoJson)
            ps.executeUpdate()
        }
    }

    private fun replay(conn: Connection, uuid: String, usuarioId: Long): FuncionDto? {
        val op = buscarOperacion(conn, uuid) ?: return null
        if (op.first != usuarioId) {
            throw AppException(HttpStatusCode.Forbidden, "UUID_AJENO", "Este uuidOperacion pertenece a otro usuario", uuidOperacion = uuid)
        }
        return json.decodeFromString<FuncionDto>(op.second)
    }

    private fun funcionPorId(conn: Connection, id: Long): FuncionDto? =
        conn.prepareStatement("SELECT id, pelicula_id, sede_id, sala_id, fecha, hora, precio_entrada, activa, version, actualizado_en FROM funciones WHERE id = ?").use { ps ->
            ps.setLong(1, id)
            ps.executeQuery().use { rs ->
                if (!rs.next()) null else FuncionDto(
                    rs.getLong("id"), rs.getLong("pelicula_id"), rs.getLong("sede_id"), rs.getLong("sala_id"),
                    rs.getDate("fecha").toString(), rs.getTime("hora").toString().substring(0, 5),
                    rs.getBigDecimal("precio_entrada").toDouble(), rs.getBoolean("activa"),
                    rs.getLong("version"), rs.getTimestamp("actualizado_en").toInstant().toString()
                )
            }
        }

    private fun existePelicula(conn: Connection, id: Long): Boolean =
        conn.prepareStatement("SELECT 1 FROM peliculas WHERE id = ?").use { ps ->
            ps.setLong(1, id); ps.executeQuery().use { it.next() }
        }

    private fun existeSede(conn: Connection, id: Long): Boolean =
        conn.prepareStatement("SELECT 1 FROM sedes WHERE id = ?").use { ps ->
            ps.setLong(1, id); ps.executeQuery().use { it.next() }
        }

    private fun salaDeSede(conn: Connection, salaId: Long): Long? =
        conn.prepareStatement("SELECT sede_id FROM salas WHERE id = ?").use { ps ->
            ps.setLong(1, salaId); ps.executeQuery().use { rs -> if (rs.next()) rs.getLong(1) else null }
        }

    private fun conflictoHorario(conn: Connection, salaId: Long, fecha: String, hora: String, excluirId: Long?): Boolean =
        conn.prepareStatement("SELECT 1 FROM funciones WHERE sala_id = ? AND fecha = ?::date AND hora = ?::time AND activa = TRUE AND (? IS NULL OR id <> ?)").use { ps ->
            ps.setLong(1, salaId); ps.setString(2, fecha); ps.setString(3, hora)
            if (excluirId == null) ps.setNull(4, java.sql.Types.BIGINT) else ps.setLong(4, excluirId)
            if (excluirId == null) ps.setNull(5, java.sql.Types.BIGINT) else ps.setLong(5, excluirId)
            ps.executeQuery().use { it.next() }
        }

    fun crear(usuarioId: Long, req: CrearFuncionRequest): Pair<FuncionDto, Boolean> {
        validarUuid(req.uuidOperacion)
        validarFechaHora(req.fecha, req.hora, req.uuidOperacion)
        if (req.precioEntrada <= 0) throw AppException(HttpStatusCode.UnprocessableEntity, "PRECIO_INVALIDO", "El precio debe ser mayor que 0", uuidOperacion = req.uuidOperacion)
        if (req.fecha.isBlank() || req.hora.isBlank()) throw AppException(HttpStatusCode.BadRequest, "VALIDACION", "fecha y hora son obligatorias", uuidOperacion = req.uuidOperacion)

        DatabaseFactory.connection().use { conn ->
            conn.autoCommit = false
            try {
                replay(conn, req.uuidOperacion, usuarioId)?.let {
                    conn.commit(); return it to true
                }
                if (!existePelicula(conn, req.peliculaId)) throw AppException(HttpStatusCode.NotFound, "PELICULA_NO_ENCONTRADA", "La pelicula no existe", uuidOperacion = req.uuidOperacion)
                if (!existeSede(conn, req.sedeId)) throw AppException(HttpStatusCode.NotFound, "SEDE_NO_ENCONTRADA", "La sede no existe", uuidOperacion = req.uuidOperacion)
                val sedeDeSala = salaDeSede(conn, req.salaId) ?: throw AppException(HttpStatusCode.NotFound, "SALA_NO_ENCONTRADA", "La sala no existe", uuidOperacion = req.uuidOperacion)
                if (sedeDeSala != req.sedeId) throw AppException(HttpStatusCode.UnprocessableEntity, "SALA_NO_PERTENECE_A_SEDE", "La sala no pertenece a la sede indicada", uuidOperacion = req.uuidOperacion)
                if (conflictoHorario(conn, req.salaId, req.fecha, req.hora, null)) {
                    throw AppException(HttpStatusCode.Conflict, "CONFLICTO_SALA_HORARIO", "La sala ya tiene una funcion programada para ese horario", detalles = emptyMap(), uuidOperacion = req.uuidOperacion)
                }
                val id = conn.prepareStatement(
                    "INSERT INTO funciones (pelicula_id, sede_id, sala_id, fecha, hora, precio_entrada) VALUES (?,?,?,?::date,?::time,?) RETURNING id"
                ).use { ps ->
                    ps.setLong(1, req.peliculaId); ps.setLong(2, req.sedeId); ps.setLong(3, req.salaId)
                    ps.setString(4, req.fecha); ps.setString(5, req.hora); ps.setBigDecimal(6, req.precioEntrada.toBigDecimal())
                    ps.executeQuery().use { rs -> if (rs.next()) rs.getLong(1) else error("sin id") }
                }
                val creada = funcionPorId(conn, id)!!
                guardarOperacion(conn, req.uuidOperacion, "CREAR_FUNCION", id, usuarioId, json.encodeToString(FuncionDto.serializer(), creada))
                conn.commit()
                return creada to false
            } catch (e: java.sql.SQLException) {
                runCatching { conn.rollback() }
                if (e.sqlState == "23505") {
                    val almacenado = DatabaseFactory.connection().use { replay(it, req.uuidOperacion, usuarioId) }
                    if (almacenado != null) return almacenado to true
                }
                throw e
            } catch (e: AppException) { runCatching { conn.rollback() }; throw e }
            catch (e: Throwable) { runCatching { conn.rollback() }; throw e }
        }
    }

    fun editar(usuarioId: Long, id: Long, req: EditarFuncionRequest): Pair<FuncionDto, Boolean> {
        validarUuid(req.uuidOperacion)
        val fecha = req.fecha
        val hora = req.hora
        if (fecha != null && hora != null) validarFechaHora(fecha, hora, req.uuidOperacion)
        else if (fecha != null) validarFechaHora(fecha, "00:00", req.uuidOperacion)
        else if (hora != null) validarFechaHora("2000-01-01", hora, req.uuidOperacion)
        if (req.precioEntrada != null && req.precioEntrada <= 0) throw AppException(HttpStatusCode.UnprocessableEntity, "PRECIO_INVALIDO", "El precio debe ser mayor que 0", uuidOperacion = req.uuidOperacion)

        DatabaseFactory.connection().use { conn ->
            conn.autoCommit = false
            try {
                replay(conn, req.uuidOperacion, usuarioId)?.let { conn.commit(); return it to true }
                val actual = funcionPorId(conn, id) ?: throw AppException(HttpStatusCode.NotFound, "FUNCION_NO_ENCONTRADA", "La funcion no existe", uuidOperacion = req.uuidOperacion)
                if (req.version != null && req.version != actual.version) {
                    throw AppException(HttpStatusCode.Conflict, "CONFLICTO_VERSION", "La funcion fue modificada por otra operacion; recarga e intenta de nuevo", uuidOperacion = req.uuidOperacion)
                }
                val peliculaId = req.peliculaId ?: actual.peliculaId
                val sedeId = req.sedeId ?: actual.sedeId
                val salaId = req.salaId ?: actual.salaId
                val fechaN = req.fecha ?: actual.fecha
                val horaN = req.hora ?: actual.hora
                val precio = req.precioEntrada ?: actual.precioEntrada
                if (!existePelicula(conn, peliculaId)) throw AppException(HttpStatusCode.NotFound, "PELICULA_NO_ENCONTRADA", "La pelicula no existe", uuidOperacion = req.uuidOperacion)
                if (!existeSede(conn, sedeId)) throw AppException(HttpStatusCode.NotFound, "SEDE_NO_ENCONTRADA", "La sede no existe", uuidOperacion = req.uuidOperacion)
                val sedeDeSala = salaDeSede(conn, salaId) ?: throw AppException(HttpStatusCode.NotFound, "SALA_NO_ENCONTRADA", "La sala no existe", uuidOperacion = req.uuidOperacion)
                if (sedeDeSala != sedeId) throw AppException(HttpStatusCode.UnprocessableEntity, "SALA_NO_PERTENECE_A_SEDE", "La sala no pertenece a la sede indicada", uuidOperacion = req.uuidOperacion)
                if (conflictoHorario(conn, salaId, fechaN, horaN, id)) {
                    throw AppException(HttpStatusCode.Conflict, "CONFLICTO_SALA_HORARIO", "La sala ya tiene una funcion programada para ese horario", detalles = emptyMap(), uuidOperacion = req.uuidOperacion)
                }
                conn.prepareStatement(
                    "UPDATE funciones SET pelicula_id=?, sede_id=?, sala_id=?, fecha=?::date, hora=?::time, precio_entrada=?, version = version + 1, actualizado_en = now() WHERE id=?"
                ).use { ps ->
                    ps.setLong(1, peliculaId); ps.setLong(2, sedeId); ps.setLong(3, salaId)
                    ps.setString(4, fechaN); ps.setString(5, horaN); ps.setBigDecimal(6, precio.toBigDecimal()); ps.setLong(7, id)
                    ps.executeUpdate()
                }
                val actualizada = funcionPorId(conn, id)!!
                guardarOperacion(conn, req.uuidOperacion, "EDITAR_FUNCION", id, usuarioId, json.encodeToString(FuncionDto.serializer(), actualizada))
                conn.commit()
                return actualizada to false
            } catch (e: java.sql.SQLException) {
                runCatching { conn.rollback() }; throw e
            } catch (e: AppException) { runCatching { conn.rollback() }; throw e }
            catch (e: Throwable) { runCatching { conn.rollback() }; throw e }
        }
    }

    fun desactivar(usuarioId: Long, id: Long, req: DesactivarFuncionRequest): Pair<FuncionDto, Boolean> {
        validarUuid(req.uuidOperacion)
        DatabaseFactory.connection().use { conn ->
            conn.autoCommit = false
            try {
                replay(conn, req.uuidOperacion, usuarioId)?.let { conn.commit(); return it to true }
                val actual = funcionPorId(conn, id) ?: throw AppException(HttpStatusCode.NotFound, "FUNCION_NO_ENCONTRADA", "La funcion no existe", uuidOperacion = req.uuidOperacion)
                conn.prepareStatement("UPDATE funciones SET activa = FALSE, version = version + 1, actualizado_en = now() WHERE id = ?").use { ps ->
                    ps.setLong(1, id); ps.executeUpdate()
                }
                val actualizada = funcionPorId(conn, id)!!
                guardarOperacion(conn, req.uuidOperacion, "DESACTIVAR_FUNCION", id, usuarioId, json.encodeToString(FuncionDto.serializer(), actualizada))
                conn.commit()
                return actualizada to false
            } catch (e: java.sql.SQLException) { runCatching { conn.rollback() }; throw e }
            catch (e: AppException) { runCatching { conn.rollback() }; throw e }
            catch (e: Throwable) { runCatching { conn.rollback() }; throw e }
        }
    }

    fun eliminar(usuarioId: Long, id: Long, uuidOperacion: String): Pair<FuncionDto, Boolean> {
        validarUuid(uuidOperacion)
        DatabaseFactory.connection().use { conn ->
            conn.autoCommit = false
            try {
                replay(conn, uuidOperacion, usuarioId)?.let { conn.commit(); return it to true }
                val actual = funcionPorId(conn, id) ?: throw AppException(HttpStatusCode.NotFound, "FUNCION_NO_ENCONTRADA", "La funcion no existe", uuidOperacion = uuidOperacion)
                val conReservas = conn.prepareStatement("SELECT COUNT(*) FROM reservas WHERE funcion_id = ? AND estado NOT IN ('CANCELADA','ELIMINADA')").use { ps ->
                    ps.setLong(1, id); ps.executeQuery().use { rs -> if (rs.next()) rs.getLong(1) else 0L }
                }
                if (conReservas > 0) {
                    throw AppException(HttpStatusCode.Conflict, "FUNCION_CON_RESERVAS", "No se puede eliminar una funcion con reservas vigentes o confirmadas", uuidOperacion = uuidOperacion)
                }
                conn.prepareStatement("DELETE FROM funciones WHERE id = ?").use { ps -> ps.setLong(1, id); ps.executeUpdate() }
                guardarOperacion(conn, uuidOperacion, "ELIMINAR_FUNCION", id, usuarioId, json.encodeToString(FuncionDto.serializer(), actual))
                conn.commit()
                return actual to false
            } catch (e: java.sql.SQLException) { runCatching { conn.rollback() }; throw e }
            catch (e: AppException) { runCatching { conn.rollback() }; throw e }
            catch (e: Throwable) { runCatching { conn.rollback() }; throw e }
        }
    }

    private fun validarFechaHora(fecha: String, hora: String, uuid: String) {
        try {
            java.time.LocalDate.parse(fecha)
            java.time.LocalTime.parse(hora.let { if (it.length == 5) "$it:00" else it })
        } catch (e: Exception) {
            throw AppException(HttpStatusCode.UnprocessableEntity, "FECHA_HORA_INVALIDA", "fecha (YYYY-MM-DD) y hora (HH:MM) deben ser validas", uuidOperacion = uuid)
        }
    }
}
