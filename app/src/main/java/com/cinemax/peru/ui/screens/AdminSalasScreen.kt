package com.cinemax.peru.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cinemax.peru.model.Funcion
import com.cinemax.peru.model.Sala
import com.cinemax.peru.state.OcupacionSala
import com.cinemax.peru.viewmodel.AdminCineViewModel

@Composable
fun AdminSalasScreen(
    viewModel: AdminCineViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val salas by viewModel.salas.collectAsState()
    val sedes by viewModel.sedes.collectAsState()
    val funciones by viewModel.funciones.collectAsState()
    val ocupacion by viewModel.ocupacion.collectAsState()

    var mostrarSelectorFuncion by remember { mutableStateOf(false) }

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
                Text("← Panel")
            }
            Text(
                text = "Revisar salas y ocupación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Salas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (salas.isEmpty()) {
                Text(
                    text = "No hay salas registradas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                salas.forEach { sala ->
                    SalaAdminCard(
                        sala = sala,
                        nombreSede = sedes.firstOrNull { it.id == sala.sedeId }?.nombre
                            ?: "Sede ${sala.sedeId}",
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ocupación por función",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { mostrarSelectorFuncion = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Seleccionar función")
            }
            Spacer(modifier = Modifier.height(12.dp))

            ocupacion?.let { OcupacionCard(it) }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (mostrarSelectorFuncion) {
        DialogSeleccionFuncion(
            funciones = funciones,
            onSeleccion = { funcionId ->
                viewModel.consultarOcupacion(funcionId)
                mostrarSelectorFuncion = false
            },
            onDescartar = { mostrarSelectorFuncion = false }
        )
    }
}

@Composable
private fun SalaAdminCard(
    sala: Sala,
    nombreSede: String,
    modifier: Modifier = Modifier
) {
    val ocupadas = sala.butacas.count { it.estado == com.cinemax.peru.model.EstadoButaca.OCUPADA }
    Card(onClick = {}, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${nombreSede} · ${sala.nombre}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Capacidad: ${sala.butacas.size} · Butacas ocupadas: $ocupadas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OcupacionCard(ocupacion: OcupacionSala) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = ocupacion.funcion.pelicula.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${ocupacion.funcion.fecha} · ${ocupacion.funcion.hora} · ${ocupacion.funcion.sede.nombre} · ${ocupacion.sala.nombre}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            FilaOcupacion("Capacidad", "${ocupacion.capacidad}")
            FilaOcupacion("Butacas ocupadas", "${ocupacion.ocupadas}")
            FilaOcupacion("Butacas disponibles", "${ocupacion.disponibles}")
            FilaOcupacion("Ocupación", "%.1f%%".format(ocupacion.porcentaje))
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (ocupacion.porcentaje / 100.0).toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FilaOcupacion(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DialogSeleccionFuncion(
    funciones: List<Funcion>,
    onSeleccion: (Int) -> Unit,
    onDescartar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDescartar,
        title = { Text("Elegir función") },
        text = {
            if (funciones.isEmpty()) {
                Text(
                    text = "No hay funciones disponibles.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(funciones, key = { it.id }) { funcion ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSeleccion(funcion.id) }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = funcion.pelicula.titulo,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${funcion.fecha} · ${funcion.hora} · ${funcion.sede.nombre} · ${funcion.sala.nombre}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDescartar) {
                Text("Cancelar")
            }
        }
    )
}