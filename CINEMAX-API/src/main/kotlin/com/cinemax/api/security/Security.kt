package com.cinemax.api.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.security.SecureRandom
import java.util.Base64
import java.util.Date

object Security {

    private val secret: String = System.getenv("JWT_SECRET")
        ?: Base64.getEncoder().encodeToString(ByteArray(32).also { SecureRandom().nextBytes(it) })
            .also { println("AVISO: JWT_SECRET no definido; usando uno aleatorio para desarrollo. Los tokens no sobreviven a reinicios.") }

    const val ISSUER = "cinemax-api"
    const val AUDIENCE = "cinemax"
    const val EXPIRA_SEG = 3600L

    val algorithm: Algorithm = Algorithm.HMAC256(secret)

    fun generarToken(id: Long, usuario: String, rol: String): String =
        JWT.create()
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withSubject(id.toString())
            .withClaim("usuario", usuario)
            .withClaim("rol", rol)
            .withExpiresAt(Date(System.currentTimeMillis() + EXPIRA_SEG * 1000))
            .sign(algorithm)
}
