package com.cinemax.cliente.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cinemax.cliente.data.local.FuncionCompleta
import com.cinemax.cliente.model.DisponibilidadButacas
import com.cinemax.cliente.state.CarteleraUiState
import com.cinemax.cliente.state.UsuarioSesion
import com.cinemax.cliente.ui.theme.CineOro
import com.cinemax.cliente.viewmodel.CarteleraViewModel

/**
 * FASE 7 - HOME / CARTELERA del Cliente.
 *
 * Es la MISMA pantalla para las rutas `home` y `cartelera`: `home` es la entrada
 * tras iniciar sesion y `cartelera` el destino al que se vuelve desde la
 * confirmacion. Se evita asi mantener dos listas sincronizadas a mano.
 *
 * ## Lo que hace y lo que NO hace
 * - Observa [CarteleraUiState] de `CarteleraViewModel` y pinta sus cuatro estados:
 *   cargando, error, vacio y exito.
 * - Pinta la barra de filtros combinando genero, fecha y sede, y la
 *   disponibilidad de cada funcion.
 * - Al pulsar una funcion navega al DETALLE de su pelicula pasando el
 *   `peliculaId`. La funcion ya viene con sede, sala y precio desde Room, asi que
 *   esta pantalla no consulta nada mas.
 * - NO decide nada de negocio ni calcula nada: el total, el precio y la
 *   disponibilidad llegan ya resueltos del ViewModel. NO accede a Room.
 *
 * ## Disponibilidad sin consulta por tarjeta
 * La ocupacion se pide UNA vez para todas las funciones
 * (`CarteleraViewModel.ocupadasPorFuncion`) y aqui se combina con la capacidad de
 * cada sala en [DisponibilidadButacas]. Una consulta por tarjeta habria sido 34
 * lecturas cada vez que alguien compra en cualquier funcion.
 */
@Composable
fun CarteleraScreen(
    viewModel: CarteleraViewModel,
    sesion: UsuarioSesion,
    onPeliculaSeleccionada: (Int) -> Unit,
    onCerrarSesion: () -> Unit,
    onMisReservas: () -> Unit,
    modifier: Modifier = Modifier,
    nombrePrueba: String = EtiquetasPrueba.PANTALLA_CARTELERA
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val opciones by viewModel.opciones.collectAsStateWithLifecycle()
    val filtros by viewModel.filtros.collectAsStateWithLifecycle()
    val ocupadas by viewModel.ocupadasPorFuncion.collectAsStateWithLifecycle()

    ContenedorPantalla(nombre = nombrePrueba, modifier = modifier) {
        CabeceraPantalla(
            titulo = "Hola, ${sesion.nombreCompleto.substringBefore(' ')}",
            subtitulo = "Peliculas en cartelera",
            accion = {
                TextButton(
                    onClick = onCerrarSesion,
                    modifier = Modifier.testTag("cartelera:cerrarSesion")
                ) {
                    Text("Salir", color = CineOro)
                }
            }
        )

        // La barra se pinta siempre, tambien durante la carga: los chips se
        // rellenan solos en cuanto `opciones` llega de Room, y el usuario puede
        // dejar un filtro preparado sin esperar a la lista.
        BarraDeFiltros(
            opciones = opciones,
            filtro = filtros,
            onGenero = viewModel::filtrarPorGenero,
            onFecha = viewModel::filtrarPorFecha,
            onSede = viewModel::filtrarPorSede,
            onLimpiar = viewModel::limpiarFiltros
        )

        when (val actual = estado) {
            is CarteleraUiState.Inicial,
            is CarteleraUiState.Cargando -> PantallaCargando("Cargando cartelera…")

            is CarteleraUiState.Error -> PantallaError(
                mensaje = actual.mensaje,
                onReintentar = viewModel::recargar
            )

            is CarteleraUiState.Exito -> {
                ResumenDeCartelera(totalFunciones = actual.totalFunciones, filtro = actual.filtro)
                Spacer(modifier = Modifier.height(8.dp))

                if (actual.vacia) {
                    CarteleraVacia(
                        hayFiltros = actual.hayFiltros,
                        onLimpiarFiltros = viewModel::limpiarFiltros,
                        onReintentar = viewModel::recargar
                    )
                } else {
                    ListaFunciones(
                        funciones = actual.funciones,
                        ocupadasPorFuncion = ocupadas,
                        onPeliculaSeleccionada = onPeliculaSeleccionada
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onMisReservas,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("cartelera:misReservas")
        ) {
            Text("Mis reservas")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag(EtiquetasPrueba.BOTON)
        ) {
            Text("Cerrar sesión")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Estado vacio con dos salidas distintas.
 *
 * Sin filtros, "no hay funciones" es un problema de datos y se ofrece recargar.
 * CON filtros, casi siempre es que la combinacion elegida no existe: recargar no
 * arregla nada, asi que la accion util es quitar el filtro. Mezclar los dos
 * mensajes habria dejado al usuario pulsando un boton que no cambia nada.
 */
@Composable
private fun CarteleraVacia(
    hayFiltros: Boolean,
    onLimpiarFiltros: () -> Unit,
    onReintentar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag(EtiquetasPrueba.ESTADO),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (hayFiltros) {
                "Ninguna función coincide con los filtros"
            } else {
                "No hay funciones en cartelera por ahora"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (hayFiltros) {
            OutlinedButton(
                onClick = onLimpiarFiltros,
                modifier = Modifier.testTag("cartelera:quitarFiltros")
            ) {
                Text("Quitar filtros")
            }
        } else {
            OutlinedButton(onClick = onReintentar) { Text("Actualizar") }
        }
    }
}

/**
 * FASE 7 - Listado de funciones con disponibilidad.
 *
 * La clave de cada elemento es el `funcionId` de Room: dos funciones nunca
 * comparten clave, y ademas evita que Compose reutilice la composicion de una
 * tarjeta cuando el filtro cambia y aparece otra pelicula en su lugar.
 */
@Composable
private fun ColumnScope.ListaFunciones(
    funciones: List<FuncionCompleta>,
    ocupadasPorFuncion: Map<Int, Int>,
    onPeliculaSeleccionada: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .weight(1f)
            .testTag(EtiquetasPrueba.LISTA),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(funciones, key = { it.funcion.id }) { completa ->
            TarjetaFuncion(
                completa = completa,
                disponibilidad = DisponibilidadButacas.de(
                    capacidadSala = completa.sala.capacidad,
                    ocupadasPorFuncion = ocupadasPorFuncion,
                    funcionId = completa.funcion.id
                ),
                onClick = { onPeliculaSeleccionada(completa.pelicula.id) }
            )
        }
    }
}
