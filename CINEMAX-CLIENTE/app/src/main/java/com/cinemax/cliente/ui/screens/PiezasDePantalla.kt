package com.cinemax.cliente.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cinemax.cliente.ui.theme.CineOro

/**
 * FASE 6 - Piezas visuales compartidas por las pantallas de navegacion.
 *
 * Son presentacion pura: ningun componente aqui lee Room, calcula precios ni
 * decide nada de negocio. Unicamente evitan que las seis pantallas repitan el
 * mismo `Column` con `systemBarsPadding`.
 */
object EtiquetasPrueba {
    const val PANTALLA_SPLASH = "pantalla:splash"
    const val PANTALLA_LOGIN = "pantalla:login"
    const val PANTALLA_REGISTRO = "pantalla:registro"
    const val PANTALLA_HOME = "pantalla:home"
    const val PANTALLA_CARTELERA = "pantalla:cartelera"
    const val PANTALLA_DETALLE = "pantalla:detalle"
    const val PANTALLA_BUTACAS = "pantalla:butacas"
    const val PANTALLA_RESUMEN = "pantalla:resumen"
    const val PANTALLA_CONFIRMACION = "pantalla:confirmacion"
    const val PANTALLA_MIS_RESERVAS = "pantalla:misReservas"

    const val TITULO = "titulo"
    const val ESTADO = "estado"
    const val LISTA = "lista"
    const val BOTON = "boton"
    const val CODIGO = "codigoReserva"
    const val TOTAL = "total"
    const val AVISO = "aviso"

    /**
     * Boton de "volver" de la cabecera. Es el MISMO componente en detalle, butacas,
     * resumen e historial, asi que lleva un tag propio: un test que lo busca por texto
     * ("< Volver", "< Volver") depende de la flecha, que es un detalle de dibujo.
     */
    const val VOLVER = "volver"

    // --- FASE 7: campos de login que maneja la prueba de navegacion ---
    const val CAMPO_USUARIO = "campo:usuario"
    const val CAMPO_CONTRASENA = "campo:contrasena"

    // --- FASE 7: controles de la barra de filtros ---
    const val FILTRO_GENERO = "filtro:genero"
    const val FILTRO_FECHA = "filtro:fecha"
    const val FILTRO_SEDE = "filtro:sede"
    const val FILTRO_LIMPIAR = "filtro:limpiar"
    const val RESUMEN_FILTROS = "resumenFiltros"
    const val CONTADOR_FUNCIONES = "contadorFunciones"

    /** `opcion:<criterio>:<valor>`, para elegir una opcion de un desplegable. */
    fun opcionFiltro(criterio: String, valor: String): String = "opcion:$criterio:$valor"

    /** `tarjeta:funcion:<funcionId>`, para abrir una funcion concreta. */
    fun tarjetaFuncion(funcionId: Int): String = "tarjeta:funcion:$funcionId"

    /** `tarjeta:reserva:<codigo>`, una linea del historial. */
    fun tarjetaReserva(codigo: String): String = "tarjeta:reserva:$codigo"

    /** `butaca:<codigo>`, tal y como lo nombra `ButacaEntity.codigo` ("A1"). */
    fun butaca(codigo: String): String = "butaca:$codigo"

    /** `leyenda:<LIBRE|SELECCIONADA|OCUPADA>`. */
    fun leyenda(estado: String): String = "leyenda:$estado"
}

/**
 * Columna raiz de una pantalla: fondo del tema, barras del sistema y padding.
 *
 * [nombre] es el `testTag` de la pantalla, de forma que las pruebas de navegacion
 * puedan afirmar QUE pantalla esta visible sin depender del texto de un boton.
 */
@Composable
fun ContenedorPantalla(
    nombre: String,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .testTag(nombre)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Top,
        content = contenido
    )
}

/** Cabecera con titulo, subtitulo opcional y accion a la derecha. */
@Composable
fun CabeceraPantalla(
    titulo: String,
    subtitulo: String? = null,
    accion: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.testTag(EtiquetasPrueba.TITULO)
            )
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
        accion?.invoke()
    }
}

/** Boton de "volver" reutilizable: delega en el `NavController` de la pantalla. */
@Composable
fun BotonVolver(texto: String, onVolver: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(
        onClick = onVolver,
        modifier = modifier.testTag(EtiquetasPrueba.VOLVER)
    ) {
        Text(texto, color = CineOro)
    }
}

/**
 * Ejecuta [alSalir] cuando la pantalla abandona la composicion.
 *
 * Es el equivalente en la UI de los `limpiar()` que ofrecen los ViewModels
 * (`DetallePeliculaViewModel.limpiar`, `SeleccionButacasViewModel.limpiar`): al
 * navegar atras se cancelan las observaciones de Room de esa pantalla en vez de
 * dejarlas vivas.
 */
@Composable
fun DisposableEffectAlSalir(alSalir: () -> Unit) {
    DisposableEffect(Unit) {
        onDispose { alSalir() }
    }
}

/** Estado de carga a pantalla completa. */
@Composable
fun PantallaCargando(mensaje: String = "Cargando…", modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(EtiquetasPrueba.ESTADO)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = CineOro)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
    }
}

/** Estado de error con reintento. El mensaje lo produce el ViewModel, no la vista. */
@Composable
fun PantallaError(
    mensaje: String,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(EtiquetasPrueba.ESTADO)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onReintentar) { Text("Reintentar", color = CineOro) }
    }
}

/**
 * Mensaje suelto de una pantalla (estado vacio, aviso puntual, error).
 *
 * El TEXTO lo produce siempre el ViewModel: aqui solo se decide el color.
 */
@Composable
fun AvisoPantalla(
    texto: String,
    esError: Boolean = false,
    modifier: Modifier = Modifier
) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyLarge,
        color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag(EtiquetasPrueba.ESTADO)
    )
}

/** Fila etiqueta / valor usada en las fichas de datos. */
@Composable
fun FilaDato(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(
            text = etiqueta.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * FASE 6 - Formato de importes.
 *
 * Es presentacion, no negocio: el total lo calcula `CalculoReserva` (Fase 5) y la
 * pantalla solo lo pinta. El helper existe para que las cinco pantallas escriban
 * `S/ 45.00` igual y para que los tests puedan buscar ese mismo texto.
 */
fun formatoMonto(valor: Double): String = "S/ " + String.format(java.util.Locale.US, "%.2f", valor)