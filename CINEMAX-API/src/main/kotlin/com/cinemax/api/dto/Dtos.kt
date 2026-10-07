package com.cinemax.api.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class LoginRequest(val usuario: String, val contrasena: String)

@Serializable
data class UsuarioDto(
    val id: Long, val usuario: String, val nombreCompleto: String,
    val email: String, val telefono: String?, val rol: String, val activo: Boolean
)

@Serializable
data class LoginResponse(val token: String, val tipo: String, val expiraEn: Long, val usuario: UsuarioDto)

@Serializable
data class FuncionDto(
    val id: Long, val peliculaId: Long, val sedeId: Long, val salaId: Long,
    val fecha: String, val hora: String, val precioEntrada: Double,
    val activa: Boolean, val version: Long, val actualizadoEn: String
)

@Serializable
data class SalaDto(
    val id: Long, val sedeId: Long, val nombre: String,
    val filas: Int, val columnas: Int, val capacidad: Int
)

@Serializable
data class SedeDto(val id: Long, val nombre: String, val distrito: String, val direccion: String)

@Serializable
data class PeliculaDto(
    val id: Long, val titulo: String, val genero: String, val clasificacionEdad: String,
    val duracionMinutos: Int, val sinopsis: String?, val posterUrl: String?,
    val trailerUrl: String?, val activo: Boolean, val version: Long
)

@Serializable
data class ButacaDto(val id: Long, val salaId: Long, val fila: String, val numero: Int, val codigo: String, val estado: String)

@Serializable
data class CrearReservaRequest(
    val uuidOperacion: String,
    val funcionId: Long,
    val butacasIds: List<Long>,
    val total: Double
)

@Serializable
data class ReservaDto(
    val id: Long, val codigo: String, val usuarioId: Long, val funcionId: Long,
    val fechaCompra: String, val total: Double, val estado: String,
    val uuidOperacion: String, val version: Long, val butacas: List<ButacaDto>
)

@Serializable
data class CrearFuncionRequest(
    val uuidOperacion: String,
    val peliculaId: Long,
    val sedeId: Long,
    val salaId: Long,
    val fecha: String,
    val hora: String,
    val precioEntrada: Double
)

@Serializable
data class EditarFuncionRequest(
    val uuidOperacion: String,
    val version: Long? = null,
    val peliculaId: Long? = null,
    val sedeId: Long? = null,
    val salaId: Long? = null,
    val fecha: String? = null,
    val hora: String? = null,
    val precioEntrada: Double? = null
)

@Serializable
data class DesactivarFuncionRequest(val uuidOperacion: String)

@Serializable
data class ErrorResponse(
    val codigo: String,
    val mensaje: String,
    val detalles: Map<String, JsonElement> = emptyMap(),
    val uuidOperacion: String? = null
)
