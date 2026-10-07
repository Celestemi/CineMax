package com.cinemax.api.repositories

import com.cinemax.api.db.DatabaseFactory
import com.cinemax.api.dto.FuncionDto
import com.cinemax.api.dto.PeliculaDto
import com.cinemax.api.dto.SalaDto
import com.cinemax.api.dto.SedeDto
import java.sql.ResultSet

object FuncionRepository {

    fun todas(genero: String?, fecha: String?, sedeId: Long?, activa: Boolean?): List<FuncionDto> {
        val sb = StringBuilder(
            """
            SELECT f.id, f.pelicula_id, f.sede_id, f.sala_id, f.fecha, f.hora,
                   f.precio_entrada, f.activa, f.version, f.actualizado_en
            FROM funciones f
            JOIN peliculas p ON p.id = f.pelicula_id
            WHERE 1=1
            """
        )
        val params = mutableListOf<Any>()
        if (genero != null) { sb.append(" AND p.genero = ?"); params.add(genero) }
        if (fecha != null) { sb.append(" AND f.fecha = ?::date"); params.add(fecha) }
        if (sedeId != null) { sb.append(" AND f.sede_id = ?"); params.add(sedeId) }
        if (activa != null) { sb.append(" AND f.activa = ?"); params.add(activa) }
        sb.append(" ORDER BY f.fecha, f.hora")

        return DatabaseFactory.connection().use { conn ->
            conn.prepareStatement(sb.toString()).use { ps ->
                params.forEachIndexed { i, v ->
                    when (v) {
                        is String -> ps.setString(i + 1, v)
                        is Long -> ps.setLong(i + 1, v)
                        is Boolean -> ps.setBoolean(i + 1, v)
                    }
                }
                ps.executeQuery().use { rs ->
                    generateSequence { if (rs.next()) rs else null }.map { it.toFuncion() }.toList()
                }
            }
        }
    }

    fun porId(id: Long): FuncionDto? = DatabaseFactory.connection().use { conn ->
        conn.prepareStatement(
            "SELECT id, pelicula_id, sede_id, sala_id, fecha, hora, precio_entrada, activa, version, actualizado_en FROM funciones WHERE id = ?"
        ).use { ps ->
            ps.setLong(1, id)
            ps.executeQuery().use { rs -> if (rs.next()) rs.toFuncion() else null }
        }
    }

    private fun ResultSet.toFuncion() = FuncionDto(
        id = getLong("id"),
        peliculaId = getLong("pelicula_id"),
        sedeId = getLong("sede_id"),
        salaId = getLong("sala_id"),
        fecha = getDate("fecha").toString(),
        hora = getTime("hora").toString().substring(0, 5),
        precioEntrada = getBigDecimal("precio_entrada").toDouble(),
        activa = getBoolean("activa"),
        version = getLong("version"),
        actualizadoEn = getTimestamp("actualizado_en").toInstant().toString()
    )
}

object SalaRepository {
    fun todas(sedeId: Long?): List<SalaDto> = DatabaseFactory.connection().use { conn ->
        val sql = if (sedeId != null)
            "SELECT id, sede_id, nombre, filas, columnas, capacidad FROM salas WHERE sede_id = ? ORDER BY nombre"
        else
            "SELECT id, sede_id, nombre, filas, columnas, capacidad FROM salas ORDER BY nombre"
        conn.prepareStatement(sql).use { ps ->
            if (sedeId != null) ps.setLong(1, sedeId)
            ps.executeQuery().use { rs ->
                generateSequence { if (rs.next()) rs else null }.map { rs ->
                    SalaDto(rs.getLong("id"), rs.getLong("sede_id"), rs.getString("nombre"),
                        rs.getInt("filas"), rs.getInt("columnas"), rs.getInt("capacidad"))
                }.toList()
            }
        }
    }

    fun porId(id: Long): SalaDto? = DatabaseFactory.connection().use { conn ->
        conn.prepareStatement("SELECT id, sede_id, nombre, filas, columnas, capacidad FROM salas WHERE id = ?").use { ps ->
            ps.setLong(1, id)
            ps.executeQuery().use { rs -> if (rs.next()) SalaDto(rs.getLong("id"), rs.getLong("sede_id"), rs.getString("nombre"), rs.getInt("filas"), rs.getInt("columnas"), rs.getInt("capacidad")) else null }
        }
    }
}

object CatalogoRepository {
    fun peliculas(): List<PeliculaDto> = DatabaseFactory.connection().use { conn ->
        conn.prepareStatement("SELECT id, titulo, genero, clasificacion_edad, duracion_minutos, sinopsis, poster_url, trailer_url, activo, version FROM peliculas ORDER BY titulo").use { ps ->
            ps.executeQuery().use { rs ->
                generateSequence { if (rs.next()) rs else null }.map { rs ->
                    PeliculaDto(rs.getLong("id"), rs.getString("titulo"), rs.getString("genero"),
                        rs.getString("clasificacion_edad"), rs.getInt("duracion_minutos"), rs.getString("sinopsis"),
                        rs.getString("poster_url"), rs.getString("trailer_url"), rs.getBoolean("activo"), rs.getLong("version"))
                }.toList()
            }
        }
    }

    fun sedes(): List<SedeDto> = DatabaseFactory.connection().use { conn ->
        conn.prepareStatement("SELECT id, nombre, distrito, direccion FROM sedes ORDER BY nombre").use { ps ->
            ps.executeQuery().use { rs ->
                generateSequence { if (rs.next()) rs else null }.map { rs ->
                    SedeDto(rs.getLong("id"), rs.getString("nombre"), rs.getString("distrito"), rs.getString("direccion"))
                }.toList()
            }
        }
    }
}
