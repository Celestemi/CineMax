package com.cinemax.cliente.data.local

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.cinemax.cliente.data.seed.SeedInitializer

/**
 * FASE 3 - Punto unico de acceso a [CineMaxClienteDatabase].
 *
 * Room abre (y por tanto crea) el archivo de base de datos de forma perezosa, en
 * la primera consulta. Por eso [SeedInitializer.programar] se invoca aqui: es la
 * garantia de que el catalogo se siembra la primera vez que se abre la app, y
 * solo una vez por proceso.
 *
 * PROVISIONAL: se reemplazara en Fase 4/5 por la inicializacion de sesion real.
 */
object DatabaseProvider {

    const val NOMBRE_BASE_DATOS = "cinemax_cliente.db"

    @Volatile
    private var instancia: CineMaxClienteDatabase? = null

    /**
     * Base sustituta usada SOLO por las pruebas instrumentadas.
     *
     * Permite montar el grafo real ([com.cinemax.cliente.ui.navigation.CineMaxClienteApp])
     * sobre una base en memoria sembrada, sin tocar el fichero de la app ni dejar
     * estado entre pruebas. En produccion permanece en `null`.
     */
    @Volatile
    private var sustitutaDePruebas: CineMaxClienteDatabase? = null

    /** Inyecta una base en memoria; `null` restaura el comportamiento normal. */
    @VisibleForTesting
    fun sustituirParaPruebas(database: CineMaxClienteDatabase?) {
        sustitutaDePruebas = database
    }

    fun obtener(context: Context): CineMaxClienteDatabase {
        val database = sustitutaDePruebas ?: instancia ?: synchronized(this) {
            instancia ?: Room
                .databaseBuilder(
                    context.applicationContext,
                    CineMaxClienteDatabase::class.java,
                    NOMBRE_BASE_DATOS
                )
                .build()
                .also { instancia = it }
        }
        SeedInitializer.programar(database)
        return database
    }
}
