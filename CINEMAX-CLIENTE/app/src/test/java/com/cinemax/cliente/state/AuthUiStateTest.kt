package com.cinemax.cliente.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FASE 4 - Estados de sesion y de UI.
 *
 * Pruebas JVM puras: no dependen de Room, solo de la traduccion de rol y de la
 * politica de acceso de la aplicacion Cliente.
 */
class AuthUiStateTest {

    private fun sesion(rol: RolCineMax, activo: Boolean = true) = UsuarioSesion(
        id = 1,
        usuario = "carla",
        nombreCompleto = "Carla Ramírez",
        email = "carla@cinemax.pe",
        telefono = "999111222",
        rol = rol,
        activo = activo,
        inicioSesionEn = 1_700_000_000_000L
    )

    @Test
    fun `el rol del dominio se lee del texto persistido`() {
        assertEquals(RolCineMax.CLIENTE, RolCineMax.desde("CLIENTE"))
        assertEquals(RolCineMax.ADMINISTRADOR, RolCineMax.desde("ADMINISTRADOR"))
        assertEquals(RolCineMax.CLIENTE, RolCineMax.desde("cliente"))
        assertEquals(RolCineMax.CLIENTE, RolCineMax.desde(" CLIENTE "))
        assertNull(RolCineMax.desde("SUPERVISOR"))
        assertNull(RolCineMax.desde(""))
    }

    @Test
    fun `D - solo el rol CLIENTE cumple el rol permitido en la app Cliente`() {
        assertEquals(RolCineMax.CLIENTE, UsuarioSesion.ROL_PERMITIDO_EN_CLIENTE)
        assertTrue(sesion(RolCineMax.CLIENTE).cumpleRolPermitido())
        assertFalse(sesion(RolCineMax.ADMINISTRADOR).cumpleRolPermitido())
    }

    @Test
    fun `F - un usuario inactivo no cumple aunque sea CLIENTE`() {
        assertFalse(sesion(RolCineMax.CLIENTE, activo = false).cumpleRolPermitido())
    }

    @Test
    fun `cada causa de rechazo tiene su propio estado`() {
        assertFalse(AuthUiState.Inicial.esAccesoDenegado)
        assertFalse(AuthUiState.Cargando.esAccesoDenegado)
        assertFalse(AuthUiState.RegistroExitoso("ok").esAccesoDenegado)
        assertFalse(AuthUiState.RegistroExitoso("ok").esFalloDeRegistro)
        assertFalse(AuthUiState.RegistroExitoso("ok").esAccesoPermitido)

        assertTrue(AuthUiState.LoginIncorrecto("x").esAccesoDenegado)
        assertTrue(AuthUiState.UsuarioInactivo("x").esAccesoDenegado)
        assertTrue(AuthUiState.RolNoPermitido("x").esAccesoDenegado)

        assertTrue(AuthUiState.UsuarioDuplicado("x").esFalloDeRegistro)
        assertTrue(AuthUiState.ErrorRegistro("x").esFalloDeRegistro)
        assertTrue(AuthUiState.ValidacionInvalida("x").esFalloDeRegistro)

        val autenticado = AuthUiState.Autenticado(sesion(RolCineMax.CLIENTE))
        assertTrue(autenticado.esAccesoPermitido)
        assertFalse(autenticado.esAccesoDenegado)
        assertFalse(AuthUiState.LoginIncorrecto("x").esAccesoPermitido)
        assertFalse(AuthUiState.UsuarioInactivo("x").esAccesoPermitido)
        assertFalse(AuthUiState.RolNoPermitido("x").esAccesoPermitido)
        assertFalse(AuthUiState.UsuarioDuplicado("x").esAccesoPermitido)
    }

    @Test
    fun `la sesion transporta los datos visibles en Home`() {
        val usuario = sesion(RolCineMax.CLIENTE)
        assertEquals("carla", usuario.usuario)
        assertEquals("Carla Ramírez", usuario.nombreCompleto)
        assertEquals("carla@cinemax.pe", usuario.email)
        assertEquals("999111222", usuario.telefono)
        assertTrue(usuario.activo)
    }
}
