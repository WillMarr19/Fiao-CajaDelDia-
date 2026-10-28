package com.williammarrugo.fiao.domain.rules

import com.williammarrugo.fiao.domain.model.Abono
import com.williammarrugo.fiao.domain.model.Fiado
import com.williammarrugo.fiao.domain.model.Movimiento
import com.williammarrugo.fiao.domain.model.ResumenDia
import com.williammarrugo.fiao.domain.model.TipoMovimiento

/**
 * Las reglas de negocio de Fiao, como funciones puras.
 *
 * "Pura" significa dos cosas: con las mismas entradas siempre devuelve
 * lo mismo, y no toca nada de afuera (ni base de datos, ni reloj, ni
 * pantalla). Por eso se pueden probar con JUnit normal, sin emulador y
 * en milisegundos. Los tests están en
 * app/src/test/java/.../ReglasTest.kt
 *
 * Este archivo no importa nada de androidx a propósito: el dominio no
 * sabe que existe Android.
 */

// ---------------------------------------------------------------------
// R1: todo monto es un entero en pesos y mayor que cero.
// ---------------------------------------------------------------------

const val MONTO_MAXIMO: Long = 999_999_999

sealed interface ResultadoValidacion {
    data object Valido : ResultadoValidacion
    data class Invalido(val mensaje: String) : ResultadoValidacion
}

fun validarMonto(monto: Long?): ResultadoValidacion = when {
    monto == null -> ResultadoValidacion.Invalido("Escribe el monto")
    monto <= 0 -> ResultadoValidacion.Invalido("El monto debe ser mayor que cero")
    monto > MONTO_MAXIMO -> ResultadoValidacion.Invalido("Ese monto es demasiado grande")
    else -> ResultadoValidacion.Valido
}

fun validarDescripcion(texto: String): ResultadoValidacion = when {
    texto.isBlank() -> ResultadoValidacion.Invalido("Escribe de qué fue")
    texto.length > 80 -> ResultadoValidacion.Invalido("Máximo 80 caracteres")
    else -> ResultadoValidacion.Valido
}

fun validarNombreCliente(texto: String): ResultadoValidacion = when {
    texto.isBlank() -> ResultadoValidacion.Invalido("Escribe el nombre")
    texto.length > 60 -> ResultadoValidacion.Invalido("Máximo 60 caracteres")
    else -> ResultadoValidacion.Valido
}

/** El mensaje de error, o null si está bien. Comodidad para el UiState. */
fun ResultadoValidacion.mensajeONull(): String? = when (this) {
    is ResultadoValidacion.Invalido -> mensaje
    ResultadoValidacion.Valido -> null
}

val ResultadoValidacion.esValido: Boolean
    get() = this is ResultadoValidacion.Valido

// ---------------------------------------------------------------------
// R2 y R3: el balance del día.
//
// Entran las ventas de contado y los abonos que pagaron los clientes.
// Salen los gastos. Un fiado NO entra: todavía no hay plata en la caja,
// solo una promesa. Por eso la función ni siquiera recibe los fiados.
// ---------------------------------------------------------------------

fun calcularResumenDia(
    movimientosDelDia: List<Movimiento>,
    abonosDelDia: List<Abono>
): ResumenDia {
    val ventas = movimientosDelDia
        .filter { it.tipo == TipoMovimiento.VENTA }
        .sumOf { it.monto }

    val gastos = movimientosDelDia
        .filter { it.tipo == TipoMovimiento.GASTO }
        .sumOf { it.monto }

    val abonos = abonosDelDia.sumOf { it.monto }

    return ResumenDia(ventas = ventas, gastos = gastos, abonos = abonos)
}

// ---------------------------------------------------------------------
// R4: el saldo de un cliente.
//
// Suma de lo fiado menos suma de lo abonado. Nunca negativo: si alguien
// abonó de más (por un error de digitación ya guardado), el saldo se
// queda en cero y no se convierte en que el negocio le deba al cliente.
// ---------------------------------------------------------------------

fun calcularSaldo(fiados: List<Fiado>, abonos: List<Abono>): Long {
    val debe = fiados.sumOf { it.monto }
    val pago = abonos.sumOf { it.monto }
    return (debe - pago).coerceAtLeast(0L)
}

// ---------------------------------------------------------------------
// R5: no se puede abonar más de lo que se debe.
// ---------------------------------------------------------------------

fun validarAbono(monto: Long?, saldoActual: Long): ResultadoValidacion {
    val base = validarMonto(monto)
    if (base is ResultadoValidacion.Invalido) return base

    return when {
        saldoActual <= 0 -> ResultadoValidacion.Invalido("Este cliente no debe nada")
        monto!! > saldoActual -> ResultadoValidacion.Invalido(
            "El abono no puede ser mayor que la deuda"
        )
        else -> ResultadoValidacion.Valido
    }
}

// ---------------------------------------------------------------------
// R6: no se elimina un cliente que todavía debe.
// ---------------------------------------------------------------------

fun puedeEliminarCliente(saldo: Long): Boolean = saldo <= 0L
