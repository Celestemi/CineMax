package com.cinemax.api.plugins

import com.auth0.jwt.JWT
import com.cinemax.api.dto.ErrorResponse
import com.cinemax.api.security.Security
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.response.respond

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "cinemax"
            verifier(
                JWT.require(Security.algorithm)
                    .withAudience(Security.AUDIENCE)
                    .withIssuer(Security.ISSUER)
                    .build()
            )
            validate { credential ->
                if (credential.payload.subject != null) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ -> call.respond(HttpStatusCode.Unauthorized, ErrorResponse("NO_AUTENTICADO", "Token ausente o invalido")) }
        }
    }
}

fun ApplicationCall.jwtPrincipal(): JWTPrincipal =
    principal<JWTPrincipal>() ?: throw AppException(HttpStatusCode.Unauthorized, "NO_AUTENTICADO", "Token requerido")

fun ApplicationCall.usuarioId(): Long =
    jwtPrincipal().payload.subject?.toLongOrNull()
        ?: throw AppException(HttpStatusCode.Unauthorized, "NO_AUTENTICADO", "Token invalido")

fun ApplicationCall.rol(): String =
    jwtPrincipal().payload.getClaim("rol")?.asString()
        ?: throw AppException(HttpStatusCode.Unauthorized, "NO_AUTENTICADO", "Token invalido")

fun ApplicationCall.requireRol(rol: String) {
    if (this.rol() != rol) {
        throw AppException(HttpStatusCode.Forbidden, "PROHIBIDO", "Se requiere rol $rol para esta operacion")
    }
}
