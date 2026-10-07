package com.cinemax.cliente.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cinemax.cliente.model.ButacaSeleccionable
import com.cinemax.cliente.model.EstadoButaca
import com.cinemax.cliente.state.SeleccionButacasUiState
import com.cinemax.cliente.ui.theme.CineOro
import com.cinemax.cliente.viewmodel.SeleccionButacasViewModel

/**
 * FASE 7 - SELECCION DE BUTACAS.
 *
 * Recibe el `funcionId` por navegacion y lo pasa a
 * [SeleccionButacasViewModel.cargar]. De alli vuelve un
 * [SeleccionButacasUiState.Exito] que ya trae el mapa con cada butaca en
 * `LIBRE`, `OCUPADA` o `SELECCIONADA`.
 *
 * ## Donde esta la decision
 * **Ninguna.** La pantalla no decide si una butaca puede tocarse: llama a
 * [SeleccionButacasViewModel.alternar] y el ViewModel es quien rechaza las ocupadas
 * y publica el aviso. Aqui solo se pinta el color que el estado ya dice. Si el
 * usuario pulsa una ocupada, el aviso llega en `estado.aviso` y se muestra sin
 * haber modificado la seleccion.
 *
 * ## La seleccion sigue siendo temporal
 * Pulsar una butaca NO escribe en Room: solo se ocupa cuando
 * `ReservaViewModel.confirmar` registra la reserva. Al pulsar "Continuar" se
 * entrega la seleccion al ViewModel de compra mediante [onContinuar].
 *
 * ## El mapa se repinta solo
 * El estado viene de un `Flow` de Room ([ButacaSeleccionable.estado]), asi que si
 * otra persona compra esta funcion mientras el usuario elige, la butaca pasa a
 * ocupada sin que haya que recargar: es el caso "alguien mas compro" resuelto.
 */
@Composable
fun SeleccionButacasScreen(
    viewModel: SeleccionButacasViewModel,
    funcionId: Int,
    onContinuar: (funcionId: Int, butacaIds: List<Int>) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(funcionId) { viewModel.cargar(funcionId) }
    DisposableEffectAlSalir(viewModel::limpiar)

    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    when (val actual = estado) {
        is SeleccionButacasUiState.Inicial,
        is SeleccionButacasUiState.Cargando -> ContenedorPantalla(EtiquetasPrueba.PANTALLA_BUTACAS) {
            PantallaCargando("Cargando mapa de butacas…")
        }

        is SeleccionButacasUiState.FuncionNoEncontrada -> ContenedorPantalla(
            EtiquetasPrueba.PANTALLA_BUTACAS
        ) {
            PantallaError(
                mensaje = "La funcion seleccionada ya no esta disponible",
                onReintentar = onVolver
            )
        }

        is SeleccionButacasUiState.Error -> ContenedorPantalla(EtiquetasPrueba.PANTALLA_BUTACAS) {
            PantallaError(
                mensaje = actual.mensaje,
                onReintentar = { viewModel.cargar(funcionId) }
            )
        }

        is SeleccionButacasUiState.Exito -> MapaButacas(
            estado = actual,
            onAlternar = viewModel::alternar,
            onContinuar = { onContinuar(funcionId, actual.seleccionadasIds.toList()) },
            onVolver = onVolver
        )
    }
}

@Composable
private fun MapaButacas(
    estado: SeleccionButacasUiState.Exito,
    onAlternar: (Int) -> Unit,
    onContinuar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(EtiquetasPrueba.PANTALLA_BUTACAS)
            .padding(horizontal = 16.dp)
    ) {
        CabeceraPantalla(
            titulo = estado.pelicula.titulo,
            subtitulo = FormatosCliente.funcionCuando(estado.funcion.fecha, estado.funcion.hora) +
                " · " + estado.sede.nombre + " · " + estado.sala.nombre,
            accion = { BotonVolver("← Volver", onVolver) }
        )

        CartelEscenario()
        LeyendaButacas()

        ContadorButacas(estado)
        Spacer(modifier = Modifier.height(6.dp))

        // El aviso lo produce el ViewModel: se muestra una vez y la siguiente
        // reconstruccion del estado ya lo ha consumido.
        val aviso = estado.aviso
        if (aviso != null) {
            Text(
                text = aviso,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag(EtiquetasPrueba.AVISO)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .testTag(EtiquetasPrueba.LISTA)
        ) {
            RejillaButacas(
                butacas = estado.butacas,
                columnas = estado.sala.columnas,
                onAlternar = onAlternar
            )
        }

        ResumenSeleccion(estado = estado)

        Button(
            onClick = onContinuar,
            enabled = estado.puedeContinuar,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(bottom = 16.dp)
                .testTag(EtiquetasPrueba.BOTON)
        ) {
            Text("Continuar al resumen", style = MaterialTheme.typography.titleMedium)
        }
    }
}

/**
 * FASE 7 - "38 libres · 12 ocupadas · 3 elegidas".
 *
 * Los tres numeros salen de [SeleccionButacasUiState] (`libres`, `ocupadas` y la
 * seleccion). Pinta tambien la capacidad total para que se vea el denominador:
 * sin el, "38 libres" no dice si es una sala de 40 o de 200.
 */
@Composable
private fun ContadorButacas(estado: SeleccionButacasUiState.Exito) {
    Text(
        text = "${estado.libres} libres · ${estado.ocupadas} ocupadas · " +
            "${estado.seleccionadas.size} elegidas de ${estado.capacidad}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(EtiquetasPrueba.CONTADOR_FUNCIONES)
    )
}

/**
 * FASE 7 - Rejilla ordenada por fila y numero, con la letra a la izquierda.
 *
 * El orden lo impone la consulta, no la vista: las butacas llegan de Room en
 * `ORDER BY fila, numero`, y `chunked` las reparte en filas de [columnas]. Con 80
 * butacas (8 filas x 10 columnas) asi caben las ocho filas en un mapa legible.
 *
 * La letra de la fila se pinta en una columna fija: sin ella, la butaca "A1" y la
 * "B1" solo se distinguen por un numero, y con el mapa entero a la vista es
 * imposible saber en que fila esta uno.
 */
@Composable
private fun RejillaButacas(
    butacas: List<ButacaSeleccionable>,
    columnas: Int,
    onAlternar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (columnas <= 0) return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        butacas.chunked(columnas).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                fila.forEach { butaca ->
                    BotonButaca(butaca = butaca, onClick = { onAlternar(butaca.id) })
                }
            }
        }
    }
}

/**
 * FASE 7 - Panel inferior con la seleccion y el importe.
 *
 * Los codigos se muestran tal cual (`A1, A2, A3`) para que el usuario pueda
 * comprobarlos contra el mapa. El importe NO se calcula aqui: lo produce
 * `CalculoReserva` dentro del ViewModel y esta pantalla solo lo pinta.
 */
@Composable
private fun ResumenSeleccion(
    estado: SeleccionButacasUiState.Exito,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Butacas: " +
                    estado.codigosSeleccionadas.joinToString(", ").ifEmpty { "ninguna" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${estado.seleccionadas.size} × " +
                        formatoMonto(estado.funcion.precioEntrada),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
                Text(
                    text = "Total: " + (estado.importe?.let { formatoMonto(it) } ?: "—"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CineOro,
                    modifier = Modifier.testTag(EtiquetasPrueba.TOTAL)
                )
            }
        }
    }
}
