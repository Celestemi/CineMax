package com.cinemax.cliente.viewmodel

import com.cinemax.cliente.state.RolCineMax
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FASE 4 - Validaciones del formulario de registro (casos I y J).
 *
 * Pruebas JVM puras: no dependen de Room ni de Android, porque
 * [ValidacionRegistro] no los necesita.
 */
class ValidacionRegistroTest {

    private fun validar(
        usuario: String = "carla",
        contrasena: String = "1234",
        nombreCompleto: String = "Carla Ramírez",
        email: String = "carla@cinemax.pe",
        telefono: String = "999111222"
    ) = ValidacionRegistro.validar(usuario, contrasena, nombreCompleto, email, telefono)

    @Test
    fun `un formulario completo no tiene errores`() {
        assertFalse(validar().hayAlguno)
    }

    @Test
    fun `J - todos los campos vacios se rechazan`() {
        val errores = validar(
            usuario = "",
            contrasena = "",
            nombreCompleto = "",
            email = "",
            telefono = ""
        )

        assertTrue(errores.hayAlguno)
        assertEquals(ValidacionRegistro.MENSAJE_USUARIO_VACIO, errores.usuario)
        assertEquals(ValidacionRegistro.MENSAJE_CONTRASENA_VACIA, errores.contrasena)
        assertEquals(ValidacionRegistro.MENSAJE_NOMBRE_VACIO, errores.nombreCompleto)
        assertEquals(ValidacionRegistro.MENSAJE_EMAIL_VACIO, errores.email)
        assertEquals(ValidacionRegistro.MENSAJE_TELEFONO_VACIO, errores.telefono)
    }

    @Test
    fun `J - solo espacios tambien cuentan como vacio`() {
        val errores = validar(
            usuario = "   ",
            contrasena = "     ",
            nombreCompleto = "   ",
            email = "  ",
            telefono = "   "
        )
        assertTrue(errores.hayAlguno)
        assertEquals(ValidacionRegistro.MENSAJE_USUARIO_VACIO, errores.usuario)
    }

    @Test
    fun `I - la contrasena vacia se rechaza`() {
        val errores = validar(contrasena = "")

        assertEquals(ValidacionRegistro.MENSAJE_CONTRASENA_VACIA, errores.contrasena)
        assertNull("Solo la contrasena falla", errores.usuario)
    }

    @Test
    fun `la contrasena demasiado corta se rechaza`() {
        assertEquals(
            ValidacionRegistro.MENSAJE_CONTRASENA_CORTA,
            validar(contrasena = "1a").contrasena
        )
    }

    @Test
    fun `el usuario demasiado corto o con simbolos se rechaza`() {
        assertEquals(ValidacionRegistro.MENSAJE_USUARIO_CORTO, validar(usuario = "ab").usuario)
        assertEquals(
            ValidacionRegistro.MENSAJE_USUARIO_CARACTERES,
            validar(usuario = "carla ramirez").usuario
        )
    }

    @Test
    fun `el email debe tener un formato razonable`() {
        listOf("carla", "carla@", "@cinemax.pe", "carla@cinemax", "carla@cinemax.", "carla @x.pe")
            .forEach { email ->
                assertEquals(
                    "Deberia rechazar $email",
                    ValidacionRegistro.MENSAJE_EMAIL_FORMATO,
                    validar(email = email).email
                )
            }

        listOf("carla@cinemax.pe", "carla.ramirez+cine@cinemax.com.pe", "a_b-c@x.co")
            .forEach { email ->
                assertNull("Deberia aceptar $email", validar(email = email).email)
            }
    }

    @Test
    fun `el telefono debe tener entre 9 y 15 digitos`() {
        listOf("123", "1234567890123456", "999-111-222", "abcde12345").forEach { telefono ->
            assertEquals(
                "Deberia rechazar $telefono",
                ValidacionRegistro.MENSAJE_TELEFONO_FORMATO,
                validar(telefono = telefono).telefono
            )
        }
        assertNull(validar(telefono = "999111222").telefono)
    }

    @Test
    fun `el login solo exige que haya algo escrito`() {
        assertEquals("Escribe tu usuario", ValidacionRegistro.validarLogin("", "1234"))
        assertEquals("Escribe tu contrasena", ValidacionRegistro.validarLogin("carla", ""))
        assertEquals("Escribe tu usuario", ValidacionRegistro.validarLogin("   ", "1234"))
        assertNull(ValidacionRegistro.validarLogin("carla", "1234"))
    }

    @Test
    fun `el rol permitido en la app Cliente es solo CLIENTE`() {
        assertEquals(RolCineMax.CLIENTE, RolCineMax.CLIENTE)
        assertEquals(RolCineMax.ADMINISTRADOR, RolCineMax.ADMINISTRADOR)
        assertEquals(2, RolCineMax.entries.size)
    }
}
