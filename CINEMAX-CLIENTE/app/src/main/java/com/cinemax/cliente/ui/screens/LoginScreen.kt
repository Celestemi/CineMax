package com.cinemax.cliente.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinemax.cliente.state.AuthUiState
import com.cinemax.cliente.state.UsuarioSesion
import com.cinemax.cliente.ui.theme.CineOro
import com.cinemax.cliente.viewmodel.AuthViewModel

/**
 * FASE 4 - Login contra Room.
 *
 * No hay usuarios hardcodeados: el texto introducido viaja al ViewModel, que
 * consulta `UsuarioDao` a traves de `AuthRepository`.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginExitoso: (UsuarioSesion) -> Unit,
    onIrARegistro: () -> Unit,
    modifier: Modifier = Modifier
) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var loginNotificado by remember { mutableStateOf(false) }
    val authUiState by viewModel.uiState.collectAsState()
    val teclado = LocalSoftwareKeyboardController.current

    LaunchedEffect(authUiState) {
        val autenticado = authUiState as? AuthUiState.Autenticado
        if (autenticado != null && !loginNotificado) {
            loginNotificado = true
            onLoginExitoso(autenticado.usuario)
        }
    }

    val cargando = authUiState is AuthUiState.Cargando
    val mensajeError = when (val estado = authUiState) {
        is AuthUiState.LoginIncorrecto -> estado.mensaje
        is AuthUiState.UsuarioInactivo -> estado.mensaje
        is AuthUiState.RolNoPermitido -> estado.mensaje
        is AuthUiState.ValidacionInvalida -> estado.mensaje
        else -> null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CineMax",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "PERÚ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = CineOro,
                letterSpacing = 6.sp
            )
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Inicia sesión",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = usuario,
                onValueChange = { usuario = it },
                modifier = Modifier.fillMaxWidth().testTag(EtiquetasPrueba.CAMPO_USUARIO),
                label = { Text("Usuario") },
                singleLine = true,
                enabled = !cargando
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                modifier = Modifier.fillMaxWidth().testTag(EtiquetasPrueba.CAMPO_CONTRASENA),
                label = { Text("Contraseña") },
                singleLine = true,
                enabled = !cargando,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        loginNotificado = false
                        viewModel.iniciarSesion(usuario.trim(), contrasena)
                    }
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    teclado?.hide()
                    loginNotificado = false
                    viewModel.iniciarSesion(usuario.trim(), contrasena)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Iniciar sesión", style = MaterialTheme.typography.titleMedium)
                }
            }

            if (mensajeError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¿Aún no tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                TextButton(
                    onClick = onIrARegistro,
                    enabled = !cargando
                ) {
                    Text("Regístrate", color = CineOro)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Usuarios de demostración: cliente, andres, lucia, diego o rosa · contraseña 1234",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}
