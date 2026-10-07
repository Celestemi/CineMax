package com.cinemax.admin.ui

/**
 * FASE 8 - `testTag` estables usados por las pruebas instrumentadas.
 *
 * Centralizarlos evita que cada test repita cadenas magicas y que un cambio de
 * copy rompa la busqueda de nodos.
 */
object EtiquetasPrueba {
    const val PANTALLA_SPLASH = "pantalla:splash"
    const val PANTALLA_LOGIN = "pantalla:login"
    const val PANTALLA_REGISTRO = "pantalla:registro"
    const val PANTALLA_HOME = "pantalla:home"

    const val CAMPO_USUARIO = "campo:usuario"
    const val CAMPO_CONTRASENA = "campo:contrasena"
    const val CAMPO_NOMBRE = "campo:nombre"
    const val CAMPO_EMAIL = "campo:email"
    const val CAMPO_TELEFONO = "campo:telefono"

    const val BOTON_ENVIAR = "boton:enviar"
    const val BOTON_REGISTRO = "boton:registro"
    const val CERRAR_SESION = "home:cerrarSesion"
    const val ESTADO = "estado"

    /** Espacios preparados para la FASE 9 (aun sin funcionalidad). */
    const val ACCION_FUNCIONES = "home:funciones"
    const val ACCION_SALAS = "home:salas"
    const val ACCION_OCUPACION = "home:ocupacion"
}
