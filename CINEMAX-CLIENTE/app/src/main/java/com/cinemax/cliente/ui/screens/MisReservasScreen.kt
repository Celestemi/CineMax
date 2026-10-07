package com.cinemax.cliente.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cinemax.cliente.model.EstadoReserva
import com.cinemax.cliente.state.MisReservasUiState
import com.cinemax.cliente.state.ReservaListada
import com.cinemax.cliente.ui.theme.CineOro
import com.cinemax.cliente.ui.theme.CineRojo
import com.cinemax.cliente.viewmodel.MisReservasViewModel

/**
 * FASE 7 - MIS RESERVAS.
 *
 * ## De donde sale el historial
 * [MisReservasViewModel] toma el `usuarioId` del `sesion` de `AuthViewModel` y
 * lee `ReservaDao.porUsuarioConDetalle`. Esta pantalla NO recibe un `usuarioId` ni
 * pide nada por parametro: por construccion no puede mostrar el historial de otra
 * persona, ni siquiera por accidente.
 *
 * ## Que se ve
 * Solo reservas REALES: el codigo es el que se genero al confirmar, el total es el
 * que se guardo y la fecha/hora de la funcion son las de `funciones`. No hay
 * ningun dato inventado ni de ejemplo en esta pantalla.
 */
@Composable
fun MisReservasScreen(
    viewModel: MisReservasViewModel,
    onVolver: () -> Unit,
    onIrACartelera: () -> Unit,
    modifier: Modifier = Modifier,
    nombrePrueba: String = EtiquetasPrueba.PANTALLA_MIS_RESERVAS
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    ContenedorPantalla(nombre = nombrePrueba, modifier = modifier) {
        CabeceraPantalla(
            titulo = "Mis reservas",
            subtitulo = "Historial de tus compras",
            accion = { BotonVolver("← Volver", onVolver) }
        )

        when (val actual = estado) {
            is MisReservasUiState.Inicial,
            is MisReservasUiState.Cargando -> PantallaCargando("Cargando tus reservas…")

            is MisReservasUiState.Error -> PantallaError(
                mensaje = actual.mensaje,
                onReintentar = { viewModel.reintentar() }
            )

            is MisReservasUiState.Exito -> {
                ResumenHistorial(actual.total)
                Spacer(modifier = Modifier.height(8.dp))
                if (actual.vacio) {
                    HistorialVacio(onIrACartelera)
                } else {
                    ListaReservas(actual.reservas)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onIrACartelera,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("misReservas:irACartelera")
        ) {
            Text("Ir a la cartelera")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * FASE 7 - Contador del historial.
 *
 * Se cuenta con [plural] para que "1 reserva" no se lea como un bug, y solo se
 * pinta el total cuando hay alguna: en el estado vacio ya hay un mensaje que lo
 * dice mejor.
 */
@Composable
private fun ResumenHistorial(total: Int) {
    if (total == 0) return
    Text(
        text = "$total ${plural(total, "reserva", "reservas")} en tu historial",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(EtiquetasPrueba.CONTADOR_FUNCIONES)
    )
}

/**
 * FASE 7 - Usuario sin compras.
 *
 * Un historial vacio no es un error: es el estado normal de quien acaba de
 * registrarse. Se explica y se ofrece el siguiente paso en vez de dejar un hueco
 * en blanco que parece una pantalla rota.
 */
@Composable
private fun ColumnScope.HistorialVacio(onIrACartelera: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .testTag(EtiquetasPrueba.ESTADO)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Todavía no tienes reservas",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Cuando reserves una función aparecerá aquí con su código.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onIrACartelera,
            modifier = Modifier.testTag("misReservas:primeraReserva")
        ) {
            Text("Ver cartelera")
        }
    }
}

@Composable
private fun ColumnScope.ListaReservas(
    reservas: List<ReservaListada>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .weight(1f)
            .testTag(EtiquetasPrueba.LISTA),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(reservas, key = { it.codigo }) { reserva ->
            TarjetaReserva(reserva = reserva)
        }
    }
}

/**
 * FASE 7 - Una reserva del historial.
 *
 * Se muestra el codigo, que es el dato que el usuario necesita para canjearla en
 * taquilla, junto a la pelicula, la fecha de la funcion, el importe y el estado.
 * El `testTag` `tarjeta:reserva:<codigo>` permite que un test compruebe que el
 * codigo REAL que se confirmo aparece en la lista.
 */
@Composable
private fun TarjetaReserva(
    reserva: ReservaListada,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(EtiquetasPrueba.tarjetaReserva(reserva.codigo))
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = reserva.tituloPelicula,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextoEstado(reserva.estado)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = reserva.cuandoFuncion,
                style = MaterialTheme.typography.bodyMedium,
                color = CineOro
            )
            Text(
                text = "Comprada el ${FormatosCliente.fechaMedia(reserva.fechaCompra)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Codigo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = reserva.codigo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CineOro,
                        modifier = Modifier.testTag(EtiquetasPrueba.CODIGO)
                    )
                }
                Text(
                    text = formatoMonto(reserva.total),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag(EtiquetasPrueba.TOTAL)
                )
            }
        }
    }
}

/**
 * FASE 7 - Etiqueta del estado de la reserva.
 *
 * `EstadoReserva` solo tiene `CONFIRMADA` y `CANCELADA`; el color sale del enum y
 * no de comparar cadenas en el Composable.
 */
@Composable
private fun TextoEstado(estado: EstadoReserva) {
    val color = when (estado) {
        EstadoReserva.CONFIRMADA -> CineOro
        EstadoReserva.CANCELADA -> CineRojo
    }
    Text(
        text = estado.nombre,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .testTag(EtiquetasPrueba.AVISO)
    )
}
