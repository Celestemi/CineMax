package com.cinemax.cliente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinemax.cliente.data.local.DatabaseProvider
import com.cinemax.cliente.data.repository.AuthRepository
import com.cinemax.cliente.data.repository.ResultadoLogin
import com.cinemax.cliente.data.repository.ResultadoRegistro
import com.cinemax.cliente.data.repository.aSesion
import com.cinemax.cliente.state.AuthUiState
import com.cinemax.cliente.state.ErroresFormulario
import com.cinemax.cliente.state.RegistroUiState
import com.cinemax.cliente.state.RolCineMax
import com.cinemax.cliente.state.UsuarioSesion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * FASE 4 - ViewModel real de autenticacion de CINEMAX-CLIENTE.
 *
 * Sustituye a `AuthViewModelStub`. No contiene ninguna consulta a Room: delega
 * todo en [AuthRepository] y expone estado listo para la UI.
 *
 * SESION: [sesion] mantiene el usuario autenticado en memoria. Es suficiente
 * para esta fase; el punto de extension para DataStore es [sesion] (todo lo que
 * navegue deberia leer de ahi, no de Room).
 *
 * [alcance] solo existe para los tests: en la app es `viewModelScope`
 * (Dispatchers.Main). Inyectarlo permite observar los estados finales sin
 * depender del looper de instrumentacion.
 */
class AuthViewModel(
    private val repositorio: AuthRepository,
    private val reloj: () -> Long = { System.currentTimeMillis() },
    private val alcance: CoroutineScope? = null
) : ViewModel() {

    private val ambito: CoroutineScope get() = alcance ?: viewModelScope

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Inicial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _sesion = MutableStateFlow<UsuarioSesion?>(null)
    val sesion: StateFlow<UsuarioSesion?> = _sesion.asStateFlow()

    private val _registroUiState = MutableStateFlow(RegistroUiState())
    val registroUiState: StateFlow<RegistroUiState> = _registroUiState.asStateFlow()

    // ------------------------------------------------------------------
    // Login
    // ------------------------------------------------------------------

    fun iniciarSesion(usuario: String, contrasena: String) {
        val usuarioLimpio = usuario.trim()
        ValidacionRegistro.validarLogin(usuarioLimpio, contrasena)?.let { motivo ->
            _uiState.value = AuthUiState.ValidacionInvalida(motivo)
            return
        }

        ambito.launch {
            _uiState.value = AuthUiState.Cargando
            when (val resultado = repositorio.login(usuarioLimpio, contrasena)) {
                is ResultadoLogin.Exito -> autenticar(resultado)
                ResultadoLogin.UsuarioInexistente -> _uiState.value =
                    AuthUiState.LoginIncorrecto("Usuario o contrasena incorrectos")

                ResultadoLogin.ContrasenaIncorrecta -> _uiState.value =
                    AuthUiState.LoginIncorrecto("Usuario o contrasena incorrectos")

                is ResultadoLogin.UsuarioInactivo -> _uiState.value =
                    AuthUiState.UsuarioInactivo(
                        "Tu cuenta esta desactivada. Contacta con CineMax para reactivarla."
                    )
            }
        }
    }

    /**
     * CONTROL DE ROL: un ADMINISTRador que estuviera en `cinemax_cliente.db` tiene
     * credenciales validas, pero no puede entrar en la aplicacion Cliente. El
     * rechazo ocurre ANTES de crear la sesion, de modo que nunca queda un
     * `UsuarioSesion` de un rol no autorizado.
     */
    private fun autenticar(resultado: ResultadoLogin.Exito) {
        val entidad = resultado.usuario
        val rol = RolCineMax.desde(entidad.rol)

        if (rol == null || rol != UsuarioSesion.ROL_PERMITIDO_EN_CLIENTE) {
            _sesion.value = null
            _uiState.value = AuthUiState.RolNoPermitido(
                "Esta cuenta es administrativa. Ingresa desde la aplicacion CineMax Admin."
            )
            return
        }

        val sesion = entidad.aSesion(reloj())
        _sesion.value = sesion
        _uiState.value = AuthUiState.Autenticado(sesion)
    }

    // ------------------------------------------------------------------
    // Registro
    // ------------------------------------------------------------------

    fun registrar(
        usuario: String,
        contrasena: String,
        nombreCompleto: String,
        email: String,
        telefono: String
    ) {
        val formulario = registroUiState.value
        val errores = ValidacionRegistro.validar(
            usuario = usuario,
            contrasena = contrasena,
            nombreCompleto = nombreCompleto,
            email = email,
            telefono = telefono
        )
        if (errores.hayAlguno) {
            _registroUiState.value = formulario.copy(enviando = false, errores = errores)
            _uiState.value = AuthUiState.ValidacionInvalida(
                listOfNotNull(
                    errores.usuario,
                    errores.contrasena,
                    errores.nombreCompleto,
                    errores.email,
                    errores.telefono
                ).first()
            )
            return
        }

        _registroUiState.value = formulario.copy(enviando = true, errores = ErroresFormulario.NINGUNO)
        _uiState.value = AuthUiState.Cargando

        ambito.launch {
            when (
                val resultado = repositorio.registrar(
                    usuario = usuario,
                    contrasena = contrasena,
                    nombreCompleto = nombreCompleto,
                    email = email,
                    telefono = telefono,
                    creadoEn = reloj()
                )
            ) {
                is ResultadoRegistro.Exito -> {
                    _registroUiState.value = RegistroUiState()
                    _uiState.value = AuthUiState.RegistroExitoso(
                        "Cuenta creada. Ya puedes iniciar sesion."
                    )
                }

                ResultadoRegistro.UsuarioDuplicado -> {
                    _registroUiState.value = registroUiState.value.copy(
                        enviando = false,
                        errores = registroUiState.value.errores.copy(
                            usuario = "Ese nombre de usuario ya existe"
                        )
                    )
                    _uiState.value = AuthUiState.UsuarioDuplicado(
                        "El usuario ya esta registrado. Prueba con otro."
                    )
                }

                is ResultadoRegistro.Error -> {
                    _registroUiState.value = registroUiState.value.copy(enviando = false)
                    _uiState.value = AuthUiState.ErrorRegistro(
                        "No se pudo crear la cuenta. Intentalo de nuevo."
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Formulario de registro
    // ------------------------------------------------------------------

    fun actualizarUsuarioCampo(valor: String) = actualizarFormulario { copy(usuario = valor) }
    fun actualizarContrasenaCampo(valor: String) = actualizarFormulario { copy(contrasena = valor) }
    fun actualizarNombreCampo(valor: String) = actualizarFormulario { copy(nombreCompleto = valor) }
    fun actualizarEmailCampo(valor: String) = actualizarFormulario { copy(email = valor) }
    fun actualizarTelefonoCampo(valor: String) = actualizarFormulario { copy(telefono = valor) }

    private inline fun actualizarFormulario(transform: RegistroUiState.() -> RegistroUiState) {
        _registroUiState.value = registroUiState.value
            .transform()
            .copy(enviando = false, errores = ErroresFormulario.NINGUNO)
    }

    fun limpiarFormulario() {
        _registroUiState.value = RegistroUiState()
    }

    // ------------------------------------------------------------------
    // Sesion / navegacion
    // ------------------------------------------------------------------

    fun cerrarSesion() {
        _sesion.value = null
        _uiState.value = AuthUiState.Inicial
    }

    /**
     * Vuelve a [AuthUiState.Inicial] al abandonar una pantalla.
     * Nunca borra un login ya concedido: [Autenticado] es lo que mantiene viva
     * la sesion, y `Cargando` se limpia solo cuando la consulta termina.
     */
    fun limpiarEstado() {
        val actual = _uiState.value
        if (actual is AuthUiState.Autenticado || actual is AuthUiState.Cargando) return
        if (actual is AuthUiState.Inicial) return
        _uiState.value = AuthUiState.Inicial
    }
}

/**
 * FASE 4 - Fabrica del ViewModel.
 *
 * La UI no construye el repositorio ni toca Room: recibe un `Context` de
 * `LocalContext` y la fabrica resuelve la base de datos por [DatabaseProvider].
 */
class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            "AuthViewModelFactory solo crea ${AuthViewModel::class.java.simpleName}"
        }
        val base = DatabaseProvider.obtener(context.applicationContext)
        return AuthViewModel(AuthRepository(base.usuarioDao())) as T
    }
}
