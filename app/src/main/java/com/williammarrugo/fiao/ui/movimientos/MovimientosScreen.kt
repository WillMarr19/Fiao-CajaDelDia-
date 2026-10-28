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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.williammarrugo.fiao.domain.model.DatosDemo
import com.williammarrugo.fiao.ui.componentes.BarraSuperior
import com.williammarrugo.fiao.ui.componentes.EstadoVacio
import com.williammarrugo.fiao.ui.componentes.FilaMovimiento
import com.williammarrugo.fiao.ui.componentes.MontoTexto
import com.williammarrugo.fiao.ui.componentes.SeparadorFila
import com.williammarrugo.fiao.ui.theme.FiaoTheme
import com.williammarrugo.fiao.ui.theme.LocalColoresFiao
import com.williammarrugo.fiao.ui.theme.estiloMontoTitular
import com.williammarrugo.fiao.util.aPesosSinSimbolo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatoFechaCorta =
    DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-CO"))

/**
 * El libro de un día, con filtro de fecha (F5).
 *
 * La pantalla recibe el estado ya calculado y avisa hacia arriba cuando
 * el usuario elige otra fecha. No sabe de dónde salen los movimientos.
 */
@Composable
fun MovimientosScreen(
    estado: MovimientosUiState,
    onVolver: () -> Unit,
    onCambiarFecha: (LocalDate) -> Unit,
    onNuevoMovimiento: () -> Unit,
    onMovimientoClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // rememberSaveable y no remember: si el usuario rota el celular con
    // el calendario abierto, debe seguir abierto.
    var mostrandoCalendario by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(
                titulo = "Movimientos",
                onVolver = onVolver,
                acciones = {
                    IconButton(onClick = { mostrandoCalendario = true }) {
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
                ResumenDelDia(estado = estado)
            }

            if (estado.movimientos.isEmpty()) {
                item {
                    EstadoVacio(
                        titulo = "Ningún movimiento este día",
                        descripcion = "Cambia de fecha con el calendario o registra uno nuevo."
                    )
                }
            } else {
                itemsIndexed(
                    items = estado.movimientos,
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

    if (mostrandoCalendario) {
        SelectorDeFecha(
            fechaActual = estado.dia,
            onCerrar = { mostrandoCalendario = false },
            onElegir = { fecha ->
                mostrandoCalendario = false
                onCambiarFecha(fecha)
            }
        )
    }
}

/**
 * El calendario de Material 3.
 *
 * Un detalle que da problemas: el selector trabaja en milisegundos UTC,
 * no en la zona horaria del dispositivo. Si se convierte con la zona
 * local, en Colombia (UTC-5) el día elegido se corre uno hacia atrás.
 * Por eso aquí se usa ZoneOffset.UTC en las dos conversiones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorDeFecha(
    fechaActual: LocalDate,
    onCerrar: () -> Unit,
    onElegir: (LocalDate) -> Unit
) {
    val estadoPicker = rememberDatePickerState(
        initialSelectedDateMillis = fechaActual
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onCerrar,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = estadoPicker.selectedDateMillis
                    if (millis != null) {
                        onElegir(
                            Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                        )
                    } else {
                        onCerrar()
                    }
                }
            ) { Text("Ver este día") }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = estadoPicker)
    }
}

@Composable
private fun ResumenDelDia(estado: MovimientosUiState) {
    val colores = LocalColoresFiao.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = etiquetaDeFecha(estado.dia),
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
                monto = estado.resumen.balance,
                estilo = estiloMontoTitular,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(
                text = "Ventas ${estado.resumen.ventas.aPesosSinSimbolo()}",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.venta
            )
            Text(
                text = "Gastos ${estado.resumen.gastos.aPesosSinSimbolo()}",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.gasto
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}

/** "Hoy" se lee mejor que la fecha de hoy escrita completa. */
private fun etiquetaDeFecha(dia: LocalDate): String {
    val hoy = LocalDate.now()
    return when (dia) {
        hoy -> "Hoy"
        hoy.minusDays(1) -> "Ayer"
        else -> dia.format(formatoFechaCorta)
    }
}

@Preview(showBackground = true)
@Composable
private fun MovimientosPreview() {
    FiaoTheme {
        MovimientosScreen(
            estado = MovimientosUiState(
                cargando = false,
                dia = LocalDate.now(),
                resumen = DatosDemo.resumenHoy,
                movimientos = DatosDemo.movimientos
            ),
            onVolver = {},
            onCambiarFecha = {},
            onNuevoMovimiento = {},
            onMovimientoClick = {}
        )
    }
}
