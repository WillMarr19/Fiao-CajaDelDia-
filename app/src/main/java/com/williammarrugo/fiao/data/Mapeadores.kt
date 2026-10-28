package com.williammarrugo.fiao.data

import com.williammarrugo.fiao.data.local.AbonoEntity
import com.williammarrugo.fiao.data.local.ClienteConSaldoRow
import com.williammarrugo.fiao.data.local.ClienteEntity
import com.williammarrugo.fiao.data.local.FiadoEntity
import com.williammarrugo.fiao.data.local.MovimientoEntity
import com.williammarrugo.fiao.domain.model.Abono
import com.williammarrugo.fiao.domain.model.Cliente
import com.williammarrugo.fiao.domain.model.ClienteConSaldo
import com.williammarrugo.fiao.domain.model.Fiado
import com.williammarrugo.fiao.domain.model.Movimiento
import com.williammarrugo.fiao.domain.model.TipoMovimiento

/**
 * Traductores entre las tablas y el dominio.
 *
 * Son funciones de extensión, así que se leen de corrido:
 *   entidad.aDominio()
 *   movimiento.aEntidad()
 *
 * Esta es la frontera donde se paga el precio de separar las dos capas.
 * Todo lo feo de la base de datos (el tipo guardado como texto) se
 * queda de este lado; del otro lado el dominio ve un enum limpio.
 */

// --- Movimiento ----------------------------------------------------------

fun MovimientoEntity.aDominio(): Movimiento = Movimiento(
    id = id,
    // valueOf lanza excepción si el texto no corresponde a ningún valor
    // del enum. Aquí es deseable: significaría que la base tiene datos
    // corruptos y es mejor enterarse que seguir con un valor inventado.
    tipo = TipoMovimiento.valueOf(tipo),
    monto = monto,
    descripcion = descripcion,
    fecha = fecha
)

fun Movimiento.aEntidad(): MovimientoEntity = MovimientoEntity(
    id = id,
    tipo = tipo.name,
    monto = monto,
    descripcion = descripcion,
    fecha = fecha
)

// --- Cliente -------------------------------------------------------------

fun ClienteEntity.aDominio(): Cliente = Cliente(
    id = id,
    nombre = nombre,
    telefono = telefono
)

fun Cliente.aEntidad(): ClienteEntity = ClienteEntity(
    id = id,
    nombre = nombre,
    telefono = telefono?.ifBlank { null }
)

fun ClienteConSaldoRow.aDominio(): ClienteConSaldo = ClienteConSaldo(
    cliente = Cliente(id = id, nombre = nombre, telefono = telefono),
    saldo = saldo
)

// --- Fiado y abono -------------------------------------------------------

fun FiadoEntity.aDominio(): Fiado = Fiado(
    id = id,
    clienteId = clienteId,
    monto = monto,
    descripcion = descripcion,
    fecha = fecha
)

fun Fiado.aEntidad(): FiadoEntity = FiadoEntity(
    id = id,
    clienteId = clienteId,
    monto = monto,
    descripcion = descripcion,
    fecha = fecha
)

fun AbonoEntity.aDominio(): Abono = Abono(
    id = id,
    clienteId = clienteId,
    monto = monto,
    fecha = fecha
)

fun Abono.aEntidad(): AbonoEntity = AbonoEntity(
    id = id,
    clienteId = clienteId,
    monto = monto,
    fecha = fecha
)
