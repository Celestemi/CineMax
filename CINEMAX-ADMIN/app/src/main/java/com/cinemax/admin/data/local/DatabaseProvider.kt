package com.cinemax.admin.data.local

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.cinemax.admin.data.local.database.CinemaxAdminDatabase
import com.cinemax.admin.data.local.seed.SeedInitializer

/**
 * FASE 8 - Punto unico de acceso a [CinemaxAdminDatabase].
 *
 * Room abre (y por tanto crea) el fichero `cinemax_admin.db` de forma perezosa,
 * en la primera consulta. Aqui se dispara [SeedInitializer.programar] para
 * garantizar que el administrador de prueba existe la primera vez que se abre la
 * app, y solo una vez por proceso.
 */
object DatabaseProvider {

    const val NOMBRE_BASE_DATOS = "cinemax_admin.db"

    @Volatile
    private var instancia: CinemaxAdminDatabase? = null

    /**
     * Base sustituta usada SOLO por las pruebas instrumentadas.
     *
     * Permite montar el grafo real ([com.cinemax.admin.ui.navigation.CineMaxAdminApp])
     * sobre una base en memoria sembrada, sin tocar el fichero de la app ni dejar
     * estado entre pruebas. En produccion permanece en `null`.
     */
    @Volatile
    private var sustitutaDePruebas: CinemaxAdminDatabase? = null

    /** Inyecta una base en memoria; `null` restaura el comportamiento normal. */
    @VisibleForTesting
    fun sustituirParaPruebas(database: CinemaxAdminDatabase?) {
        sustitutaDePruebas = database
    }

    fun obtener(context: Context): CinemaxAdminDatabase {
        val database = sustitutaDePruebas ?: instancia ?: synchronized(this) {
            instancia ?: Room
                .databaseBuilder(
                    context.applicationContext,
                    CinemaxAdminDatabase::class.java,
                    NOMBRE_BASE_DATOS
                )
                .build()
                .also { instancia = it }
        }
        SeedInitializer.programar(database)
        return database
    }
}
