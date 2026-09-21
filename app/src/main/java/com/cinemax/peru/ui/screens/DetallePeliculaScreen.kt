package com.cinemax.peru.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cinemax.peru.model.Funcion
import com.cinemax.peru.state.DetallePeliculaUiState
import com.cinemax.peru.state.EstadoDetalle
import com.cinemax.peru.viewmodel.DetallePeliculaViewModel
import java.util.Locale

@Composable
fun DetallePeliculaScreen(
    funcionId: Int,
    onSeleccionarFuncion: (Funcion) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = remember(funcionId) { DetallePeliculaViewModel(funcionId) }
    val uiState by viewModel.uiState.collectAsState()

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
                Text("← Cartelera")
            }
            Text(
                text = "Detalle de película",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        when (uiState.estado) {
            EstadoDetalle.INICIAL, EstadoDetalle.CARGANDO ->
                MensajeCargandoDetalle(Modifier.fillMaxSize())

            EstadoDetalle.ERROR ->
                MensajeErrorDetalle(
                    mensaje = uiState.mensajeError,
                    onReintentar = viewModel::cargarDetalle,
                    modifier = Modifier.fillMaxSize()
                )

            EstadoDetalle.CARGADO, EstadoDetalle.VACIO ->
                ContenidoDetalle(
                    uiState = uiState,
                    onSeleccionarFuncion = onSeleccionarFuncion,
                    modifier = Modifier.fillMaxSize()
                )
        }
    }
}

@Composable
private fun ContenidoDetalle(
    uiState: DetallePeliculaUiState,
    onSeleccionarFuncion: (Funcion) -> Unit,
    modifier: Modifier = Modifier
) {
    val pelicula = uiState.pelicula
    val contexto = LocalContext.current

    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        PosterDestacado(
            titulo = pelicula?.titulo.orEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            pelicula?.let { p ->
                Text(
                    text = p.titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${p.genero.nombre} · ${p.clasificacionEdad} · ${p.duracionMinutos} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sinopsis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = p.sinopsis,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { abrirTrailer(contexto, p.trailerUrl) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver tráiler")
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Funciones disponibles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.funcionesDePelicula.isEmpty()) {
                Text(
                    text = "No hay funciones disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                uiState.funcionesDePelicula.forEach { funcion ->
                    FuncionResumenCard(
                        funcion = funcion,
                        onClick = { onSeleccionarFuncion(funcion) },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PosterDestacado(
    titulo: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = titulo.take(3).uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun FuncionResumenCard(
    funcion: Funcion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${funcion.fecha} · ${funcion.hora}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${funcion.sede.nombre} · ${funcion.sala.nombre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "S/ ${"%.2f".format(funcion.precio)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MensajeCargandoDetalle(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MensajeErrorDetalle(
    mensaje: String?,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = mensaje ?: "Ocurrió un error inesperado.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onReintentar) {
            Text("Reintentar")
        }
    }
}

private fun abrirTrailer(contexto: Context, url: String) {
    try {
        contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(contexto, "No se pudo abrir el tráiler", Toast.LENGTH_SHORT).show()
    }
}