package com.cinemax.api.repositories

import com.cinemax.api.db.DatabaseFactory
import com.cinemax.api.dto.ButacaDto

object ButacaRepository {

    /** Butacas de la sala de la función, con estado DISPONIBLE/OCUPADA para esa función. */
    fun porFuncion(funcionId: Long): List<ButacaDto> = DatabaseFactory.connection().use { conn ->
        conn.prepareStatement(
            """
            SELECT b.id, b.sala_id, b.fila, b.numero,
                   CASE WHEN br.butaca_id IS NULL THEN 'DISPONIBLE' ELSE 'OCUPADA' END AS estado
            FROM funciones f
            JOIN butacas b ON b.sala_id = f.sala_id
            LEFT JOIN butaca_reserva br
                   ON br.butaca_id = b.id AND br.funcion_id = f.id
            WHERE f.id = ?
            ORDER BY b.fila, b.numero
            """
        ).use { ps ->
            ps.setLong(1, funcionId)
            ps.executeQuery().use { rs ->
                generateSequence { if (rs.next()) rs else null }.map { rs ->
                    val fila = rs.getString("fila")
                    val numero = rs.getInt("numero")
                    ButacaDto(rs.getLong("id"), rs.getLong("sala_id"), fila, numero, "$fila$numero", rs.getString("estado"))
                }.toList()
            }
        }
    }

    /** Butacas ocupadas en la función (para mensajes de conflicto). */
    fun ocupadasEn(funcionId: Long, butacasIds: List<Long>): List<String> {
        if (butacasIds.isEmpty()) return emptyList()
        return DatabaseFactory.connection().use { conn ->
            val placeholders = butacasIds.joinToString(",") { "?" }
            conn.prepareStatement(
                """
                SELECT b.fila || b.numero AS codigo
                FROM butaca_reserva br
                JOIN butacas b ON b.id = br.butaca_id
                WHERE br.funcion_id = ? AND br.butaca_id IN ($placeholders)
                """
            ).use { ps ->
                ps.setLong(1, funcionId)
                butacasIds.forEachIndexed { i, id -> ps.setLong(i + 2, id) }
                ps.executeQuery().use { rs ->
                    generateSequence { if (rs.next()) rs else null }.map { it.getString("codigo") }.toList()
                }
            }
        }
    }

    /** Butacas por id y su sala (para validar pertenencia). */
    fun porIds(ids: List<Long>): List<ButacaDto> {
        if (ids.isEmpty()) return emptyList()
        return DatabaseFactory.connection().use { conn ->
            val placeholders = ids.joinToString(",") { "?" }
            conn.prepareStatement("SELECT id, sala_id, fila, numero FROM butacas WHERE id IN ($placeholders)").use { ps ->
                ids.forEachIndexed { i, id -> ps.setLong(i + 1, id) }
                ps.executeQuery().use { rs ->
                    generateSequence { if (rs.next()) rs else null }.map { rs ->
                        val f = rs.getString("fila"); val n = rs.getInt("numero")
                        ButacaDto(rs.getLong("id"), rs.getLong("sala_id"), f, n, "$f$n", "DISPONIBLE")
                    }.toList()
                }
            }
        }
    }
}
