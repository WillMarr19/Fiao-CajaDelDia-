package com.williammarrugo.fiao.domain.repository

import com.williammarrugo.fiao.domain.model.Abono
import com.williammarrugo.fiao.domain.model.Cliente
import com.williammarrugo.fiao.domain.model.ClienteConSaldo
import com.williammarrugo.fiao.domain.model.EntradaHistorial
import com.williammarrugo.fiao.domain.model.Fiado
import com.williammarrugo.fiao.domain.model.Movimiento
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Los contratos de acceso a datos.
 *
 * Esto es lo más importante de la arquitectura y lo que más se pregunta
 * en entrevista, así que vale la pena leerlo despacio.
 *
 * Estas son INTERFACES, no implementaciones. Viven en el paquete
 * `domain` y no mencionan Room por ningún lado: hablan de `Movimiento`
 * y `Cliente`, que son modelos del dominio.
 *
 * La implementación con Room vive en el paquete `data`. Los ViewModel
 * dependen de esta interfaz, nunca de la clase concreta.
 *
 * ¿Para qué sirve esa vuelta?
 *
 * 1. Para probar. En un test le paso una implementación falsa en
 *    memoria y no necesito base de datos ni emulador.
 * 2. Para cambiar el origen de los datos. Si mañana sincronizo con
 *    Supabase, escribo otra implementación de esta misma interfaz y
 *    ni los ViewModel ni las pantallas se enteran.
 * 3. Para que la dependencia apunte hacia adentro: `data` conoce a
 *    `domain`, pero `domain` no conoce a `data`. Eso se llama
 *    inversión de dependencias, la D de SOLID.
 *
 * Los métodos de lectura devuelven `Flow`: la base de datos avisa sola
 * cuando algo cambia y la pantalla se redibuja. No hay que pedirle a
 * Room que recargue.
 *
 * Los de escritura son `suspend`: tocan disco, así que no pueden
 * bloquear el hilo principal.
 */

interface MovimientoRepository {

    /** Los movimientos de un día, ordenados del más reciente al más viejo. */
    fun observarPorDia(dia: LocalDate): Flow<List<Movimiento>>

    /** Un movimiento puntual, para el formulario de edición. */
    suspend fun obtenerPorId(id: Long): Movimiento?

    /** Inserta y devuelve el id asignado. */
    suspend fun insertar(movimiento: Movimiento): Long

    suspend fun actualizar(movimiento: Movimiento)

    suspend fun eliminar(id: Long)
}

interface ClienteRepository {

    /** Todos los clientes con su saldo ya calculado. */
    fun observarConSaldo(): Flow<List<ClienteConSaldo>>

    /** El total que deben todos los clientes juntos. */
    fun observarTotalPorCobrar(): Flow<Long>

    fun observarCliente(id: Long): Flow<Cliente?>

    fun observarSaldo(clienteId: Long): Flow<Long>

    fun observarHistorial(clienteId: Long): Flow<List<EntradaHistorial>>

    suspend fun obtenerPorId(id: Long): Cliente?

    suspend fun insertar(cliente: Cliente): Long

    suspend fun actualizar(cliente: Cliente)

    suspend fun eliminar(id: Long)
}

/**
 * Fiados (F7).
 *
 * Solo tiene insertar y no actualizar ni eliminar, y eso es una
 * decisión, no un olvido. Un fiado es un hecho que ocurrió: el cliente
 * se llevó mercancía. Borrarlo falsearía la cuenta.
 *
 * Si hubo un error de digitación, lo correcto en contabilidad es
 * registrar el ajuste, no hacer desaparecer el registro. Esa función
 * podría agregarse más adelante como "corrección", que es distinto de
 * un borrado.
 */
interface FiadoRepository {
    fun observarPorCliente(clienteId: Long): Flow<List<Fiado>>
    suspend fun insertar(fiado: Fiado): Long
}

/** Abonos (F8). Un abono también es un hecho: el cliente pagó. */
interface AbonoRepository {
    /** Los abonos de un día. Entran al balance (regla R2). */
    fun observarPorDia(dia: LocalDate): Flow<List<Abono>>

    fun observarPorCliente(clienteId: Long): Flow<List<Abono>>

    suspend fun insertar(abono: Abono): Long
}
