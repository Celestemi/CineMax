package com.cinemax.cliente.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.DatabaseProvider
import com.cinemax.cliente.data.seed.SeedData
import com.cinemax.cliente.data.seed.SeedInitializer
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
 * FASE 4 - Caso H: un usuario registrado sigue existiendo despues de cerrar y
 * reabrir la base de datos.
 *
 * A diferencia del resto de pruebas, aqui la base NO es `:memory:`: se crea un
 * archivo temporal con el MISMO nombre de base de la app (`cinemax_cliente.db`)
 * dentro del directorio de cache, de modo que la prueba cubra el ciclo real de
 * apertura/cierre de Room y no solo el de una base efimera.
 */
@RunWith(AndroidJUnit4::class)
class AuthPersistenceTest {

    private var database: CineMaxClienteDatabase? = null

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(DatabaseProvider.NOMBRE_BASE_DATOS)
    }

    @After
    fun tearDown() {
        database?.close()
        database = null
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(DatabaseProvider.NOMBRE_BASE_DATOS)
    }

    private fun abrir(context: android.content.Context): CineMaxClienteDatabase =
        Room.databaseBuilder(context, CineMaxClienteDatabase::class.java, DatabaseProvider.NOMBRE_BASE_DATOS)
            .allowMainThreadQueries()
            .build()
            .also { database = it }

    @Test
    fun h_elUsuarioRegistradoPersisteAlCerrarYReabrirLaBase() = runBlocking<Unit> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val primeraApertura = abrir(context)
        val registro = AuthRepository(primeraApertura.usuarioDao()).registrar(
            usuario = "carla",
            contrasena = "clave123",
            nombreCompleto = "Carla Ramírez",
            email = "carla@cinemax.pe",
            telefono = "999111222"
        )
        assertTrue(registro is ResultadoRegistro.Exito)
        assertTrue(
            "Debe existir el archivo cinemax_cliente.db",
            context.getDatabasePath(DatabaseProvider.NOMBRE_BASE_DATOS).exists()
        )
        primeraApertura.close()
        database = null

        // Reapertura: nueva instancia de Room sobre el mismo archivo.
        val segundaApertura = abrir(context)
        val repositorio = AuthRepository(segundaApertura.usuarioDao())

        val guardado = repositorio.buscarPorUsuario("carla")
        assertNotNull("La cuenta debe seguir en la base tras reabrir", guardado)
        assertEquals(RolCineMax.CLIENTE.name, requireNotNull(guardado).rol)
        assertTrue(requireNotNull(guardado).activo)

        val login = repositorio.login("carla", "clave123")
        assertTrue("El login debe funcionar sobre la base reabierta", login is ResultadoLogin.Exito)
        assertEquals(1, repositorio.contar())
    }

    @Test
    fun h_losUsuariosDemoDelSeedTambienPersistenYSirvenParaEntrar() = runBlocking<Unit> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val primeraApertura = abrir(context)
        assertTrue(SeedInitializer.sembrar(primeraApertura))
        primeraApertura.close()
        database = null

        val segundaApertura = abrir(context)
        val repositorio = AuthRepository(segundaApertura.usuarioDao())
        assertEquals(SeedData.usuarios().size, repositorio.contar())

        listOf("cliente", "andres", "lucia", "diego", "rosa").forEach { usuario ->
            assertTrue(
                "El usuario demo $usuario debe poder entrar tras reabrir",
                repositorio.login(usuario, SeedData.CONTRASENA_DEMO) is ResultadoLogin.Exito
            )
        }
        // El admin existe en la base, pero el rol lo rechaza la app Cliente.
        assertNotNull(repositorio.buscarPorUsuario("admin"))
        assertTrue(
            "El repositorio no valida el rol; de eso se encarga AuthViewModel",
            repositorio.login("admin", SeedData.CONTRASENA_DEMO) is ResultadoLogin.Exito
        )
    }

    @Test
    fun h_unaCuentaRegistradaNoSeBorraAlVolverASembrar() = runBlocking<Unit> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val primeraApertura = abrir(context)
        AuthRepository(primeraApertura.usuarioDao()).registrar(
            usuario = "carla",
            contrasena = "clave123",
            nombreCompleto = "Carla Ramírez",
            email = "carla@cinemax.pe",
            telefono = "999111222"
        )
        primeraApertura.close()
        database = null

        val segundaApertura = abrir(context)
        // El sembrado sigue siendo idempotente y no toca lo ya escrito.
        assertFalse(SeedInitializer.sembrar(segundaApertura))
        assertEquals(1, segundaApertura.usuarioDao().contar())
        assertNull(segundaApertura.usuarioDao().buscarPorUsuario("cliente"))
    }
}
