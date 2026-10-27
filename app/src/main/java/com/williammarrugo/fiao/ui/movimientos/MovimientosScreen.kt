package com.williammarrugo.fiao.ui.movimientos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.williammarrugo.fiao.domain.model.DatosDemo
import com.williammarrugo.fiao.domain.model.Movimiento
import com.williammarrugo.fiao.domain.model.ResumenDia
import com.williammarrugo.fiao.ui.componentes.BarraSuperior
import com.williammarrugo.fiao.ui.componentes.EstadoVacio
import com.williammarrugo.fiao.ui.componentes.FilaMovimiento
import com.williammarrugo.fiao.ui.componentes.MontoTexto
import com.williammarrugo.fiao.ui.componentes.SeparadorFila
import com.williammarrugo.fiao.ui.theme.FiaoTheme
import com.williammarrugo.fiao.ui.theme.LocalColoresFiao
import com.williammarrugo.fiao.ui.theme.estiloMontoTitular
import com.williammarrugo.fiao.util.aPesosSinSimbolo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatoFechaCorta =
    DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-CO"))

/**
 * El libro del día: todos los movimientos de una fecha.
 *
 * La diferencia con Inicio es que aquí se puede cambiar de día. Por eso
 * la fecha es un control y no un rótulo.
 */
@Composable
fun MovimientosScreen(
    fecha: LocalDate,
    resumen: ResumenDia,
    movimientos: List<Movimiento>,
    onVolver: () -> Unit,
    onCambiarFecha: () -> Unit,
    onNuevoMovimiento: () -> Unit,
    onMovimientoClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(
                titulo = "Movimientos",
                onVolver = onVolver,
                acciones = {
                    IconButton(onClick = onCambiarFecha) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Cambiar fecha"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNuevoMovimiento,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar movimiento")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                ResumenDelDia(fecha = fecha, resumen = resumen)
            }

            if (movimientos.isEmpty()) {
                item {
                    EstadoVacio(
                        titulo = "Ningún movimiento este día",
                        descripcion = "Cambia de fecha o registra uno nuevo."
                    )
                }
            } else {
                itemsIndexed(
                    items = movimientos,
                    key = { _, movimiento -> movimiento.id }
                ) { indice, movimiento ->
                    if (indice > 0) SeparadorFila()
                    FilaMovimiento(
                        movimiento = movimiento,
                        onClick = { onMovimientoClick(movimiento.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(96.dp)) }
        }
    }
}

@Composable
private fun ResumenDelDia(fecha: LocalDate, resumen: ResumenDia) {
    val colores = LocalColoresFiao.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = fecha.format(formatoFechaCorta),
            style = MaterialTheme.typography.bodyMedium,
            color = colores.tintaSuave
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Balance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            MontoTexto(
                monto = resumen.balance,
                estilo = estiloMontoTitular,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(
                text = "Ventas ${resumen.ventas.aPesosSinSimbolo()}",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.venta
            )
            Text(
                text = "Gastos ${resumen.gastos.aPesosSinSimbolo()}",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.gasto
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun MovimientosPreview() {
    FiaoTheme {
        MovimientosScreen(
            fecha = LocalDate.now(),
            resumen = DatosDemo.resumenHoy,
            movimientos = DatosDemo.movimientos,
            onVolver = {},
            onCambiarFecha = {},
            onNuevoMovimiento = {},
            onMovimientoClick = {}
        )
    }
}
