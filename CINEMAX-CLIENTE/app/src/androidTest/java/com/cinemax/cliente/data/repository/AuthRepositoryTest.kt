package com.cinemax.cliente.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.UsuarioEntity
import com.cinemax.cliente.data.seed.SeedData
import com.cinemax.cliente.state.RolCineMax
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * FASE 4 - Autenticacion real contra Room.
 *
 * Usa una base SQLite en memoria de verdad (`Room.inMemoryDatabaseBuilder`), con
 * la misma clase [CineMaxClienteDatabase] y el mismo `UsuarioDao` que usa la app,
 * de modo que los casos A-H se comprueban sobre el mismo esquema y los mismos
 * indices UNIQUE.
 */
@RunWith(AndroidJUnit4::class)
class AuthRepositoryTest {

    private lateinit var database: CineMaxClienteDatabase
    private lateinit var repositorio: AuthRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, CineMaxClienteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repositorio = AuthRepository(database.usuarioDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun cliente(
        usuario: String,
        contrasena: String = "1234",
        rol: String = RolCineMax.CLIENTE.name,
        activo: Boolean = true
    ) = UsuarioEntity(
        usuario = usuario,
        contrasena = contrasena,
        nombreCompleto = "Usuario $usuario",
        email = "$usuario@cinemax.pe",
        telefono = "999111222",
        rol = rol,
        activo = activo,
        creadoEn = 1_700_000_000_000L
    )

    // ------------------------------------------------------------------
    // A) Registro de usuario nuevo
    // ------------------------------------------------------------------

    @Test
    fun a_registraUnUsuarioNuevoActivoComoCliente() = runBlocking<Unit> {
        val resultado = repositorio.registrar(
            usuario = "carla",
            contrasena = "clave123",
            nombreCompleto = "Carla Ramírez",
            email = "Carla.Ramirez@Cinemax.PE",
            telefono = " 999111222 "
        )

        assertTrue(resultado is ResultadoRegistro.Exito)
        assertEquals(1, repositorio.contar())

        val guardado = repositorio.buscarPorUsuario("carla")
        assertNotNull(guardado)
        requireNotNull(guardado)
        assertEquals("Carla Ramírez", guardado.nombreCompleto)
        assertEquals("carla.ramirez@cinemax.pe", guardado.email)
        assertEquals("999111222", guardado.telefono)
        assertEquals(RolCineMax.CLIENTE.name, guardado.rol)
        assertTrue(guardado.activo)
    }

    @Test
    fun a_elRolYElActivoNoDependenDeQuienRegistra() = runBlocking<Unit> {
        // El registro no acepta rol ni activo como parametros: siempre CLIENTE + true.
        repositorio.registrar("carla", "clave123", "Carla Ramírez", "c@cinemax.pe", "999111222")
        val guardado = requireNotNull(repositorio.buscarPorUsuario("carla"))
        assertEquals("CLIENTE", guardado.rol)
        assertTrue(guardado.activo)
    }

    @Test
    fun a_creadoEnGuardaUnTimestampReal() = runBlocking<Unit> {
        val antes = System.currentTimeMillis()
        repositorio.registrar("carla", "clave123", "Carla Ramírez", "c@cinemax.pe", "999111222")
        val despues = System.currentTimeMillis()

        val creadoEn = requireNotNull(repositorio.buscarPorUsuario("carla")).creadoEn
        assertTrue("creadoEn debe caer dentro de la ventana de la prueba", creadoEn in antes..despues)
    }

    // ------------------------------------------------------------------
    // B) Registro duplicado
    // ------------------------------------------------------------------

    @Test
    fun b_rechazaUnUsuarioDuplicado() = runBlocking<Unit> {
        database.usuarioDao().insertar(cliente("carla"))

        val resultado = repositorio.registrar(
            usuario = "carla",
            contrasena = "otra",
            nombreCompleto = "Otra Persona",
            email = "otra@cinemax.pe",
            telefono = "999000000"
        )

        assertTrue(resultado is ResultadoRegistro.UsuarioDuplicado)
        assertEquals(1, repositorio.contar())
        // La contrasena original no debe sobrescribirse.
        assertEquals("1234", requireNotNull(repositorio.buscarPorUsuario("carla")).contrasena)
    }

    @Test
    fun b_rechazaElDuplicadoInclusiveConTrasEspacios() = runBlocking<Unit> {
        database.usuarioDao().insertar(cliente("carla"))

        val resultado = repositorio.registrar(
            usuario = "  carla ",
            contrasena = "otra",
            nombreCompleto = "Otra",
            email = "otra@cinemax.pe",
            telefono = "999000000"
        )

        assertTrue(resultado is ResultadoRegistro.UsuarioDuplicado)
        assertEquals(1, repositorio.contar())
    }

    // ------------------------------------------------------------------
    // C) Login correcto
    // ------------------------------------------------------------------

    @Test
    fun c_loginCorrectoDevuelveElUsuarioDeRoom() = runBlocking<Unit> {
        database.usuarioDao().insertar(cliente("carla", contrasena = "clave123"))

        val resultado = repositorio.login("carla", "clave123")

        assertTrue(resultado is ResultadoLogin.Exito)
        assertEquals("carla", (resultado as ResultadoLogin.Exito).usuario.usuario)
        assertEquals("Usuario carla", resultado.usuario.nombreCompleto)
        assertEquals("carla@cinemax.pe", resultado.usuario.email)
        assertTrue(resultado.usuario.activo)
    }

    @Test
    fun c_losUsuariosDemoDelSeedSiguenSirviendoParaEntrar() = runBlocking<Unit> {
        SeedData.usuarios().forEach { database.usuarioDao().insertar(it) }

        // Documentacion de Fase 4: estos son los credenciales del seed.
        listOf("cliente", "andres", "lucia", "diego", "rosa").forEach { usuario ->
            val resultado = repositorio.login(usuario, SeedData.CONTRASENA_DEMO)
            assertTrue(
                "El usuario demo $usuario deberia poder entrar",
                resultado is ResultadoLogin.Exito
            )
        }
    }

    // ------------------------------------------------------------------
    // D) Contrasena incorrecta
    // ------------------------------------------------------------------

    @Test
    fun d_rechazaUnaContrasenaIncorrecta() = runBlocking<Unit> {
        database.usuarioDao().insertar(cliente("carla", contrasena = "clave123"))

        val resultado = repositorio.login("carla", "clave124")

        assertEquals(ResultadoLogin.ContrasenaIncorrecta, resultado)
    }

    // ------------------------------------------------------------------
    // E) Usuario inexistente
    // ------------------------------------------------------------------

    @Test
    fun e_rechazaUnUsuarioInexistente() = runBlocking<Unit> {
        database.usuarioDao().insertar(cliente("carla", contrasena = "clave123"))

        assertEquals(ResultadoLogin.UsuarioInexistente, repositorio.login("marta", "clave123"))
        assertFalse(repositorio.existeUsuario("marta"))
    }

    @Test
    fun e_distingueUsuarioInexistenteDeContrasenaIncorrecta() = runBlocking<Unit> {
        database.usuarioDao().insertar(cliente("carla", contrasena = "clave123"))

        assertEquals(ResultadoLogin.UsuarioInexistente, repositorio.login("carla2", "malo"))
        assertEquals(ResultadoLogin.ContrasenaIncorrecta, repositorio.login("carla", "malo"))
    }

    // ------------------------------------------------------------------
    // F) Usuario inactivo
    // ------------------------------------------------------------------

    @Test
    fun f_rechazaUnUsuarioInactivoAunqueLaContrasenaSeaCorrecta() = runBlocking<Unit> {
        database.usuarioDao().insertar(cliente("carla", contrasena = "clave123", activo = false))

        val resultado = repositorio.login("carla", "clave123")

        assertTrue(resultado is ResultadoLogin.UsuarioInactivo)
        assertEquals("carla", (resultado as ResultadoLogin.UsuarioInactivo).usuario.usuario)
    }

    @Test
    fun f_desactivarYReactivarCambiaElAcceso() = runBlocking<Unit> {
        val carla = cliente("carla", contrasena = "clave123")
        val id = database.usuarioDao().insertar(carla)

        val guardada = requireNotNull(repositorio.buscarPorUsuario("carla"))
        repositorio.desactivar(guardada)
        assertTrue(repositorio.login("carla", "clave123") is ResultadoLogin.UsuarioInactivo)

        val dadoDeBaja = requireNotNull(repositorio.buscarPorUsuario("carla"))
        repositorio.activar(dadoDeBaja)
        assertTrue(repositorio.login("carla", "clave123") is ResultadoLogin.Exito)
        assertEquals(id.toInt(), dadoDeBaja.id)
    }

    // ------------------------------------------------------------------
    // Utilidades exigidas al DAO
    // ------------------------------------------------------------------

    @Test
    fun elDaoSoportaContarBuscarExistirActualizarYEliminar() = runBlocking<Unit> {
        val dao = database.usuarioDao()
        assertEquals(0, repositorio.contar())

        val id = dao.insertar(cliente("carla", contrasena = "clave123"))
        assertEquals(1, repositorio.contar())
        assertTrue(repositorio.existeUsuario("carla"))
        assertEquals("clave123", requireNotNull(repositorio.buscarPorUsuario("carla")).contrasena)
        assertNotNull(dao.porId(id.toInt()))
        assertNull(dao.buscarPorUsuario("marta"))

        dao.actualizar(requireNotNull(repositorio.buscarPorUsuario("carla")).copy(contrasena = "nueva"))
        assertEquals("nueva", requireNotNull(repositorio.buscarPorUsuario("carla")).contrasena)

        repositorio.eliminar(requireNotNull(repositorio.buscarPorUsuario("carla")))
        assertEquals(0, repositorio.contar())
        assertEquals(ResultadoLogin.UsuarioInexistente, repositorio.login("carla", "nueva"))
    }

    @Test
    fun laQueryLoginDeRoomSoloDevuelveConCoincidenciaExacta() = runBlocking<Unit> {
        val dao = database.usuarioDao()
        dao.insertar(cliente("carla", contrasena = "clave123"))

        assertNotNull(dao.login("carla", "clave123"))
        assertNull(dao.login("carla", "CLAVE123"))
        assertNull(dao.login("carla", "clave123 "))
        assertNull(dao.login("Carla", "clave123"))
        assertNull(dao.login("carla", ""))
    }
}
