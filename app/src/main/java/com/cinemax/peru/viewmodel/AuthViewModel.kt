package com.cinemax.peru.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinemax.peru.state.AuthUiState
import com.cinemax.peru.state.RolCineMax
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Inicial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun iniciarSesion(usuario: String, contrasena: String) {
        if (usuario.isBlank() || contrasena.isBlank()) {
            _uiState.value = AuthUiState.Error("Ingresa usuario y contraseña")
            return
        }

        _uiState.value = AuthUiState.Cargando
        viewModelScope.launch {
            delay(DELAY_LOGIN)
            val rol = resolverRol(usuario, contrasena)
            if (rol == null) {
                _uiState.value = AuthUiState.Error("Credenciales no válidas")
            } else {
                _uiState.value = AuthUiState.Autenticado(
                    usuario = usuario,
                    rol = rol
                )
            }
        }
    }

    fun cerrarSesion() {
        _uiState.value = AuthUiState.Inicial
    }

    private fun resolverRol(usuario: String, contrasena: String): RolCineMax? = when {
        usuario == USUARIO_ADMIN && contrasena == PASSWORD -> RolCineMax.ADMINISTRADOR
        usuario == USUARIO_CLIENTE && contrasena == PASSWORD -> RolCineMax.CLIENTE
        else -> null
    }

    private companion object {
        const val USUARIO_ADMIN = "admin"
        const val USUARIO_CLIENTE = "cliente"
        const val PASSWORD = "1234"
        const val DELAY_LOGIN = 600L
    }
}