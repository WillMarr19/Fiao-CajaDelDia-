package com.williammarrugo.fiao.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Los DAO: Data Access Object.
 *
 * Un DAO es una interfaz donde cada método describe una operación
 * sobre la base de datos. Room genera la implementación al compilar;
 * por eso hace falta KSP, que es el procesador de anotaciones.
 *
 * Dos detalles que conviene entender:
 *
 * - Los métodos que devuelven `Flow` NO son `suspend`. Room devuelve un
 *   flujo que emite la lista cada vez que la tabla cambia. Se suscribe
 *   una vez y la pantalla se actualiza sola.
 * - Los que escriben SÍ son `suspend`, porque tocan disco y no deben
 *   bloquear el hilo principal. Room se niega a compilar si intentas
 *   hacer una escritura bloqueante en el hilo de la interfaz.
 */

@Dao
interface MovimientoDao {

    /**
     * Los movimientos entre dos instantes, del más nuevo al más viejo.
     *
     * El filtro por día se hace con un rango de milisegundos y no con
     * funciones de fecha de SQLite, porque así se aprovecha el orden
     * natural de la columna.
     */
    @Query(
        """
        SELECT * FROM movimientos
        WHERE fecha BETWEEN :desde AND :hasta
        ORDER BY fecha DESC
        """
    )
    fun observarEntre(desde: Long, hasta: Long): Flow<List<MovimientoEntity>>

    @Query("SELECT * FROM movimientos WHERE id = :id")
    suspend fun obtenerPorId(id: Long): MovimientoEntity?

    @Insert
    suspend fun insertar(movimiento: MovimientoEntity): Long

    @Update
    suspend fun actualizar(movimiento: MovimientoEntity)

    @Query("DELETE FROM movimientos WHERE id = :id")
    suspend fun eliminar(id: Long)
}

@Dao
interface ClienteDao {

    /**
     * Cada cliente con su saldo.
     *
     * El saldo sale de dos subconsultas: lo fiado menos lo abonado.
     * COALESCE convierte el NULL de una suma vacía en 0, porque un
     * cliente sin fiados daría NULL y no cero.
     *
     * MAX(..., 0) aplica la regla R4 también en SQL: el saldo nunca es
     * negativo. La misma regla está en Kotlin, en Reglas.kt, que es la
     * versión que se prueba con tests.
     */
    @Query(
        """
        SELECT c.id, c.nombre, c.telefono,
               MAX(
                 COALESCE((SELECT SUM(f.monto) FROM fiados f WHERE f.clienteId = c.id), 0) -
                 COALESCE((SELECT SUM(a.monto) FROM abonos a WHERE a.clienteId = c.id), 0),
                 0
               ) AS saldo
        FROM clientes c
        ORDER BY saldo DESC, c.nombre ASC
        """
    )
    fun observarConSaldo(): Flow<List<ClienteConSaldoRow>>

    @Query(
        """
        SELECT COALESCE(SUM(saldo), 0) FROM (
          SELECT MAX(
            COALESCE((SELECT SUM(f.monto) FROM fiados f WHERE f.clienteId = c.id), 0) -
            COALESCE((SELECT SUM(a.monto) FROM abonos a WHERE a.clienteId = c.id), 0),
            0
          ) AS saldo
          FROM clientes c
        )
        """
    )
    fun observarTotalPorCobrar(): Flow<Long>

    @Query("SELECT * FROM clientes WHERE id = :id")
    fun observarCliente(id: Long): Flow<ClienteEntity?>

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun obtenerPorId(id: Long): ClienteEntity?

    @Insert
    suspend fun insertar(cliente: ClienteEntity): Long

    @Update
    suspend fun actualizar(cliente: ClienteEntity)

    @Query("DELETE FROM clientes WHERE id = :id")
    suspend fun eliminar(id: Long)
}

@Dao
interface FiadoDao {

    @Query("SELECT * FROM fiados WHERE clienteId = :clienteId ORDER BY fecha DESC")
    fun observarPorCliente(clienteId: Long): Flow<List<FiadoEntity>>

    @Insert
    suspend fun insertar(fiado: FiadoEntity): Long
}

@Dao
interface AbonoDao {

    @Query("SELECT * FROM abonos WHERE clienteId = :clienteId ORDER BY fecha DESC")
    fun observarPorCliente(clienteId: Long): Flow<List<AbonoEntity>>

    @Query(
        """
        SELECT * FROM abonos
        WHERE fecha BETWEEN :desde AND :hasta
        ORDER BY fecha DESC
        """
    )
    fun observarEntre(desde: Long, hasta: Long): Flow<List<AbonoEntity>>

    @Insert
    suspend fun insertar(abono: AbonoEntity): Long
}
