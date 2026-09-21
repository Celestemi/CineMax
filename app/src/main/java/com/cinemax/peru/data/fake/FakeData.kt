package com.cinemax.peru.data.fake

import com.cinemax.peru.model.Butaca
import com.cinemax.peru.model.EstadoButaca
import com.cinemax.peru.model.Funcion
import com.cinemax.peru.model.Genero
import com.cinemax.peru.model.Pelicula
import com.cinemax.peru.model.Reserva
import com.cinemax.peru.model.Sala
import com.cinemax.peru.model.Sede

object FakeData {

    val peliculas: List<Pelicula> = listOf(
        Pelicula(
            id = 1,
            titulo = "Cerro Veloz",
            genero = Genero.ACCION,
            clasificacionEdad = "14+",
            duracionMinutos = 128,
            sinopsis = "Un operador de montaña debe rescatar a un grupo de turistas atrapados por una avalancha en la sierra del Perú.",
            posterUrl = "https://picsum.photos/seed/cinemax-1/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+1"
        ),
        Pelicula(
            id = 2,
            titulo = "Horizonte Cero",
            genero = Genero.CIENCIA_FICCION,
            clasificacionEdad = "14+",
            duracionMinutos = 145,
            sinopsis = "La última tripulación de una estación orbital descubre la verdad sobre la desaparición de la Tierra.",
            posterUrl = "https://picsum.photos/seed/cinemax-2/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+2"
        ),
        Pelicula(
            id = 3,
            titulo = "La Vecina del 9",
            genero = Genero.COMEDIA,
            clasificacionEdad = "APT",
            duracionMinutos = 96,
            sinopsis = "Un joven chef descubre que su nueva vecina es la crítica gastronómica más temida de Lima.",
            posterUrl = "https://picsum.photos/seed/cinemax-3/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+3"
        ),
        Pelicula(
            id = 4,
            titulo = "Cacería Nocturna",
            genero = Genero.TERROR,
            clasificacionEdad = "18+",
            duracionMinutos = 104,
            sinopsis = "Cada noche, un faro abandonado de Paracas enciende su luz sin que nadie lo opere.",
            posterUrl = "https://picsum.photos/seed/cinemax-4/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+4"
        ),
        Pelicula(
            id = 5,
            titulo = "Alma de Acero",
            genero = Genero.DRAMA,
            clasificacionEdad = "14+",
            duracionMinutos = 132,
            sinopsis = "Un herrero cusqueño lucha por mantener viva la tradición de su taller frente a la modernidad.",
            posterUrl = "https://picsum.photos/seed/cinemax-5/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+5"
        ),
        Pelicula(
            id = 6,
            titulo = "Robin y el Bosque Mágico",
            genero = Genero.ANIMACION,
            clasificacionEdad = "APT",
            duracionMinutos = 92,
            sinopsis = "Un pajarito curioso debe reunir a los animales del valle para salvar el último árbol ancestral.",
            posterUrl = "https://picsum.photos/seed/cinemax-6/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+6"
        ),
        Pelicula(
            id = 7,
            titulo = "Tensión Máxima",
            genero = Genero.SUSPENSO,
            clasificacionEdad = "16+",
            duracionMinutos = 110,
            sinopsis = "Un fiscal de Chiclayo recibe llamadas que predicen cada una de sus decisiones.",
            posterUrl = "https://picsum.photos/seed/cinemax-7/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+7"
        ),
        Pelicula(
            id = 8,
            titulo = "El Último Guardián",
            genero = Genero.AVENTURA,
            clasificacionEdad = "14+",
            duracionMinutos = 121,
            sinopsis = "Dos hermanos recorren la selva amazónica tras la pista de un relicario perdido.",
            posterUrl = "https://picsum.photos/seed/cinemax-8/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+8"
        ),
        Pelicula(
            id = 9,
            titulo = "Bajo el Sol de Arequipa",
            genero = Genero.ROMANCE,
            clasificacionEdad = "APT",
            duracionMinutos = 108,
            sinopsis = "Una fotógrafa limeña y un guía arequipeño se reencuentran cada año en el Mirador de Yanahuara.",
            posterUrl = "https://picsum.photos/seed/cinemax-9/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+9"
        ),
        Pelicula(
            id = 10,
            titulo = "Sinfonía de Medianoche",
            genero = Genero.MUSICAL,
            clasificacionEdad = "APT",
            duracionMinutos = 115,
            sinopsis = "Un pianista de bar restaurante compone una obra que solo suena cuando la ciudad duerme.",
            posterUrl = "https://picsum.photos/seed/cinemax-10/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+10"
        ),
        Pelicula(
            id = 11,
            titulo = "Pulso Digital",
            genero = Genero.ACCION,
            clasificacionEdad = "16+",
            duracionMinutos = 118,
            sinopsis = "Un analista de datos es perseguido por una inteligencia artificial que aprende de cada huida.",
            posterUrl = "https://picsum.photos/seed/cinemax-11/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+11"
        ),
        Pelicula(
            id = 12,
            titulo = "Crónicas de Marte",
            genero = Genero.CIENCIA_FICCION,
            clasificacionEdad = "14+",
            duracionMinutos = 140,
            sinopsis = "La primera colonia marciana envía señales que llegan a la Tierra con diez años de retraso.",
            posterUrl = "https://picsum.photos/seed/cinemax-12/400/600",
            trailerUrl = "https://www.youtube.com/results?search_query=CineMax+peru+trailer+12"
        )
    )

    val sedes: List<Sede> = listOf(
        Sede(id = 1, nombre = "CineMax Miraflores", distrito = "Miraflores", direccion = "Av. Larco 1234"),
        Sede(id = 2, nombre = "CineMax San Miguel", distrito = "San Miguel", direccion = "Av. La Marina 3456"),
        Sede(id = 3, nombre = "CineMax Centro", distrito = "Lima Cercado", direccion = "Jr. de la Unión 789"),
        Sede(id = 4, nombre = "CineMax Surco", distrito = "Santiago de Surco", direccion = "Av. Caminos del Inca 222")
    )

    val salas: List<Sala> = crearSalas()

    private val programacion: List<Programacion> = listOf(
        Programacion(peliculas[0], sedes[0], salas[0], "2026-09-22", "10:00", 12.50),
        Programacion(peliculas[0], sedes[0], salas[0], "2026-09-22", "15:00", 12.50),
        Programacion(peliculas[1], sedes[1], salas[2], "2026-09-22", "17:30", 14.00),
        Programacion(peliculas[2], sedes[2], salas[4], "2026-09-22", "19:30", 12.00),
        Programacion(peliculas[3], sedes[3], salas[6], "2026-09-22", "21:30", 13.00),
        Programacion(peliculas[4], sedes[0], salas[1], "2026-09-23", "10:00", 12.50),
        Programacion(peliculas[1], sedes[0], salas[0], "2026-09-23", "12:30", 14.00),
        Programacion(peliculas[5], sedes[1], salas[3], "2026-09-23", "15:00", 11.00),
        Programacion(peliculas[6], sedes[1], salas[2], "2026-09-23", "17:30", 13.00),
        Programacion(peliculas[7], sedes[2], salas[5], "2026-09-23", "19:30", 12.00),
        Programacion(peliculas[0], sedes[3], salas[7], "2026-09-23", "21:30", 14.50),
        Programacion(peliculas[8], sedes[0], salas[1], "2026-09-24", "10:00", 12.00),
        Programacion(peliculas[9], sedes[0], salas[0], "2026-09-24", "12:30", 11.50),
        Programacion(peliculas[2], sedes[1], salas[2], "2026-09-24", "15:00", 12.00),
        Programacion(peliculas[1], sedes[2], salas[4], "2026-09-24", "17:30", 14.00),
        Programacion(peliculas[10], sedes[2], salas[5], "2026-09-24", "19:30", 13.50),
        Programacion(peliculas[11], sedes[3], salas[6], "2026-09-24", "21:30", 12.50),
        Programacion(peliculas[3], sedes[0], salas[0], "2026-09-25", "10:00", 13.00),
        Programacion(peliculas[4], sedes[1], salas[3], "2026-09-25", "12:30", 12.50),
        Programacion(peliculas[7], sedes[1], salas[2], "2026-09-25", "15:00", 12.00),
        Programacion(peliculas[6], sedes[2], salas[4], "2026-09-25", "17:30", 13.00),
        Programacion(peliculas[8], sedes[3], salas[7], "2026-09-25", "19:30", 12.00),
        Programacion(peliculas[5], sedes[3], salas[6], "2026-09-25", "21:30", 11.00),
        Programacion(peliculas[9], sedes[0], salas[1], "2026-09-26", "10:00", 11.50),
        Programacion(peliculas[2], sedes[0], salas[0], "2026-09-26", "12:30", 12.00),
        Programacion(peliculas[10], sedes[1], salas[2], "2026-09-26", "15:00", 13.50),
        Programacion(peliculas[11], sedes[1], salas[3], "2026-09-26", "17:30", 12.50),
        Programacion(peliculas[1], sedes[2], salas[5], "2026-09-26", "19:30", 14.00),
        Programacion(peliculas[3], sedes[2], salas[4], "2026-09-26", "21:30", 13.00),
        Programacion(peliculas[6], sedes[3], salas[6], "2026-09-27", "10:00", 13.00),
        Programacion(peliculas[0], sedes[3], salas[7], "2026-09-27", "12:30", 14.50),
        Programacion(peliculas[4], sedes[1], salas[2], "2026-09-27", "15:00", 12.50),
        Programacion(peliculas[7], sedes[2], salas[4], "2026-09-27", "17:30", 12.00),
        Programacion(peliculas[8], sedes[0], salas[0], "2026-09-27", "19:30", 12.00)
    )

    val funciones: List<Funcion> = programacion.mapIndexed { index, p ->
        Funcion(
            id = index + 1,
            pelicula = p.pelicula,
            sede = p.sede,
            sala = p.sala,
            fecha = p.fecha,
            hora = p.hora,
            precio = p.precio
        )
    }

    val reservas: List<Reserva> = crearReservas()

    private data class Programacion(
        val pelicula: Pelicula,
        val sede: Sede,
        val sala: Sala,
        val fecha: String,
        val hora: String,
        val precio: Double
    )

    private fun crearSalas(): List<Sala> = sedes.flatMapIndexed { index, sede ->
        listOf(
            Sala(
                id = index * 2 + 1,
                sedeId = sede.id,
                nombre = "Sala ${index * 2 + 1}",
                butacas = crearButacas(index * 2 + 1)
            ),
            Sala(
                id = index * 2 + 2,
                sedeId = sede.id,
                nombre = "Sala ${index * 2 + 2}",
                butacas = crearButacas(index * 2 + 2)
            )
        )
    }

    private fun crearButacas(salaId: Int): List<Butaca> {
        val filas = 8
        val butacasPorFila = 10
        return (0 until filas).flatMap { filaIndex ->
            val fila = ('A'.code + filaIndex).toChar()
            (1..butacasPorFila).map { numero ->
                val indice = filaIndex * butacasPorFila + numero
                Butaca(
                    id = salaId * 100 + indice,
                    fila = fila,
                    numero = numero,
                    estado = if ((salaId * 7 + indice) % 6 == 0) EstadoButaca.OCUPADA else EstadoButaca.DISPONIBLE
                )
            }
        }
    }

    private fun crearReservas(): List<Reserva> {
        val funcion1 = funciones[0]
        val butacas1 = funcion1.sala.butacas.filter { it.estado == EstadoButaca.OCUPADA }.take(2)

        val funcion2 = funciones[7]
        val butacas2 = funcion2.sala.butacas.filter { it.estado == EstadoButaca.OCUPADA }.take(2)

        val funcion3 = funciones[20]
        val butacas3 = funcion3.sala.butacas.filter { it.estado == EstadoButaca.OCUPADA }.take(3)

        return listOf(
            Reserva(
                id = 1,
                codigo = "CINEMAX-0001",
                fechaCompra = "2026-09-20",
                funcion = funcion1,
                butacas = butacas1,
                total = butacas1.size * funcion1.precio
            ),
            Reserva(
                id = 2,
                codigo = "CINEMAX-0002",
                fechaCompra = "2026-09-21",
                funcion = funcion2,
                butacas = butacas2,
                total = butacas2.size * funcion2.precio
            ),
            Reserva(
                id = 3,
                codigo = "CINEMAX-0003",
                fechaCompra = "2026-09-21",
                funcion = funcion3,
                butacas = butacas3,
                total = butacas3.size * funcion3.precio
            )
        )
    }
}