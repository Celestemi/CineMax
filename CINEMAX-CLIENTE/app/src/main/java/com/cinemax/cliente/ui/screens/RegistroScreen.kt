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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinemax.cliente.state.AuthUiState
import com.cinemax.cliente.ui.theme.CineOro
import com.cinemax.cliente.viewmodel.AuthViewModel

/**
 * FASE 4 - Alta de cuentas de Cliente.
 *
 * El rol no aparece en el formulario: [AuthViewModel] siempre graba
 * `RolCineMax.CLIENTE`. Tampoco hay forma de elegir `activo`.
 *
 * La pantalla no consulta Room; todo pasa por el ViewModel.
 */
@Composable
fun RegistroScreen(
    viewModel: AuthViewModel,
    onRegistroExitoso: () -> Unit,
    onVolverAlLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formulario by viewModel.registroUiState.collectAsState()
    val authUiState by viewModel.uiState.collectAsState()
    val teclado = LocalSoftwareKeyboardController.current

    LaunchedEffect(authUiState) {
        if (authUiState is AuthUiState.RegistroExitoso) onRegistroExitoso()
    }

    val mensajeError = when (val estado = authUiState) {
        is AuthUiState.UsuarioDuplicado -> estado.mensaje
        is AuthUiState.ErrorRegistro -> estado.mensaje
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
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CineMax",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "PERÚ",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = CineOro,
                letterSpacing = 6.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Crea tu cuenta",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Te registrarás como cliente de CineMax",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = formulario.usuario,
                onValueChange = viewModel::actualizarUsuarioCampo,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Usuario") },
                isError = formulario.errores.usuario != null,
                supportingText = formulario.errores.usuario?.let { { Text(it) } },
                singleLine = true,
                enabled = !formulario.enviando
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = formulario.contrasena,
                onValueChange = viewModel::actualizarContrasenaCampo,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contraseña") },
                isError = formulario.errores.contrasena != null,
                supportingText = formulario.errores.contrasena?.let { { Text(it) } },
                singleLine = true,
                enabled = !formulario.enviando,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = formulario.nombreCompleto,
                onValueChange = viewModel::actualizarNombreCampo,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre completo") },
                isError = formulario.errores.nombreCompleto != null,
                supportingText = formulario.errores.nombreCompleto?.let { { Text(it) } },
                singleLine = true,
                enabled = !formulario.enviando
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = formulario.email,
                onValueChange = viewModel::actualizarEmailCampo,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                isError = formulario.errores.email != null,
                supportingText = formulario.errores.email?.let { { Text(it) } },
                singleLine = true,
                enabled = !formulario.enviando,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = formulario.telefono,
                onValueChange = viewModel::actualizarTelefonoCampo,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Teléfono") },
                isError = formulario.errores.telefono != null,
                supportingText = formulario.errores.telefono?.let { { Text(it) } },
                singleLine = true,
                enabled = !formulario.enviando,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    teclado?.hide()
                    viewModel.registrar(
                        usuario = formulario.usuario.trim(),
                        contrasena = formulario.contrasena,
                        nombreCompleto = formulario.nombreCompleto,
                        email = formulario.email,
                        telefono = formulario.telefono
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !formulario.enviando
            ) {
                if (formulario.enviando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Registrarme", style = MaterialTheme.typography.titleMedium)
                }
            }

            if (mensajeError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¿Ya tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                TextButton(
                    onClick = onVolverAlLogin,
                    enabled = !formulario.enviando
                ) {
                    Text("Inicia sesión", color = CineOro)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Limitación académica: la contraseña se guarda en texto plano, " +
                    "sin cifrado. Es una decisión del coursework, no una práctica de producción.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}
