package com.williammarrugo.fiao.domain.model

import java.time.LocalDateTime

/**
 * Modelos del dominio.
 *
 * Son data class puras: no saben nada de Room, ni de Compose, ni de red.
 * Esa separación es la que después permite probar las reglas de negocio
 * con tests normales de JUnit, sin necesidad de un emulador.
 *
 * Todos los montos son Long en pesos enteros (ver util/Moneda.kt).
 */

enum class TipoMovimiento { VENTA, GASTO }

/** Una venta de contado o un gasto. Afecta la caja del día. */
data class Movimiento(
    val id: Long = 0,
    val tipo: TipoMovimiento,
    val monto: Long,
    val descripcion: String,
    val fecha: LocalDateTime
)

/** Un cliente al que se le fía. El teléfono es opcional. */
data class Cliente(
    val id: Long = 0,
    val nombre: String,
    val telefono: String? = null
)

/** Una venta fiada. No entra a la caja del día: aumenta lo que deben. */
data class Fiado(
    val id: Long = 0,
    val clienteId: Long,
    val monto: Long,
    val descripcion: String,
    val fecha: LocalDateTime
)

/** Un pago parcial o total de una deuda. Sí entra a la caja del día. */
data class Abono(
    val id: Long = 0,
    val clienteId: Long,
    val monto: Long,
    val fecha: LocalDateTime
)

/**
 * Cliente junto con su saldo ya calculado.
 *
 * La lista de clientes necesita mostrar el saldo, pero Cliente no debe
 * cargarlo: el saldo es un cálculo, no un dato del cliente. Este tipo
 * existe para esa pantalla.
 */
data class ClienteConSaldo(
    val cliente: Cliente,
    val saldo: Long
)

/**
 * Una entrada del historial de un cliente: puede ser un fiado o un abono.
 *
 * Es una sealed class porque el historial mezcla dos cosas distintas y
 * solo esas dos. Al recorrerla con `when`, el compilador obliga a cubrir
 * ambos casos; si mañana se agrega un tercero, el código deja de compilar
 * en todos los lugares que haya que actualizar. Eso es exactamente lo que
 * se quiere.
 */
sealed class EntradaHistorial {
    abstract val monto: Long
    abstract val fecha: LocalDateTime

    data class DeFiado(
        val fiado: Fiado
    ) : EntradaHistorial() {
        override val monto: Long get() = fiado.monto
        override val fecha: LocalDateTime get() = fiado.fecha
    }

    data class DeAbono(
        val abono: Abono
    ) : EntradaHistorial() {
        override val monto: Long get() = abono.monto
        override val fecha: LocalDateTime get() = abono.fecha
    }
}

/** Totales de un día, para la pantalla de inicio. */
data class ResumenDia(
    val ventas: Long,
    val gastos: Long,
    val abonos: Long
) {
    /** Regla R2: el balance del día es ventas más abonos, menos gastos. */
    val balance: Long get() = ventas + abonos - gastos
}

/** Un día dentro del resumen semanal. */
data class DiaResumen(
    val etiqueta: String,
    val ventas: Long,
    val gastos: Long
)
