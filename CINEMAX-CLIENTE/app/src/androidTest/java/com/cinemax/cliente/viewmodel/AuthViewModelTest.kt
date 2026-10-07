package com.cinemax.cliente.viewmodel

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.UsuarioEntity
import com.cinemax.cliente.data.repository.AuthRepository
import com.cinemax.cliente.data.seed.SeedData
import com.cinemax.cliente.state.AuthUiState
import com.cinemax.cliente.state.RolCineMax
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
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
 * FASE 4 - Estados de [AuthViewModel] contra Room real.
 *
 * El ViewModel recibe la misma base en memoria que la app, de modo que cada
 * estado se comprueba de extremo a extremo: validacion -> DAO -> Repository ->
 * estado de UI.
 */
@RunWith(AndroidJUnit4::class)
class AuthViewModelTest {

    private lateinit var database: CineMaxClienteDatabase
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, CineMaxClienteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        viewModel = AuthViewModel(
            repositorio = AuthRepository(database.usuarioDao()),
            reloj = { 1_700_000_000_000L },
            alcance = CoroutineScope(Dispatchers.Unconfined)
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun sembrar(
        usuario: String,
        contrasena: String = "1234",
        rol: String = RolCineMax.CLIENTE.name,
        activo: Boolean = true
    ): Long = database.usuarioDao().insertar(
        UsuarioEntity(
            usuario = usuario,
            contrasena = contrasena,
            nombreCompleto = "Usuario $usuario",
            email = "$usuario@cinemax.pe",
            telefono = "999111222",
            rol = rol,
            activo = activo,
            creadoEn = 1_700_000_000_000L
        )
    )

    /** Espera a que el estado cumpla la condicion; Room escribe fuera del hilo de test. */
    private fun <T> StateFlow<T>.esperarA(ms: Long = 5_000, condicion: (T) -> Boolean): T {
        val limite = System.currentTimeMillis() + ms
        var actual = value
        while (!condicion(actual)) {
            if (System.currentTimeMillis() > limite) {
                throw AssertionError("El estado $actual no cumplio la condicion esperada")
            }
            runBlocking { delay(10) }
            actual = value
        }
        return actual
    }

    private fun loginYEsperar(usuario: String, contrasena: String): AuthUiState {
        viewModel.iniciarSesion(usuario, contrasena)
        return viewModel.uiState.esperarA { it !is AuthUiState.Cargando && it !is AuthUiState.Inicial }
    }

    private fun registrarYEsperar(
        usuario: String = "carla",
        contrasena: String = "clave123",
        nombreCompleto: String = "Carla Ramírez",
        email: String = "carla@cinemax.pe",
        telefono: String = "999111222"
    ): AuthUiState {
        viewModel.registrar(usuario, contrasena, nombreCompleto, email, telefono)
        return viewModel.uiState.esperarA { it !is AuthUiState.Cargando && it !is AuthUiState.Inicial }
    }

    // ------------------------------------------------------------------
    // Estados base
    // ------------------------------------------------------------------

    @Test
    fun elEstadoInicialEsInicialYCargandoDespuesDeIntentar() = runBlocking<Unit> {
        assertEquals(AuthUiState.Inicial, viewModel.uiState.value)
        assertNull(viewModel.sesion.value)
        assertTrue(viewModel.registroUiState.value.usuario.isEmpty())

        sembrar("carla", "1234")
        viewModel.iniciarSesion("carla", "1234")
        // La pantalla ve `Cargando` mientras Room responde.
        viewModel.uiState.esperarA { it is AuthUiState.Autenticado }
    }

    // ------------------------------------------------------------------
    // A) Registro de usuario nuevo
    // ------------------------------------------------------------------

    @Test
    fun a_registroNuevoTerminaEnRegistroExitosoYCreaElCliente() = runBlocking<Unit> {
        val estado = registrarYEsperar()

        assertTrue(estado is AuthUiState.RegistroExitoso)
        assertEquals(1, database.usuarioDao().contar())
        assertTrue(viewModel.registroUiState.value.enviando.not())
        assertTrue(viewModel.registroUiState.value.usuario.isEmpty())

        val guardado = requireNotNull(database.usuarioDao().buscarPorUsuario("carla"))
        assertEquals(RolCineMax.CLIENTE.name, guardado.rol)
        assertTrue(guardado.activo)
        assertEquals(1_700_000_000_000L, guardado.creadoEn)
    }

    @Test
    fun a_elUsuarioRegistradoPuedeEntrarInmediatamente() = runBlocking<Unit> {
        registrarYEsperar()
        viewModel.limpiarEstado()

        val estado = loginYEsperar("carla", "clave123")

        assertTrue(estado is AuthUiState.Autenticado)
        assertEquals("carla", requireNotNull(viewModel.sesion.value).usuario)
    }

    // ------------------------------------------------------------------
    // B) Usuario duplicado
    // ------------------------------------------------------------------

    @Test
    fun b_registroDuplicadoTerminaEnUsuarioDuplicado() = runBlocking<Unit> {
        sembrar("carla")
        viewModel.actualizarUsuarioCampo("carla")

        val estado = registrarYEsperar()

        assertTrue(estado is AuthUiState.UsuarioDuplicado)
        assertEquals(1, database.usuarioDao().contar())
        assertNotNull(viewModel.registroUiState.value.errores.usuario)
        assertTrue(viewModel.registroUiState.value.errores.hayAlguno)
        assertTrue(viewModel.registroUiState.value.enviando.not())
        assertNull("No debe abrirse sesion al registrar", viewModel.sesion.value)
    }

    // ------------------------------------------------------------------
    // C) Login correcto
    // ------------------------------------------------------------------

    @Test
    fun c_loginCorrectoTerminaEnAutenticadoYGuardaLaSesion() = runBlocking<Unit> {
        val id = sembrar("carla", contrasena = "clave123")

        val estado = loginYEsperar("carla", "clave123")

        assertTrue(estado is AuthUiState.Autenticado)
        val sesion = requireNotNull(viewModel.sesion.value)
        assertEquals(id.toInt(), sesion.id)
        assertEquals("carla", sesion.usuario)
        assertEquals(RolCineMax.CLIENTE, sesion.rol)
        assertTrue(sesion.activo)
        assertEquals(1_700_000_000_000L, sesion.inicioSesionEn)
        assertTrue(estado.esAccesoPermitido)
    }

    // ------------------------------------------------------------------
    // D) Contrasena incorrecta
    // ------------------------------------------------------------------

    @Test
    fun d_loginConContrasenaIncorrectaTerminaEnLoginIncorrecto() = runBlocking<Unit> {
        sembrar("carla", contrasena = "clave123")

        val estado = loginYEsperar("carla", "clave999")

        assertTrue(estado is AuthUiState.LoginIncorrecto)
        assertTrue(estado.esAccesoDenegado)
        assertTrue(estado !is AuthUiState.Autenticado)
        assertNull(viewModel.sesion.value)
    }

    // ------------------------------------------------------------------
    // E) Usuario inexistente
    // ------------------------------------------------------------------

    @Test
    fun e_loginConUsuarioInexistenteTerminaEnLoginIncorrecto() = runBlocking<Unit> {
        sembrar("carla", contrasena = "clave123")

        val estado = loginYEsperar("marta", "clave123")

        assertTrue(estado is AuthUiState.LoginIncorrecto)
        assertNull(viewModel.sesion.value)
    }

    // ------------------------------------------------------------------
    // F) Usuario inactivo
    // ------------------------------------------------------------------

    @Test
    fun f_loginConUsuarioInactivoTerminaEnUsuarioInactivo() = runBlocking<Unit> {
        sembrar("carla", contrasena = "clave123", activo = false)

        val estado = loginYEsperar("carla", "clave123")

        assertTrue(estado is AuthUiState.UsuarioInactivo)
        assertTrue(estado.esAccesoDenegado)
        assertNull(viewModel.sesion.value)
    }

    // ------------------------------------------------------------------
    // G) ADMIN rechazado en la app Cliente
    // ------------------------------------------------------------------

    @Test
    fun g_unAdminNoEntraEnLaAppCliente() = runBlocking<Unit> {
        sembrar("admin", contrasena = "1234", rol = RolCineMax.ADMINISTRADOR.name)

        val estado = loginYEsperar("admin", "1234")

        assertTrue(estado is AuthUiState.RolNoPermitido)
        assertTrue(estado.esAccesoDenegado)
        assertFalse(estado.esAccesoPermitido)
        assertNull("Un ADMIN nunca debe crear sesion", viewModel.sesion.value)
    }

    @Test
    fun g_elAdminDelSeedTampocoEntra() = runBlocking<Unit> {
        // El seed sigue sembrando `admin/1234` con rol ADMINISTRADOR.
        val admin = requireNotNull(SeedData.usuarios().first { it.usuario == "admin" })
        database.usuarioDao().insertar(admin)
        assertEquals(RolCineMax.ADMINISTRADOR.name, admin.rol)

        val estado = loginYEsperar("admin", SeedData.CONTRASENA_DEMO)

        assertTrue(estado is AuthUiState.RolNoPermitido)
        assertNull(viewModel.sesion.value)
    }

    @Test
    fun g_unRolDesconocidoTambienSeRechaza() = runBlocking<Unit> {
        sembrar("raro", contrasena = "1234", rol = "SUPERVISOR")

        val estado = loginYEsperar("raro", "1234")

        assertTrue(estado is AuthUiState.RolNoPermitido)
        assertNull(viewModel.sesion.value)
    }

    @Test
    fun g_todosLosClientesDelSeedEntranYElAdminNo() = runBlocking<Unit> {
        SeedData.usuarios().forEach { database.usuarioDao().insertar(it) }

        val clientes = SeedData.usuarios().filter { it.rol == RolCineMax.CLIENTE.name }
        val administradores = SeedData.usuarios().filter { it.rol == RolCineMax.ADMINISTRADOR.name }
        assertTrue(clientes.isNotEmpty())
        assertTrue(administradores.isNotEmpty())

        clientes.forEach { usuario ->
            viewModel.cerrarSesion()
            assertTrue(
                "${usuario.usuario} deberia entrar",
                loginYEsperar(usuario.usuario, SeedData.CONTRASENA_DEMO) is AuthUiState.Autenticado
            )
        }
        administradores.forEach { usuario ->
            viewModel.cerrarSesion()
            assertTrue(
                "${usuario.usuario} es ADMIN y no debe entrar",
                loginYEsperar(usuario.usuario, SeedData.CONTRASENA_DEMO) is AuthUiState.RolNoPermitido
            )
        }
    }

    // ------------------------------------------------------------------
    // I) Contrasena vacia
    // ------------------------------------------------------------------

    @Test
    fun i_loginConContrasenaVaciaSeRechazaSinConsultarRoom() = runBlocking<Unit> {
        sembrar("carla", "1234")

        viewModel.iniciarSesion("carla", "")

        val estado = viewModel.uiState.value
        assertTrue(estado is AuthUiState.ValidacionInvalida)
        assertNull(viewModel.sesion.value)
    }

    // ------------------------------------------------------------------
    // J) Campos obligatorios vacios
    // ------------------------------------------------------------------

    @Test
    fun j_registroConTodosLosCamposVaciosSeRechaza() = runBlocking<Unit> {
        val estado = registrarYEsperar(
            usuario = "",
            contrasena = "",
            nombreCompleto = "",
            email = "",
            telefono = ""
        )

        assertTrue(estado is AuthUiState.ValidacionInvalida)
        assertEquals(0, database.usuarioDao().contar())
        assertEquals(5, listOf(
            viewModel.registroUiState.value.errores.usuario,
            viewModel.registroUiState.value.errores.contrasena,
            viewModel.registroUiState.value.errores.nombreCompleto,
            viewModel.registroUiState.value.errores.email,
            viewModel.registroUiState.value.errores.telefono
        ).count { it != null })
        assertTrue(viewModel.registroUiState.value.enviando.not())
    }

    @Test
    fun j_registroConUnSoloCampoVacioSeRechaza() = runBlocking<Unit> {
        val estado = registrarYEsperar(email = "")

        assertTrue(estado is AuthUiState.ValidacionInvalida)
        assertEquals(0, database.usuarioDao().contar())
        assertNotNull(viewModel.registroUiState.value.errores.email)
        assertNull(viewModel.registroUiState.value.errores.telefono)
    }

    @Test
    fun j_registroConEmailMalFormadoSeRechaza() = runBlocking<Unit> {
        val estado = registrarYEsperar(email = "esto-no-es-un-email")

        assertTrue(estado is AuthUiState.ValidacionInvalida)
        assertEquals(0, database.usuarioDao().contar())
    }

    // ------------------------------------------------------------------
    // Sesion
    // ------------------------------------------------------------------

    @Test
    fun cerrarSesionLimpiaElEstadoYVuelveAlLogin() = runBlocking<Unit> {
        sembrar("carla", "1234")
        loginYEsperar("carla", "1234")
        assertNotNull(viewModel.sesion.value)

        viewModel.cerrarSesion()

        assertNull(viewModel.sesion.value)
        assertEquals(AuthUiState.Inicial, viewModel.uiState.value)
    }

    @Test
    fun limpiarEstadoNoBorraUnaSesionYaConcedida() = runBlocking<Unit> {
        sembrar("carla", "1234")
        loginYEsperar("carla", "1234")

        viewModel.limpiarEstado()

        assertTrue(viewModel.uiState.value is AuthUiState.Autenticado)
        assertNotNull(viewModel.sesion.value)
    }

    @Test
    fun elFormularioSeLimpiaAlEscribirEnCadaCampo() = runBlocking<Unit> {
        viewModel.actualizarUsuarioCampo("carla")
        viewModel.actualizarContrasenaCampo("clave123")
        viewModel.actualizarNombreCampo("Carla Ramírez")
        viewModel.actualizarEmailCampo("carla@cinemax.pe")
        viewModel.actualizarTelefonoCampo("999111222")

        val formulario = viewModel.registroUiState.value
        assertEquals("carla", formulario.usuario)
        assertEquals("clave123", formulario.contrasena)
        assertEquals("Carla Ramírez", formulario.nombreCompleto)
        assertEquals("carla@cinemax.pe", formulario.email)
        assertEquals("999111222", formulario.telefono)
        assertFalse(formulario.errores.hayAlguno)
    }
}
