package com.cinemax.api.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

object DatabaseFactory {

    private lateinit var dataSource: HikariDataSource

    fun init() {
        val url = System.getenv("DATABASE_URL")
            ?: "jdbc:postgresql://127.0.0.1:5433/cinemax"
        val user = System.getenv("DATABASE_USER") ?: "postgres"
        val pass = System.getenv("DATABASE_PASSWORD") ?: ""

        val config = HikariConfig().apply {
            jdbcUrl = url
            username = user
            password = pass
            maximumPoolSize = 10
        }
        dataSource = HikariDataSource(config)
        createSchema()
    }

    fun connection(): Connection = dataSource.connection

    @Suppress("SqlSourceToSinkFlow")
    private fun createSchema() {
        val ddl = listOf(
            """
            CREATE TABLE IF NOT EXISTS usuarios (
                id BIGSERIAL PRIMARY KEY,
                usuario VARCHAR(50) UNIQUE NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                nombre_completo VARCHAR(120) NOT NULL,
                email VARCHAR(120) UNIQUE NOT NULL,
                telefono VARCHAR(20),
                rol VARCHAR(20) NOT NULL CHECK (rol IN ('CLIENTE','ADMINISTRADOR')),
                activo BOOLEAN NOT NULL DEFAULT TRUE,
                creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
                actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
                version BIGINT NOT NULL DEFAULT 1
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS peliculas (
                id BIGSERIAL PRIMARY KEY,
                titulo VARCHAR(150) NOT NULL,
                genero VARCHAR(50) NOT NULL,
                clasificacion_edad VARCHAR(10) NOT NULL,
                duracion_minutos INT NOT NULL,
                sinopsis TEXT,
                poster_url TEXT,
                trailer_url TEXT,
                activo BOOLEAN NOT NULL DEFAULT TRUE,
                creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
                actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
                version BIGINT NOT NULL DEFAULT 1
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS sedes (
                id BIGSERIAL PRIMARY KEY,
                nombre VARCHAR(100) UNIQUE NOT NULL,
                distrito VARCHAR(80) NOT NULL,
                direccion VARCHAR(200) NOT NULL,
                creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
                actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
                version BIGINT NOT NULL DEFAULT 1
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS salas (
                id BIGSERIAL PRIMARY KEY,
                sede_id BIGINT NOT NULL REFERENCES sedes(id) ON DELETE CASCADE,
                nombre VARCHAR(50) NOT NULL,
                filas INT NOT NULL,
                columnas INT NOT NULL,
                capacidad INT NOT NULL,
                creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
                actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
                version BIGINT NOT NULL DEFAULT 1,
                UNIQUE (sede_id, nombre)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS butacas (
                id BIGSERIAL PRIMARY KEY,
                sala_id BIGINT NOT NULL REFERENCES salas(id) ON DELETE CASCADE,
                fila CHAR(1) NOT NULL,
                numero INT NOT NULL,
                creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
                actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
                version BIGINT NOT NULL DEFAULT 1,
                UNIQUE (sala_id, fila, numero)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS funciones (
                id BIGSERIAL PRIMARY KEY,
                pelicula_id BIGINT NOT NULL REFERENCES peliculas(id),
                sede_id BIGINT NOT NULL REFERENCES sedes(id),
                sala_id BIGINT NOT NULL REFERENCES salas(id),
                fecha DATE NOT NULL,
                hora TIME NOT NULL,
                precio_entrada NUMERIC(10,2) NOT NULL,
                activa BOOLEAN NOT NULL DEFAULT TRUE,
                creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
                actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
                version BIGINT NOT NULL DEFAULT 1
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS reservas (
                id BIGSERIAL PRIMARY KEY,
                codigo VARCHAR(30) UNIQUE NOT NULL,
                usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
                funcion_id BIGINT NOT NULL REFERENCES funciones(id),
                fecha_compra TIMESTAMPTZ NOT NULL DEFAULT now(),
                total NUMERIC(10,2) NOT NULL,
                estado VARCHAR(25) NOT NULL DEFAULT 'CONFIRMADA',
                uuid_operacion UUID UNIQUE NOT NULL,
                version BIGINT NOT NULL DEFAULT 1,
                actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now()
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS butaca_reserva (
                reserva_id BIGINT NOT NULL REFERENCES reservas(id) ON DELETE CASCADE,
                funcion_id BIGINT NOT NULL REFERENCES funciones(id) ON DELETE CASCADE,
                butaca_id BIGINT NOT NULL REFERENCES butacas(id) ON DELETE CASCADE,
                PRIMARY KEY (reserva_id, funcion_id, butaca_id)
            )
            """,
            """
            CREATE UNIQUE INDEX IF NOT EXISTS ux_butaca_reserva_funcion_butaca
                ON butaca_reserva (funcion_id, butaca_id)
            """,
            """
            CREATE TABLE IF NOT EXISTS operaciones_procesadas (
                uuid_operacion UUID PRIMARY KEY,
                tipo VARCHAR(50) NOT NULL,
                id_recurso BIGINT,
                usuario_id BIGINT REFERENCES usuarios(id),
                resultado_json JSONB NOT NULL,
                procesado_en TIMESTAMPTZ NOT NULL DEFAULT now()
            )
            """,
            "ALTER TABLE operaciones_procesadas ADD COLUMN IF NOT EXISTS usuario_id BIGINT REFERENCES usuarios(id)",
            "ALTER TABLE sedes ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 1",
            "ALTER TABLE sedes ADD COLUMN IF NOT EXISTS creado_en TIMESTAMPTZ NOT NULL DEFAULT now()",
            "ALTER TABLE salas ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 1",
            "ALTER TABLE salas ADD COLUMN IF NOT EXISTS creado_en TIMESTAMPTZ NOT NULL DEFAULT now()",
            "ALTER TABLE butacas ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 1",
            "ALTER TABLE butacas ADD COLUMN IF NOT EXISTS creado_en TIMESTAMPTZ NOT NULL DEFAULT now()",
            "ALTER TABLE peliculas ADD COLUMN IF NOT EXISTS creado_en TIMESTAMPTZ NOT NULL DEFAULT now()",
            "ALTER TABLE funciones ADD COLUMN IF NOT EXISTS creado_en TIMESTAMPTZ NOT NULL DEFAULT now()",
            "CREATE INDEX IF NOT EXISTS ix_funciones_fecha ON funciones (fecha, activa)",
            "CREATE INDEX IF NOT EXISTS ix_funciones_sede ON funciones (sede_id)",
            "CREATE INDEX IF NOT EXISTS ix_butaca_reserva_butaca ON butaca_reserva (butaca_id)",
            "CREATE INDEX IF NOT EXISTS ix_reservas_usuario ON reservas (usuario_id)",
            "CREATE INDEX IF NOT EXISTS ix_reservas_funcion ON reservas (funcion_id)"
        )
        connection().use { conn ->
            conn.autoCommit = true
            ddl.forEach { sql ->
                conn.createStatement().use { stmt -> stmt.execute(sql) }
            }
        }
    }
}
