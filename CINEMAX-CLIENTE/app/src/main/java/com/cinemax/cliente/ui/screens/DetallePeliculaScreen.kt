package com.cinemax.cliente.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cinemax.cliente.data.local.FuncionCompleta
import com.cinemax.cliente.state.DetallePeliculaUiState
import com.cinemax.cliente.ui.theme.CineOro
import com.cinemax.cliente.viewmodel.DetallePeliculaViewModel

/**
 * FASE 7 - DETALLE DE PELICULA.
 *
 * Recibe el `peliculaId` como ARGUMENTO DE NAVEGACION y se lo entrega a
 * [DetallePeliculaViewModel.cargar]; nunca recibe una `PeliculaEntity`. Los datos
 * vuelven por el `uiState` del ViewModel, que a su vez los pide a los
 * repositorios: la pantalla no hace ninguna consulta a Room.
 *
 * ## Ciclo de vida
 * `cargar` se llama una vez al entrar ([LaunchedEffect] con la propia id como
 * clave). Al salir se llama [DetallePeliculaViewModel.limpiar], que devuelve el
 * estado a `Inicial` y cancela la observacion de Room.
 *
 * ## Navegacion de salida
 * Al pulsar una funcion se pasa el `funcionId` a la pantalla de butacas. Ese id
 * es el unico dato que cruza la frontera: la pantalla de butacas vuelve a leer
 * funcion, pelicula, sede y sala desde su propio ViewModel, de modo que no puede
 * inventarse ni desincronizarse nada por el camino.
 *
 * ## El poster es local
 * `PeliculaEntity.posterUrl` es metadato del SeedData, pero esta app no depende de
 * Coil: el poster se dibuja con [PosterPelicula], un degradado estable derivado
 * del titulo. Ver la nota de [PosterPelicula] para el porque.
 */
@Composable
fun DetallePeliculaScreen(
    viewModel: DetallePeliculaViewModel,
    peliculaId: Int,
    onFuncionSeleccionada: (Int) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(peliculaId) { viewModel.cargar(peliculaId) }
    DisposableEffectAlSalir(viewModel::limpiar)

    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    ContenedorPantalla(EtiquetasPrueba.PANTALLA_DETALLE, modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            CabeceraPantalla(
                titulo = when (val actual = estado) {
                    is DetallePeliculaUiState.Exito -> actual.titulo
                    else -> "Pelicula"
                },
                accion = { BotonVolver("← Volver", onVolver) }
            )

            when (val actual = estado) {
                is DetallePeliculaUiState.Inicial,
                is DetallePeliculaUiState.Cargando -> PantallaCargando("Cargando pelicula…")

                is DetallePeliculaUiState.NoEncontrado -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(EtiquetasPrueba.ESTADO),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Esta pelicula ya no esta disponible",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onVolver) {
                        Text("Volver a la cartelera", color = CineOro)
                    }
                }

                is DetallePeliculaUiState.Error -> PantallaError(
                    mensaje = actual.mensaje,
                    onReintentar = { viewModel.cargar(peliculaId) }
                )

                is DetallePeliculaUiState.Exito -> DetalleCargado(
                    estado = actual,
                    onFuncionSeleccionada = onFuncionSeleccionada,
                    onVolver = onVolver
                )
            }
        }
    }
}

/**
 * FASE 7 - Cabecera con poster, ficha de datos y sinopsis, sobre la lista de
 * funciones.
 *
 * La parte de arriba se desplaza en un `Column` con scroll propio y la lista de
 * funciones en otro, para que una pelicula con 14 funciones no empuje la sinopsis
 * fuera de la pantalla.
 */
@Composable
private fun DetalleCargado(
    estado: DetallePeliculaUiState.Exito,
    onFuncionSeleccionada: (Int) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                PosterPelicula(
                    titulo = estado.pelicula.titulo,
                    modifier = Modifier
                        .size(width = 96.dp, height = 132.dp)
                        .testTag("poster:${estado.pelicula.id}")
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    FilaDato("Genero", estado.genero)
                    FilaDato("Duracion", "${estado.pelicula.duracionMinutos} minutos")
                    FilaDato("Clasificacion", estado.pelicula.clasificacionEdad)
                    estado.precioMinimo?.let { minimo ->
                        val maximo = estado.precioMaximo ?: minimo
                        FilaDato(
                            etiqueta = "Precio",
                            valor = if (minimo == maximo) {
                                formatoMonto(minimo)
                            } else {
                                "desde ${formatoMonto(minimo)}"
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "SINOPSIS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = estado.pelicula.sinopsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (estado.sinFunciones) "Sin funciones" else "Funciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!estado.sinFunciones) {
                Text(
                    text = "${estado.totalFunciones} " + plural(estado.totalFunciones, "funcion", "funciones"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        if (estado.sinFunciones) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(EtiquetasPrueba.ESTADO)
            ) {
                Text(
                    text = "Esta pelicula todavia no tiene funciones programadas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onVolver) {
                    Text("Volver a la cartelera", color = CineOro)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag(EtiquetasPrueba.LISTA),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(estado.funciones, key = { it.funcion.id }) { completa ->
                    TarjetaFuncionDetalle(
                        completa = completa,
                        onClick = { onFuncionSeleccionada(completa.funcion.id) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * FASE 7 - Funcion elegible del detalle.
 *
 * Es una fila y no la tarjeta grande de la cartelera: aqui ya se sabe que la
 * pelicula es una sola, asi que lo que importa es comparar RAPIDO fecha, hora,
 * sede y precio entre varias funciones. El `testTag` es
 * `tarjeta:funcion:<funcionId>`, el mismo que en la cartelera, para que un test
 * pueda abrir una funcion concreta sin saber de que pantalla viene.
 */
@Composable
private fun TarjetaFuncionDetalle(
    completa: FuncionCompleta,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag(EtiquetasPrueba.tarjetaFuncion(completa.funcion.id)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = FormatosCliente.funcionCuando(completa.funcion.fecha, completa.funcion.hora),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = CineOro,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = completa.sede.nombre + " · " + completa.sala.nombre,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = formatoMonto(completa.funcion.precioEntrada),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
