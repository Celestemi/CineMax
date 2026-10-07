package com.cinemax.admin.ui.auth

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinemax.admin.state.AuthUiState
import com.cinemax.admin.ui.EtiquetasPrueba
import com.cinemax.admin.ui.theme.CineOro
import com.cinemax.admin.viewmodel.AuthViewModel

/**
 * FASE 8 - Registro de administrador.
 *
 * Los valores viven en el ViewModel (`registroUiState`), no en `remember`, para
 * poder validarlos sin Compose. El rol de la cuenta creada es siempre
 * ADMINISTRADOR: la UI no ofrece elegir rol. Al completarse el alta, el grafo
 * vuelve al login (no se inicia sesion automaticamente).
 */
@Composable
fun RegistroScreen(
    viewModel: AuthViewModel,
    onVolverAlLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formulario by viewModel.registroUiState.collectAsState()
    val authUiState by viewModel.uiState.collectAsState()

    val mensajeError = when (val estado = authUiState) {
        is AuthUiState.ValidacionInvalida -> estado.mensaje
        is AuthUiState.UsuarioDuplicado -> estado.mensaje
        is AuthUiState.ErrorRegistro -> estado.mensaje
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
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "ADMIN",
                style = MaterialTheme.typography.labelLarge,
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
                text = "Te registrarás como administrador de CineMax",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = formulario.usuario,
                onValueChange = viewModel::actualizarUsuarioCampo,
                modifier = Modifier.fillMaxWidth().testTag(EtiquetasPrueba.CAMPO_USUARIO),
                label = { Text("Usuario") },
                singleLine = true,
                enabled = !formulario.enviando,
                isError = formulario.errores.usuario != null,
                supportingText = formulario.errores.usuario?.let { error -> { Text(error) } }
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = formulario.contrasena,
                onValueChange = viewModel::actualizarContrasenaCampo,
                modifier = Modifier.fillMaxWidth().testTag(EtiquetasPrueba.CAMPO_CONTRASENA),
                label = { Text("Contraseña") },
                singleLine = true,
                enabled = !formulario.enviando,
                isError = formulario.errores.contrasena != null,
                supportingText = formulario.errores.contrasena?.let { error -> { Text(error) } },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = formulario.nombreCompleto,
                onValueChange = viewModel::actualizarNombreCampo,
                modifier = Modifier.fillMaxWidth().testTag(EtiquetasPrueba.CAMPO_NOMBRE),
                label = { Text("Nombre completo") },
                singleLine = true,
                enabled = !formulario.enviando,
                isError = formulario.errores.nombreCompleto != null,
                supportingText = formulario.errores.nombreCompleto?.let { error -> { Text(error) } }
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = formulario.email,
                onValueChange = viewModel::actualizarEmailCampo,
                modifier = Modifier.fillMaxWidth().testTag(EtiquetasPrueba.CAMPO_EMAIL),
                label = { Text("Correo") },
                singleLine = true,
                enabled = !formulario.enviando,
                isError = formulario.errores.email != null,
                supportingText = formulario.errores.email?.let { error -> { Text(error) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = formulario.telefono,
                onValueChange = viewModel::actualizarTelefonoCampo,
                modifier = Modifier.fillMaxWidth().testTag(EtiquetasPrueba.CAMPO_TELEFONO),
                label = { Text("Teléfono (opcional)") },
                singleLine = true,
                enabled = !formulario.enviando,
                isError = formulario.errores.telefono != null,
                supportingText = formulario.errores.telefono?.let { error -> { Text(error) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.registrar(
                        usuario = formulario.usuario,
                        contrasena = formulario.contrasena,
                        nombreCompleto = formulario.nombreCompleto,
                        email = formulario.email,
                        telefono = formulario.telefono
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag(EtiquetasPrueba.BOTON_REGISTRO),
                enabled = !formulario.enviando
            ) {
                if (formulario.enviando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Crear cuenta")
                }
            }

            if (mensajeError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag(EtiquetasPrueba.ESTADO)
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
