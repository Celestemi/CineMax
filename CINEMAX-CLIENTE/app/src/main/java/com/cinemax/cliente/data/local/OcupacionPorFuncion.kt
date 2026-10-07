package com.cinemax.cliente.data.local

/**
 * FASE 7 - Fila del `GROUP BY` que cuenta las butacas ocupadas de cada funcion.
 *
 * Es un POJO de solo lectura, no una entidad: no tiene tabla, no tiene clave
 * primaria y por tanto **no forma parte del esquema**. `OcupacionPorFuncion` no
 * aparece en `@Database(entities = ...)` ni cambia la `version = 1`, asi que no
 * hace falta ninguna migracion.
 *
 * Existe porque la cartelera tiene que poder pintar la disponibilidad basica de
 * cada funcion ("Quedan 78 de 80") sin abrir una consulta por tarjeta: con una
 * unica lectura agrupada se obtiene el dato de las 34 funciones a la vez.
 */
data class OcupacionPorFuncion(
    val funcionId: Int,
    val ocupadas: Int
)