package com.cinemax.cliente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.DatabaseProvider
import com.cinemax.cliente.data.repository.FuncionRepository
import com.cinemax.cliente.data.repository.PeliculaRepository
import com.cinemax.cliente.data.repository.RepositoriosCliente
import com.cinemax.cliente.state.DetallePeliculaUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * FASE 5 - ViewModel del DETALLE de pelicula.
 *
 * Carga la pelicula seleccionada y su programacion (funciones con sede y sala) en
 * una sola observacion, sin consultar Room directamente: delega en
 * `PeliculaRepository` y `FuncionRepository`.
 *
 * ## Por que `flatMapLatest` y no un `launch` por llamada
 * [cargar] escribe la id en un `StateFlow` y la observacion se reconstruye sola.
 * Así, abrir el detalle de la pelicula 2 cancela la consulta de la pelicula 1
 * (carrera resuelta por el propio ViewModel, sin `Job.cancel()` manual) y recargar
 * la misma pelicula no duplica suscripciones.
 *
 * Una pelicula dada de baja (`activo = 0`) produce
 * [DetallePeliculaUiState.NoEncontrado]: la app Cliente nunca muestra peliculas
 * retiradas de cartelera.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetallePeliculaViewModel(
    private val peliculas: PeliculaRepository,
    private val funciones: FuncionRepository,
    private val alcance: CoroutineScope? = null
) : ViewModel() {

    private val ambito: CoroutineScope get() = alcance ?: viewModelScope

    private val _peliculaId = MutableStateFlow<Int?>(null)

    private val _uiState = MutableStateFlow<DetallePeliculaUiState>(DetallePeliculaUiState.Inicial)
    val uiState: StateFlow<DetallePeliculaUiState> = _uiState.asStateFlow()

    init {
        observarDetalle()
    }

    /** Abre el detalle de [peliculaId]. Reabrir la misma id no reinicia la consulta. */
    fun cargar(peliculaId: Int) {
        if (peliculaId <= 0) {
            _peliculaId.value = null
            _uiState.value = DetallePeliculaUiState.NoEncontrado
            return
        }
        _peliculaId.value = peliculaId
    }

    /** Vuelve a [DetallePeliculaUiState.Inicial] al abandonar la pantalla. */
    fun limpiar() {
        _peliculaId.value = null
        _uiState.value = DetallePeliculaUiState.Inicial
    }

    private fun observarDetalle() {
        ambito.launch {
            _peliculaId
                .filterNotNull()
                .flatMapLatest { id -> detalleDe(id) }
                .collect { _uiState.value = it }
        }
    }

    /**
     * Emite [DetallePeliculaUiState.Cargando] y despues combina pelicula y
     * funciones. `combine` espera a que ambos `Flow` de Room hayan emitido, de
     * modo que nunca se muestra una pelicula con una lista de funciones a medio
     * cargar.
     */
    private fun detalleDe(peliculaId: Int): Flow<DetallePeliculaUiState> = flow {
        emit(DetallePeliculaUiState.Cargando)
        emitAll(
            combine(
                peliculas.observarPorId(peliculaId),
                funciones.observarPorPelicula(peliculaId)
            ) { pelicula, lista ->
                when {
                    pelicula == null || !pelicula.activo -> DetallePeliculaUiState.NoEncontrado
                    // Solo se ofrecen funciones activas: las dadas de baja no se venden.
                    else -> DetallePeliculaUiState.Exito(
                        pelicula = pelicula,
                        funciones = lista.filter { it.funcion.activa }
                    )
                }
            }.catch { causa ->
                emit(DetallePeliculaUiState.Error(ErrorDeCarga.mensaje(causa)))
            }
        )
    }
}

/** FASE 5 - Fabrica del detalle. */
class DetallePeliculaViewModelFactory(private val database: CineMaxClienteDatabase) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(DetallePeliculaViewModel::class.java)) {
            "DetallePeliculaViewModelFactory solo crea ${DetallePeliculaViewModel::class.java.simpleName}"
        }
        return DetallePeliculaViewModel(
            peliculas = RepositoriosCliente.peliculas(database),
            funciones = RepositoriosCliente.funciones(database)
        ) as T
    }

    companion object {
        fun desde(context: Context): DetallePeliculaViewModelFactory =
            DetallePeliculaViewModelFactory(DatabaseProvider.obtener(context))
    }
}
