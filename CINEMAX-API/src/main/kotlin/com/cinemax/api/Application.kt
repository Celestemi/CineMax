package com.cinemax.api

import com.cinemax.api.db.DatabaseFactory
import com.cinemax.api.db.Seed
import com.cinemax.api.plugins.configureCors
import com.cinemax.api.plugins.configureSecurity
import com.cinemax.api.plugins.configureSerialization
import com.cinemax.api.plugins.configureStatusPages
import com.cinemax.api.routes.authRoutes
import com.cinemax.api.routes.adminFuncionRoutes
import com.cinemax.api.routes.butacaRoutes
import com.cinemax.api.routes.catalogoRoutes
import com.cinemax.api.routes.funcionRoutes
import com.cinemax.api.routes.reservaRoutes
import com.cinemax.api.routes.salaRoutes
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    DatabaseFactory.init()
    Seed.run()
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureStatusPages()
    configureCors()
    configureSecurity()
    authRoutes()
    funcionRoutes()
    adminFuncionRoutes()
    salaRoutes()
    butacaRoutes()
    reservaRoutes()
    catalogoRoutes()
}
