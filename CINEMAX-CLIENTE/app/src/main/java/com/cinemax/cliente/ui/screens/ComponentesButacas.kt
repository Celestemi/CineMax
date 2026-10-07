package com.cinemax.cliente.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cinemax.cliente.model.ButacaSeleccionable
import com.cinemax.cliente.model.EstadoButaca
import com.cinemax.cliente.ui.theme.CineOro
import com.cinemax.cliente.ui.theme.CineRojo

/**
 * FASE 7 - Piezas visuales del mapa de butacas.
 *
 * Aqui no hay NINGUNA decision de negocio. El estado de cada butaca lo decide
 * `SeleccionButacasViewModel` y llega ya resuelto en
 * [ButacaSeleccionable.estado] (el enum [EstadoButaca] de `model`, aprobado en
 * Fase 5). Este fichero solo traduce ese estado a un color y a un borde, y por eso
 * NO vuelve a declarar ningun enum: usar el de `model` garantiza que la UI y el
 * ViewModel no puedan discrepar sobre que significa "ocupada".
 */
@Composable
fun LeyendaButacas(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemLeyenda(EstadoButaca.LIBRE, "Libre")
        ItemLeyenda(EstadoButaca.SELECCIONADA, "Seleccionada")
        ItemLeyenda(EstadoButaca.OCUPADA, "Ocupada")
    }
}

@Composable
private fun ItemLeyenda(estado: EstadoButaca, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.testTag(EtiquetasPrueba.leyenda(estado.name))
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colorDe(estado))
                .border(1.dp, bordeDe(estado), RoundedCornerShape(4.dp))
        )
        Box(modifier = Modifier.width(6.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
        )
    }
}

/**
 * FASE 7 - Butaca individual del mapa.
 *
 * Recibe el [ButacaSeleccionable] tal cual llega del ViewModel. La butaca se
 * pinta con su CODIGO real de Room ("A1", "H10") en vez de solo el numero, para que
 * el código que luego aparece en el resumen y en la confirmacion se pueda
 * comprobar a simple vista, y el `testTag` es `butaca:<codigo>` para lo mismo.
 *
 * Una butaca ocupada se dibuja DESACTIVADA (`clickable(enabled = false)`) en vez
 * de ignorarse el toque: el mapa sigue respondiendo y la butaca es realmente
 * inalcanzable. El aviso de "ya ocupada" lo publica
 * `SeleccionButacasViewModel`, que es quien sabe por que se rechazo.
 */
@Composable
fun BotonButaca(
    butaca: ButacaSeleccionable,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado = butaca.estado
    Box(
        modifier = modifier
            .size(width = 36.dp, height = 32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colorDe(estado))
            .border(
                width = if (estado == EstadoButaca.SELECCIONADA) 2.dp else 1.dp,
                color = bordeDe(estado),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(enabled = butaca.seleccionable, onClick = onClick)
            .testTag(EtiquetasPrueba.butaca(butaca.codigo)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = butaca.codigo,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (estado == EstadoButaca.SELECCIONADA) FontWeight.Bold else FontWeight.Normal,
            color = textoDe(estado),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * FASE 7 - Cartel de "ESCENARIO" que encabeza el mapa.
 *
 * Situa la pantalla y evita que la fila A (la mas cercana a ella) parezca la
 * primera fila de un almacen. No es un dato de Room: es orientacion visual.
 */
@Composable
fun CartelEscenario(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "— ESCENARIO —",
            style = MaterialTheme.typography.labelSmall,
            color = CineOro.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(CineOro.copy(alpha = 0.12f))
                .padding(horizontal = 32.dp, vertical = 4.dp)
        )
    }
}

/** Relleno de la butaca segun su estado. */
internal fun colorDe(estado: EstadoButaca): Color = when (estado) {
    EstadoButaca.LIBRE -> Color(0xFF2A2A2A)
    EstadoButaca.SELECCIONADA -> CineOro
    EstadoButaca.OCUPADA -> CineRojo.copy(alpha = 0.4f)
}

/** Contorno de la butaca segun su estado. */
internal fun bordeDe(estado: EstadoButaca): Color = when (estado) {
    EstadoButaca.LIBRE -> CineOro.copy(alpha = 0.45f)
    EstadoButaca.SELECCIONADA -> CineOro
    EstadoButaca.OCUPADA -> CineRojo.copy(alpha = 0.85f)
}

/** Texto de la butaca segun su estado. */
internal fun textoDe(estado: EstadoButaca): Color = when (estado) {
    EstadoButaca.LIBRE -> CineOro.copy(alpha = 0.9f)
    EstadoButaca.SELECCIONADA -> CineOro
    EstadoButaca.OCUPADA -> Color.White.copy(alpha = 0.7f)
}
