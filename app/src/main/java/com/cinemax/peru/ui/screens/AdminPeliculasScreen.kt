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
import com.cinemax.peru.model.Genero
import com.cinemax.peru.model.Pelicula
import com.cinemax.peru.ui.components.CampoSeleccionGenerico
import com.cinemax.peru.ui.components.EstadoActivoBadge
import com.cinemax.peru.viewmodel.AdminCineViewModel

@Composable
fun AdminPeliculasScreen(
    viewModel: AdminCineViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val peliculas by viewModel.peliculas.collectAsState()
    var mostrarRegistro by remember { mutableStateOf(false) }
    var peliculaEnEdicion by remember { mutableStateOf<Pelicula?>(null) }

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
                    text = "Gestionar películas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (peliculas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No hay películas registradas.",
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
                    items(peliculas, key = { it.id }) { pelicula ->
                        PeliculaAdminCard(
                            pelicula = pelicula,
                            onEditar = { peliculaEnEdicion = pelicula },
                            onAlternarActivo = {
                                if (pelicula.activo) {
                                    viewModel.desactivarPelicula(pelicula.id)
                                } else {
                                    viewModel.activarPelicula(pelicula.id)
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
        PeliculaDialog(
            peliculaOriginal = null,
            onConfirmar = { pelicula ->
                viewModel.registrarPelicula(pelicula)
                mostrarRegistro = false
            },
            onDescartar = { mostrarRegistro = false }
        )
    }

    peliculaEnEdicion?.let { pelicula ->
        PeliculaDialog(
            peliculaOriginal = pelicula,
            onConfirmar = { editada ->
                viewModel.actualizarPelicula(editada)
                peliculaEnEdicion = null
            },
            onDescartar = { peliculaEnEdicion = null }
        )
    }
}

@Composable
private fun PeliculaAdminCard(
    pelicula: Pelicula,
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
                        text = pelicula.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${pelicula.genero.nombre} · ${pelicula.clasificacionEdad} · ${pelicula.duracionMinutos} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                EstadoActivoBadge(activo = pelicula.activo)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = pelicula.sinopsis,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditar) {
                    Text("Editar")
                }
                TextButton(onClick = onAlternarActivo) {
                    Text(if (pelicula.activo) "Desactivar" else "Activar")
                }
            }
        }
    }
}

@Composable
private fun PeliculaDialog(
    peliculaOriginal: Pelicula?,
    onConfirmar: (Pelicula) -> Unit,
    onDescartar: () -> Unit
) {
    var titulo by rememberSaveable { mutableStateOf(peliculaOriginal?.titulo ?: "") }
    var genero by rememberSaveable { mutableStateOf(peliculaOriginal?.genero ?: Genero.ACCION) }
    var clasificacion by rememberSaveable { mutableStateOf(peliculaOriginal?.clasificacionEdad ?: "") }
    var duracion by rememberSaveable { mutableStateOf(peliculaOriginal?.duracionMinutos?.toString() ?: "") }
    var sinopsis by rememberSaveable { mutableStateOf(peliculaOriginal?.sinopsis ?: "") }

    val duracionValida = duracion.toIntOrNull()?.let { it > 0 } ?: false
    val habilitado = titulo.isNotBlank() && duracionValida

    AlertDialog(
        onDismissRequest = onDescartar,
        title = { Text(if (peliculaOriginal == null) "Registrar película" else "Editar película") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                CampoSeleccionGenerico(
                    label = "Género",
                    opciones = Genero.entries,
                    seleccionado = genero,
                    etiqueta = { it.nombre },
                    onSeleccion = { genero = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = clasificacion,
                    onValueChange = { clasificacion = it },
                    label = { Text("Clasificación (ej. PG-13)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = duracion,
                    onValueChange = { duracion = it },
                    label = { Text("Duración (minutos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = sinopsis,
                    onValueChange = { sinopsis = it },
                    label = { Text("Sinopsis") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmar(
                        (peliculaOriginal ?: Pelicula(
                            id = 0,
                            titulo = titulo.trim(),
                            genero = genero,
                            clasificacionEdad = clasificacion.trim(),
                            duracionMinutos = duracion.toInt(),
                            sinopsis = sinopsis.trim(),
                            posterUrl = POSTER_PLACEHOLDER,
                            trailerUrl = TRAILER_PLACEHOLDER
                        )).copy(
                            titulo = titulo.trim(),
                            genero = genero,
                            clasificacionEdad = clasificacion.trim(),
                            duracionMinutos = duracion.toInt(),
                            sinopsis = sinopsis.trim()
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

private const val POSTER_PLACEHOLDER = "https://ejemplo.com/poster.jpg"
private const val TRAILER_PLACEHOLDER = "https://www.youtube.com/watch?v=placeholder"