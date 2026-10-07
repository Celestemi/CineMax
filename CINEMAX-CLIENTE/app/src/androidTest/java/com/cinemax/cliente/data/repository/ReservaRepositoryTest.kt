package com.cinemax.cliente.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cinemax.cliente.data.BaseDePrueba
import com.cinemax.cliente.data.Catalogo
import com.cinemax.cliente.data.local.ButacaReservaEntity
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.ReservaEntity
import com.cinemax.cliente.data.seed.SeedData
import com.cinemax.cliente.model.EstadoReserva
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * FASE 5 - Casos L a Q: registro de la reserva, validaciones de negocio y, sobre
 * todo, la garantia de que nunca queda una reserva a medias ni se vende dos
 * veces la misma butaca.
 */
@RunWith(AndroidJUnit4::class)
class ReservaRepositoryTest {

    private lateinit var database: CineMaxClienteDatabase
    private lateinit var reservas: ReservaRepository
    private lateinit var salas: SalaButacaRepository
    private lateinit var catalogo: Catalogo

    @Before
    fun setUp() = runBlocking<Unit> {
        database = BaseDePrueba.abrirEnMemoria()
        catalogo = BaseDePrueba.sembrarYLeer(database)
        reservas = RepositoriosCliente.reservas(database)
        salas = RepositoriosCliente.salas(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    /** Butacas libres de la primera funcion de la primera sala, para no depender de ids fijos. */
    private suspend fun funcionYButacasLibres(cantidad: Int = 2): Pair<Int, List<Int>> {
        val sala = catalogo.salas.first()
        val funcion = catalogo.funciones.first { it.salaId == sala.id }
        val ocupadas = salas.observarOcupadas(funcion.id).first().map { it.id }.toSet()
        val libres = catalogo.butacas(sala.id).filter { !ocupadas.contains(it.id) }.take(cantidad)
        assertTrue("La sala del seed debe tener butacas libres", libres.size == cantidad)
        return funcion.id to libres.map { it.id }
    }

    private fun solicitud(
        funcionId: Int,
        butacaIds: List<Int>,
        usuarioId: Int = catalogo.usuarios.first().id,
        total: Double = 30.0
    ) = SolicitudReserva(
        usuarioId = usuarioId,
        funcionId = funcionId,
        butacaIds = butacaIds,
        total = total,
        fechaCompra = "2026-05-01"
    )

    // ------------------------------------------------------------------
    // L) Reserva valida
    // ------------------------------------------------------------------

    @Test
    fun l_unaReservaValidaSeRegistra() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres(2)
        val total = catalogo.funcion(funcionId).precioEntrada * 2

        val resultado = reservas.registrar(solicitud(funcionId, butacas, total = total))

        assertTrue("Se esperaba Exito, fue $resultado", resultado is ResultadoReserva.Exito)
        val exito = resultado as ResultadoReserva.Exito
        assertTrue(exito.reserva.id > 0)
        assertTrue(exito.reserva.codigo.startsWith("CINEMAX-"))
        assertEquals(EstadoReserva.CONFIRMADA.valorPersistido, exito.reserva.estado)
        assertEquals(total, exito.reserva.total, 0.0001)
        assertEquals(butacas.size, exito.butacas.size)
    }

    @Test
    fun l_laReservaEsConsultaPorCodigo() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres(2)
        val exito = reservas.registrar(solicitud(funcionId, butacas)) as ResultadoReserva.Exito

        val leida = reservas.obtenerPorCodigo(exito.reserva.codigo)

        assertNotNull(leida)
        assertEquals(exito.reserva.id, leida!!.id)
        assertEquals(exito.reserva.funcionId, leida.funcionId)
        assertEquals(exito.reserva.usuarioId, leida.usuarioId)
    }

    @Test
    fun l_lasButacasQuedanOcupadasParaEsaFuncion() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres(2)
        // La funcion elegida por `funcionYButacasLibres` ya trae las reservas del
        // seed, asi que lo que se comprueba es el incremento, no el total.
        val ocupadasAntes = salas.contarOcupadas(funcionId)
        val exito = reservas.registrar(solicitud(funcionId, butacas)) as ResultadoReserva.Exito

        val ocupadas = salas.observarOcupadas(funcionId).first().map { it.id }.toSet()

        assertTrue(butacas.all { ocupadas.contains(it) })
        assertEquals(ocupadasAntes + butacas.size, salas.contarOcupadas(funcionId))
        assertEquals(2, reservas.contarButacasDeReserva(exito.reserva.id))
    }

    @Test
    fun l_unaReservaNoOcupaLasButacasDeOtraFuncion() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres(1)
        val otraFuncion = catalogo.funciones.first { it.salaId == catalogo.funcion(funcionId).salaId && it.id != funcionId }
        reservas.registrar(solicitud(funcionId, butacas))

        val ocupadasEnOtra = salas.observarOcupadas(otraFuncion.id).first()

        assertTrue(ocupadasEnOtra.none { butacas.contains(it.id) })
    }

    @Test
    fun l_lasReservasDelUsuarioSeListanPorUsuario() = runBlocking<Unit> {
        val usuario = catalogo.usuarios.first { it.usuario == "cliente" }
        val (funcionId, butacas) = funcionYButacasLibres(1)
        val exito = reservas.registrar(solicitud(funcionId, butacas, usuarioId = usuario.id)) as ResultadoReserva.Exito

        val delUsuario = reservas.observarPorUsuario(usuario.id).first()

        assertTrue(delUsuario.any { it.id == exito.reserva.id })
        val confirmadas = reservas.observarPorUsuarioYEstado(usuario.id, EstadoReserva.CONFIRMADA).first()
        assertTrue(confirmadas.any { it.id == exito.reserva.id })
    }

    // ------------------------------------------------------------------
    // M) Sin butacas seleccionadas
    // ------------------------------------------------------------------

    @Test
    fun m_unaReservaSinButacasSeRechaza() = runBlocking<Unit> {
        val (funcionId, _) = funcionYButacasLibres()
        val antes = reservas.contar()

        val resultado = reservas.registrar(solicitud(funcionId, emptyList()))

        assertEquals(MotivoReserva.SIN_BUTACAS, (resultado as ResultadoReserva.Rechazada).motivo)
        assertEquals(antes, reservas.contar())
    }

    // ------------------------------------------------------------------
    // N) Funcion inexistente o inactiva
    // ------------------------------------------------------------------

    @Test
    fun n_unaFuncionInexistenteSeRechaza() = runBlocking<Unit> {
        val (_, butacas) = funcionYButacasLibres()
        val antes = reservas.contar()

        val resultado = reservas.registrar(solicitud(999_999, butacas))

        assertEquals(MotivoReserva.FUNCION_INEXISTENTE, (resultado as ResultadoReserva.Rechazada).motivo)
        assertEquals(antes, reservas.contar())
    }

    @Test
    fun n_unaFuncionSinIdSeRechaza() = runBlocking<Unit> {
        val resultado = reservas.registrar(solicitud(0, listOf(1)))
        assertEquals(MotivoReserva.SIN_FUNCION, (resultado as ResultadoReserva.Rechazada).motivo)
    }

    @Test
    fun n_unaFuncionInactivaSeRechaza() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres()
        database.funcionDao().actualizar(catalogo.funcion(funcionId).copy(activa = false))
        val antes = reservas.contar()
        val ocupadasAntes = salas.contarOcupadas(funcionId)

        val resultado = reservas.registrar(solicitud(funcionId, butacas))

        assertEquals(MotivoReserva.FUNCION_INACTIVA, (resultado as ResultadoReserva.Rechazada).motivo)
        assertEquals(antes, reservas.contar())
        // La funcion ya traia ocupadas del seed: lo relevante es que no se sume ninguna.
        assertEquals(ocupadasAntes, salas.contarOcupadas(funcionId))
    }

    // ------------------------------------------------------------------
    // O) Sin sesion / usuario inexistente
    // ------------------------------------------------------------------

    @Test
    fun o_sinSesionSeRechaza() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres()
        val antes = reservas.contar()

        val resultado = reservas.registrar(solicitud(funcionId, butacas, usuarioId = 0))

        assertEquals(MotivoReserva.SIN_SESION, (resultado as ResultadoReserva.Rechazada).motivo)
        assertEquals(antes, reservas.contar())
    }

    @Test
    fun o_unUsuarioInexistenteSeRechaza() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres()
        val antes = reservas.contar()

        val resultado = reservas.registrar(solicitud(funcionId, butacas, usuarioId = 999_999))

        assertEquals(MotivoReserva.USUARIO_INEXISTENTE, (resultado as ResultadoReserva.Rechazada).motivo)
        assertEquals(antes, reservas.contar())
    }

    // ------------------------------------------------------------------
    // Otras reglas de negocio
    // ------------------------------------------------------------------

    @Test
    fun unTotalInvalidoSeRechaza() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres()
        val antes = reservas.contar()

        assertEquals(
            MotivoReserva.TOTAL_INVALIDO,
            (reservas.registrar(solicitud(funcionId, butacas, total = 0.0)) as ResultadoReserva.Rechazada).motivo
        )
        assertEquals(
            MotivoReserva.TOTAL_INVALIDO,
            (reservas.registrar(solicitud(funcionId, butacas, total = -5.0)) as ResultadoReserva.Rechazada).motivo
        )
        assertEquals(
            MotivoReserva.TOTAL_INVALIDO,
            (reservas.registrar(solicitud(funcionId, butacas, total = Double.NaN)) as ResultadoReserva.Rechazada).motivo
        )
        assertEquals(antes, reservas.contar())
    }

    @Test
    fun butacasRepetidasSeRechazan() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres(1)
        val repetida = butacas + butacas

        val resultado = reservas.registrar(solicitud(funcionId, repetida))

        assertEquals(MotivoReserva.BUTACAS_DUPLICADAS, (resultado as ResultadoReserva.Rechazada).motivo)
    }

    @Test
    fun butacasInexistentesSeRechazan() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres(1)
        val antes = reservas.contar()

        val resultado = reservas.registrar(solicitud(funcionId, butacas + listOf(999_999)))

        assertEquals(MotivoReserva.BUTACAS_DESCONOCIDAS, (resultado as ResultadoReserva.Rechazada).motivo)
        assertEquals(antes, reservas.contar())
    }

    @Test
    fun butacasDeOtraSalaSeRechazan() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres(1)
        // `salaDao.observarTodas` ordena por NOMBRE, no por sede, asi que hay que
        // descartar la sala de la funcion: si no, el "A1" added seria de la misma
        // sala y la reserva se rechazaria por ocupada, no por sala ajena.
        val salaAjena = catalogo.salas.first { it.id != catalogo.funcion(funcionId).salaId }
        val antes = reservas.contar()

        val resultado = reservas.registrar(solicitud(funcionId, butacas + listOf(catalogo.butaca(salaAjena.id, "A1").id)))

        assertEquals(MotivoReserva.BUTACAS_DE_OTRA_SALA, (resultado as ResultadoReserva.Rechazada).motivo)
        assertEquals(antes, reservas.contar())
    }

    @Test
    fun butacasYaOcupadasSeRechazan() = runBlocking<Unit> {
        // La funcion 0 del seed tiene A1 y A2 ocupadas.
        val funcionConOcupadas = catalogo.funcionDePrograma(0)
        val salaId = funcionConOcupadas.salaId
        val ocupada = catalogo.butaca(salaId, "A1").id
        val libre = funcionYButacasLibres(1).second.first()
        val antes = reservas.contar()

        val resultado = reservas.registrar(solicitud(funcionConOcupadas.id, listOf(ocupada, libre)))

        assertEquals(MotivoReserva.BUTACAS_YA_OCUPADAS, (resultado as ResultadoReserva.Rechazada).motivo)
        assertEquals(antes, reservas.contar())
        // La butaca libre tampoco se ocupa: la validacion es previa al insert.
        assertTrue(salas.observarOcupadas(funcionConOcupadas.id).first().none { it.id == libre })
    }

    // ------------------------------------------------------------------
    // Q) Nunca queda una reserva a medias
    // ------------------------------------------------------------------

    @Test
    fun q_siUnaButacaSeOcupaSeRevierteTodo() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres(2)
        val reservasAntes = reservas.contar()
        val ocupacionAntes = salas.contarOcupadas(funcionId)

        // Se ocupa la SEGUNDA butaca por fuera del repositorio, simulando que otro
        // cliente la compro entre la eleccion y la confirmacion.
        val intrusa = database.reservaDao().buscarPorCodigo(SeedData.reservasDemo.first().codigo)!!
        database.reservaDao().insertarDetalleButacas(
            listOf(ButacaReservaEntity(reservaId = intrusa.id, funcionId = funcionId, butacaId = butacas[1]))
        )

        val resultado = reservas.registrar(solicitud(funcionId, butacas))

        assertEquals(MotivoReserva.BUTACAS_YA_OCUPADAS, (resultado as ResultadoReserva.Rechazada).motivo)
        assertEquals(reservasAntes, reservas.contar())
        assertEquals(ocupacionAntes + 1, salas.contarOcupadas(funcionId))
        // La primera butaca de la seleccion NO se ocupa: no hay nada parcial.
        assertTrue(salas.observarOcupadas(funcionId).first().none { it.id == butacas[0] })
    }

    @Test
    fun q_elIndiceUnicoRevierteLaTransaccionDelDao() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres(1)
        val butaca = butacas.first()
        val reservasAntes = reservas.contar()

        // Dos reservas para la MISMA (funcion, butaca): la segunda viola el UNIQUE
        // y Room revierte la transaccion completa de `registrarReserva`.
        val primera = ReservaEntity(
            codigo = "CINEMAX-7001",
            usuarioId = catalogo.usuarios.first().id,
            funcionId = funcionId,
            fechaCompra = "2026-05-01",
            total = 15.0,
            estado = EstadoReserva.CONFIRMADA.valorPersistido
        )
        val segunda = primera.copy(codigo = "CINEMAX-7002")
        val entidadButaca = catalogo.butacas(catalogo.funcion(funcionId).salaId)
            .first { it.id == butaca }

        database.reservaDao().registrarReserva(primera, listOf(entidadButaca))
        val error = try {
            database.reservaDao().registrarReserva(segunda, listOf(entidadButaca))
            null
        } catch (fallo: Exception) {
            fallo
        }

        assertNotNull("El UNIQUE(funcion_id, butaca_id) debe rechazar la segunda", error)
        // No queda la segunda reserva: la transaccion se revirtio entera.
        assertEquals(reservasAntes + 1, reservas.contar())
        assertNull(reservas.obtenerPorCodigo("CINEMAX-7002"))
        assertNotNull(reservas.obtenerPorCodigo("CINEMAX-7001"))
    }

    // ------------------------------------------------------------------
    // Concurrencia: la garantia real contra la doble venta
    // ------------------------------------------------------------------

    @Test
    fun dosReservasSimultaneasDeLaMismaButacaSoloUnaGana() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres(1)
        val butaca = butacas.first()
        val usuarioA = catalogo.usuarios.first { it.usuario == "cliente" }
        val usuarioB = catalogo.usuarios.first { it.usuario == "andres" }
        val reservasAntes = reservas.contar()
        val ocupacionAntes = salas.contarOcupadas(funcionId)

        val resultados = withContext(Dispatchers.IO) {
            listOf(
                async { reservas.registrar(solicitud(funcionId, listOf(butaca), usuarioId = usuarioA.id)) },
                async { reservas.registrar(solicitud(funcionId, listOf(butaca), usuarioId = usuarioB.id)) }
            ).awaitAll()
        }

        val exitos = resultados.count { it is ResultadoReserva.Exito }
        val rechazada = resultados.filterIsInstance<ResultadoReserva.Rechazada>()

        assertEquals("Solo una confirmacion puede ganar", 1, exitos)
        assertEquals(1, rechazada.size)
        assertEquals(MotivoReserva.BUTACAS_YA_OCUPADAS, rechazada.first().motivo)
        assertEquals(reservasAntes + 1, reservas.contar())
        assertEquals(ocupacionAntes + 1, salas.contarOcupadas(funcionId))
    }

    @Test
    fun dosReservasSimultaneasDeButacasDistintasAmbasGanan() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres(2)
        val ocupacionAntes = salas.contarOcupadas(funcionId)

        val resultados = withContext(Dispatchers.IO) {
            listOf(
                async { reservas.registrar(solicitud(funcionId, listOf(butacas[0]))) },
                async { reservas.registrar(solicitud(funcionId, listOf(butacas[1]))) }
            ).awaitAll()
        }

        assertTrue(resultados.all { it is ResultadoReserva.Exito })
        assertEquals(ocupacionAntes + 2, salas.contarOcupadas(funcionId))
    }

    // ------------------------------------------------------------------
    // Consultas y cancelacion
    // ------------------------------------------------------------------

    @Test
    fun lasButacasDeUnaReservaSeLeenPorReserva() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres(2)
        val exito = reservas.registrar(solicitud(funcionId, butacas)) as ResultadoReserva.Exito

        val leidas = reservas.observarButacasDeReserva(exito.reserva.id).first()

        assertEquals(butacas.toSet(), leidas.map { it.id }.toSet())
    }

    @Test
    fun cancelarLiberaLasButacas() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres(2)
        val exito = reservas.registrar(solicitud(funcionId, butacas)) as ResultadoReserva.Exito
        val ocupadasAntes = salas.contarOcupadas(funcionId)

        val resultado = reservas.cancelar(exito.reserva.codigo, exito.reserva.usuarioId)

        assertTrue("Se esperaba Exito, fue $resultado", resultado is ResultadoCancelacion.Exito)
        assertEquals(ocupadasAntes - 2, salas.contarOcupadas(funcionId))
        assertEquals(EstadoReserva.CANCELADA.valorPersistido, reservas.obtenerPorCodigo(exito.reserva.codigo)!!.estado)
        assertEquals(0, reservas.contarButacasDeReserva(exito.reserva.id))
    }

    @Test
    fun laButacaCanceladaVuelveAEstarLibreParaOtraReserva() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres(1)
        val exito = reservas.registrar(solicitud(funcionId, butacas)) as ResultadoReserva.Exito
        reservas.cancelar(exito.reserva.codigo, exito.reserva.usuarioId)

        val segundo = reservas.registrar(solicitud(funcionId, butacas))

        assertTrue("Se esperaba Exito, fue $segundo", segundo is ResultadoReserva.Exito)
    }

    @Test
    fun cancelarRechazaLoQueNoLePertenece() = runBlocking<Unit> {
        val (funcionId, butacas) = funcionYButacasLibres(1)
        val exito = reservas.registrar(solicitud(funcionId, butacas)) as ResultadoReserva.Exito

        assertTrue(
            reservas.cancelar("CINEMAX-9999", exito.reserva.usuarioId) is ResultadoCancelacion.NoEncontrada
        )
        assertTrue(
            reservas.cancelar(exito.reserva.codigo, 999_999) is ResultadoCancelacion.NoPerteneceAlUsuario
        )
        reservas.cancelar(exito.reserva.codigo, exito.reserva.usuarioId)
        assertTrue(
            reservas.cancelar(exito.reserva.codigo, exito.reserva.usuarioId) is ResultadoCancelacion.YaCancelada
        )
    }
}
