package com.cinemax.cliente.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cinemax.cliente.state.ReservaUiState
import com.cinemax.cliente.ui.theme.CineOro
import com.cinemax.cliente.viewmodel.ReservaViewModel

/**
 * FASE 7 - CONFIRMACION de la reserva.
 *
 * Recibe el `codigo` como ARGUMENTO DE NAVEGACION y lo muestra tal cual. Ese
 * codigo no lo inventa esta pantalla: llega aqui desde
 * `ReservaUiState.Exito.codigo`, es decir del `ReservaEntity` que
 * `ReservaRepository` acaba de escribir en Room (`CodigoReserva.generar` mas el
 * indice UNIQUE de `reservas.codigo`).
 *
 * ## De donde salen el resto de datos
 * Del MISMO `ReservaViewModel`, cuyo `uiState` sigue en `Exito` con el total, las
 * butacas y la cotizacion. Si el proceso se recrease y ese estado se perdiera, la
 * pantalla muestra el codigo y un aviso en lugar de inventar cifras: es preferible
 * una pantalla incompleta a un total falso.
 *
 * ## Volver atras
 * `resumen` se elimino del back stack al navegar aqui (ver
 * [com.cinemax.cliente.ui.navigation.NavegacionCliente.irAConfirmacion]), de modo
 * que el boton de atras NO devuelve al formulario de compra. Volver a comprar
 * exigiria entrar de nuevo por el flujo `cartelera -> detalle -> butacas`.
 *
 * ## Mis reservas
 * El historial ya no es "proxima fase": el boton lleva a `MisReservasScreen`, que
 * lee las reservas REALES del usuario desde Room. Como la compra que acabo de
 * confirmarse esta en la base, aparece alli de inmediato.
 */
@Composable
fun ConfirmacionScreen(
    viewModel: ReservaViewModel,
    codigo: String,
    onVolverACartelera: () -> Unit,
    onMisReservas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val exito = estado as? ReservaUiState.Exito
    val corresponde = exito != null && exito.codigo.equals(codigo, ignoreCase = true)

    ContenedorPantalla(EtiquetasPrueba.PANTALLA_CONFIRMACION, modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            CabeceraPantalla(titulo = "¡Reserva confirmada!")

            Text(
                text = "Presenta este codigo en la boleteria de la sede",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            PanelCodigo(codigo = codigo)

            Spacer(modifier = Modifier.height(16.dp))

            if (corresponde && exito != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        FilaDato("Pelicula", exito.cotizacion.pelicula.titulo)
                        FilaDato(
                            etiqueta = "Sede",
                            valor = exito.cotizacion.nombreSede.ifEmpty {
                                "Sede #${exito.cotizacion.funcion.sedeId}"
                            }
                        )
                        FilaDato(
                            etiqueta = "Sala",
                            valor = exito.cotizacion.nombreSala.ifEmpty {
                                "Sala #${exito.cotizacion.funcion.salaId}"
                            }
                        )
                        FilaDato(
                            etiqueta = "Funcion",
                            valor = FormatosCliente.funcionCuando(
                                exito.cotizacion.funcion.fecha,
                                exito.cotizacion.funcion.hora
                            )
                        )
                        FilaDato("Butacas", exito.codigosButacas.joinToString(", "))
                        FilaDato("Cantidad", exito.cantidad.toString())
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formatoMonto(exito.total),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CineOro,
                                modifier = Modifier.testTag(EtiquetasPrueba.TOTAL)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "La compra ya esta registrada. Si pulsas atras no volveras al " +
                        "formulario: la operacion no puede repetirse desde aqui.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            } else {
                AvisoPantalla(
                    texto = "El detalle de la compra ya no esta disponible en memoria, " +
                        "pero tu reserva quedo registrada con este codigo.",
                    esError = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onVolverACartelera,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag(EtiquetasPrueba.BOTON)
            ) {
                Text("Volver a la cartelera", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onMisReservas,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirmacion:misReservas")
            ) {
                Text("Ver mis reservas")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * FASE 7 - El codigo de reserva, isolated y destacado.
 *
 * Va en su propia tarjeta centrada porque es el dato que el usuario tiene que
 * dictar en taquilla: separarlo del resto de la ficha evita que se lea de un
 * vistazo y se pase por alto. El texto es EXACTAMENTE el de la reserva, con sus
 * mayusculas, porque es el mismo que se busca en `reservas.codigo`.
 */
@Composable
private fun PanelCodigo(codigo: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(EtiquetasPrueba.ESTADO),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CODIGO DE RESERVA",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = codigo,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CineOro,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag(EtiquetasPrueba.CODIGO)
            )
        }
    }
}
