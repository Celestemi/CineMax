package com.cinemax.peru.state

sealed class AuthUiState {
    data object Inicial : AuthUiState()
    data object Cargando : AuthUiState()
    data class Autenticado(
        val usuario: String,
        val rol: RolCineMax
    ) : AuthUiState()
    data class Error(
        val mensaje: String
    ) : AuthUiState()
}

enum class RolCineMax {
    CLIENTE,
    ADMINISTRADOR
}