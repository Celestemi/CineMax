package com.cinemax.peru.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinemax.peru.model.Butaca
import com.cinemax.peru.model.EstadoButaca
import com.cinemax.peru.model.Funcion
import com.cinemax.peru.state.ReservaUiState
import com.cinemax.peru.viewmodel.ReservaViewModel

@Composable
fun SeleccionButacasScreen(
    viewModel: ReservaViewModel,
    funcionId: Int,
    onContinuar: (Int) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(funcionId) {
        viewModel.cargarFuncion(funcionId)
    }

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
                Text("← Detalle")
            }
            Text(
                text = "Selecciona tus butacas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        val funcion = uiState.funcionSeleccionada
        when {
            uiState.cargando -> MensajeCargandoButacas(Modifier.fillMaxSize())

            uiState.mensajeError != null -> MensajeErrorButacas(
                mensaje = uiState.mensajeError.orEmpty(),
                onReintentar = { viewModel.cargarFuncion(funcionId) },
                modifier = Modifier.fillMaxSize()
            )

            funcion == null -> MensajeCargandoButacas(Modifier.fillMaxSize())

            uiState.butacas.isEmpty() -> MensajeSinAsientos(Modifier.fillMaxSize())

            else -> ContenidoSeleccion(
                uiState = uiState,
                onAlternar = viewModel::alternarButaca,
                onContinuar = { onContinuar(funcion.id) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ContenidoSeleccion(
    uiState: ReservaUiState,
    onAlternar: (Butaca) -> Unit,
    onContinuar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val funcion = uiState.funcionSeleccionada ?: return

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            InfoFuncion(funcion)
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "PANTALLA",
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.6f)
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(20.dp))

            MapaButacas(
                butacas = uiState.butacas,
                seleccionadas = uiState.butacasSeleccionadas,
                onAlternar = onAlternar
            )
            Spacer(modifier = Modifier.height(20.dp))

            LeyendaButacas()
            Spacer(modifier = Modifier.height(8.dp))
        }

        Surface(shadowElevation = 8.dp) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            ) {
                Text(
                    text = "Entradas seleccionadas: ${uiState.butacasSeleccionadas.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Precio unitario: S/ %.2f".format(funcion.precio),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                Button(
                    onClick = onContinuar,
                    enabled = uiState.butacasSeleccionadas.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continuar")
                }
            }
        }
    }
}

@Composable
private fun InfoFuncion(funcion: Funcion) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = funcion.pelicula.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${funcion.fecha} · ${funcion.hora}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${funcion.sede.nombre} · ${funcion.sala.nombre}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MapaButacas(
    butacas: List<Butaca>,
    seleccionadas: List<Butaca>,
    onAlternar: (Butaca) -> Unit
) {
    val filas = butacas.groupBy { it.fila }.toSortedMap()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        filas.forEach { (_, butacasDeFila) ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                butacasDeFila.forEach { butaca ->
                    ButacaCelda(
                        butaca = butaca,
                        seleccionada = seleccionadas.any { it.id == butaca.id },
                        onClick = { onAlternar(butaca) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ButacaCelda(
    butaca: Butaca,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    val estadoVisible = if (seleccionada) EstadoButaca.SELECCIONADA else butaca.estado
    val scheme = MaterialTheme.colorScheme
    val habilitada = estadoVisible != EstadoButaca.OCUPADA

    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colorFondoButaca(estadoVisible, scheme))
            .then(if (habilitada) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${butaca.fila}${butaca.numero}",
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorTextoButaca(estadoVisible, scheme)
        )
    }
}

@Composable
private fun LeyendaButacas() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemLeyenda(EstadoButaca.DISPONIBLE, "Disponible")
        Spacer(modifier = Modifier.width(24.dp))
        ItemLeyenda(EstadoButaca.SELECCIONADA, "Seleccionada")
        Spacer(modifier = Modifier.width(24.dp))
        ItemLeyenda(EstadoButaca.OCUPADA, "Ocupada")
    }
}

@Composable
private fun ItemLeyenda(estado: EstadoButaca, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colorFondoButaca(estado, MaterialTheme.colorScheme))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = texto, style = MaterialTheme.typography.bodySmall)
    }
}

private fun colorFondoButaca(estado: EstadoButaca, scheme: ColorScheme): Color = when (estado) {
    EstadoButaca.DISPONIBLE -> scheme.surfaceVariant
    EstadoButaca.SELECCIONADA -> scheme.primary
    EstadoButaca.OCUPADA -> scheme.onSurface.copy(alpha = 0.22f)
}

private fun colorTextoButaca(estado: EstadoButaca, scheme: ColorScheme): Color = when (estado) {
    EstadoButaca.DISPONIBLE -> scheme.onSurface.copy(alpha = 0.7f)
    EstadoButaca.SELECCIONADA -> scheme.onPrimary
    EstadoButaca.OCUPADA -> scheme.onSurface.copy(alpha = 0.2f)
}

@Composable
private fun MensajeCargandoButacas(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MensajeErrorButacas(
    mensaje: String,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = mensaje,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onReintentar) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun MensajeSinAsientos(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No hay información de asientos disponible.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}