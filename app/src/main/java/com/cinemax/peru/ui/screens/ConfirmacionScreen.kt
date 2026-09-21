package com.cinemax.peru.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cinemax.peru.model.Butaca
import com.cinemax.peru.viewmodel.ReservaViewModel

@Composable
fun ConfirmacionScreen(
    viewModel: ReservaViewModel,
    onVolverACartelera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val reserva = uiState.reservaConfirmada

    if (reserva == null) {
        MensajeSinReserva(onVolverACartelera, Modifier.fillMaxSize())
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Compra confirmada",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = reserva.codigo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resumen de la compra",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FilaResumenConfirmacion("Película", reserva.funcion.pelicula.titulo)
                    FilaResumenConfirmacion("Fecha", reserva.funcion.fecha)
                    FilaResumenConfirmacion("Hora", reserva.funcion.hora)
                    FilaResumenConfirmacion("Sede", reserva.funcion.sede.nombre)
                    FilaResumenConfirmacion("Sala", reserva.funcion.sala.nombre)
                    FilaResumenConfirmacion("Butacas", textoButacasConfirmacion(reserva.butacas))
                    FilaResumenConfirmacion("Entradas", reserva.butacas.size.toString())
                    FilaResumenConfirmacion("Total", "S/ %.2f".format(reserva.total))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onVolverACartelera,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver a la cartelera")
            }
        }
    }
}

@Composable
private fun FilaResumenConfirmacion(etiqueta: String, valor: String) {
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
            textAlign = TextAlign.End
        )
    }
}

private fun textoButacasConfirmacion(butacas: List<Butaca>): String =
    butacas.joinToString(", ") { "${it.fila}${it.numero}" }

@Composable
private fun MensajeSinReserva(onVolverACartelera: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No hay una compra confirmada.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = onVolverACartelera) {
            Text("Volver a la cartelera")
        }
    }
}