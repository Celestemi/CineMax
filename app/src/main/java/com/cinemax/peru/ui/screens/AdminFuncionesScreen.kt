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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cinemax.peru.model.Funcion
import com.cinemax.peru.model.Pelicula
import com.cinemax.peru.model.Sala
import com.cinemax.peru.model.Sede
import com.cinemax.peru.ui.components.CampoSeleccionGenerico
import com.cinemax.peru.ui.components.EstadoActivoBadge
import com.cinemax.peru.viewmodel.AdminCineViewModel

@Composable
fun AdminFuncionesScreen(
    viewModel: AdminCineViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val funciones by viewModel.funciones.collectAsState()
    val peliculas by viewModel.peliculasActivas.collectAsState()
    val sedes by viewModel.sedes.collectAsState()
    val salas by viewModel.salas.collectAsState()

    var mostrarRegistro by remember { mutableStateOf(false) }
    var funcionEnEdicion by remember { mutableStateOf<Funcion?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onVolver) {
                    Text("← Panel")
                }
                Text(
                    text = "Gestionar funciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (funciones.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No hay funciones registradas.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(funciones, key = { it.id }) { funcion ->
                        FuncionAdminCard(
                            funcion = funcion,
                            onEditar = { funcionEnEdicion = funcion },
                            onAlternarActivo = {
                                if (funcion.activa) {
                                    viewModel.desactivarFuncion(funcion.id)
                                } else {
                                    viewModel.activarFuncion(funcion.id)
                                }
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { mostrarRegistro = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Text(text = "+", style = MaterialTheme.typography.headlineMedium)
        }
    }

    if (mostrarRegistro) {
        FuncionDialog(
            funcionOriginal = null,
            peliculas = peliculas,
            sedes = sedes,
            salas = salas,
            onConfirmar = { funcion ->
                viewModel.registrarFuncion(funcion)
                mostrarRegistro = false
            },
            onDescartar = { mostrarRegistro = false }
        )
    }

    funcionEnEdicion?.let { funcion ->
        FuncionDialog(
            funcionOriginal = funcion,
            peliculas = peliculas,
            sedes = sedes,
            salas = salas,
            onConfirmar = { editada ->
                viewModel.actualizarFuncion(editada)
                funcionEnEdicion = null
            },
            onDescartar = { funcionEnEdicion = null }
        )
    }
}

@Composable
private fun FuncionAdminCard(
    funcion: Funcion,
    onEditar: () -> Unit,
    onAlternarActivo: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = funcion.pelicula.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${funcion.fecha} · ${funcion.hora}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${funcion.sede.nombre} · ${funcion.sala.nombre}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                EstadoActivoBadge(activo = funcion.activa)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditar) {
                    Text("Editar")
                }
                TextButton(onClick = onAlternarActivo) {
                    Text(if (funcion.activa) "Desactivar" else "Activar")
                }
            }
        }
    }
}

@Composable
private fun FuncionDialog(
    funcionOriginal: Funcion?,
    peliculas: List<Pelicula>,
    sedes: List<Sede>,
    salas: List<Sala>,
    onConfirmar: (Funcion) -> Unit,
    onDescartar: () -> Unit
) {
    var pelicula by remember { mutableStateOf(funcionOriginal?.pelicula) }
    var sede by remember { mutableStateOf(funcionOriginal?.sede) }
    var sala by remember { mutableStateOf(funcionOriginal?.sala) }
    var fecha by rememberSaveable { mutableStateOf(funcionOriginal?.fecha ?: "") }
    var hora by rememberSaveable { mutableStateOf(funcionOriginal?.hora ?: "") }
    var precio by rememberSaveable { mutableStateOf(funcionOriginal?.precio?.toString() ?: "") }

    val salasDeSede = if (sede == null) emptyList() else salas.filter { it.sedeId == sede!!.id }
    val precioValido = (precio.toDoubleOrNull() ?: 0.0) > 0
    val habilitado = pelicula != null &&
        sede != null &&
        sala != null &&
        fecha.isNotBlank() &&
        hora.isNotBlank() &&
        precioValido

    AlertDialog(
        onDismissRequest = onDescartar,
        title = { Text(if (funcionOriginal == null) "Registrar función" else "Editar función") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                CampoSeleccionGenerico(
                    label = "Película",
                    opciones = peliculas,
                    seleccionado = pelicula,
                    etiqueta = { it.titulo },
                    onSeleccion = { pelicula = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                CampoSeleccionGenerico(
                    label = "Sede",
                    opciones = sedes,
                    seleccionado = sede,
                    etiqueta = { it.nombre },
                    onSeleccion = { nuevaSede ->
                        sede = nuevaSede
                        sala = null
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                CampoSeleccionGenerico(
                    label = "Sala",
                    opciones = salasDeSede,
                    seleccionado = sala,
                    etiqueta = { it.nombre },
                    onSeleccion = { sala = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha (AAAA-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = hora,
                    onValueChange = { hora = it },
                    label = { Text("Hora (HH:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio (S/)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmar(
                        (funcionOriginal ?: Funcion(
                            id = 0,
                            pelicula = pelicula!!,
                            sede = sede!!,
                            sala = sala!!,
                            fecha = fecha.trim(),
                            hora = hora.trim(),
                            precio = precio.toDouble()
                        )).copy(
                            pelicula = pelicula!!,
                            sede = sede!!,
                            sala = sala!!,
                            fecha = fecha.trim(),
                            hora = hora.trim(),
                            precio = precio.toDouble()
                        )
                    )
                },
                enabled = habilitado
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDescartar) {
                Text("Cancelar")
            }
        }
    )
}