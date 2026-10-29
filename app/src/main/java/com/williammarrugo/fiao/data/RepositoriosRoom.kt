package com.williammarrugo.fiao.data

import com.williammarrugo.fiao.data.local.AbonoDao
import com.williammarrugo.fiao.data.local.ClienteDao
import com.williammarrugo.fiao.data.local.FiadoDao
import com.williammarrugo.fiao.data.local.MovimientoDao
import com.williammarrugo.fiao.domain.model.Abono
import com.williammarrugo.fiao.domain.model.Cliente
import com.williammarrugo.fiao.domain.model.ClienteConSaldo
import com.williammarrugo.fiao.domain.model.EntradaHistorial
import com.williammarrugo.fiao.domain.model.Fiado
import com.williammarrugo.fiao.domain.model.Movimiento
import com.williammarrugo.fiao.domain.repository.AbonoRepository
import com.williammarrugo.fiao.domain.repository.ClienteRepository
import com.williammarrugo.fiao.domain.repository.FiadoRepository
import com.williammarrugo.fiao.domain.repository.MovimientoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

/**
 * Las implementaciones con Room de los contratos del dominio.
 *
 * Cada una recibe su DAO por el constructor. No crea nada por dentro:
 * eso se llama inyección de dependencias por constructor, y es lo que
 * permite pasarle un doble en los tests.
 *
 * El trabajo de estas clases es traducir: reciben y devuelven modelos
 * del dominio, y por dentro hablan con entidades.
 */

/**
 * Convierte un día del calendario en el rango de milisegundos que lo
 * cubre completo, usando la zona horaria del dispositivo (regla R7).
 *
 * Se usa `atStartOfDay` del día siguiente menos 1 milisegundo en vez de
 * las 23:59:59, para no perder los movimientos registrados en el último
 * segundo del día.
 */
private fun LocalDate.rangoDelDia(): Pair<Long, Long> {
    val zona = ZoneId.systemDefault()
    val inicio = this.atStartOfDay(zona).toInstant().toEpochMilli()
    val fin = this.plusDays(1).atStartOfDay(zona).toInstant().toEpochMilli() - 1
    return inicio to fin
}

class MovimientoRepositoryRoom(
    private val dao: MovimientoDao
) : MovimientoRepository {

    override fun observarPorDia(dia: LocalDate): Flow<List<Movimiento>> {
        val (desde, hasta) = dia.rangoDelDia()
        // `map` sobre un Flow transforma cada lista que emite la base
        // de datos, sin volver a consultar.
        return dao.observarEntre(desde, hasta)
            .map { lista -> lista.map { it.aDominio() } }
    }

    override suspend fun obtenerPorId(id: Long): Movimiento? =
        dao.obtenerPorId(id)?.aDominio()

    override suspend fun insertar(movimiento: Movimiento): Long =
        dao.insertar(movimiento.aEntidad())

    override suspend fun actualizar(movimiento: Movimiento) =
        dao.actualizar(movimiento.aEntidad())

    override suspend fun eliminar(id: Long) = dao.eliminar(id)
}

class ClienteRepositoryRoom(
    private val clienteDao: ClienteDao,
    private val fiadoDao: FiadoDao,
    private val abonoDao: AbonoDao
) : ClienteRepository {

    override fun observarConSaldo(): Flow<List<ClienteConSaldo>> =
        clienteDao.observarConSaldo()
            .map { filas -> filas.map { it.aDominio() } }

    override fun observarTotalPorCobrar(): Flow<Long> =
        clienteDao.observarTotalPorCobrar()

    override fun observarCliente(id: Long): Flow<Cliente?> =
        clienteDao.observarCliente(id).map { it?.aDominio() }

    override fun observarSaldo(clienteId: Long): Flow<Long> =
        combine(
            fiadoDao.observarPorCliente(clienteId),
            abonoDao.observarPorCliente(clienteId)
        ) { fiados, abonos ->
            val debe = fiados.sumOf { it.monto }
            val pago = abonos.sumOf { it.monto }
            (debe - pago).coerceAtLeast(0L)
        }

    /**
     * El historial mezcla dos tablas distintas.
     *
     * `combine` toma los dos flujos y produce uno nuevo cada vez que
     * cualquiera de los dos emite. Dentro se juntan las dos listas,
     * se envuelven en el tipo sellado y se ordenan por fecha.
     */
    override fun observarHistorial(clienteId: Long): Flow<List<EntradaHistorial>> =
        combine(
            fiadoDao.observarPorCliente(clienteId),
            abonoDao.observarPorCliente(clienteId)
        ) { fiados, abonos ->
            val entradas = fiados.map { EntradaHistorial.DeFiado(it.aDominio()) } +
                abonos.map { EntradaHistorial.DeAbono(it.aDominio()) }
            entradas.sortedByDescending { it.fecha }
        }

    override suspend fun obtenerPorId(id: Long): Cliente? =
        clienteDao.obtenerPorId(id)?.aDominio()

    override suspend fun insertar(cliente: Cliente): Long =
        clienteDao.insertar(cliente.aEntidad())

    override suspend fun actualizar(cliente: Cliente) =
        clienteDao.actualizar(cliente.aEntidad())

    override suspend fun eliminar(id: Long) = clienteDao.eliminar(id)
}

class FiadoRepositoryRoom(
    private val dao: FiadoDao
) : FiadoRepository {

    override fun observarPorCliente(clienteId: Long): Flow<List<Fiado>> =
        dao.observarPorCliente(clienteId).map { lista -> lista.map { it.aDominio() } }

    override suspend fun insertar(fiado: Fiado): Long =
        dao.insertar(fiado.aEntidad())
}

class AbonoRepositoryRoom(
    private val dao: AbonoDao
) : AbonoRepository {

    override fun observarPorDia(dia: LocalDate): Flow<List<Abono>> {
        val (desde, hasta) = dia.rangoDelDia()
        return dao.observarEntre(desde, hasta)
            .map { lista -> lista.map { it.aDominio() } }
    }

    override fun observarPorCliente(clienteId: Long): Flow<List<Abono>> =
        dao.observarPorCliente(clienteId).map { lista -> lista.map { it.aDominio() } }

    override suspend fun insertar(abono: Abono): Long =
        dao.insertar(abono.aEntidad())
}
