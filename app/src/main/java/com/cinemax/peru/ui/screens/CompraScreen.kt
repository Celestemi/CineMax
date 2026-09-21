package com.cinemax.peru.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.cinemax.peru.model.Butaca
import com.cinemax.peru.state.EstadoCompra
import com.cinemax.peru.viewmodel.ReservaViewModel

@Composable
fun CompraScreen(
    viewModel: ReservaViewModel,
    onCompraExitosa: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.estadoCompra) {
        if (uiState.estadoCompra == EstadoCompra.COMPLETADA) {
            onCompraExitosa()
        }
    }

    var numeroTarjeta by rememberSaveable { mutableStateOf("") }
    var titular by rememberSaveable { mutableStateOf("") }
    var vencimiento by rememberSaveable { mutableStateOf("") }
    var cvv by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onVolver) {
                Text("← Butacas")
            }
            Text(
                text = "Confirmar compra",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        val funcion = uiState.funcionSeleccionada
        if (funcion == null) {
            MensajeSinDatosCompra(onVolver, Modifier.fillMaxSize())
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resumen de compra",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FilaResumen("Película", funcion.pelicula.titulo)
                        FilaResumen("Fecha", funcion.fecha)
                        FilaResumen("Hora", funcion.hora)
                        FilaResumen("Sede", funcion.sede.nombre)
                        FilaResumen("Sala", funcion.sala.nombre)
                        FilaResumen("Butacas", textoButacas(uiState.butacasSeleccionadas))
                        FilaResumen("Entradas", uiState.butacasSeleccionadas.size.toString())
                        FilaResumen("Precio unitario", "S/ %.2f".format(funcion.precio))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Método de pago (simulación)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Datos ficticios: no se procesa ningún pago real.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = numeroTarjeta,
                    onValueChange = { numeroTarjeta = it },
                    label = { Text("Número de tarjeta") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = titular,
                    onValueChange = { titular = it },
                    label = { Text("Nombre del titular") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = vencimiento,
                        onValueChange = { vencimiento = it },
                        label = { Text("Vencimiento (MM/AA)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { cvv = it },
                        label = { Text("CVV") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.estadoCompra == EstadoCompra.ERROR && uiState.mensaje != null) {
                    Text(
                        text = uiState.mensaje.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Surface(shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "S/ %.2f".format(uiState.total),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.estadoCompra == EstadoCompra.COMPRANDO) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Procesando pago...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.confirmarCompra(
                                    numeroTarjeta = numeroTarjeta,
                                    titular = titular,
                                    vencimiento = vencimiento,
                                    cvv = cvv
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirmar compra")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaResumen(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

private fun textoButacas(butacas: List<Butaca>): String =
    butacas.joinToString(", ") { "${it.fila}${it.numero}" }

@Composable
private fun MensajeSinDatosCompra(onVolver: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No hay información de compra disponible.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = onVolver) {
                Text("Volver")
            }
        }
    }
}