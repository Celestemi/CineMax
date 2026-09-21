package com.cinemax.peru.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun <T> CampoSeleccionGenerico(
    label: String,
    opciones: List<T>,
    seleccionado: T?,
    etiqueta: (T) -> String,
    onSeleccion: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            OutlinedButton(
                onClick = { expandido = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (seleccionado != null) etiqueta(seleccionado) else "Seleccionar",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            DropdownMenu(
                expanded = expandido,
                onDismissRequest = { expandido = false }
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(etiqueta(opcion)) },
                        onClick = {
                            onSeleccion(opcion)
                            expandido = false
                        }
                    )
                }
            }
        }
    }
}