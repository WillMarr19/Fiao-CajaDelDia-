package com.williammarrugo.fiao.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Las tablas de la base de datos.
 *
 * Fíjate en que son clases DISTINTAS a las de `domain/model`, aunque se
 * parezcan. Es a propósito.
 *
 * Si usara las del dominio como tablas, el dominio tendría que llevar
 * anotaciones de Room encima (@Entity, @PrimaryKey), y quedaría atado a
 * la base de datos. Entonces no podría probarlo sin Room ni cambiar de
 * almacenamiento sin tocar el dominio.
 *
 * El precio de separarlas es escribir mapeadores (ver Mapeadores.kt).
 * Es código aburrido pero poco, y compra independencia. En proyectos
 * chicos mucha gente une las dos capas; saber por qué se separan, y
 * poder defender la decisión, es lo que importa en una entrevista.
 */

@Entity(tableName = "movimientos")
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Se guarda el nombre del enum como texto: "VENTA" o "GASTO". */
    val tipo: String,
    val monto: Long,
    val descripcion: String,
    val fecha: LocalDateTime
)

@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val telefono: String?
)

/**
 * Un fiado pertenece a un cliente.
 *
 * `ForeignKey` con `onDelete = CASCADE` hace que, al borrar un cliente,
 * la propia base de datos borre sus fiados. Sin eso quedarían filas
 * huérfanas apuntando a un cliente que ya no existe.
 *
 * El `Index` sobre clienteId no es decorativo: todas las consultas
 * filtran por esa columna, y sin índice Room recorre la tabla entera.
 * Room avisa con una advertencia si falta.
 */
@Entity(
    tableName = "fiados",
    foreignKeys = [
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("clienteId")]
)
data class FiadoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clienteId: Long,
    val monto: Long,
    val descripcion: String,
    val fecha: LocalDateTime
)

@Entity(
    tableName = "abonos",
    foreignKeys = [
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("clienteId")]
)
data class AbonoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clienteId: Long,
    val monto: Long,
    val fecha: LocalDateTime
)

/**
 * Resultado de la consulta que trae cada cliente con su saldo.
 *
 * No es una tabla: es la forma del resultado de un SELECT con sumas.
 * Room sabe llenar una data class así siempre que los nombres de las
 * columnas coincidan con los de las propiedades.
 */
data class ClienteConSaldoRow(
    val id: Long,
    val nombre: String,
    val telefono: String?,
    val saldo: Long
)
