package com.cinemax.peru.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinemax.peru.data.repository.FakeFuncionRepository
import com.cinemax.peru.data.repository.FuncionRepository
import com.cinemax.peru.state.DetallePeliculaUiState
import com.cinemax.peru.state.EstadoDetalle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetallePeliculaViewModel(
    private val funcionId: Int,
    private val funcionRepository: FuncionRepository = FakeFuncionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetallePeliculaUiState())
    val uiState: StateFlow<DetallePeliculaUiState> = _uiState.asStateFlow()

    init {
        cargarDetalle()
    }

    fun cargarDetalle() {
        viewModelScope.launch {
            _uiState.value = DetallePeliculaUiState(estado = EstadoDetalle.CARGANDO)

            try {
                val funcion = funcionRepository.obtenerTodas().firstOrNull { it.id == funcionId }

                if (funcion == null) {
                    _uiState.value = DetallePeliculaUiState(
                        estado = EstadoDetalle.ERROR,
                        mensajeError = "No se encontró la función seleccionada."
                    )
                    return@launch
                }

                val funcionesDePelicula = funcionRepository.obtenerPorPelicula(funcion.pelicula.id)
                val estado = if (funcionesDePelicula.isEmpty()) {
                    EstadoDetalle.VACIO
                } else {
                    EstadoDetalle.CARGADO
                }

                _uiState.value = DetallePeliculaUiState(
                    estado = estado,
                    funcionSeleccionada = funcion,
                    pelicula = funcion.pelicula,
                    funcionesDePelicula = funcionesDePelicula
                )
            } catch (e: Exception) {
                _uiState.value = DetallePeliculaUiState(
                    estado = EstadoDetalle.ERROR,
                    mensajeError = "Ocurrió un error al cargar el detalle."
                )
            }
        }
    }
}