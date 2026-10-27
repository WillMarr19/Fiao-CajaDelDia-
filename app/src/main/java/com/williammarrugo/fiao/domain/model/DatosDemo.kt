package com.williammarrugo.fiao.domain.model

import java.time.LocalDateTime

/**
 * Datos falsos para las previews y para ver las pantallas antes de que
 * exista la base de datos.
 *
 * Cuando Room entre en escena (día 7), estas listas se borran y las
 * pantallas reciben los datos reales. Mientras tanto permiten revisar
 * el diseño en el panel de preview sin compilar ni instalar nada.
 */
object DatosDemo {

    private val hoy: LocalDateTime = LocalDateTime.now()

    val movimientos = listOf(
        Movimiento(1, TipoMovimiento.VENTA, 12_000, "Gaseosas y papas", hoy.minusHours(1)),
        Movimiento(2, TipoMovimiento.GASTO, 8_000, "Bolsa de hielo", hoy.minusHours(2)),
        Movimiento(3, TipoMovimiento.VENTA, 45_000, "Mercado doña Rosa", hoy.minusHours(3)),
        Movimiento(4, TipoMovimiento.VENTA, 6_500, "Minutos", hoy.minusHours(4)),
        Movimiento(5, TipoMovimiento.GASTO, 30_000, "Pago del domiciliario", hoy.minusHours(5)),
        Movimiento(6, TipoMovimiento.VENTA, 18_000, "Cerveza", hoy.minusHours(6))
    )

    val clientes = listOf(
        ClienteConSaldo(Cliente(1, "Rosa Herrera", "3001234567"), 85_000),
        ClienteConSaldo(Cliente(2, "Jairo Pérez", "3109876543"), 42_500),
        ClienteConSaldo(Cliente(3, "La señora del 3B"), 30_000),
        ClienteConSaldo(Cliente(4, "Nelson Ariza", "3205554433"), 12_000),
        ClienteConSaldo(Cliente(5, "Marta Cecilia", null), 0)
    )

    val resumenHoy = ResumenDia(ventas = 81_500, gastos = 38_000, abonos = 20_000)

    val historialCliente = listOf(
        EntradaHistorial.DeFiado(Fiado(1, 1, 45_000, "Mercado de la quincena", hoy.minusDays(6))),
        EntradaHistorial.DeAbono(Abono(1, 1, 20_000, hoy.minusDays(4))),
        EntradaHistorial.DeFiado(Fiado(2, 1, 35_000, "Pañales y leche", hoy.minusDays(2))),
        EntradaHistorial.DeFiado(Fiado(3, 1, 25_000, "Surtido", hoy.minusDays(1)))
    )

    val semana = listOf(
        DiaResumen("Lun", 120_000, 40_000),
        DiaResumen("Mar", 95_000, 22_000),
        DiaResumen("Mié", 143_000, 51_000),
        DiaResumen("Jue", 88_000, 18_000),
        DiaResumen("Vie", 205_000, 72_000),
        DiaResumen("Sáb", 262_000, 61_000),
        DiaResumen("Dom", 81_500, 38_000)
    )

    val totalPorCobrar: Long = clientes.sumOf { it.saldo }
    val clientesQueDeben: Int = clientes.count { it.saldo > 0 }
}
