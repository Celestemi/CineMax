package com.cinemax.cliente.model

/**
 * Genero de pelicula.
 *
 * FASE 3: replica los valores del enum `Genero` del proyecto original CINEMAX
 * (`com.cinemax.peru.model.Genero`) para no perder semantica de UI.
 *
 * - `nombre`       -> etiqueta visible para la UI ("Accion", "Ciencia FICCion", ...).
 * - `valorPersistido` -> texto estable guardado en Room (columna `peliculas.genero`,
 *                       afinidad TEXT, migracion NO necesaria).
 */
enum class Genero(val nombre: String) {
    ACCION("Acción"),
    CIENCIA_FICCION("Ciencia Ficción"),
    COMEDIA("Comedia"),
    DRAMA("Drama"),
    TERROR("Terror"),
    SUSPENSO("Suspenso"),
    ROMANCE("Romance"),
    AVENTURA("Aventura"),
    ANIMACION("Animación"),
    MUSICAL("Musical"),
    DOCUMENTAL("Documental");

    val valorPersistido: String
        get() = name

    companion object {
        fun desdeTexto(texto: String): Genero? =
            entries.firstOrNull { it.name == texto.trim().uppercase() }

        fun desdeNombre(nombre: String): Genero? =
            entries.firstOrNull { it.nombre.equals(nombre.trim(), ignoreCase = true) }
    }
}
