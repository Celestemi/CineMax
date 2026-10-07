package com.cinemax.api.db

import at.favre.lib.crypto.bcrypt.BCrypt
import java.time.LocalDate
import java.time.LocalTime

object Seed {

    private const val PASSWORD = "1234"

    fun run() {
        val hash = BCrypt.withDefaults().hashToString(12, PASSWORD.toCharArray())
        DatabaseFactory.connection().use { conn ->
            conn.autoCommit = false
            try {
                val hayUsuarios = conn.prepareStatement("SELECT COUNT(*) FROM usuarios").use {
                    it.executeQuery().use { rs -> if (rs.next()) rs.getInt(1) else 0 }
                }
                if (hayUsuarios > 0) {
                    conn.rollback()
                    return
                }

                fun insertUsuario(usuario: String, nombre: String, email: String, rol: String) {
                    conn.prepareStatement(
                        "INSERT INTO usuarios (usuario, password_hash, nombre_completo, email, telefono, rol) VALUES (?,?,?,?,?,?)"
                    ).use {
                        it.setString(1, usuario); it.setString(2, hash); it.setString(3, nombre)
                        it.setString(4, email); it.setString(5, "999000111"); it.setString(6, rol)
                        it.executeUpdate()
                    }
                }
                insertUsuario("admin", "Administrador General", "admin@cinemax.pe", "ADMINISTRADOR")
                insertUsuario("supervisor", "Supervisor de Sala", "supervisor@cinemax.pe", "ADMINISTRADOR")
                insertUsuario("cliente", "Cliente Demo", "cliente@cinemax.pe", "CLIENTE")
                insertUsuario("cliente2", "Cliente Demo 2", "cliente2@cinemax.pe", "CLIENTE")

                val peliculas = listOf(
                    Triple("Cerro Veloz", "ACCION", "14+") to 128,
                    Triple("Horizonte Cero", "CIENCIA_FICCION", "14+") to 145,
                    Triple("La Vecina del 9", "COMEDIA", "APT") to 96,
                    Triple("Cacería Nocturna", "TERROR", "18+") to 104,
                    Triple("Alma de Acero", "DRAMA", "14+") to 132,
                    Triple("Robin y el Bosque Mágico", "ANIMACION", "APT") to 92,
                    Triple("Tensión Máxima", "SUSPENSO", "16+") to 110,
                    Triple("Oro Andino", "AVENTURA", "14+") to 118,
                    Triple("El Último Faro", "DRAMA", "APT") to 105,
                    Triple("Código Pacífico", "ACCION", "16+") to 122,
                    Triple("Risas al Atardecer", "COMEDIA", "APT") to 99,
                    Triple("Sombras del Callao", "TERROR", "18+") to 108
                )
                val peliculaIds = peliculas.mapIndexed { idx, (t, dur) ->
                    conn.prepareStatement(
                        "INSERT INTO peliculas (titulo, genero, clasificacion_edad, duracion_minutos, sinopsis, poster_url, trailer_url) VALUES (?,?,?,?,?,?,?) RETURNING id"
                    ).use {
                        it.setString(1, t.first); it.setString(2, t.second); it.setString(3, t.third)
                        it.setInt(4, dur); it.setString(5, "Sinopsis de ${t.first}")
                        it.setString(6, "https://picsum.photos/seed/cinemax-$idx/400/600")
                        it.setString(7, "")
                        it.executeQuery().use { rs -> if (rs.next()) rs.getLong(1) else error("sin id") }
                    }
                }

                val sedes = listOf(
                    Triple("CineMax Mall del Sur", "Surco", "Av. Caminos del Inca 123"),
                    Triple("CineMax Plaza Norte", "Independencia", "Av. Túpac Amaru 456"),
                    Triple("CineMax San Isidro", "San Isidro", "Av. Conquistadores 789"),
                    Triple("CineMax Miraflores", "Miraflores", "Av. Larco 321")
                )
                val sedeIds = sedes.map { (n, d, dir) ->
                    conn.prepareStatement("INSERT INTO sedes (nombre, distrito, direccion) VALUES (?,?,?) RETURNING id").use {
                        it.setString(1, n); it.setString(2, d); it.setString(3, dir)
                        it.executeQuery().use { rs -> if (rs.next()) rs.getLong(1) else error("sin id") }
                    }
                }

                val salaIds = mutableListOf<Pair<Long, Long>>() // idSala to idSede
                sedeIds.forEachIndexed { i, sedeId ->
                    for (s in 1..2) {
                        val salaId = conn.prepareStatement(
                            "INSERT INTO salas (sede_id, nombre, filas, columnas, capacidad) VALUES (?,?,?,?,?) RETURNING id"
                        ).use {
                            it.setLong(1, sedeId); it.setString(2, "Sala $s")
                            it.setInt(3, 8); it.setInt(4, 10); it.setInt(5, 80)
                            it.executeQuery().use { rs -> if (rs.next()) rs.getLong(1) else error("sin id") }
                        }
                        salaIds += salaId to sedeId
                        for (f in 0 until 8) {
                            for (n in 1..10) {
                                conn.prepareStatement("INSERT INTO butacas (sala_id, fila, numero) VALUES (?,?,?)").use {
                                    it.setLong(1, salaId); it.setString(2, ('A'.code + f).toChar().toString())
                                    it.setInt(3, n); it.executeUpdate()
                                }
                            }
                        }
                    }
                }

                val horas = listOf("10:00", "13:30", "16:00", "19:30", "22:00")
                for (i in 0 until 34) {
                    val peliculaId = peliculaIds[i % peliculaIds.size]
                    val sedeId = sedeIds[i % sedeIds.size]
                    val salaId = salaIds.filter { it.second == sedeId }[i % 2].first
                    val fecha = LocalDate.now().plusDays((i % 6) + 1L)
                    val hora = LocalTime.parse(horas[i % horas.size])
                    val precio = 18.0 + (i % 3) * 4.0
                    conn.prepareStatement(
                        "INSERT INTO funciones (pelicula_id, sede_id, sala_id, fecha, hora, precio_entrada) VALUES (?,?,?,?,?,?)"
                    ).use {
                        it.setLong(1, peliculaId); it.setLong(2, sedeId); it.setLong(3, salaId)
                        it.setObject(4, fecha); it.setObject(5, hora); it.setBigDecimal(6, precio.toBigDecimal())
                        it.executeUpdate()
                    }
                }

                conn.commit()
            } catch (t: Throwable) {
                conn.rollback()
                throw t
            }
        }
    }
}
