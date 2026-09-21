package com.cinemax.peru.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinemax.peru.data.repository.FakeFuncionRepository
import com.cinemax.peru.data.repository.FakePeliculaRepository
import com.cinemax.peru.data.repository.FuncionRepository
import com.cinemax.peru.data.repository.PeliculaRepository
import com.cinemax.peru.model.Genero
import com.cinemax.peru.state.CarteleraUiState
import com.cinemax.peru.state.EstadoCartelera
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CarteleraViewModel(
    private val funcionRepository: FuncionRepository = FakeFuncionRepository(),
    private val peliculaRepository: PeliculaRepository = FakePeliculaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CarteleraUiState())
    val uiState: StateFlow<CarteleraUiState> = _uiState.asStateFlow()

    init {
        cargarCartelera()
    }

    fun cargarCartelera() {
        viewModelScope.launch {
            _uiState.value = CarteleraUiState(estado = EstadoCartelera.CARGANDO)

            try {
                val funcionesCargadas = funcionRepository.obtenerTodas()
                val peliculasCargadas = peliculaRepository.obtenerTodas()

                if (funcionesCargadas.isEmpty()) {
                    _uiState.value = CarteleraUiState(estado = EstadoCartelera.VACIO)
                    return@launch
                }

                _uiState.value = CarteleraUiState(
                    estado = EstadoCartelera.CARGADO,
                    peliculas = peliculasCargadas,
                    sedes = funcionesCargadas.map { it.sede }.distinctBy { it.id },
                    funciones = funcionesCargadas,
                    funcionesFiltradas = funcionesCargadas
                )
            } catch (e: Exception) {
                _uiState.value = CarteleraUiState(
                    estado = EstadoCartelera.ERROR,
                    mensajeError = "Ocurrió un error al cargar la cartelera"
                )
            }
        }
    }

    fun buscar(texto: String) {
        val actual = _uiState.value
        aplicarFiltros(texto, actual.generoSeleccionado, actual.fechaSeleccionada, actual.sedeSeleccionada)
    }

    fun filtrarPorGenero(genero: Genero?) {
        val actual = _uiState.value
        aplicarFiltros(actual.textoBusqueda, genero, actual.fechaSeleccionada, actual.sedeSeleccionada)
    }

    fun filtrarPorFecha(fecha: String?) {
        val actual = _uiState.value
        aplicarFiltros(actual.textoBusqueda, actual.generoSeleccionado, fecha, actual.sedeSeleccionada)
    }

    fun filtrarPorSede(sedeId: Int?) {
        val actual = _uiState.value
        aplicarFiltros(actual.textoBusqueda, actual.generoSeleccionado, actual.fechaSeleccionada, sedeId)
    }

    fun limpiarFiltros() {
        aplicarFiltros("", null, null, null)
    }

    private fun aplicarFiltros(texto: String, genero: Genero?, fecha: String?, sedeId: Int?) {
        val actual = _uiState.value

        var resultado = funcionRepository.filtrar(
            fecha = fecha,
            sedeId = sedeId,
            genero = genero
        )

        val termino = texto.trim()
        if (termino.isNotEmpty()) {
            resultado = resultado.filter { peliculaCoincide(it, termino) }
        }

        _uiState.value = actual.copy(
            textoBusqueda = texto,
            generoSeleccionado = genero,
            fechaSeleccionada = fecha,
            sedeSeleccionada = sedeId,
            funcionesFiltradas = resultado,
            estado = if (resultado.isEmpty()) EstadoCartelera.VACIO else EstadoCartelera.CARGADO
        )
    }

    private fun peliculaCoincide(funcion: com.cinemax.peru.model.Funcion, termino: String): Boolean =
        funcion.pelicula.titulo.contains(termino, ignoreCase = true)
}