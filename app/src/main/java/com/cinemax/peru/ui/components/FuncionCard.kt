package com.cinemax.peru.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cinemax.peru.model.Funcion
import com.cinemax.peru.model.Pelicula
import java.util.Locale

@Composable
fun FuncionCard(
    funcion: Funcion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PosterMiniatura(
                pelicula = funcion.pelicula,
                modifier = Modifier.size(width = 72.dp, height = 100.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = funcion.pelicula.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${funcion.pelicula.genero.nombre} · ${funcion.pelicula.clasificacionEdad} · ${funcion.pelicula.duracionMinutos} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${funcion.fecha}  ·  ${funcion.hora}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${funcion.sede.nombre} · ${funcion.sala.nombre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = precioFormateado(funcion.precio),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PosterMiniatura(
    pelicula: Pelicula,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp)
        ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = pelicula.titulo.take(2).uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

private fun precioFormateado(precio: Double): String =
    String.format(Locale.getDefault(), "S/ %.2f", precio)