package com.cinemax.cliente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinemax.cliente.data.local.ButacaEntity
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.DatabaseProvider
import com.cinemax.cliente.data.local.FuncionCompleta
import com.cinemax.cliente.data.repository.FuncionRepository
import com.cinemax.cliente.data.repository.RepositoriosCliente
import com.cinemax.cliente.data.repository.SalaButacaRepository
import com.cinemax.cliente.model.ButacaSeleccionable
import com.cinemax.cliente.model.EstadoButaca
import com.cinemax.cliente.state.SeleccionButacasUiState
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * FASE 5 - ViewModel de la SELECCION DE BUTACAS.
 *
 * ## LA SELECCION ES TEMPORAL Y SOLO EXISTE EN MEMORIA
 * [seleccion] es un `Set<Int>` dentro del ViewModel. **Ningun metodo de esta clase
 * escribe en Room**: no hay ni un `insert` en el fichero. Las butacas se ocupan
 * unicamente cuando `ReservaViewModel.confirmar` registra la reserva, y solo tras
 * volver a comprobar la disponibilidad contra la base de datos.
 *
 * ## Regla de oro: la ocupada no se puede tocar
 * [seleccionar] rechaza cualquier butaca que Room ya tiene ocupada en ESTA funcion
 * y lo comunica con [SeleccionButacasUiState.Exito.aviso], sin modificar la
 * seleccion. El rechazo se hace aqui y no en el Composable, que solo lee
 * [ButacaSeleccionable.seleccionable].
 *
 * ## La disponibilidad se observa viva
 * El mapa se reconstruye combinando dos `Flow` de Room (mapa de la sala y butacas
 * ocupadas en la funcion). Si otra persona compra mientras esta pantalla esta
 * abierta, su butaca pasa a `OCUPADA` y, si estaba seleccionada, se quita de la
 * seleccion automaticamente.
 *
 * @param alcance solo para tests; en la app es `viewModelScope`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SeleccionButacasViewModel(
    private val funciones: FuncionRepository,
    private val salas: SalaButacaRepository,
    private val alcance: CoroutineScope? = null
) : ViewModel() {

    private val ambito: CoroutineScope get() = alcance ?: viewModelScope

    private val _funcionId = MutableStateFlow<Int?>(null)

    /** Ids de las butacas elegidas. Temporal: nunca se persiste aqui. */
    private val _seleccion = MutableStateFlow<Set<Int>>(emptySet())
    val seleccion: StateFlow<Set<Int>> = _seleccion.asStateFlow()

    private val _uiState = MutableStateFlow<SeleccionButacasUiState>(SeleccionButacasUiState.Inicial)
    val uiState: StateFlow<SeleccionButacasUiState> = _uiState.asStateFlow()

    /** Ultimo mapa leido de Room; permite reconstruir el estado sin volver a consultar. */
    private var contexto: ContextoButacas? = null

    /** Aviso pendiente de mostrar, que se consume al reconstruir el estado. */
    private var avisoPendiente: String? = null

    init {
        observarMapa()
    }

    // ------------------------------------------------------------------
    // Carga
    // ------------------------------------------------------------------

    /** Abre el mapa de butacas de [funcionId] y descarta cualquier seleccion previa. */
    fun cargar(funcionId: Int) {
        contexto = null
        avisoPendiente = null
        _seleccion.value = emptySet()
        _uiState.value = SeleccionButacasUiState.Inicial
        _funcionId.value = if (funcionId > 0) funcionId else null
        if (funcionId <= 0) {
            _uiState.value = SeleccionButacasUiState.FuncionNoEncontrada
        }
    }

    /** Vuelve a [SeleccionButacasUiState.Inicial] al abandonar la pantalla. */
    fun limpiar() {
        _funcionId.value = null
        contexto = null
        avisoPendiente = null
        _seleccion.value = emptySet()
        _uiState.value = SeleccionButacasUiState.Inicial
    }

    // ------------------------------------------------------------------
    // Seleccion temporal
    // ------------------------------------------------------------------

    /**
     * Añade una butaca a la seleccion temporal.
     *
     * No hace nada si la butaca no pertenece al mapa actual, si ya estaba
     * seleccionada o si ya esta ocupada.
     */
    fun seleccionar(butacaId: Int) {
        val contextoActual = contexto ?: return
        val butaca = contextoActual.buscar(butacaId) ?: return
        if (butaca.salaId != contextoActual.completa.funcion.salaId) {
            avisar("Esa butaca no pertenece a esta sala")
            return
        }
        if (contextoActual.ocupada(butacaId)) {
            avisar("La butaca ${butaca.codigo} ya esta ocupada")
            return
        }
        if (_seleccion.value.contains(butacaId)) return
        _seleccion.value = _seleccion.value + butacaId
        reconstruir()
    }

    /** Quita una butaca de la seleccion temporal. */
    fun deseleccionar(butacaId: Int) {
        if (!_seleccion.value.contains(butacaId)) return
        _seleccion.value = _seleccion.value - butacaId
        reconstruir()
    }

    /** Alterna el estado de una butaca, rechazando las ocupadas. */
    fun alternar(butacaId: Int) {
        if (_seleccion.value.contains(butacaId)) deseleccionar(butacaId) else seleccionar(butacaId)
    }

    /** Vacia la seleccion temporal. */
    fun limpiarSeleccion() {
        if (_seleccion.value.isEmpty()) return
        _seleccion.value = emptySet()
        reconstruir()
    }

    /** Selecciona todas las butacas que siguen libres. */
    fun seleccionarTodasLasLibres() {
        val contextoActual = contexto ?: return
        val libres = contextoActual.butacas
            .filter { contextoActual.ocupada(it.id).not() }
            .map { it.id }
            .toSet()
        if (libres.isEmpty()) {
            avisar("No quedan butacas libres en esta funcion")
            return
        }
        _seleccion.value = libres
        reconstruir()
    }

    // ------------------------------------------------------------------
    // Observacion del mapa
    // ------------------------------------------------------------------

    private fun observarMapa() {
        ambito.launch {
            _funcionId
                .filterNotNull()
                .flatMapLatest { id -> estadoDe(id) }
                .catch { causa ->
                    emit(SeleccionButacasUiState.Error(ErrorDeCarga.mensaje(causa)))
                }
                .collect { _uiState.value = it }
        }
    }

    private fun estadoDe(funcionId: Int): Flow<SeleccionButacasUiState> = flow {
        emit(SeleccionButacasUiState.Cargando)
        emitAll(
            funciones.observarCompletaPorId(funcionId).flatMapLatest { completa ->
                if (completa == null) {
                    flowOf(SeleccionButacasUiState.FuncionNoEncontrada)
                } else {
                    combine(
                        salas.observarButacasDeSala(completa.funcion.salaId),
                        salas.observarOcupadas(funcionId)
                    ) { mapa, ocupadas ->
                        // Se descarta de la seleccion cualquier butaca que se haya
                        // ocupado mientras el usuario tenia la pantalla abierta.
                        val idsOcupadas = ocupadas.map { it.id }.toSet()
                        val perdidas = _seleccion.value intersect idsOcupadas
                        val seleccionVigente = _seleccion.value - idsOcupadas
                        _seleccion.value = seleccionVigente
                        // Perder butacas sin avisar seria el peor resultado posible
                        // para el usuario: creeria que las sigue teniendo elegidas.
                        if (perdidas.isNotEmpty()) {
                            avisoPendiente = if (perdidas.size == 1) {
                                "Una butaca de tu seleccion acaba de ocuparse"
                            } else {
                                "${perdidas.size} butacas de tu seleccion acaban de ocuparse"
                            }
                        }
                        construir(completa, mapa, ocupadas, seleccionVigente)
                    }
                }
            }
        )
    }

    private fun construir(
        completa: FuncionCompleta,
        mapa: List<ButacaEntity>,
        ocupadas: List<ButacaEntity>,
        seleccion: Set<Int>
    ): SeleccionButacasUiState {
        val idsOcupadas = ocupadas.map { it.id }.toSet()

        val seleccionables: List<ButacaSeleccionable> = mapa.map { butaca ->
            val estado = when {
                idsOcupadas.contains(butaca.id) -> EstadoButaca.OCUPADA
                seleccion.contains(butaca.id) -> EstadoButaca.SELECCIONADA
                else -> EstadoButaca.LIBRE
            }
            ButacaSeleccionable(butaca = butaca, estado = estado)
        }

        contexto = ContextoButacas(completa = completa, butacas = mapa, ocupadas = ocupadas)

        val cantidad = seleccionables.count { it.seleccionada }
        val total = CalculoReserva.calcularTotal(completa.funcion.precioEntrada, cantidad)

        val aviso = avisoPendiente
        avisoPendiente = null

        return SeleccionButacasUiState.Exito(
            funcion = completa.funcion,
            pelicula = completa.pelicula,
            sede = completa.sede,
            sala = completa.sala,
            butacas = seleccionables,
            precioEntrada = completa.funcion.precioEntrada,
            total = total,
            aviso = aviso
        )
    }

    private fun reconstruir() {
        val contextoActual = contexto ?: return
        val estadoActual = _uiState.value
        if (estadoActual !is SeleccionButacasUiState.Exito) return
        _uiState.value = construir(
            completa = contextoActual.completa,
            mapa = contextoActual.butacas,
            ocupadas = contextoActual.ocupadas,
            seleccion = _seleccion.value
        )
    }

    private fun avisar(mensaje: String) {
        avisoPendiente = mensaje
        reconstruir()
    }

    /**
     * Foto del mapa tal y como lo devolvio Room, sin seleccion aplicada.
     * Permite reconstruir el estado tras un cambio de seleccion sin volver a
     * consultar la base de datos.
     */
    private class ContextoButacas(
        val completa: FuncionCompleta,
        val butacas: List<ButacaEntity>,
        val ocupadas: List<ButacaEntity>
    ) {
        fun buscar(butacaId: Int): ButacaEntity? = butacas.firstOrNull { it.id == butacaId }

        fun ocupada(butacaId: Int): Boolean = ocupadas.any { it.id == butacaId }
    }
}

/** FASE 5 - Fabrica de la pantalla de butacas. */
class SeleccionButacasViewModelFactory(private val database: CineMaxClienteDatabase) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SeleccionButacasViewModel::class.java)) {
            "SeleccionButacasViewModelFactory solo crea ${SeleccionButacasViewModel::class.java.simpleName}"
        }
        return SeleccionButacasViewModel(
            funciones = RepositoriosCliente.funciones(database),
            salas = RepositoriosCliente.salas(database)
        ) as T
    }

    companion object {
        fun desde(context: Context): SeleccionButacasViewModelFactory =
            SeleccionButacasViewModelFactory(DatabaseProvider.obtener(context))
    }
}
