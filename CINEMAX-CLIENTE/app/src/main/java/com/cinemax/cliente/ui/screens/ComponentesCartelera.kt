package com.cinemax.cliente.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cinemax.cliente.data.local.FuncionCompleta
import com.cinemax.cliente.model.DisponibilidadButacas
import com.cinemax.cliente.model.FiltroCartelera
import com.cinemax.cliente.state.OpcionesFiltro
import com.cinemax.cliente.ui.theme.CineOro
import com.cinemax.cliente.ui.theme.CineRojo

/**
 * FASE 7 - Barra de filtros de la cartelera.
 *
 * ## Por que `FilterChip` y no un desplegable
 * Los tres criterios son enumeraciones cortas y cerradas (los generos, las fechas
 * y las sedes que existen DE VERDAD en `funciones`), asi que se pintan como filas
 * de chips desplazables en horizontal en vez de como tres `ExposedDropdownMenu`.
 * Gana en dos cosas: no hay que abrir un menu para ver las opciones, y cada chip
 * se puede pulsar directamente en un test de UI, sin depender de la visibilidad
 * de un menu desplegado.
 *
 * ## El filtro NUNCA se filtra aqui
 * Los chips solo avisan al `CarteleraViewModel` mediante [onGenero], [onFecha],
 * [onSede] y [onLimpiar]. Quien decide es el ViewModel, aplicando los criterios EN
 * SQL (`FuncionDao.filtrarCompletas`); este componente no compara ni un dato.
 *
 * ## Chip "Todos"
 * El primer chip de cada fila siempre vale `null` y equivale a "sin restriccion en
 * este criterio". Se ofrece siempre, para que la idea sea evidente, y las demas
 * opciones quedan inactivas mientras [OpcionesFiltro] aun no ha devuelto datos de
 * Room (durante la carga inicial no hay ningun valor que elegir).
 */
@Composable
fun BarraDeFiltros(
    opciones: OpcionesFiltro,
    filtro: FiltroCartelera,
    onGenero: (String?) -> Unit,
    onFecha: (String?) -> Unit,
    onSede: (Int?) -> Unit,
    onLimpiar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(EtiquetasPrueba.RESUMEN_FILTROS)
    ) {
        FilaChips(
            etiqueta = "Genero",
            opciones = opciones.generos,
            seleccion = filtro.genero,
            etiquetaDeOpcion = { it },
            claveDeOpcion = { it },
            onSeleccion = onGenero,
            testTagContenedor = EtiquetasPrueba.FILTRO_GENERO
        )

        FilaChips(
            etiqueta = "Fecha",
            opciones = opciones.fechas,
            seleccion = filtro.fecha,
            etiquetaDeOpcion = { etiquetaDiaRelativo(it) },
            claveDeOpcion = { it },
            onSeleccion = onFecha,
            testTagContenedor = EtiquetasPrueba.FILTRO_FECHA
        )

        FilaChips(
            etiqueta = "Sede",
            opciones = opciones.sedes.map { it.id to it.nombre },
            seleccion = filtro.sedeId,
            etiquetaDeOpcion = { (_, nombre) -> nombre },
            claveDeOpcion = { (id, _) -> id.toString() },
            // `onSeleccion` recibe la opcion de tipo `Pair<Int, String>?`; el chip
            // "Todos" entrega `null` y se traduce al `null` que espera el filtro.
            onSeleccion = { opcion -> onSede(opcion?.first) },
            testTagContenedor = EtiquetasPrueba.FILTRO_SEDE
        )

        if (filtro.hayAlguno) {
            TextButton(
                onClick = onLimpiar,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag(EtiquetasPrueba.FILTRO_LIMPIAR)
            ) {
                Text("Limpiar filtros", color = CineOro)
            }
        }
    }
}

/**
 * Una fila de chips para UN criterio.
 *
 * @param seleccion valor activo, o `null` si ese criterio no filtra.
 * @param claveDeOpcion identidad estable de la opcion; es la que va al `testTag`,
 *        de modo que cambiar el texto de la etiqueta no rompe los tests.
 */
@Composable
private fun <T> FilaChips(
    etiqueta: String,
    opciones: List<T>,
    seleccion: Any?,
    etiquetaDeOpcion: (T) -> String,
    claveDeOpcion: (T) -> String,
    onSeleccion: (T?) -> Unit,
    testTagContenedor: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTagContenedor)
            .padding(bottom = 6.dp)
    ) {
        Text(
            text = etiqueta.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = seleccion == null,
                onClick = { onSeleccion(null) },
                label = { Text("Todos") },
                colors = coloresChip(),
                modifier = Modifier.testTag(opcionFiltroTag(testTagContenedor, "TODOS"))
            )
            opciones.forEach { opcion ->
                FilterChip(
                    selected = claveDeOpcion(opcion) == seleccion.toString(),
                    onClick = { onSeleccion(opcion) },
                    label = {
                        Text(etiquetaDeOpcion(opcion), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    colors = coloresChip(),
                    modifier = Modifier.testTag(
                        opcionFiltroTag(testTagContenedor, claveDeOpcion(opcion))
                    )
                )
            }
        }
    }
}

/** `filtro:genero:ACCION` a partir del contenedor y de la clave de la opcion. */
private fun opcionFiltroTag(contenedor: String, clave: String): String =
    "$contenedor:$clave"

@Composable
private fun coloresChip() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = CineOro.copy(alpha = 0.22f),
    selectedLabelColor = MaterialTheme.colorScheme.onBackground,
    labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
)

/**
 * FASE 7 - Cabecera del listado: cuantas funciones se ven y con que filtros.
 *
 * El contador se calcula en el ViewModel ([CarteleraUiState.Exito.totalFunciones]);
 * aqui solo se redacta. Cuando hay un filtro activo se anuncia para que el usuario
 * sepa que la lista esta recortada y no se haya quedado vacia sola.
 */
@Composable
fun ResumenDeCartelera(
    totalFunciones: Int,
    filtro: FiltroCartelera,
    modifier: Modifier = Modifier
) {
    val texto = if (filtro.hayAlguno) {
        "$totalFunciones ${plural(totalFunciones, "funcion", "funciones")} con filtro"
    } else {
        "$totalFunciones ${plural(totalFunciones, "funcion", "funciones")} en cartelera"
    }
    Text(
        text = texto,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        modifier = modifier
            .fillMaxWidth()
            .testTag(EtiquetasPrueba.CONTADOR_FUNCIONES)
    )
}

/** "funcion" / "funciones" sin dependencias de plurales de recursos. */
internal fun plural(cantidad: Int, singular: String, plural: String): String =
    if (cantidad == 1) singular else plural

/**
 * FASE 7 - Tarjeta de una funcion en la cartelera.
 *
 * ## Disponibilidad basica
 * Muestra "Quedan N de M butacas" con [DisponibilidadButacas], que se calcula en
 * el ViewModel a partir del `GROUP BY` de `butaca_reservas` (una lectura para
 * todas las funciones). Si no queda ninguna, la tarjeta se marca como `agotada` y
 * avisa en lugar de dejar un contador en cero que parece un error.
 *
 * ## El `posterUrl` no se descarga
 * `PeliculaEntity.posterUrl` viene del SeedData como metadato, pero esta app no
 * depende de Coil ni de ninguna otra libreria de imagen: el poster se dibuja con
 * un degradado derivado del titulo. Es una decision de FASE 7, no una carencia:
 * anadir una libreria de red para 34 imagenes locales no compensa.
 */
@Composable
fun TarjetaFuncion(
    completa: FuncionCompleta,
    disponibilidad: DisponibilidadButacas,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag(EtiquetasPrueba.tarjetaFuncion(completa.funcion.id)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (disponibilidad.agotada) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            PosterPelicula(
                titulo = completa.pelicula.titulo,
                modifier = Modifier
                    .size(width = 72.dp, height = 96.dp)
                    .testTag("poster:${completa.pelicula.id}")
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = completa.pelicula.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = completa.pelicula.clasificacionEdad +
                        " · " + completa.pelicula.duracionMinutos + " min · " + completa.pelicula.genero,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = FormatosCliente.funcionCuando(completa.funcion.fecha, completa.funcion.hora),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = CineOro
                )
                Text(
                    text = completa.sede.nombre + " · " + completa.sala.nombre,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                EtiquetaDisponibilidad(disponibilidad)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatoMonto(completa.funcion.precioEntrada),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reservar",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = CineOro,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * FASE 7 - "Quedan N de M butacas" / "Agotada".
 *
 * El texto lo produce [DisponibilidadButacas]; aqui solo se elige el color y el
 * aviso. Con la funcion AGOTADA se avisa porque una tarjeta con 0 libres y sin
 * ninguna pista parece un fallo de la consulta.
 */
@Composable
fun EtiquetaDisponibilidad(
    disponibilidad: DisponibilidadButacas,
    modifier: Modifier = Modifier
) {
    val texto = if (disponibilidad.vacia) {
        "Sala sin butacas"
    } else if (disponibilidad.agotada) {
        "Agotada"
    } else {
        "Quedan " + disponibilidad.libres + " de " + disponibilidad.capacidad + " butacas"
    }
    val color = when {
        disponibilidad.vacia -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        disponibilidad.agotada -> CineRojo
        disponibilidad.libres <= 10 -> CineOro
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    }
    Text(
        text = texto,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.testTag(EtiquetasPrueba.AVISO)
    )
}

/**
 * FASE 7 - Poster de pelicula SIN libreria de imagenes.
 *
 * Se dibuja un degradado estable derivado del titulo (mismo titulo, mismo color,
 * en cualquier recomposicion) con las iniciales encima. Es determinista: no
 * depende de la red, no necesita Coil y un test puede afirmar el texto que
 * muestra sin esperar a que "cargue" una imagen.
 */
@Composable
fun PosterPelicula(
    titulo: String,
    modifier: Modifier = Modifier
) {
    val color = colorEstable(titulo)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(listOf(color, color.copy(alpha = 0.55f))))
            .border(1.dp, CineOro.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = iniciales(titulo),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CineOro,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

/** Iniciales del titulo: hasta dos letras, en mayusculas. */
internal fun iniciales(titulo: String): String =
    titulo.trim()
        .split(' ')
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

/**
 * Color estable a partir de un texto.
 *
 * Usa `hashCode` a proposito: la misma cadena produce SIEMPRE el mismo color, sin
 * guardar un mapa en memoria ni depender del orden en que se pintan las
 * peliculas. `abs` protege del `Int.MIN_VALUE`, cuyo absoluto sigue siendo negativo.
 */
internal fun colorEstable(texto: String): Color {
    val suma = texto.fold(0) { acumulado, caracter -> acumulado + caracter.code }
    val paleta = listOf(
        Color(0xFF1B3A5C), Color(0xFF3E2A47), Color(0xFF14453D),
        Color(0xFF4A2C2C), Color(0xFF243B6B), Color(0xFF453B14)
    )
    return paleta[Math.floorMod(suma, paleta.size)]
}
