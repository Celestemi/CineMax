package com.cinemax.peru.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinemax.peru.state.AuthUiState
import com.cinemax.peru.state.RolCineMax
import com.cinemax.peru.ui.theme.CineOro
import com.cinemax.peru.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginExitoso: (RolCineMax) -> Unit,
    modifier: Modifier = Modifier
) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var loginNotificado by remember { mutableStateOf(false) }
    val authUiState by viewModel.uiState.collectAsState()

    LaunchedEffect(authUiState) {
        val autenticado = authUiState as? AuthUiState.Autenticado
        if (autenticado != null && !loginNotificado) {
            loginNotificado = true
            onLoginExitoso(autenticado.rol)
        }
    }

    val cargando = authUiState is AuthUiState.Cargando
    val mensajeError = (authUiState as? AuthUiState.Error)?.mensaje

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
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Usuario") },
                singleLine = true,
                enabled = !cargando
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contraseña") },
                singleLine = true,
                enabled = !cargando,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
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

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Prueba: cliente / 1234 · admin / 1234",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}