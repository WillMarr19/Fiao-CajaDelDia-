package com.williammarrugo.fiao.domain.rules

import com.williammarrugo.fiao.domain.model.Abono
import com.williammarrugo.fiao.domain.model.Fiado
import com.williammarrugo.fiao.domain.model.Movimiento
import com.williammarrugo.fiao.domain.model.TipoMovimiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/**
 * Pruebas de las reglas de negocio.
 *
 * Corren en la JVM, sin emulador y en milisegundos, porque las reglas
 * son funciones puras que no tocan Android ni la base de datos. Esa
 * velocidad es el premio de haber separado el dominio.
 *
 * Para ejecutarlas: clic derecho sobre el archivo, Run. O desde la
 * terminal: ./gradlew test
 *
 * Cada test se llama describiendo el comportamiento esperado, no el
 * nombre del método. Un test que falla debe decir qué se rompió sin
 * que haya que abrir el código.
 */
class ReglasTest {

    private val ahora: LocalDateTime = LocalDateTime.of(2026, 10, 28, 10, 0)

    private fun venta(monto: Long) =
        Movimiento(0, TipoMovimiento.VENTA, monto, "venta", ahora)

    private fun gasto(monto: Long) =
        Movimiento(0, TipoMovimiento.GASTO, monto, "gasto", ahora)

    private fun abono(monto: Long) = Abono(0, 1, monto, ahora)

    private fun fiado(monto: Long) = Fiado(0, 1, monto, "fiado", ahora)

    // --- R1: validación de montos ---------------------------------------

    @Test
    fun `un monto nulo no es valido`() {
        assertFalse(validarMonto(null).esValido)
    }

    @Test
    fun `un monto de cero no es valido`() {
        assertFalse(validarMonto(0).esValido)
    }

    @Test
    fun `un monto negativo no es valido`() {
        assertFalse(validarMonto(-500).esValido)
    }

    @Test
    fun `un monto positivo es valido`() {
        assertTrue(validarMonto(12_000).esValido)
    }

    @Test
    fun `un monto absurdamente grande no es valido`() {
        assertFalse(validarMonto(MONTO_MAXIMO + 1).esValido)
    }

    @Test
    fun `una descripcion en blanco no es valida`() {
        assertFalse(validarDescripcion("   ").esValido)
    }

    // --- R2: el balance del día -----------------------------------------

    @Test
    fun `el balance es ventas mas abonos menos gastos`() {
        val resumen = calcularResumenDia(
            movimientosDelDia = listOf(venta(100_000), gasto(30_000)),
            abonosDelDia = listOf(abono(20_000))
        )

        assertEquals(100_000L, resumen.ventas)
        assertEquals(30_000L, resumen.gastos)
        assertEquals(20_000L, resumen.abonos)
        assertEquals(90_000L, resumen.balance)
    }

    @Test
    fun `un dia sin movimientos da balance cero`() {
        val resumen = calcularResumenDia(emptyList(), emptyList())
        assertEquals(0L, resumen.balance)
    }

    @Test
    fun `el balance puede ser negativo si se gasto mas de lo que se vendio`() {
        val resumen = calcularResumenDia(
            movimientosDelDia = listOf(venta(10_000), gasto(50_000)),
            abonosDelDia = emptyList()
        )
        assertEquals(-40_000L, resumen.balance)
    }

    // --- R3: un fiado no entra al balance --------------------------------

    @Test
    fun `calcularResumenDia ni siquiera recibe fiados, solo abonos`() {
        // Esta es la regla R3 expresada en la firma de la función: los
        // fiados no pueden afectar el balance porque no entran.
        // El test comprueba que dos abonos sí suman, como contraste.
        val resumen = calcularResumenDia(
            movimientosDelDia = emptyList(),
            abonosDelDia = listOf(abono(15_000), abono(5_000))
        )
        assertEquals(20_000L, resumen.balance)
    }

    // --- R4: el saldo de un cliente --------------------------------------

    @Test
    fun `el saldo es lo fiado menos lo abonado`() {
        val saldo = calcularSaldo(
            fiados = listOf(fiado(45_000), fiado(35_000)),
            abonos = listOf(abono(20_000))
        )
        assertEquals(60_000L, saldo)
    }

    @Test
    fun `un cliente sin fiados tiene saldo cero`() {
        assertEquals(0L, calcularSaldo(emptyList(), emptyList()))
    }

    @Test
    fun `el saldo nunca es negativo aunque se haya abonado de mas`() {
        val saldo = calcularSaldo(
            fiados = listOf(fiado(10_000)),
            abonos = listOf(abono(25_000))
        )
        assertEquals(0L, saldo)
    }

    // --- R5: no se abona más de lo que se debe ---------------------------

    @Test
    fun `no se puede abonar mas que el saldo`() {
        assertFalse(validarAbono(monto = 50_000, saldoActual = 30_000).esValido)
    }

    @Test
    fun `se puede abonar exactamente el saldo`() {
        assertTrue(validarAbono(monto = 30_000, saldoActual = 30_000).esValido)
    }

    @Test
    fun `no se puede abonar a un cliente que no debe nada`() {
        assertFalse(validarAbono(monto = 1_000, saldoActual = 0).esValido)
    }

    // --- R6: no se borra un cliente que debe ------------------------------

    @Test
    fun `un cliente con saldo no se puede eliminar`() {
        assertFalse(puedeEliminarCliente(saldo = 1))
    }

    @Test
    fun `un cliente al dia si se puede eliminar`() {
        assertTrue(puedeEliminarCliente(saldo = 0))
    }
}
