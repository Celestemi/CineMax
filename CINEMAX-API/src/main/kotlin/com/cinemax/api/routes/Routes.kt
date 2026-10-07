package com.cinemax.api.routes

import com.cinemax.api.dto.CrearFuncionRequest
import com.cinemax.api.dto.CrearReservaRequest
import com.cinemax.api.dto.DesactivarFuncionRequest
import com.cinemax.api.dto.EditarFuncionRequest
import com.cinemax.api.dto.LoginRequest
import com.cinemax.api.dto.LoginResponse
import com.cinemax.api.plugins.AppException
import com.cinemax.api.plugins.requireRol
import com.cinemax.api.plugins.rol
import com.cinemax.api.plugins.usuarioId
import com.cinemax.api.repositories.AuthRepository
import com.cinemax.api.repositories.ButacaRepository
import com.cinemax.api.repositories.CatalogoRepository
import com.cinemax.api.repositories.FuncionAdminRepository
import com.cinemax.api.repositories.FuncionRepository
import com.cinemax.api.repositories.ReservaRepository
import com.cinemax.api.repositories.SalaRepository
import com.cinemax.api.security.Security
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.authRoutes() {
    routing {
        route("/api/auth") {
            post("/login") {
                val request = call.receive<LoginRequest>()
                val usuario = AuthRepository.login(request)
                val token = Security.generarToken(usuario.id, usuario.usuario, usuario.rol)
                call.respond(LoginResponse(token, "Bearer", Security.EXPIRA_SEG, usuario))
            }
        }
    }
}

fun Application.funcionRoutes() {
    routing {
        route("/api/funciones") {
            get {
                val genero = call.request.queryParameters["genero"]
                val fecha = call.request.queryParameters["fecha"]
                val sedeId = call.request.queryParameters["sedeId"]?.toLongOrNull()
                val activa = call.request.queryParameters["activa"]?.toBooleanStrictOrNull()
                call.respond(FuncionRepository.todas(genero, fecha, sedeId, activa))
            }
            get("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: throw AppException(HttpStatusCode.BadRequest, "ID_INVALIDO", "El id debe ser numerico")
                val funcion = FuncionRepository.porId(id)
                    ?: throw AppException(HttpStatusCode.NotFound, "FUNCION_NO_ENCONTRADA", "La funcion no existe")
                call.respond(funcion)
            }
        }
    }
}

fun Application.salaRoutes() {
    routing {
        route("/api/salas") {
            get {
                val sedeId = call.request.queryParameters["sedeId"]?.toLongOrNull()
                call.respond(SalaRepository.todas(sedeId))
            }
            get("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: throw AppException(HttpStatusCode.BadRequest, "ID_INVALIDO", "El id debe ser numerico")
                val sala = SalaRepository.porId(id)
                    ?: throw AppException(HttpStatusCode.NotFound, "SALA_NO_ENCONTRADA", "La sala no existe")
                call.respond(sala)
            }
        }
    }
}

fun Application.butacaRoutes() {
    routing {
        get("/api/butacas/{funcionId}") {
            val funcionId = call.parameters["funcionId"]?.toLongOrNull()
                ?: throw AppException(HttpStatusCode.BadRequest, "ID_INVALIDO", "El funcionId debe ser numerico")
            FuncionRepository.porId(funcionId)
                ?: throw AppException(HttpStatusCode.NotFound, "FUNCION_NO_ENCONTRADA", "La funcion no existe")
            call.respond(ButacaRepository.porFuncion(funcionId))
        }
    }
}

fun Application.catalogoRoutes() {
    routing {
        get("/api/peliculas") { call.respond(CatalogoRepository.peliculas()) }
        get("/api/sedes") { call.respond(CatalogoRepository.sedes()) }
    }
}

fun Application.adminFuncionRoutes() {
    routing {
        authenticate("auth-jwt") {
            route("/api/funciones") {
                post {
                    call.requireRol("ADMINISTRADOR")
                    val request = call.receive<CrearFuncionRequest>()
                    val (funcion, idempotente) = FuncionAdminRepository.crear(call.usuarioId(), request)
                    if (idempotente) call.response.header("X-Idempotente", "true")
                    call.respond(if (idempotente) HttpStatusCode.OK else HttpStatusCode.Created, funcion)
                }
                put("/{id}") {
                    call.requireRol("ADMINISTRADOR")
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: throw AppException(HttpStatusCode.BadRequest, "ID_INVALIDO", "El id debe ser numerico")
                    val request = call.receive<EditarFuncionRequest>()
                    val (funcion, idempotente) = FuncionAdminRepository.editar(call.usuarioId(), id, request)
                    if (idempotente) call.response.header("X-Idempotente", "true")
                    call.respond(if (idempotente) HttpStatusCode.OK else HttpStatusCode.OK, funcion)
                }
                patch("/{id}/desactivar") {
                    call.requireRol("ADMINISTRADOR")
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: throw AppException(HttpStatusCode.BadRequest, "ID_INVALIDO", "El id debe ser numerico")
                    val request = call.receive<DesactivarFuncionRequest>()
                    val (funcion, idempotente) = FuncionAdminRepository.desactivar(call.usuarioId(), id, request)
                    if (idempotente) call.response.header("X-Idempotente", "true")
                    call.respond(HttpStatusCode.OK, funcion)
                }
                delete("/{id}") {
                    call.requireRol("ADMINISTRADOR")
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: throw AppException(HttpStatusCode.BadRequest, "ID_INVALIDO", "El id debe ser numerico")
                    val uuid = call.request.queryParameters["uuidOperacion"]
                        ?: throw AppException(HttpStatusCode.BadRequest, "UUID_INVALIDO", "uuidOperacion es obligatorio")
                    val (funcion, idempotente) = FuncionAdminRepository.eliminar(call.usuarioId(), id, uuid)
                    if (idempotente) call.response.header("X-Idempotente", "true")
                    call.respond(HttpStatusCode.OK, funcion)
                }
            }
        }
    }
}

fun Application.reservaRoutes() {
    routing {
        authenticate("auth-jwt") {
            route("/api/reservas") {
                post {
                    call.requireRol("CLIENTE")
                    val request = call.receive<CrearReservaRequest>()
                    val usuarioId = call.usuarioId()
                    val (reserva, idempotente) = ReservaRepository.crear(usuarioId, request)
                    if (idempotente) {
                        call.response.header("X-Idempotente", "true")
                    }
                    call.respond(if (idempotente) HttpStatusCode.OK else HttpStatusCode.Created, reserva)
                }
                get {
                    call.respond(ReservaRepository.listar(call.usuarioId(), call.rol()))
                }
                get("/{id}") {
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: throw AppException(HttpStatusCode.BadRequest, "ID_INVALIDO", "El id debe ser numerico")
                    val reserva = ReservaRepository.porId(id, call.usuarioId(), call.rol())
                        ?: throw AppException(HttpStatusCode.NotFound, "RESERVA_NO_ENCONTRADA", "La reserva no existe")
                    call.respond(reserva)
                }
            }
        }
    }
}
