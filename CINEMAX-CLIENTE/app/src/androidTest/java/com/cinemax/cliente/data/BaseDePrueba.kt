package com.cinemax.cliente.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.cinemax.cliente.data.local.ButacaEntity
import com.cinemax.cliente.data.local.CineMaxClienteDatabase
import com.cinemax.cliente.data.local.FuncionEntity
import com.cinemax.cliente.data.local.PeliculaEntity
import com.cinemax.cliente.data.local.SalaEntity
import com.cinemax.cliente.data.local.SedeEntity
import com.cinemax.cliente.data.local.UsuarioEntity
import com.cinemax.cliente.data.seed.SeedData
import com.cinemax.cliente.data.seed.SeedInitializer
import com.cinemax.cliente.state.RolCineMax
import com.cinemax.cliente.state.UsuarioSesion
import kotlinx.coroutines.flow.first

/**
 * FASE 5 - Base compartida de las pruebas instrumentadas.
 *
 * Reutiliza el catalogo demo de `SeedData` (Fase 3) en vez de inventar otro: asi
 * las pruebas de la capa Repository/ViewModel se ejecutan sobre el mismo esquema,
 * los mismos indices UNIQUE y el mismo volumen de datos que vera la app.
 *
 * `Catalogo` expone las entidades YA sembradas con sus ids reales de Room, de
 * modo que los tests pueden referringarse a "la funcion 7" o "la sala 3" sin
 * depender de ids literales.
 */
object BaseDePrueba {

    /** Base en memoria de verdad: mismo esquema y mismos indices que `cinemax_cliente.db`. */
    fun abrirEnMemoria(): CineMaxClienteDatabase =
        Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            CineMaxClienteDatabase::class.java
        ).allowMainThreadQueries().build()

    /** Siembra el catalogo demo y devuelve las entidades con sus ids reales. */
    suspend fun sembrarYLeer(database: CineMaxClienteDatabase): Catalogo {
        SeedInitializer.sembrar(database)
        return leer(database)
    }

    suspend fun leer(database: CineMaxClienteDatabase): Catalogo {
        val peliculas = database.peliculaDao().observarTodas().first()
        val sedes = database.sedeDao().observarTodas().first()
        val salas = database.salaDao().observarTodas().first()
        val funciones = database.funcionDao().observarTodas().first()

        return Catalogo(
            peliculas = peliculas,
            sedes = sedes,
            salas = salas,
            funciones = funciones,
            usuarios = database.usuarioDao().observarPorRol(RolCineMax.CLIENTE.name).first(),
            butacasPorSala = database.salaDao().observarTodasConButacas().first()
                .associate { it.sala.id to it.butacas },
            funcionesPorPrograma = SeedData.funciones(
                peliculaIds = SeedData.peliculas.map { demo -> peliculas.first { it.titulo == demo.titulo }.id.toLong() },
                sedeIds = SeedData.sedes.map { demo -> sedes.first { it.nombre == demo.nombre }.id.toLong() },
                salaIds = SeedData.salas(SeedData.sedes.map { demo -> sedes.first { it.nombre == demo.nombre }.id.toLong() })
                    .map { demo -> salas.first { it.nombre == demo.nombre }.id.toLong() }
            ).map { demo ->
                funciones.first {
                    it.peliculaId == demo.peliculaId &&
                        it.salaId == demo.salaId &&
                        it.fecha == demo.fecha &&
                        it.hora == demo.hora
                }
            }
        )
    }
}

/** FASE 5 - Instantanea del catalogo demo con los ids que genero Room. */
data class Catalogo(
    val peliculas: List<PeliculaEntity>,
    val sedes: List<SedeEntity>,
    val salas: List<SalaEntity>,
    val funciones: List<FuncionEntity>,
    val usuarios: List<UsuarioEntity>,
    val butacasPorSala: Map<Int, List<ButacaEntity>>,
    /**
     * Las mismas funciones que [funciones], pero en el ORDEN de la programacion
     * demo, que es el orden en que las indexa `SeedData.reservasDemo`.
     *
     * Hace falta porque `FuncionDao.observarTodas` devuelve `ORDER BY fecha, hora`:
     * indices que hoy coinciden con los del seed dejarian de coincidir en cuanto
     * se anadiera o quitara una funcion del catalogo demo, y un
     * `catalogo.funciones[6]` silencioso pasaria a ser otra pelicula.
     */
    val funcionesPorPrograma: List<FuncionEntity>
) {
    /** La funcion numero [indice] de la programacion demo, con su id real de Room. */
    fun funcionDePrograma(indice: Int): FuncionEntity = funcionesPorPrograma[indice]

    fun funcion(id: Int): FuncionEntity = funciones.first { it.id == id }
    fun pelicula(id: Int): PeliculaEntity = peliculas.first { it.id == id }
    fun sede(id: Int): SedeEntity = sedes.first { it.id == id }
    fun sala(id: Int): SalaEntity = salas.first { it.id == id }
    fun usuario(nombre: String): UsuarioEntity = usuarios.first { it.usuario == nombre }
    fun butacas(salaId: Int): List<ButacaEntity> = butacasPorSala.getValue(salaId)

    fun butaca(salaId: Int, codigo: String): ButacaEntity =
        butacas(salaId).first { it.codigo == codigo }

    fun funcionesDe(peliculaId: Int): List<FuncionEntity> = funciones.filter { it.peliculaId == peliculaId }
    fun funcionesDeSede(sedeId: Int): List<FuncionEntity> = funciones.filter { it.sedeId == sedeId }
    fun funcionesDeFecha(fecha: String): List<FuncionEntity> = funciones.filter { it.fecha == fecha }
    fun funcionesDeGenero(genero: String): List<FuncionEntity> {
        val ids = peliculas.filter { it.genero == genero }.map { it.id }
        return funciones.filter { ids.contains(it.peliculaId) }
    }

    /** Genero que existe en el seed y tiene al menos una funcion. */
    fun unGeneroConFunciones(): String = funciones.first().let { pelicula(it.peliculaId).genero }

    fun fechas(): List<String> = funciones.map { it.fecha }.distinct().sorted()
}

/**
 * FASE 7 - Cuantas butacas ocupa el catalogo demo YA sembrado, por funcion.
 *
 * El seed mete 3 reservas de ejemplo ([SeedData.reservasDemo]) y solo las
 * CONFIRMADAS ocupan butacas. Las pruebas de disponibilidad tienen que partir de
 * esa foto real en vez de suponer un catalogo vacio: si no, comprarian butacas que
 * el seed ya vendio y el repositorio las rechazaria con `BUTACAS_YA_OCUPADAS`.
 */
suspend fun ocupacionSembrada(database: CineMaxClienteDatabase): Map<Int, Int> =
    database.butacaDao().contarOcupadasPorFuncion().first()
        .associate { fila -> fila.funcionId to fila.ocupadas }

/**
 * Funcion del catalogo demo que el seed deja completamente libre, con sus 80
 * butacas disponibles. Es la que usan las pruebas que necesitan COMPRAR sin
 * chocar con las reservas de ejemplo.
 */
fun Catalogo.funcionLibre(ocupadas: Map<Int, Int>): FuncionEntity =
    funciones.first { (ocupadas[it.id] ?: 0) == 0 }

/** Cliente del seed que todavia no tiene ninguna reserva, ni siquiera cancelada. */
suspend fun Catalogo.clienteSinReservas(database: CineMaxClienteDatabase): UsuarioEntity =
    usuarios.first { database.reservaDao().porUsuario(it.id).first().isEmpty() }

/** Construye la sesion en memoria que `ReservaViewModel` consume. */
fun UsuarioEntity.aSesionDePrueba(): UsuarioSesion = UsuarioSesion(
    id = id,
    usuario = usuario,
    nombreCompleto = nombreCompleto,
    email = email,
    telefono = telefono,
    rol = RolCineMax.CLIENTE,
    activo = activo,
    inicioSesionEn = 1_700_000_000_000L
)
