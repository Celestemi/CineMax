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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cinemax.cliente.model.CotizacionReserva
import com.cinemax.cliente.state.ReservaUiState
import com.cinemax.cliente.ui.theme.CineOro
import com.cinemax.cliente.viewmodel.ReservaViewModel

/**
 * Seleccion heredada de la pantalla de butacas.
 *
 * Solo viajan IDENTIFICADORES y dos nombres ya resueltos, para pintar la ficha.
 * No se cruza por la RUTA (que sigue sin argumentos) ni se reconstruye desde Room:
 * al confirmar, `ReservaViewModel` vuelve a leer de la base la funcion, la
 * pelicula y las butacas, y es el quien valida que sigan disponibles.
 */
data class SeleccionDeButacas(
    val funcionId: Int,
    val butacaIds: List<Int>,
    val nombreSede: String = "",
    val nombreSala: String = ""
) {
    val vacia: Boolean get() = butacaIds.isEmpty()
}

/**
 * FASE 6 - RESUMEN / COMPRA.
 *
 * No recibe argumentos de ruta: la seleccion de butacas es TEMPORAL y vive en
 * `SeleccionButacasViewModel`, asi que serializarla en la URL seria duplicar
 * estado. Al entrar, [seleccion] le entrega a [ReservaViewModel.preparar] lo que el
 * usuario eligio en el mapa, y a partir de ahi la pantalla vive solo del
 * `uiState` del ViewModel de compra.
 *
 * ## NO hay calculo aqui
 * El total, la cantidad y el precio por entrada ya vienen calculados por
 * `CalculoReserva` dentro del ViewModel (Fase 5). Esta pantalla no multiplica
 * nada: si hiciera `precio * cantidad` existirian dos fuentes de verdad para el
 * importe y podrian discrepar.
 *
 * ## Doble pulsacion
 * El boton solo esta habilitado en `ReservaUiState.Calculando`, de modo que
 * mientras la transaccion esta abierta no se puede volver a pulsar. Ademas
 * `ReservaViewModel` ignora un `confirmar` en curso y el repositorio mantiene el
 * indice UNIQUE: son tres capas, y esta pantalla es la primera.
 *
 * El formulario de pago visual completo es de la Fase 7.
 */
@Composable
fun ResumenCompraScreen(
    viewModel: ReservaViewModel,
    seleccion: SeleccionDeButacas,
    onReservaConfirmada: (codigo: String) -> Unit,
    onVolverAButacas: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(seleccion.funcionId, seleccion.butacaIds) {
        viewModel.preparar(seleccion.funcionId, seleccion.butacaIds)
    }

    // NO se llama `viewModel.limpiar()` al salir: la pantalla de confirmacion lee el
    // MISMO `uiState` para mostrar el total y las butacas de la reserva que se
    // acaba de registrar. El estado se reinicia al volver a la cartelera, en el grafo
    // de navegacion.

    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    // La cotizacion no se pierde al pasar a `Registrando`: se recuerda la ultima
    // vista para que los importes no desaparezcan bajo los dedos del usuario.
    var ultimaCotizacion by remember { mutableStateOf<CotizacionReserva?>(null) }
    val estadoActual = estado
    LaunchedEffect(estadoActual) {
        when (val actual = estadoActual) {
            is ReservaUiState.Calculando -> ultimaCotizacion = actual.cotizacion
            // El codigo lo genera `ReservaRepository` y llega en `ReservaUiState.Exito`:
            // la navegacion usa ESE, nunca uno inventado por la pantalla.
            is ReservaUiState.Exito -> onReservaConfirmada(actual.codigo)
            else -> Unit
        }
    }

    ContenedorPantalla(EtiquetasPrueba.PANTALLA_RESUMEN, modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            CabeceraPantalla(
                titulo = "Resumen de compra",
                accion = { BotonVolver("‹ Volver", onVolver) }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                when (val actual = estado) {
                    is ReservaUiState.Inicial -> Aviso("Preparando la compra…")

                    is ReservaUiState.Cargando -> PantallaCargando("Calculando el total…")

                    is ReservaUiState.Calculando -> FichaCotizacion(
                        cotizacion = actual.cotizacion,
                        sede = seleccion.nombreSede,
                        sala = seleccion.nombreSala
                    )

                    is ReservaUiState.Registrando -> {
                        val pendiente = ultimaCotizacion
                        if (pendiente == null) {
                            PantallaCargando("Registrando la reserva…")
                        } else {
                            FichaCotizacion(
                                cotizacion = pendiente,
                                sede = seleccion.nombreSede,
                                sala = seleccion.nombreSala
                            )
                            Aviso("Guardando la reserva…")
                        }
                    }

                    is ReservaUiState.ValidacionInvalida -> Aviso(
                        texto = actual.mensaje,
                        esError = true
                    )

                    is ReservaUiState.Exito -> Aviso("Reserva registrada")

                    is ReservaUiState.Error -> Aviso(texto = actual.mensaje, esError = true)
                }
            }

            val invalido = estado as? ReservaUiState.ValidacionInvalida
            if (invalido?.requiereButacas == true) {
                OutlinedButton(
                    onClick = onVolverAButacas,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        // Tag PROPIO y no `BOTON`: los dos botones de esta pantalla
                        // comparten etiqueta, y un test que pulsase `BOTON` no sabria
                        // cual de los dos ha accionado.
                        .testTag("resumen:volverButacas")
                ) {
                    Text("Volver a elegir butacas")
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Button(
                onClick = { viewModel.confirmar() },
                enabled = estado is ReservaUiState.Calculando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 20.dp)
                    .testTag(EtiquetasPrueba.BOTON)
            ) {
                Text(
                    text = if (estado is ReservaUiState.Registrando) "Confirmando…" else "Confirmar compra",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/**
 * FASE 7 - Datos de la compra, todos YA calculados y validados por el ViewModel.
 *
 * La fecha y la hora son las de `cotizacion.funcion`, el precio por entrada es
 * `cotizacion.precioEntrada` y el total es `cotizacion.total`: los tres vienen de
 * `CalculoReserva`, no de esta pantalla.
 *
 * La sede y la sala tienen tres procedencias, en este orden: los nombres que
 * trajo la seleccion de la pantalla de butacas, los que ahora rellena
 * `CotizacionReserva` desde `FuncionCompleta`, y como ultimo recurso los ids
 * numericos. Los nombres los lee `ReservaViewModel` de la misma `FuncionCompleta`
 * que uso para validar, de modo que la UI no hace ninguna consulta nueva.
 */
@Composable
private fun FichaCotizacion(
    cotizacion: CotizacionReserva,
    sede: String,
    sala: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(EtiquetasPrueba.ESTADO),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            FilaDato("Pelicula", cotizacion.pelicula.titulo)
            val nombreSede = sede.ifEmpty { if (cotizacion.nombreSede.isNotEmpty()) cotizacion.nombreSede else "Sede #${cotizacion.funcion.sedeId}" }
            val nombreSala = sala.ifEmpty { if (cotizacion.nombreSala.isNotEmpty()) cotizacion.nombreSala else "Sala #${cotizacion.funcion.salaId}" }
            FilaDato("Sede", nombreSede)
            FilaDato("Sala", nombreSala)
            FilaDato("Fecha", cotizacion.funcion.fecha)
            FilaDato("Hora", cotizacion.funcion.hora)
            FilaDato("Butacas", cotizacion.codigosButacas.joinToString(", "))
            FilaDato("Cantidad", cotizacion.cantidad.toString())
            FilaDato("Precio por entrada", formatoMonto(cotizacion.precioEntrada))

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TOTAL",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatoMonto(cotizacion.total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CineOro,
                    modifier = Modifier.testTag(EtiquetasPrueba.TOTAL)
                )
            }
        }
    }
}

@Composable
private fun Aviso(texto: String, esError: Boolean = false, modifier: Modifier = Modifier) {
    AvisoPantalla(texto = texto, esError = esError, modifier = modifier)
}