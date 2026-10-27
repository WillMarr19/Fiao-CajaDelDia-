package com.williammarrugo.fiao.ui.inicio

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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.williammarrugo.fiao.domain.model.DatosDemo
import com.williammarrugo.fiao.domain.model.Movimiento
import com.williammarrugo.fiao.domain.model.ResumenDia
import com.williammarrugo.fiao.ui.componentes.EstadoVacio
import com.williammarrugo.fiao.ui.componentes.FilaMovimiento
import com.williammarrugo.fiao.ui.componentes.SeparadorFila
import com.williammarrugo.fiao.ui.componentes.TarjetaPorCobrar
import com.williammarrugo.fiao.ui.theme.FiaoTheme
import com.williammarrugo.fiao.ui.theme.LocalColoresFiao
import com.williammarrugo.fiao.ui.theme.estiloMontoHeroe
import com.williammarrugo.fiao.util.aPesos
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatoFecha =
    DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.forLanguageTag("es-CO"))

/**
 * Pantalla de inicio.
 *
 * El protagonista es el balance del día: un número grande, arriba, sin
 * nada que compita con él. Todo lo demás de la pantalla existe para
 * explicarlo o para llevar a otra parte.
 *
 * Esta pantalla todavía no tiene ViewModel: recibe los datos por
 * parámetro. Eso la hace fácil de ver en preview y, cuando llegue el
 * ViewModel en el día 6, solo cambia quién le pasa los datos.
 */
@Composable
fun InicioScreen(
    resumen: ResumenDia,
    movimientos: List<Movimiento>,
    totalPorCobrar: Long,
    clientesQueDeben: Int,
    onNuevoMovimiento: () -> Unit,
    onMovimientoClick: (Long) -> Unit,
    onVerMovimientos: () -> Unit,
    onVerClientes: () -> Unit,
    onVerResumen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevoMovimiento,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(
                    text = "Registrar",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                CabeceraBalance(resumen = resumen)
            }

            item {
                TarjetaPorCobrar(
                    total = totalPorCobrar,
                    cantidadClientes = clientesQueDeben,
                    onClick = onVerClientes,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = onVerResumen,
                    modifier = Modifier.padding(start = 8.dp)
                ) { Text("Ver cómo va la semana") }
                Spacer(Modifier.height(14.dp))
            }

            item {
                EncabezadoSeccion(
                    titulo = "Movimientos de hoy",
                    textoAccion = if (movimientos.isEmpty()) null else "Ver todos",
                    onAccion = onVerMovimientos
                )
            }

            if (movimientos.isEmpty()) {
                item {
                    EstadoVacio(
                        titulo = "Todavía no has registrado nada hoy",
                        descripcion = "Apunta la primera venta y el balance empieza a moverse.",
                        textoAccion = "Registrar venta",
                        onAccion = onNuevoMovimiento
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
private fun CabeceraBalance(resumen: ResumenDia) {
    val colores = LocalColoresFiao.current
    val hoy = LocalDate.now().format(formatoFecha)
        .replaceFirstChar { it.uppercase() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = hoy,
            style = MaterialTheme.typography.bodyMedium,
            color = colores.tintaSuave
        )
        Text(
            text = resumen.balance.aPesos(),
            style = estiloMontoHeroe,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            DatoBalance("Ventas", resumen.ventas.aPesos(), colores.venta)
            DatoBalance("Gastos", resumen.gastos.aPesos(), colores.gasto)
            DatoBalance("Abonos", resumen.abonos.aPesos(), colores.venta)
        }
    }
}

@Composable
private fun DatoBalance(etiqueta: String, valor: String, color: androidx.compose.ui.graphics.Color) {
    val colores = LocalColoresFiao.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = colores.tintaSuave
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
    }
}

@Composable
internal fun EncabezadoSeccion(
    titulo: String,
    textoAccion: String? = null,
    onAccion: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, bottom = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
        if (textoAccion != null) {
            TextButton(onClick = onAccion) { Text(textoAccion) }
        }
    }
}

@Preview(showBackground = true, name = "Inicio con datos")
@Composable
private fun InicioPreview() {
    FiaoTheme {
        InicioScreen(
            resumen = DatosDemo.resumenHoy,
            movimientos = DatosDemo.movimientos,
            totalPorCobrar = DatosDemo.totalPorCobrar,
            clientesQueDeben = DatosDemo.clientesQueDeben,
            onNuevoMovimiento = {},
            onMovimientoClick = {},
            onVerMovimientos = {},
            onVerClientes = {},
            onVerResumen = {}
        )
    }
}

@Preview(showBackground = true, name = "Inicio vacío")
@Composable
private fun InicioVacioPreview() {
    FiaoTheme {
        InicioScreen(
            resumen = ResumenDia(0, 0, 0),
            movimientos = emptyList(),
            totalPorCobrar = 0,
            clientesQueDeben = 0,
            onNuevoMovimiento = {},
            onMovimientoClick = {},
            onVerMovimientos = {},
            onVerClientes = {},
            onVerResumen = {}
        )
    }
}
