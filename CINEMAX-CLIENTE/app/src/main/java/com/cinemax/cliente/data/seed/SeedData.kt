package com.cinemax.cliente.data.seed

import com.cinemax.cliente.data.local.ButacaEntity
import com.cinemax.cliente.data.local.FuncionEntity
import com.cinemax.cliente.data.local.PeliculaEntity
import com.cinemax.cliente.data.local.SalaEntity
import com.cinemax.cliente.data.local.SedeEntity
import com.cinemax.cliente.data.local.UsuarioEntity
import com.cinemax.cliente.model.EstadoReserva
import com.cinemax.cliente.model.Genero
import com.cinemax.cliente.state.RolCineMax
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * FASE 3 - Fuente de datos inicial de CineMax Cliente.
 *
 * Reemplaza conceptualmente a `FakeData` del proyecto original CINEMAX, pero
 * devuelve entidades de Room en lugar de modelos en memoria.
 *
 * Volumenes fijos:
 * - 12 peliculas, 4 sedes, 8 salas (2 por sede), 80 butacas por sala (640), 34 funciones.
 *
 * Las funciones se generan con fechas RELATIVAS a la fecha de ejecucion: la
 * cartelera arranca manana (`hoy + 1`) y cubre [DIAS_CARTELERA] dias, por lo que
 * nunca queda vencida. Se usa `java.util.Calendar` en lugar de
 * `java.time.LocalDate` porque `java.time` exige API 26 y el proyecto declara
 * `minSdk 24`; `Calendar` funciona en API 24 sin core library desugaring ni
 * dependencias adicionales.
 */
object SeedData {

    const val FILAS_POR_SALA = 8
    const val COLUMNAS_POR_SALA = 10
    const val BUTACAS_POR_SALA = FILAS_POR_SALA * COLUMNAS_POR_SALA
    const val SALAS_POR_SEDE = 2
    const val DIAS_CARTELERA = 6
    const val CONTRASENA_DEMO = "1234"

    private const val FORMATO_FECHA = "yyyy-MM-dd"

    /** Filas A-H. */
    val FILAS: List<String> =
        (0 until FILAS_POR_SALA).map { ('A'.code + it).toChar().toString() }

    /** Numeros 1-10. */
    val NUMEROS: List<Int> = (1..COLUMNAS_POR_SALA).toList()

    // ------------------------------------------------------------------
    // Fechas relativas a la fecha de ejecucion
    // ------------------------------------------------------------------

    /** Fecha en ISO (`yyyy-MM-dd`) situada [diasDesdeHoy] dias despues de hoy. */
    fun fechaIso(diasDesdeHoy: Int): String {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_MONTH, diasDesdeHoy)
        return SimpleDateFormat(FORMATO_FECHA, Locale.US).format(calendario.time)
    }

    fun hoyIso(): String = fechaIso(0)

    /** Cartelera: hoy+1 ... hoy+[DIAS_CARTELERA]. Siempre futura. */
    fun fechasDeCartelera(): List<String> = (1..DIAS_CARTELERA).map { fechaIso(it) }

    // ------------------------------------------------------------------
    // Peliculas (12)
    // ------------------------------------------------------------------

    val peliculas: List<PeliculaEntity> = listOf(
        PeliculaEntity(
            titulo = "Cerro Veloz",
            genero = Genero.ACCION.valorPersistido,
            clasificacionEdad = "14+",
            duracionMinutos = 128,
            sinopsis = "Un operador de montaña debe rescatar a un grupo de turistas atrapados por una avalancha en la sierra del Perú.",
            posterUrl = "https://picsum.photos/seed/cinemax-1/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+1"
        ),
        PeliculaEntity(
            titulo = "Horizonte Cero",
            genero = Genero.CIENCIA_FICCION.valorPersistido,
            clasificacionEdad = "14+",
            duracionMinutos = 145,
            sinopsis = "La última tripulación de una estación orbital descubre la verdad sobre la desaparición de la Tierra.",
            posterUrl = "https://picsum.photos/seed/cinemax-2/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+2"
        ),
        PeliculaEntity(
            titulo = "La Vecina del 9",
            genero = Genero.COMEDIA.valorPersistido,
            clasificacionEdad = "APT",
            duracionMinutos = 96,
            sinopsis = "Un joven chef descubre que su nueva vecina es la crítica gastronómica más temida de Lima.",
            posterUrl = "https://picsum.photos/seed/cinemax-3/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+3"
        ),
        PeliculaEntity(
            titulo = "Cacería Nocturna",
            genero = Genero.TERROR.valorPersistido,
            clasificacionEdad = "18+",
            duracionMinutos = 104,
            sinopsis = "Cada noche, un faro abandonado de Paracas enciende su luz sin que nadie lo opere.",
            posterUrl = "https://picsum.photos/seed/cinemax-4/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+4"
        ),
        PeliculaEntity(
            titulo = "Alma de Acero",
            genero = Genero.DRAMA.valorPersistido,
            clasificacionEdad = "14+",
            duracionMinutos = 132,
            sinopsis = "Un herrero cusqueño lucha por mantener viva la tradición de su taller frente a la modernidad.",
            posterUrl = "https://picsum.photos/seed/cinemax-5/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+5"
        ),
        PeliculaEntity(
            titulo = "Robin y el Bosque Mágico",
            genero = Genero.ANIMACION.valorPersistido,
            clasificacionEdad = "APT",
            duracionMinutos = 92,
            sinopsis = "Un pajarito curioso debe reunir a los animales del valle para salvar el último árbol ancestral.",
            posterUrl = "https://picsum.photos/seed/cinemax-6/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+6"
        ),
        PeliculaEntity(
            titulo = "Tensión Máxima",
            genero = Genero.SUSPENSO.valorPersistido,
            clasificacionEdad = "16+",
            duracionMinutos = 110,
            sinopsis = "Un fiscal de Chiclayo recibe llamadas que predicen cada una de sus decisiones.",
            posterUrl = "https://picsum.photos/seed/cinemax-7/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+7"
        ),
        PeliculaEntity(
            titulo = "El Último Guardián",
            genero = Genero.AVENTURA.valorPersistido,
            clasificacionEdad = "14+",
            duracionMinutos = 121,
            sinopsis = "Dos hermanos recorren la selva amazónica tras la pista de un relicario perdido.",
            posterUrl = "https://picsum.photos/seed/cinemax-8/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+8"
        ),
        PeliculaEntity(
            titulo = "Bajo el Sol de Arequipa",
            genero = Genero.ROMANCE.valorPersistido,
            clasificacionEdad = "APT",
            duracionMinutos = 108,
            sinopsis = "Una fotógrafa limeña y un guía arequipeño se reencuentran cada año en el Mirador de Yanahuara.",
            posterUrl = "https://picsum.photos/seed/cinemax-9/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+9"
        ),
        PeliculaEntity(
            titulo = "Sinfonía de Medianoche",
            genero = Genero.MUSICAL.valorPersistido,
            clasificacionEdad = "APT",
            duracionMinutos = 115,
            sinopsis = "Un pianista de bar restaurante compone una obra que solo suena cuando la ciudad duerme.",
            posterUrl = "https://picsum.photos/seed/cinemax-10/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+10"
        ),
        PeliculaEntity(
            titulo = "Pulso Digital",
            genero = Genero.ACCION.valorPersistido,
            clasificacionEdad = "16+",
            duracionMinutos = 118,
            sinopsis = "Un analista de datos es perseguido por una inteligencia artificial que aprende de cada huida.",
            posterUrl = "https://picsum.photos/seed/cinemax-11/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+11"
        ),
        PeliculaEntity(
            titulo = "Crónicas de Marte",
            genero = Genero.CIENCIA_FICCION.valorPersistido,
            clasificacionEdad = "14+",
            duracionMinutos = 140,
            sinopsis = "La primera colonia marciana envía señales que llegan a la Tierra con diez años de retraso.",
            posterUrl = "https://picsum.photos/seed/cinemax-12/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+12"
        )
    )

    // ------------------------------------------------------------------
    // Sedes (4)
    // ------------------------------------------------------------------

    val sedes: List<SedeEntity> = listOf(
        SedeEntity(nombre = "CineMax Miraflores", distrito = "Miraflores", direccion = "Av. Larco 1234"),
        SedeEntity(nombre = "CineMax San Miguel", distrito = "San Miguel", direccion = "Av. La Marina 3456"),
        SedeEntity(nombre = "CineMax Centro", distrito = "Lima Cercado", direccion = "Jr. de la Unión 789"),
        SedeEntity(nombre = "CineMax Surco", distrito = "Santiago de Surco", direccion = "Av. Caminos del Inca 222")
    )

    /**
     * Salas (8): dos por sede, en el mismo orden que las butacas globales.
     * La lista devuelta mantiene el orden `sede 0 sala 1, sede 0 sala 2, ...`.
     */
    fun salas(sedeIds: List<Long>): List<SalaEntity> {
        require(sedeIds.size == sedes.size) {
            "Se esperaban ${sedes.size} sedes pero se recibieron ${sedeIds.size}"
        }
        return sedes.indices.flatMap { indiceSede ->
            (1..SALAS_POR_SEDE).map { numero ->
                SalaEntity(
                    sedeId = sedeIds[indiceSede].toInt(),
                    nombre = "Sala ${indiceSede * SALAS_POR_SEDE + numero}",
                    filas = FILAS_POR_SALA,
                    columnas = COLUMNAS_POR_SALA
                )
            }
        }
    }

    /** Butacas de una sala: filas A-H x numeros 1-10 = 80 butacas. */
    fun butacasDeSala(salaId: Int): List<ButacaEntity> =
        FILAS.flatMap { fila ->
            NUMEROS.map { numero ->
                ButacaEntity(salaId = salaId, fila = fila, numero = numero)
            }
        }

    // ------------------------------------------------------------------
    // Funciones (34) - fechas relativas a la fecha de ejecucion
    // ------------------------------------------------------------------

    data class ProgramacionDemo(
        val pelicula: Int,
        val sede: Int,
        val salaEnSede: Int,
        val dia: Int,
        val hora: String,
        val precio: Double,
        val activa: Boolean = true
    )

    private val programacion: List<ProgramacionDemo> = listOf(
        ProgramacionDemo(0, 0, 1, 0, "10:00", 12.50),
        ProgramacionDemo(0, 0, 1, 0, "15:00", 12.50),
        ProgramacionDemo(1, 1, 1, 0, "17:30", 14.00),
        ProgramacionDemo(2, 2, 1, 0, "19:30", 12.00),
        ProgramacionDemo(3, 3, 1, 0, "21:30", 13.00),
        ProgramacionDemo(4, 0, 2, 1, "10:00", 12.50),
        ProgramacionDemo(1, 0, 1, 1, "12:30", 14.00),
        ProgramacionDemo(5, 1, 2, 1, "15:00", 11.00),
        ProgramacionDemo(6, 1, 1, 1, "17:30", 13.00),
        ProgramacionDemo(7, 2, 2, 1, "19:30", 12.00),
        ProgramacionDemo(0, 3, 2, 1, "21:30", 14.50),
        ProgramacionDemo(8, 0, 2, 2, "10:00", 12.00),
        ProgramacionDemo(9, 0, 1, 2, "12:30", 11.50),
        ProgramacionDemo(2, 1, 1, 2, "15:00", 12.00),
        ProgramacionDemo(1, 2, 1, 2, "17:30", 14.00),
        ProgramacionDemo(10, 2, 2, 2, "19:30", 13.50),
        ProgramacionDemo(11, 3, 1, 2, "21:30", 12.50),
        ProgramacionDemo(3, 0, 1, 3, "10:00", 13.00),
        ProgramacionDemo(4, 1, 2, 3, "12:30", 12.50),
        ProgramacionDemo(7, 1, 1, 3, "15:00", 12.00),
        ProgramacionDemo(6, 2, 1, 3, "17:30", 13.00),
        ProgramacionDemo(8, 3, 2, 3, "19:30", 12.00),
        ProgramacionDemo(5, 3, 1, 3, "21:30", 11.00),
        ProgramacionDemo(9, 0, 2, 4, "10:00", 11.50),
        ProgramacionDemo(2, 0, 1, 4, "12:30", 12.00),
        ProgramacionDemo(10, 1, 1, 4, "15:00", 13.50),
        ProgramacionDemo(11, 1, 2, 4, "17:30", 12.50),
        ProgramacionDemo(1, 2, 2, 4, "19:30", 14.00),
        ProgramacionDemo(3, 2, 1, 4, "21:30", 13.00),
        ProgramacionDemo(6, 3, 1, 5, "10:00", 13.00),
        ProgramacionDemo(0, 3, 2, 5, "12:30", 14.50),
        ProgramacionDemo(4, 1, 1, 5, "15:00", 12.50),
        ProgramacionDemo(7, 2, 1, 5, "17:30", 12.00),
        ProgramacionDemo(8, 0, 1, 5, "19:30", 12.00)
    )

    val funcionesProgramadas: Int get() = programacion.size

    /**
     * Traduce la programacion demo a entidades usando los ids reales generados por
     * Room. Los indices de la programacion se validan contra los ids recibidos, de
     * modo que nunca puedan quedar referencias a peliculas/sedes/salas inexistentes.
     */
    fun funciones(
        peliculaIds: List<Long>,
        sedeIds: List<Long>,
        salaIds: List<Long>,
        fechas: List<String> = fechasDeCartelera()
    ): List<FuncionEntity> {
        require(peliculaIds.size == peliculas.size)
        require(sedeIds.size == sedes.size)
        require(salaIds.size == sedes.size * SALAS_POR_SEDE)
        require(fechas.size == DIAS_CARTELERA)
        return programacion.map { demo ->
            require(demo.pelicula in peliculas.indices)
            require(demo.sede in sedes.indices)
            require(demo.salaEnSede in 1..SALAS_POR_SEDE)
            require(demo.dia in fechas.indices)
            FuncionEntity(
                peliculaId = peliculaIds[demo.pelicula].toInt(),
                sedeId = sedeIds[demo.sede].toInt(),
                salaId = salaIds[demo.sede * SALAS_POR_SEDE + (demo.salaEnSede - 1)].toInt(),
                fecha = fechas[demo.dia],
                hora = demo.hora,
                precioEntrada = demo.precio,
                activa = demo.activa
            )
        }
    }

    // ------------------------------------------------------------------
    // Usuarios demo (6) - mismo par usuario/contrasena que el login de Fase 2
    // ------------------------------------------------------------------

    fun usuarios(creadoEn: Long = System.currentTimeMillis()): List<UsuarioEntity> = listOf(
        UsuarioEntity(
            usuario = "cliente",
            contrasena = CONTRASENA_DEMO,
            nombreCompleto = "Carla Ramírez",
            email = "carla.ramirez@cinemax.pe",
            telefono = "999111222",
            rol = RolCineMax.CLIENTE.name,
            activo = true,
            creadoEn = creadoEn
        ),
        UsuarioEntity(
            usuario = "admin",
            contrasena = CONTRASENA_DEMO,
            nombreCompleto = "Administrador CineMax",
            email = "admin@cinemax.pe",
            telefono = "999000111",
            rol = RolCineMax.ADMINISTRADOR.name,
            activo = true,
            creadoEn = creadoEn
        ),
        UsuarioEntity(
            usuario = "andres",
            contrasena = CONTRASENA_DEMO,
            nombreCompleto = "Andrés Quispe",
            email = "andres.quispe@cinemax.pe",
            telefono = "988222333",
            rol = RolCineMax.CLIENTE.name,
            activo = true,
            creadoEn = creadoEn
        ),
        UsuarioEntity(
            usuario = "lucia",
            contrasena = CONTRASENA_DEMO,
            nombreCompleto = "Lucía Fernández",
            email = "lucia.fernandez@cinemax.pe",
            telefono = "977444555",
            rol = RolCineMax.CLIENTE.name,
            activo = true,
            creadoEn = creadoEn
        ),
        UsuarioEntity(
            usuario = "diego",
            contrasena = CONTRASENA_DEMO,
            nombreCompleto = "Diego Salazar",
            email = "diego.salazar@cinemax.pe",
            telefono = "966555777",
            rol = RolCineMax.CLIENTE.name,
            activo = true,
            creadoEn = creadoEn
        ),
        UsuarioEntity(
            usuario = "rosa",
            contrasena = CONTRASENA_DEMO,
            nombreCompleto = "Rosa Medina",
            email = "rosa.medina@cinemax.pe",
            telefono = "955666888",
            rol = RolCineMax.CLIENTE.name,
            activo = true,
            creadoEn = creadoEn
        )
    )

    // ------------------------------------------------------------------
    // Reservas demo (3) - solo para demostrar las relaciones
    // ------------------------------------------------------------------

    data class ReservaDemo(
        val codigo: String,
        val usuario: Int,
        val funcion: Int,
        val butacas: List<String>,
        val estado: EstadoReserva
    )

    /**
     * Las butacas se referencian por codigo ("A1") dentro de la sala de la funcion.
     * Solo las reservas CONFIRMADAS ocupan butacas (una CANCELADA libera las suyas,
     * igual que `ReservaDao.cancelarReserva`).
     */
    val reservasDemo: List<ReservaDemo> = listOf(
        ReservaDemo("CINEMAX-0001", usuario = 0, funcion = 0, butacas = listOf("A1", "A2"), estado = EstadoReserva.CONFIRMADA),
        ReservaDemo("CINEMAX-0002", usuario = 0, funcion = 6, butacas = listOf("B3", "B4", "B5"), estado = EstadoReserva.CONFIRMADA),
        ReservaDemo("CINEMAX-0003", usuario = 1, funcion = 12, butacas = listOf("C7", "C8"), estado = EstadoReserva.CANCELADA)
    )
}
