package com.williammarrugo.fiao.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.williammarrugo.fiao.domain.model.TipoMovimiento
import com.williammarrugo.fiao.ui.theme.FiaoTheme
import com.williammarrugo.fiao.ui.theme.LocalColoresFiao

/**
 * Elegir entre venta y gasto.
 *
 * Dos chips y no un menú desplegable: son solo dos opciones y se usan
 * todo el tiempo, así que deben estar a un toque. Cada uno se pinta con
 * su color semántico cuando está activo, de modo que el color refuerza
 * lo que dice la palabra.
 *
 * Los chips miden 48dp de alto. El mínimo recomendado para tocar con el
 * dedo es 48dp, y esta app se usa de pie y con una mano.
 */
@Composable
fun SelectorTipo(
    seleccionado: TipoMovimiento,
    onSeleccionar: (TipoMovimiento) -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = LocalColoresFiao.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterChip(
            selected = seleccionado == TipoMovimiento.VENTA,
            onClick = { onSeleccionar(TipoMovimiento.VENTA) },
            label = {
                Text(
                    text = "Venta",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = colores.ventaSuave,
                selectedLabelColor = colores.venta
            )
        )
        FilterChip(
            selected = seleccionado == TipoMovimiento.GASTO,
            onClick = { onSeleccionar(TipoMovimiento.GASTO) },
            label = {
                Text(
                    text = "Gasto",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = colores.gastoSuave,
                selectedLabelColor = colores.gasto
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectorTipoPreview() {
    FiaoTheme {
        SelectorTipo(seleccionado = TipoMovimiento.VENTA, onSeleccionar = {})
    }
}
