package com.cinemax.peru.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cinemax.peru.viewmodel.AdminCineViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: AdminCineViewModel,
    onGestionarPeliculas: () -> Unit,
    onGestionarFunciones: () -> Unit,
    onRevisarSalas: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val peliculas by viewModel.peliculas.collectAsState()
    val funciones by viewModel.funciones.collectAsState()
    val sedes by viewModel.sedes.collectAsState()
    val salas by viewModel.salas.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Panel del administrador",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "CineMax Perú",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onCerrarSesion) {
                Text("Salir")
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaEstadistica(peliculas.size.toString(), "Películas", Modifier.weight(1f))
                TarjetaEstadistica(funciones.size.toString(), "Funciones", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaEstadistica(sedes.size.toString(), "Sedes", Modifier.weight(1f))
                TarjetaEstadistica(salas.size.toString(), "Salas", Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Opciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            TarjetaAcceso(
                titulo = "Gestionar películas",
                descripcion = "Registrar, editar y desactivar películas",
                onClick = onGestionarPeliculas
            )
            Spacer(modifier = Modifier.height(8.dp))
            TarjetaAcceso(
                titulo = "Gestionar funciones",
                descripcion = "Registrar, editar y desactivar funciones",
                onClick = onGestionarFunciones
            )
            Spacer(modifier = Modifier.height(8.dp))
            TarjetaAcceso(
                titulo = "Revisar salas",
                descripcion = "Capacidad y distribución de asientos",
                onClick = onRevisarSalas
            )
            Spacer(modifier = Modifier.height(8.dp))
            TarjetaAcceso(
                titulo = "Consultar ocupación",
                descripcion = "Butacas ocupadas y disponibilidad por función",
                onClick = onRevisarSalas
            )
        }
    }
}

@Composable
private fun TarjetaEstadistica(valor: String, etiqueta: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TarjetaAcceso(titulo: String, descripcion: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "→",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}