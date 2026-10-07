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
import com.cinemax.cliente.data.repository.SalaButacaRepository
import com.cinemax.cliente.model.DisponibilidadButacas
import com.cinemax.cliente.model.FiltroCartelera
import com.cinemax.cliente.state.CarteleraUiState
import com.cinemax.cliente.state.OpcionesFiltro
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * FASE 5 - ViewModel de la CARTELERA.
 *
 * Carga peliculas y funciones desde Room a traves de los repositorios y expone
 * un unico estado sellado. **No hace ninguna consulta a Room**: eso es trabajo de
 * `PeliculaRepository` y `FuncionRepository`, de modo que un Composable jamas
 * llega al DAO.
 *
 * ## Filtros reactivos
 * [filtros] es un `StateFlow` y la consulta se reconstruye con `flatMapLatest`:
 * cada cambio de genero, fecha o sede cancela la consulta anterior y arranca la
 * nueva. Los filtros se aplican EN SQL (`FuncionDao.filtrarCompletas`), no
 * filtrando la lista en el Composable, y `null` significa "sin restriccion", de
 * modo que la combinacion de los tres criterios es la interseccion de los tres.
 *
 * @param alcance solo existe para los tests: en la app es `viewModelScope`.
 *         Inyectarlo permite observar los estados finales sin depender del looper
 *         de instrumentacion (mismo criterio que `AuthViewModel` en Fase 4).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CarteleraViewModel(
    private val peliculas: PeliculaRepository,
    private val funciones: FuncionRepository,
    private val salas: SalaButacaRepository,
    private val alcance: CoroutineScope? = null
) : ViewModel() {

    private val ambito: CoroutineScope get() = alcance ?: viewModelScope

    private val _filtros = MutableStateFlow(FiltroCartelera.SIN_FILTROS)
    val filtros: StateFlow<FiltroCartelera> = _filtros.asStateFlow()

    private val _uiState = MutableStateFlow<CarteleraUiState>(CarteleraUiState.Inicial)
    val uiState: StateFlow<CarteleraUiState> = _uiState.asStateFlow()

    private val _opciones = MutableStateFlow(OpcionesFiltro())
    val opciones: StateFlow<OpcionesFiltro> = _opciones.asStateFlow()

    /**
     * FASE 7 - Ocupacion por funcion para la cartelera.
     *
     * Va en un `StateFlow` aparte y no dentro de `CarteleraUiState` a proposito:
     * la ocupacion cambia cuando alguien compra, y mezclarla con el estado de la
     * consulta haria que un simple "alguien mas compro" pusiera la cartelera en
     * estado de carga. Ademas asi la disponibilidad sobrevive a un cambio de
     * filtro sin volver a consultar la cartelera.
     *
     * Es un `Flow` de Room agrupado por funcion (una sola lectura para todas), de
     * modo que no hace falta una consulta por tarjeta.
     */
    private val _ocupadasPorFuncion = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val ocupadasPorFuncion: StateFlow<Map<Int, Int>> = _ocupadasPorFuncion.asStateFlow()

    init {
        observarCartelera()
        observarOpciones()
        observarOcupacion()
    }

    /** Disponibilidad de la funcion [funcionId], a partir de [ocupadasPorFuncion]. */
    fun disponibilidadDe(capacidadSala: Int, funcionId: Int): DisponibilidadButacas =
        DisponibilidadButacas.de(capacidadSala, _ocupadasPorFuncion.value, funcionId)

    private fun observarOcupacion() {
        ambito.launch {
            salas.observarOcupadasPorFuncion()
                .catch {
                    // La cartelera ya esta pintandose: si la ocupacion falla se
                    // deja en cero (ninguna butaca ocupada) en vez de dejar la
                    // pantalla a medias o cambiar el estado de la consulta.
                    _ocupadasPorFuncion.value = emptyMap()
                }
                .collect { _ocupadasPorFuncion.value = it }
        }
    }

    // ------------------------------------------------------------------
    // Filtros
    // ------------------------------------------------------------------

    /** `null` o un texto vacio quita el filtro de genero. */
    fun filtrarPorGenero(genero: String?) = actualizarFiltros { copy(genero = genero) }

    /** La fecha va en ISO `yyyy-MM-dd` (mismo formato que `funciones.fecha`). */
    fun filtrarPorFecha(fecha: String?) = actualizarFiltros { copy(fecha = fecha) }

    /** `null` o un id no positivo quita el filtro de sede. */
    fun filtrarPorSede(sedeId: Int?) = actualizarFiltros { copy(sedeId = sedeId) }

    /** Aplica los tres criterios a la vez. */
    fun filtrar(genero: String?, fecha: String?, sedeId: Int?) = actualizarFiltros {
        copy(genero = genero, fecha = fecha, sedeId = sedeId)
    }

    fun limpiarFiltros() {
        _filtros.value = FiltroCartelera.SIN_FILTROS
    }

    private inline fun actualizarFiltros(transform: FiltroCartelera.() -> FiltroCartelera) {
        val siguiente = _filtros.value.transform().normalizado()
        if (siguiente == _filtros.value) return
        _filtros.value = siguiente
    }

    // ------------------------------------------------------------------
    // Observacion
    // ------------------------------------------------------------------

    /**
     * Un solo `Flow` reactivo para toda la pantalla.
     *
     * `flatMapLatest` sobre [filtros] reinicia la consulta en cada cambio de
     * filtro; el `flow { emit(Cargando) ; emitAll(...) }` hace que ese reinicio sea
     * visible en la UI sin perder la reactividad del `Flow` de Room.
     */
    private fun observarCartelera() {
        ambito.launch {
            _filtros
                .flatMapLatest { filtro -> carteleraDe(filtro) }
                .collect { _uiState.value = it }
        }
    }

    private fun carteleraDe(filtro: FiltroCartelera): Flow<CarteleraUiState> = flow {
        emit(CarteleraUiState.Cargando(filtro))
        emitAll(
            funciones.observarFiltradas(filtro)
                .map { lista -> CarteleraUiState.Exito(funciones = lista, filtro = filtro) }
                .catch { causa -> emit(CarteleraUiState.Error(ErrorDeCarga.mensaje(causa))) }
        )
    }

    /**
     * Generos, fechas y sedes disponibles.
     *
     * Cada `Flow` lleva su propio `catch` para que la caída de uno no vacie los
     * demas: la barra de filtros debe seguir funcionando aunque un `SELECT DISTINCT`
     * falle.
     */
    private fun observarOpciones() {
        ambito.launch {
            combine(
                peliculas.observarGeneros().catch { emit(emptyList()) },
                funciones.observarFechas().catch { emit(emptyList()) },
                salas.observarSedes().catch { emit(emptyList()) }
            ) { generos, fechas, sedes ->
                OpcionesFiltro(generos = generos, fechas = fechas, sedes = sedes)
            }.collect { _opciones.value = it }
        }
    }

    /** Vuelve a la cartelera completa y arranca la carga desde [CarteleraUiState.Inicial]. */
    fun recargar() {
        limpiarFiltros()
        _uiState.value = CarteleraUiState.Cargando(_filtros.value)
    }
}

/** FASE 5 - Fabrica: la UI nunca construye los repositorios ni abre Room. */
class CarteleraViewModelFactory(private val database: CineMaxClienteDatabase) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(CarteleraViewModel::class.java)) {
            "CarteleraViewModelFactory solo crea ${CarteleraViewModel::class.java.simpleName}"
        }
        return CarteleraViewModel(
            peliculas = RepositoriosCliente.peliculas(database),
            funciones = RepositoriosCliente.funciones(database),
            salas = RepositoriosCliente.salas(database)
        ) as T
    }

    companion object {
        /** Atajo para el `LocalContext` de un Composable. */
        fun desde(context: Context): CarteleraViewModelFactory =
            CarteleraViewModelFactory(DatabaseProvider.obtener(context))
    }
}
