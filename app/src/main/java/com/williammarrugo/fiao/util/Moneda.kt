package com.williammarrugo.fiao.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Formato de pesos colombianos.
 *
 * El dinero se guarda como Long en pesos enteros, nunca como Double.
 * Un Double no puede representar 0,1 de forma exacta, y al sumar muchos
 * valores el error se acumula: la caja del día no cuadraría por unos pesos.
 * Como en Colombia no se manejan centavos en el día a día, el entero
 * resuelve el problema sin complicaciones.
 */

private val localeColombia: Locale = Locale.forLanguageTag("es-CO")

private val formatoMoneda: NumberFormat =
    NumberFormat.getCurrencyInstance(localeColombia).apply {
        maximumFractionDigits = 0
    }

private val formatoNumero: NumberFormat =
    NumberFormat.getNumberInstance(localeColombia).apply {
        maximumFractionDigits = 0
    }

/** 45000 -> "$ 45.000" */
fun Long.aPesos(): String = formatoMoneda.format(this)

/** 45000 -> "45.000", sin el símbolo. Para cuando la columna ya dice que es plata. */
fun Long.aPesosSinSimbolo(): String = formatoNumero.format(this)

/** -8000 -> "-$ 8.000" y 12000 -> "+$ 12.000". Para filas de movimiento. */
fun Long.aPesosConSigno(): String {
    val signo = if (this < 0) "-" else "+"
    return signo + kotlin.math.abs(this).aPesos()
}
