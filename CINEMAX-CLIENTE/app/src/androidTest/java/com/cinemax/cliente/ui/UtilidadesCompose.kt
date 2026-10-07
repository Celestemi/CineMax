package com.cinemax.cliente.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag

/**
 * FASE 7 - Espera a que la UI llegue al nodo que la prueba va a tocar.
 *
 * `waitForIdle()` solo garantiza que Compose no tiene recomposiciones pendientes: no
 * sabe nada de un `Flow` de Room que se recoge en `Dispatchers.IO`. Por eso, en una
 * pantalla que arranca en `Cargando` y carga desde la base, `waitForIdle()` puede
 * volver antes de que exista el primer nodo y la prueba falla con "could not find any
 * node" en lugar de esperar a lo que de verdad queria comprobar.
 *
 * [nodo] espera al nodo concreto y lo devuelve, para no repetir `onNodeWithTag` en
 * cada linea de la prueba.
 */
private const val ESPERA_MS = 10_000L

/** Espera a que exista [tag] y devuelve su nodo. */
fun ComposeTestRule.nodo(tag: String, timeoutMs: Long = ESPERA_MS): SemanticsNodeInteraction {
    waitUntil(timeoutMs) { onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
    return onNodeWithTag(tag)
}

/**
 * Espera a que el nodo [tag] exista y este HABILITADO, y devuelve el primer nodo que
 * cumple las dos cosas. Necesario para "Continuar al resumen", que arranca en `false`
 * mientras no hay nada seleccionado.
 */
fun ComposeTestRule.nodoHabilitado(tag: String, timeoutMs: Long = ESPERA_MS): SemanticsNodeInteraction {
    waitUntil(timeoutMs) {
        onAllNodesWithTag(tag)
            .fetchSemanticsNodes()
            .any { nodo -> !nodo.config.contains(SemanticsProperties.Disabled) }
    }
    return onNodeWithTag(tag)
}

/**
 * Espera a que el nodo [tag] exista y su texto contenga [texto], y lo devuelve.
 *
 * `assert` NO reintenta: en cuanto falla, la prueba muere. Eso importa para lo
 * reactivo: si otra compra baja "Quedan 80" a "Quedan 78", el texto correcto llega
 * unas milisegundos despues, cuando el `Flow` de Room ha repintado. Sin esta espera
 * la prueba seria aleatoria.
 */
fun ComposeTestRule.nodoConTexto(
    tag: String,
    texto: String,
    timeoutMs: Long = ESPERA_MS
): SemanticsNodeInteraction {
    waitUntil(timeoutMs) {
        onAllNodesWithTag(tag)
            .fetchSemanticsNodes()
            .any { nodo ->
                nodo.config.getOrNull(SemanticsProperties.Text)
                    ?.any { valor -> valor.text.contains(texto) } == true
            }
    }
    return onNodeWithTag(tag)
}
