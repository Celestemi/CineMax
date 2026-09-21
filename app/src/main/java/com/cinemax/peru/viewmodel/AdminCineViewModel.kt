package com.cinemax.peru.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinemax.peru.data.repository.FakeFuncionRepository
import com.cinemax.peru.data.repository.FakePeliculaRepository
import com.cinemax.peru.data.repository.FuncionRepository
import com.cinemax.peru.data.repository.PeliculaRepository
import com.cinemax.peru.model.EstadoButaca
import com.cinemax.peru.model.Funcion
import com.cinemax.peru.model.Pelicula
import com.cinemax.peru.model.Sala
import com.cinemax.peru.model.Sede
import com.cinemax.peru.state.OcupacionSala
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminCineViewModel(
    private val funcionRepository: FuncionRepository = FakeFuncionRepository(),
    private val peliculaRepository: PeliculaRepository = FakePeliculaRepository()
) : ViewModel() {

    private val _peliculas = MutableStateFlow<List<Pelicula>>(emptyList())
    val peliculas: StateFlow<List<Pelicula>> = _peliculas.asStateFlow()

    private val _funciones = MutableStateFlow<List<Funcion>>(emptyList())
    val funciones: StateFlow<List<Funcion>> = _funciones.asStateFlow()

    private val _salas = MutableStateFlow<List<Sala>>(emptyList())
    val salas: StateFlow<List<Sala>> = _salas.asStateFlow()

    private val _sedes = MutableStateFlow<List<Sede>>(emptyList())
    val sedes: StateFlow<List<Sede>> = _sedes.asStateFlow()

    private val _peliculasActivas = MutableStateFlow<List<Pelicula>>(emptyList())
    val peliculasActivas: StateFlow<List<Pelicula>> = _peliculasActivas.asStateFlow()

    private val _funcionesActivas = MutableStateFlow<List<Funcion>>(emptyList())
    val funcionesActivas: StateFlow<List<Funcion>> = _funcionesActivas.asStateFlow()

    private val _butacasOcupadas = MutableStateFlow(0)
    val butacasOcupadas: StateFlow<Int> = _butacasOcupadas.asStateFlow()

    private val _ocupacion = MutableStateFlow<OcupacionSala?>(null)
    val ocupacion: StateFlow<OcupacionSala?> = _ocupacion.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    init {
        cargarDatosAdministrativos()
    }

    fun cargarDatosAdministrativos() {
        viewModelScope.launch {
            _cargando.value = true
            val peliculasCargadas = peliculaRepository.obtenerTodas()
            val funcionesCargadas = funcionRepository.obtenerTodas()
            val salasCargadas = funcionesCargadas.map { it.sala }.distinctBy { it.id }
            val sedesCargadas = funcionesCargadas.map { it.sede }.distinctBy { it.id }

            _peliculas.value = peliculasCargadas
            _funciones.value = funcionesCargadas
            _salas.value = salasCargadas
            _sedes.value = sedesCargadas
            _peliculasActivas.value = peliculasCargadas.filter { it.activo }
            _funcionesActivas.value = funcionesCargadas.filter { it.activa }
            _butacasOcupadas.value = salasCargadas.sumOf { sala ->
                sala.butacas.count { it.estado == EstadoButaca.OCUPADA }
            }
            _cargando.value = false
        }
    }

    fun registrarPelicula(pelicula: Pelicula) {
        peliculaRepository.registrarPelicula(pelicula)
        cargarDatosAdministrativos()
    }

    fun actualizarPelicula(pelicula: Pelicula) {
        peliculaRepository.actualizarPelicula(pelicula)
        cargarDatosAdministrativos()
    }

    fun desactivarPelicula(peliculaId: Int) {
        peliculaRepository.desactivarPelicula(peliculaId)
        cargarDatosAdministrativos()
    }

    fun activarPelicula(peliculaId: Int) {
        peliculaRepository.activarPelicula(peliculaId)
        cargarDatosAdministrativos()
    }

    fun registrarFuncion(funcion: Funcion) {
        funcionRepository.registrarFuncion(funcion)
        cargarDatosAdministrativos()
    }

    fun actualizarFuncion(funcion: Funcion) {
        funcionRepository.actualizarFuncion(funcion)
        cargarDatosAdministrativos()
    }

    fun desactivarFuncion(funcionId: Int) {
        funcionRepository.desactivarFuncion(funcionId)
        cargarDatosAdministrativos()
    }

    fun activarFuncion(funcionId: Int) {
        funcionRepository.activarFuncion(funcionId)
        cargarDatosAdministrativos()
    }

    fun consultarOcupacion(funcionId: Int) {
        val funcion = _funciones.value.firstOrNull { it.id == funcionId }
        if (funcion == null) {
            _ocupacion.value = null
            return
        }
        val sala = funcion.sala
        val capacidad = sala.butacas.size
        val ocupadas = sala.butacas.count { it.estado == EstadoButaca.OCUPADA }
        val disponibles = sala.butacas.count { it.estado == EstadoButaca.DISPONIBLE }
        val porcentaje = if (capacidad == 0) 0.0 else ocupadas * 100.0 / capacidad
        _ocupacion.value = OcupacionSala(
            funcion = funcion,
            sala = sala,
            capacidad = capacidad,
            ocupadas = ocupadas,
            disponibles = disponibles,
            porcentaje = porcentaje
        )
    }
}