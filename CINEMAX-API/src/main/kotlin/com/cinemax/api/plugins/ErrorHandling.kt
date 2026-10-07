package com.cinemax.api.plugins

import com.cinemax.api.dto.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

class AppException(
    val httpStatus: HttpStatusCode,
    val codigo: String,
    override val message: String,
    val detalles: Map<String, kotlinx.serialization.json.JsonElement> = emptyMap(),
    val uuidOperacion: String? = null
) : RuntimeException(message)

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<AppException> { call, cause ->
            call.respond(
                cause.httpStatus,
                ErrorResponse(cause.codigo, cause.message ?: "Error", cause.detalles, cause.uuidOperacion)
            )
        }
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", "Solicitud invalida"))
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Error no controlado", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("ERROR_INTERNO", "Error interno"))
        }
    }
}
