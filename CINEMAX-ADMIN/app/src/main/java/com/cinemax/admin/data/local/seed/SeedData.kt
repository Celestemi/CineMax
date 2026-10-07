package com.cinemax.admin.data.local.seed

import com.cinemax.admin.data.local.entity.UsuarioEntity
import com.cinemax.admin.model.RolAdmin

/**
 * FASE 8 - Catalogo minimo de datos iniciales de CINEMAX-ADMIN.
 *
 * Solo contiene cuentas de administracion. La base pertenece al panel de
 * administrador, asi que todos los usuarios sembrados tienen rol
 * [RolAdmin.ADMINISTRADOR]; el resto de tablas (funciones, salas, ocupacion)
 * llegaran en la FASE 9.
 *
 * SEGURIDAD (limitacion academica): la contrasena se guarda en TEXTO PLANO.
 * Es una decision del coursework, no una practica de produccion.
 */
object SeedData {

    /** Contrasena de las cuentas de demostracion. */
    const val CONTRASENA_DEMO = "1234"

    /** Marca temporal fija para que el seed sea determinista en las pruebas. */
    const val CREADO_EN_SEED = 1_700_000_000_000L

    /**
     * Cuenta administradora de prueba exigida por la fase: `admin` / `1234`.
     * Se anade una segunda cuenta (`supervisor`) para poder probar el panel con
     * mas de un administrador sin tocar el codigo.
     */
    fun usuarios(): List<UsuarioEntity> = listOf(
        UsuarioEntity(
            usuario = "admin",
            contrasena = CONTRASENA_DEMO,
            nombreCompleto = "Administrador General",
            email = "admin@cinemax.pe",
            telefono = "999000111",
            rol = RolAdmin.ADMINISTRADOR.name,
            activo = true,
            creadoEn = CREADO_EN_SEED
        ),
        UsuarioEntity(
            usuario = "supervisor",
            contrasena = CONTRASENA_DEMO,
            nombreCompleto = "Supervisor de Sala",
            email = "supervisor@cinemax.pe",
            telefono = "999000222",
            rol = RolAdmin.ADMINISTRADOR.name,
            activo = true,
            creadoEn = CREADO_EN_SEED
        )
    )
}
