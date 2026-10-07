package com.cinemax.cliente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.DatabaseProvider
import com.cinemax.cliente.data.repository.FuncionRepository
import com.cinemax.cliente.data.repository.RepositoriosCliente
import com.cinemax.cliente.data.repository.ReservaRepository
import com.cinemax.cliente.data.repository.ResultadoReserva
import com.cinemax.cliente.data.repository.SalaButacaRepository
import com.cinemax.cliente.data.repository.SolicitudReserva
import com.cinemax.cliente.model.CotizacionReserva
import com.cinemax.cliente.model.FechaCineMax
import com.cinemax.cliente.state.MotivoValidacionReserva
import com.cinemax.cliente.state.ReservaUiState
import com.cinemax.cliente.state.UsuarioSesion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * FASE 5 - ViewModel de la RESERVA (pantalla de compra).
 *
 * ## De donde sale el usuario
 * [sesion] es el `StateFlow<UsuarioSesion?>` de `AuthViewModel` (Fase 4). El
 * `usuarioId` que se graba se lee SIEMPRE de ahi: **ningun metodo publico acepta
 * un `usuarioId`**, de modo que la UI no puede registrar una reserva en nombre de
 * otra persona. Si no hay sesion, la compra se rechaza.
 *
 * ## Donde vive el calculo
 * [CalculoReserva.calcularTotal] corre aqui, no en el Composable:
 *
 *     TOTAL = precioEntrada x cantidadDeButacas
 *
 * ## Revalidacion contra Room (punto 4.F)
 * La pantalla de butacas muestra la disponibilidad, pero esa informacion puede
 * quedarse obsoleta en cuanto otra persona compra. [confirmar] vuelve a
 * consultar `SalaButacaRepository.butacasOcupadas` y, si alguna butaca ya no esta
 * libre, NO escribe nada y devuelve
 * [ReservaUiState.ValidacionInvalida] con [MotivoValidacionReserva.ButacasNoDisponibles].
 * La garantia definitiva sigue siendo el indice `UNIQUE(funcion_id, butaca_id)`
 * que aplica `ReservaRepository` dentro de su transaccion.
 *
 * @param reloj fecha de compra en ISO; se inyecta para que los tests no dependan
 *        del dia en que se ejecutan.
 * @param alcance solo para tests; en la app es `viewModelScope`.
 */
class ReservaViewModel(
    private val reservas: ReservaRepository,
    private val funciones: FuncionRepository,
    private val salas: SalaButacaRepository,
    private val sesion: StateFlow<UsuarioSesion?>,
    private val reloj: () -> String = { FechaCineMax.hoyIso() },
    private val alcance: CoroutineScope? = null
) : ViewModel() {

    private val ambito: CoroutineScope get() = alcance ?: viewModelScope

    private val _uiState = MutableStateFlow<ReservaUiState>(ReservaUiState.Inicial)
    val uiState: StateFlow<ReservaUiState> = _uiState.asStateFlow()

    /** Funcion y butacas elegidas en la pantalla anterior. */
    private var seleccion: SeleccionPendiente? = null

    /** `true` mientras se esta escribiendo, para que la UI no acepte doble pulsacion. */
    private var registrando = false

    // ------------------------------------------------------------------
    // Preparacion
    // ------------------------------------------------------------------

    /**
     * Recibe la funcion y las butacas seleccionadas y cotiza la compra.
     *
     * No acepta `usuarioId`: el usuario sale de [sesion] en el momento de
     * confirmar.
     */
    fun preparar(funcionId: Int, butacaIds: List<Int>) {
        seleccion = SeleccionPendiente(funcionId, butacaIds.distinct())
        ambito.launch { cotizar() }
    }

    /** Carga la ultima cotizacion persistida en la sesion del ViewModel. */
    private suspend fun cotizar() {
        val pendiente = seleccion
        if (pendiente == null) {
            _uiState.value = ReservaUiState.Inicial
            return
        }

        _uiState.value = ReservaUiState.Cargando

        // El usuario se valida tambien al cotizar para no mostrar un total a alguien
        // que no va a poder pagar.
        if (sesion.value == null) {
            invalidar(MotivoValidacionReserva.SinSesion)
            return
        }
        if (pendiente.funcionId <= 0) {
            invalidar(MotivoValidacionReserva.SinFuncion)
            return
        }

        val completa = funciones.obtenerCompletaPorId(pendiente.funcionId)
        if (completa == null || !completa.funcion.activa) {
            invalidar(MotivoValidacionReserva.FuncionInvalida)
            return
        }

        if (pendiente.butacaIds.isEmpty()) {
            invalidar(MotivoValidacionReserva.SinButacas)
            return
        }

        val butacas = salas.butacasDeSala(completa.funcion.salaId)
            .filter { pendiente.butacaIds.contains(it.id) }
        if (butacas.size != pendiente.butacaIds.size) {
            invalidar(MotivoValidacionReserva.ButacasNoDisponibles(pendiente.butacaIds.map { it.toString() }))
            return
        }

        val total = when (val resultado = CalculoReserva.calcularTotal(
            completa.funcion.precioEntrada,
            butacas.size
        )) {
            is CalculoReserva.Resultado.Valido -> resultado.total
            is CalculoReserva.Resultado.Invalido -> {
                invalidar(MotivoValidacionReserva.CalculoInvalido(resultado.error))
                return
            }
        }

        _uiState.value = ReservaUiState.Calculando(
            CotizacionReserva(
                funcion = completa.funcion,
                pelicula = completa.pelicula,
                butacas = butacas,
                precioEntrada = completa.funcion.precioEntrada,
                cantidad = butacas.size,
                total = total,
                nombreSede = completa.sede.nombre,
                nombreSala = completa.sala.nombre
            )
        )
    }

    // ------------------------------------------------------------------
    // Confirmacion
    // ------------------------------------------------------------------

    /**
     * Registra la reserva.
     *
     * Orden de validaciones (punto 4 de la guia):
     * A. hay sesion abierta y con rol CLIENTE;
     * B. hay una funcion seleccionada;
     * C. hay al menos una butaca seleccionada;
     * D/E. el total es calculable (precio > 0 y cantidad > 0);
     * F. se revalida la disponibilidad contra Room;
     * y solo entonces se escribe, en una unica transaccion.
     */
    fun confirmar() {
        val pendiente = seleccion ?: run {
            invalidar(MotivoValidacionReserva.SinFuncion)
            return
        }
        if (registrando) return

        ambito.launch {
            registrando = true
            try {
                ejecutarConfirmacion(pendiente)
            } finally {
                registrando = false
            }
        }
    }

    private suspend fun ejecutarConfirmacion(pendiente: SeleccionPendiente) {
        _uiState.value = ReservaUiState.Cargando

        // --- A) usuario autenticado (nunca desde la UI) ---
        val usuario = sesion.value
        if (usuario == null || !usuario.cumpleRolPermitido()) {
            invalidar(MotivoValidacionReserva.SinSesion)
            return
        }

        // --- B) funcion seleccionada ---
        if (pendiente.funcionId <= 0) {
            invalidar(MotivoValidacionReserva.SinFuncion)
            return
        }

        // --- C) butacas seleccionadas ---
        if (pendiente.butacaIds.isEmpty()) {
            invalidar(MotivoValidacionReserva.SinButacas)
            return
        }

        val completa = funciones.obtenerCompletaPorId(pendiente.funcionId)
        if (completa == null || !completa.funcion.activa) {
            invalidar(MotivoValidacionReserva.FuncionInvalida)
            return
        }

        val butacas = salas.butacasDeSala(completa.funcion.salaId)
            .filter { pendiente.butacaIds.contains(it.id) }
        if (butacas.size != pendiente.butacaIds.size) {
            invalidar(MotivoValidacionReserva.FuncionInvalida)
            return
        }

        // --- D/E) calculo de negocio ---
        val total = when (val resultado = CalculoReserva.calcularTotal(
            completa.funcion.precioEntrada,
            butacas.size
        )) {
            is CalculoReserva.Resultado.Valido -> resultado.total
            is CalculoReserva.Resultado.Invalido -> {
                invalidar(MotivoValidacionReserva.CalculoInvalido(resultado.error))
                return
            }
        }

        val cotizacion = CotizacionReserva(
            funcion = completa.funcion,
            pelicula = completa.pelicula,
            butacas = butacas,
            precioEntrada = completa.funcion.precioEntrada,
            cantidad = butacas.size,
            total = total,
            nombreSede = completa.sede.nombre,
            nombreSala = completa.sala.nombre
        )
        _uiState.value = ReservaUiState.Calculando(cotizacion)

        // --- F) REVALIDACION contra Room antes de escribir ---
        val ocupadas = salas.butacasOcupadas(pendiente.funcionId, pendiente.butacaIds)
        if (ocupadas.isNotEmpty()) {
            invalidar(MotivoValidacionReserva.ButacasNoDisponibles(ocupadas.map { it.codigo }))
            return
        }

        // --- escritura transaccional ---
        _uiState.value = ReservaUiState.Registrando
        when (
            val resultado = reservas.registrar(
                SolicitudReserva(
                    usuarioId = usuario.id,
                    funcionId = pendiente.funcionId,
                    butacaIds = butacas.map { it.id },
                    total = total,
                    fechaCompra = reloj()
                )
            )
        ) {
            is ResultadoReserva.Exito -> {
                seleccion = null
                _uiState.value = ReservaUiState.Exito(
                    reserva = resultado.reserva,
                    butacas = resultado.butacas,
                    cotizacion = cotizacion
                )
            }

            is ResultadoReserva.Rechazada -> invalidar(
                MotivoValidacionReserva.Rechazada(resultado.motivo),
                resultado.mensaje
            )

            is ResultadoReserva.Error -> {
                // La transaccion se ha revertido: no hay reserva parcial.
                _uiState.value = ReservaUiState.Error(ErrorDeCarga.mensaje(resultado.causa))
            }
        }
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    /** Vuelve a [ReservaUiState.Inicial] y descarta la seleccion en curso. */
    fun limpiar() {
        seleccion = null
        registrando = false
        _uiState.value = ReservaUiState.Inicial
    }

    private fun invalidar(motivo: MotivoValidacionReserva, mensaje: String = motivo.mensaje) {
        _uiState.value = ReservaUiState.ValidacionInvalida(motivo, mensaje)
    }

    private data class SeleccionPendiente(val funcionId: Int, val butacaIds: List<Int>)
}

/**
 * FASE 5 - Fabrica de la pantalla de compra.
 *
 * [sesion] debe ser el `sesion` de `AuthViewModel`: es la garantia de que la
 * reserva queda asociada al usuario realmente autenticado. Se recibe por
 * parametro para que la pantalla no tenga acceso al `AuthViewModel` completo.
 */
class ReservaViewModelFactory(
    private val database: CineMaxClienteDatabase,
    private val sesion: StateFlow<UsuarioSesion?>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ReservaViewModel::class.java)) {
            "ReservaViewModelFactory solo crea ${ReservaViewModel::class.java.simpleName}"
        }
        return ReservaViewModel(
            reservas = RepositoriosCliente.reservas(database),
            funciones = RepositoriosCliente.funciones(database),
            salas = RepositoriosCliente.salas(database),
            sesion = sesion
        ) as T
    }

    companion object {
        fun desde(context: Context, sesion: StateFlow<UsuarioSesion?>): ReservaViewModelFactory =
            ReservaViewModelFactory(DatabaseProvider.obtener(context), sesion)
    }
}
