package com.cinemax.peru.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.cinemax.peru.model.Funcion
import com.cinemax.peru.model.Sede
import com.cinemax.peru.state.CarteleraUiState
import com.cinemax.peru.state.EstadoCartelera
import com.cinemax.peru.ui.components.FuncionCard
import com.cinemax.peru.viewmodel.CarteleraViewModel

@Composable
fun CarteleraScreen(
    viewModel: CarteleraViewModel,
    onSeleccionarFuncion: (Funcion) -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cartelera",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onCerrarSesion) {
                Text("Salir")
            }
        }

        OutlinedTextField(
            value = uiState.textoBusqueda,
            onValueChange = viewModel::buscar,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar por película") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        FilaFiltros(
            etiqueta = "Género",
            opciones = opcionesGenero(uiState, viewModel)
        )
        Spacer(modifier = Modifier.height(12.dp))

        FilaFiltros(
            etiqueta = "Fecha",
            opciones = opcionesFecha(uiState, viewModel)
        )
        Spacer(modifier = Modifier.height(12.dp))

        FilaFiltros(
            etiqueta = "Sede",
            opciones = opcionesSede(uiState, viewModel)
        )

        if (hayFiltrosActivos(uiState)) {
            TextButton(onClick = viewModel::limpiarFiltros) {
                Text("Limpiar filtros")
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (uiState.estado) {
                EstadoCartelera.INICIAL, EstadoCartelera.CARGANDO ->
                    MensajeCargando(Modifier.fillMaxSize())

                EstadoCartelera.VACIO ->
                    MensajeVacio(
                        mostrarLimpiar = hayFiltrosActivos(uiState),
                        onLimpiar = viewModel::limpiarFiltros,
                        modifier = Modifier.fillMaxSize()
                    )

                EstadoCartelera.ERROR ->
                    MensajeError(
                        mensaje = uiState.mensajeError,
                        onReintentar = viewModel::cargarCartelera,
                        modifier = Modifier.fillMaxSize()
                    )

                EstadoCartelera.CARGADO ->
                    ListaFunciones(
                        funciones = uiState.funcionesFiltradas,
                        onSeleccionarFuncion = onSeleccionarFuncion
                    )
            }
        }
    }
}

@Composable
private fun FilaFiltros(
    etiqueta: String,
    opciones: List<OpcionFiltro>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(opciones) { opcion ->
                FilterChip(
                    selected = opcion.seleccionado,
                    onClick = opcion.onSeleccionar,
                    label = { Text(opcion.texto) }
                )
            }
        }
    }
}

@Composable
private fun ListaFunciones(
    funciones: List<Funcion>,
    onSeleccionarFuncion: (Funcion) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(funciones, key = { it.id }) { funcion ->
            FuncionCard(
                funcion = funcion,
                onClick = { onSeleccionarFuncion(funcion) }
            )
        }
    }
}

@Composable
private fun MensajeCargando(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MensajeVacio(
    mostrarLimpiar: Boolean,
    onLimpiar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No se encontraron funciones.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        if (mostrarLimpiar) {
            TextButton(onClick = onLimpiar) {
                Text("Limpiar filtros")
            }
        }
    }
}

@Composable
private fun MensajeError(
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

private data class OpcionFiltro(
    val texto: String,
    val seleccionado: Boolean,
    val onSeleccionar: () -> Unit
)

private fun opcionesGenero(
    uiState: CarteleraUiState,
    viewModel: CarteleraViewModel
): List<OpcionFiltro> {
    val generos = uiState.peliculas.map { it.genero }.distinctBy { it.name }
    val opciones = mutableListOf(
        OpcionFiltro(
            texto = "Todos",
            seleccionado = uiState.generoSeleccionado == null,
            onSeleccionar = { viewModel.filtrarPorGenero(null) }
        )
    )
    generos.forEach { genero ->
        opciones += OpcionFiltro(
            texto = genero.nombre,
            seleccionado = uiState.generoSeleccionado == genero,
            onSeleccionar = { viewModel.filtrarPorGenero(genero) }
        )
    }
    return opciones
}

private fun opcionesFecha(
    uiState: CarteleraUiState,
    viewModel: CarteleraViewModel
): List<OpcionFiltro> {
    val fechas = uiState.funciones.map { it.fecha }.distinct().sorted()
    val opciones = mutableListOf(
        OpcionFiltro(
            texto = "Todas",
            seleccionado = uiState.fechaSeleccionada == null,
            onSeleccionar = { viewModel.filtrarPorFecha(null) }
        )
    )
    fechas.forEach { fecha ->
        opciones += OpcionFiltro(
            texto = fecha,
            seleccionado = uiState.fechaSeleccionada == fecha,
            onSeleccionar = { viewModel.filtrarPorFecha(fecha) }
        )
    }
    return opciones
}

private fun opcionesSede(
    uiState: CarteleraUiState,
    viewModel: CarteleraViewModel
): List<OpcionFiltro> {
    val opciones = mutableListOf(
        OpcionFiltro(
            texto = "Todas",
            seleccionado = uiState.sedeSeleccionada == null,
            onSeleccionar = { viewModel.filtrarPorSede(null) }
        )
    )
    uiState.sedes.forEach { sede: Sede ->
        opciones += OpcionFiltro(
            texto = sede.nombre,
            seleccionado = uiState.sedeSeleccionada == sede.id,
            onSeleccionar = { viewModel.filtrarPorSede(sede.id) }
        )
    }
    return opciones
}

private fun hayFiltrosActivos(uiState: CarteleraUiState): Boolean =
    uiState.generoSeleccionado != null ||
        uiState.fechaSeleccionada != null ||
        uiState.sedeSeleccionada != null ||
        uiState.textoBusqueda.isNotBlank()