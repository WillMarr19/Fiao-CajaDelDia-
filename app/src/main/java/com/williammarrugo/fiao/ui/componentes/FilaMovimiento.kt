package com.williammarrugo.fiao.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.williammarrugo.fiao.domain.model.DatosDemo
import com.williammarrugo.fiao.domain.model.Movimiento
import com.williammarrugo.fiao.domain.model.TipoMovimiento
import com.williammarrugo.fiao.ui.theme.FiaoTheme
import com.williammarrugo.fiao.ui.theme.LocalColoresFiao
import java.time.format.DateTimeFormatter

private val formatoHora = DateTimeFormatter.ofPattern("h:mm a")

/**
 * Una línea del libro del día.
 *
 * Es una fila, no una tarjeta. En esta app las tarjetas significan
 * "esto es un objeto aparte"; una lista de veinte movimientos son
 * veinte líneas de un mismo libro, y convertirlas en veinte tarjetas
 * idénticas agregaría ruido sin agregar información.
 */
@Composable
fun FilaMovimiento(
    movimiento: Movimiento,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = LocalColoresFiao.current
    val esVenta = movimiento.tipo == TipoMovimiento.VENTA

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(
                    color = if (esVenta) colores.venta else colores.gasto,
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = movimiento.descripcion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = movimiento.fecha.format(formatoHora),
                style = MaterialTheme.typography.bodyMedium,
                color = colores.tintaSuave
            )
        }

        MontoMovimiento(
            tipo = movimiento.tipo,
            monto = movimiento.monto,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

/** Línea divisoria fina entre filas. Empieza donde empieza el texto. */
@Composable
fun SeparadorFila(modifier: Modifier = Modifier) {
    val colores = LocalColoresFiao.current
    Row(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.width(44.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(colores.borde)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilaMovimientoPreview() {
    FiaoTheme {
        Column {
            FilaMovimiento(DatosDemo.movimientos[0], onClick = {})
            SeparadorFila()
            FilaMovimiento(DatosDemo.movimientos[1], onClick = {})
        }
    }
}
