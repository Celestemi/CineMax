package com.cinemax.cliente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.DatabaseProvider
import com.cinemax.cliente.data.repository.PeliculaRepository
import com.cinemax.cliente.data.repository.RepositoriosCliente
import com.cinemax.cliente.data.repository.ReservaRepository
import com.cinemax.cliente.state.MisReservasUiState
import com.cinemax.cliente.state.UsuarioSesion
import com.cinemax.cliente.state.aReservaListada
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * FASE 7 - ViewModel de "MIS RESERVAS".
 *
 * ## De donde sale el usuario
 * [sesion] es el `sesion` de `AuthViewModel`. El `usuarioId` NUNCA se pasa por
 * parametro desde la UI: se lee de la sesion, igual que hace `ReservaViewModel`
 * al comprar. Asi es imposible que el historial de otra persona aparezca en
 * pantalla.
 *
 * ## Dos `Flow`, una pantalla
 * `combine` espera a que lleguen las reservas y el catalogo de peliculas antes de
 * emitir, de modo que la lista nunca sale con titulos vacios a medio cargar. Las
 * opciones vienen de `ReservaDao.porUsuarioConDetalle`, una consulta `@Transaction`
 * ya existente: no hay N+1 ni consultas nuevas en el esquema.
 *
 * @param alcance solo para tests; en la app es `viewModelScope`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MisReservasViewModel(
    private val reservas: ReservaRepository,
    private val peliculas: PeliculaRepository,
    private val sesion: StateFlow<UsuarioSesion?>,
    private val alcance: CoroutineScope? = null
) : ViewModel() {

    private val ambito: CoroutineScope get() = alcance ?: viewModelScope

    private val _uiState = MutableStateFlow<MisReservasUiState>(MisReservasUiState.Inicial)
    val uiState: StateFlow<MisReservasUiState> = _uiState.asStateFlow()

    /** Contador de reintentos; ver [reintentar]. */
    private val _disparo = MutableStateFlow(0)

    init {
        observarHistorial()
    }

    /**
     * El historial se reconstruye cada vez que la sesion cambia o se reintenta.
     *
     * Sin sesion el estado pasa a [MisReservasUiState.Inicial] y es
     * `PantallaProtegida` quien devuelve al login: no queda historial de la sesion
     * anterior en pantalla.
     */
    private fun observarHistorial() {
        ambito.launch {
            combine(sesion, _disparo) { usuario, _ -> usuario }
                .flatMapLatest { usuario ->
                    if (usuario == null) flowOf(MisReservasUiState.Inicial)
                    else historialDe(usuario.id)
                }
                .collect { _uiState.value = it }
        }
    }

    /**
     * Vuelve a leer el historial desde Room.
     *
     * Es lo que ofrece el boton "Reintentar" de [MisReservasUiState.Error]. Como el
     * flujo ya es reactivo, normalmente no haria falta: solo se usa tras un fallo.
     *
     * Se incrementa [_disparo] en vez de abrir una segunda coleccion: dos
     * `collect` sobre el mismo historial pelearian por escribir en `_uiState` y el
     * resultado dependeria de cual llegara ultimo.
     */
    fun reintentar() {
        _disparo.value = _disparo.value + 1
    }

    private fun historialDe(usuarioId: Int): Flow<MisReservasUiState> = flow {
        emit(MisReservasUiState.Cargando)
        emitAll(
            combine(
                reservas.observarPorUsuarioConDetalle(usuarioId),
                peliculas.observarActivas()
            ) { filas, catalogo ->
                val titulos = catalogo.associate { it.id to it.titulo }
                MisReservasUiState.Exito(filas.map { fila -> fila.aReservaListada(titulos) })
            }.catch { causa ->
                emit(MisReservasUiState.Error(ErrorDeCarga.mensaje(causa)))
            }
        )
    }
}

/**
 * FASE 7 - Fabrica de "Mis Reservas".
 *
 * [sesion] debe ser el `sesion` del `AuthViewModel` real, que es lo que garantiza
 * que el historial corresponda al usuario autenticado.
 */
class MisReservasViewModelFactory(
    private val database: CineMaxClienteDatabase,
    private val sesion: StateFlow<UsuarioSesion?>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MisReservasViewModel::class.java)) {
            "MisReservasViewModelFactory solo crea ${MisReservasViewModel::class.java.simpleName}"
        }
        return MisReservasViewModel(
            reservas = RepositoriosCliente.reservas(database),
            peliculas = RepositoriosCliente.peliculas(database),
            sesion = sesion
        ) as T
    }

    companion object {
        fun desde(context: Context, sesion: StateFlow<UsuarioSesion?>): MisReservasViewModelFactory =
            MisReservasViewModelFactory(DatabaseProvider.obtener(context), sesion)
    }
}