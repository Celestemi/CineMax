package com.cinemax.admin.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinemax.admin.model.UsuarioSesion
import com.cinemax.admin.ui.EtiquetasPrueba
import com.cinemax.admin.ui.theme.CineOro

/**
 * FASE 8 - Pantalla inicial del panel de administracion.
 *
 * Es SOLO el punto de entrada para la FASE 9: no implementa CRUD de funciones ni
 * salas, ni consulta de ocupacion. Deja preparados los espacios para esas tres
 * areas, de modo que la fase siguiente solo tenga que conectar cada tarjeta.
 */
@Composable
fun HomeAdminScreen(
    sesion: UsuarioSesion,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .testTag(EtiquetasPrueba.PANTALLA_HOME)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CINEMAX ADMIN",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Panel de administración",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            TextButton(
                onClick = onCerrarSesion,
                modifier = Modifier.testTag(EtiquetasPrueba.CERRAR_SESION)
            ) {
                Text("Cerrar sesión", color = CineOro)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Bienvenido, ${sesion.nombreCompleto}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Sesión iniciada como ${sesion.rol.name} · @${sesion.usuario}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Gestión",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        EspacioAdministrativo(
            titulo = "Gestión de funciones",
            descripcion = "Cartelera, horarios y salas de cada función.",
            etiqueta = EtiquetasPrueba.ACCION_FUNCIONES
        )
        Spacer(modifier = Modifier.height(12.dp))
        EspacioAdministrativo(
            titulo = "Gestión de salas",
            descripcion = "Sedes, salas y su capacidad de butacas.",
            etiqueta = EtiquetasPrueba.ACCION_SALAS
        )
        Spacer(modifier = Modifier.height(12.dp))
        EspacioAdministrativo(
            titulo = "Ocupación",
            descripcion = "Consulta de reservas y butacas ocupadas.",
            etiqueta = EtiquetasPrueba.ACCION_OCUPACION
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Estas secciones se implementarán en la FASE 9.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

/**
 * Espacio reservado para una seccion administrativa futura. Cada tarjeta lleva su
 * `testTag` para poder afirmar en las pruebas que el Home las ofrece, sin
 * implementar todavia su comportamiento.
 */
@Composable
private fun EspacioAdministrativo(
    titulo: String,
    descripcion: String,
    etiqueta: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(etiqueta),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "PRÓXIMAMENTE",
                style = MaterialTheme.typography.labelSmall,
                color = CineOro,
                letterSpacing = 2.sp
            )
        }
    }
}
