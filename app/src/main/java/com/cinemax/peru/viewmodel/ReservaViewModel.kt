package com.cinemax.peru.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinemax.peru.data.repository.FakeFuncionRepository
import com.cinemax.peru.data.repository.FakeReservaRepository
import com.cinemax.peru.data.repository.FuncionRepository
import com.cinemax.peru.data.repository.ReservaRepository
import com.cinemax.peru.model.Butaca
import com.cinemax.peru.model.EstadoButaca
import com.cinemax.peru.model.Funcion
import com.cinemax.peru.state.EstadoCompra
import com.cinemax.peru.state.ReservaUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReservaViewModel(
    private val reservaRepository: ReservaRepository = FakeReservaRepository(),
    private val funcionRepository: FuncionRepository = FakeFuncionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservaUiState())
    val uiState: StateFlow<ReservaUiState> = _uiState.asStateFlow()

    fun cargarFuncion(funcionId: Int) {
        val actual = _uiState.value
        if (actual.funcionSeleccionada?.id == funcionId && !actual.cargando) return

        viewModelScope.launch {
            _uiState.value = ReservaUiState(cargando = true)
            try {
                val funcion = funcionRepository.obtenerTodas().firstOrNull { it.id == funcionId }
                if (funcion == null) {
                    _uiState.value = ReservaUiState(
                        mensajeError = "Función no encontrada"
                    )
                } else {
                    seleccionarFuncion(funcion)
                }
            } catch (e: Exception) {
                _uiState.value = ReservaUiState(
                    mensajeError = "No se pudieron cargar los asientos"
                )
            }
        }
    }

    fun seleccionarFuncion(funcion: Funcion) {
        val disponibles = funcion.sala.butacas.filter { it.estado == EstadoButaca.DISPONIBLE }
        _uiState.value = ReservaUiState(
            cargando = false,
            mensajeError = null,
            funcionSeleccionada = funcion,
            butacas = funcion.sala.butacas,
            butacasDisponibles = disponibles,
            estadoCompra = EstadoCompra.SELECCIONANDO
        )
    }

    fun alternarButaca(butaca: Butaca) {
        val actual = _uiState.value
        val funcion = actual.funcionSeleccionada
        if (funcion == null) return
        if (butaca.estado == EstadoButaca.OCUPADA) return

        val nuevaSeleccion = if (actual.butacasSeleccionadas.any { it.id == butaca.id }) {
            actual.butacasSeleccionadas.filterNot { it.id == butaca.id }
        } else {
            actual.butacasSeleccionadas + butaca
        }

        _uiState.value = actual.copy(
            butacasSeleccionadas = nuevaSeleccion,
            total = reservaRepository.calcularTotal(funcion, nuevaSeleccion)
        )
    }

    fun confirmarCompra(
        numeroTarjeta: String,
        titular: String,
        vencimiento: String,
        cvv: String
    ) {
        val actual = _uiState.value
        if (actual.funcionSeleccionada == null) {
            _uiState.value = actual.copy(
                estadoCompra = EstadoCompra.ERROR,
                mensaje = "No hay una función seleccionada"
            )
            return
        }
        if (actual.butacasSeleccionadas.isEmpty()) {
            _uiState.value = actual.copy(
                estadoCompra = EstadoCompra.ERROR,
                mensaje = "Selecciona al menos una butaca"
            )
            return
        }
        if (numeroTarjeta.isBlank() || titular.isBlank() || vencimiento.isBlank() || cvv.isBlank()) {
            _uiState.value = actual.copy(
                estadoCompra = EstadoCompra.ERROR,
                mensaje = "Completa todos los datos de pago"
            )
            return
        }
        comprar()
    }

    fun comprar() {
        val actual = _uiState.value
        val funcion = actual.funcionSeleccionada
        if (funcion == null || actual.butacasSeleccionadas.isEmpty()) {
            _uiState.value = actual.copy(
                estadoCompra = EstadoCompra.ERROR,
                mensaje = "Selecciona al menos una butaca"
            )
            return
        }

        _uiState.value = actual.copy(estadoCompra = EstadoCompra.COMPRANDO)
        viewModelScope.launch {
            delay(1200)
            val reserva = reservaRepository.registrarReserva(
                funcion = funcion,
                butacas = actual.butacasSeleccionadas,
                fechaCompra = fechaActual()
            )

            val butacasActualizadas = actual.butacas.map { b ->
                if (actual.butacasSeleccionadas.any { it.id == b.id }) {
                    b.copy(estado = EstadoButaca.OCUPADA)
                } else {
                    b
                }
            }

            _uiState.value = actual.copy(
                butacas = butacasActualizadas,
                butacasDisponibles = butacasActualizadas.filter { it.estado == EstadoButaca.DISPONIBLE },
                butacasSeleccionadas = emptyList(),
                total = reserva.total,
                estadoCompra = EstadoCompra.COMPLETADA,
                reservaConfirmada = reserva,
                mensaje = "Compra confirmada. Código: ${reserva.codigo}"
            )
        }
    }

    fun limpiarSeleccion() {
        val actual = _uiState.value
        _uiState.value = actual.copy(
            butacasSeleccionadas = emptyList(),
            total = 0.0,
            mensaje = null,
            estadoCompra = if (actual.funcionSeleccionada != null) {
                EstadoCompra.SELECCIONANDO
            } else {
                EstadoCompra.INICIAL
            }
        )
    }

    private fun fechaActual(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}