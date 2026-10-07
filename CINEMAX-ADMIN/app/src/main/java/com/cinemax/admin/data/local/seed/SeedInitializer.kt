package com.cinemax.admin.data.local.seed

import androidx.room.withTransaction
import com.cinemax.admin.data.local.database.CinemaxAdminDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * FASE 8 - Carga inicial de la base ADMIN.
 *
 * Idempotente por diseno:
 * - [programar] solo encola una vez por proceso.
 * - [sembrar] solo inserta cuando la tabla `usuarios` esta vacia, dentro de una
 *   unica transaccion; ejecutarlo N veces nunca duplica cuentas.
 */
object SeedInitializer {

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var programado = false

    /** Encola el sembrado en segundo plano, como maximo una vez por proceso. */
    fun programar(database: CinemaxAdminDatabase) {
        synchronized(this) {
            if (programado) return
            programado = true
        }
        alcance.launch { sembrar(database) }
    }

    /**
     * Inserta las cuentas de administracion en una unica transaccion.
     * @return `true` si se sembro, `false` si ya habia usuarios.
     */
    suspend fun sembrar(database: CinemaxAdminDatabase): Boolean = database.withTransaction {
        if (yaSembrada(database)) return@withTransaction false
        SeedData.usuarios().forEach { database.usuarioDao().insertar(it) }
        true
    }

    /** `true` si ya existe al menos un usuario. */
    suspend fun yaSembrada(database: CinemaxAdminDatabase): Boolean =
        database.usuarioDao().contar() > 0
}
