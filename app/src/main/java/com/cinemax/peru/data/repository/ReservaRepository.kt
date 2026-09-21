package com.cinemax.peru.data.repository

import com.cinemax.peru.data.fake.FakeData
import com.cinemax.peru.model.Butaca
import com.cinemax.peru.model.Funcion
import com.cinemax.peru.model.Reserva

interface ReservaRepository {
    fun obtenerReservas(): List<Reserva>
    fun obtenerPorCodigo(codigo: String): Reserva?
    fun calcularTotal(funcion: Funcion, butacas: List<Butaca>): Double
    fun registrarReserva(funcion: Funcion, butacas: List<Butaca>, fechaCompra: String): Reserva
}

class FakeReservaRepository(
    iniciales: List<Reserva> = FakeData.reservas
) : ReservaRepository {

    private val reservasEnMemoria: MutableList<Reserva> = iniciales.toMutableList()
    private var siguienteId: Int = (iniciales.maxOfOrNull { it.id } ?: 0) + 1

    override fun obtenerReservas(): List<Reserva> = reservasEnMemoria.toList()

    override fun obtenerPorCodigo(codigo: String): Reserva? =
        reservasEnMemoria.firstOrNull { it.codigo == codigo }

    override fun calcularTotal(funcion: Funcion, butacas: List<Butaca>): Double =
        funcion.precio * butacas.size

    override fun registrarReserva(funcion: Funcion, butacas: List<Butaca>, fechaCompra: String): Reserva {
        val id = siguienteId++
        val reserva = Reserva(
            id = id,
            codigo = "CINEMAX-%04d".format(id),
            fechaCompra = fechaCompra,
            funcion = funcion,
            butacas = butacas,
            total = calcularTotal(funcion, butacas)
        )
        reservasEnMemoria += reserva
        return reserva
    }
}