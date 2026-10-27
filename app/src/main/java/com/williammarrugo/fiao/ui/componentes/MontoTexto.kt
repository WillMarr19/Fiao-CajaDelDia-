package com.williammarrugo.fiao.ui.componentes

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.williammarrugo.fiao.domain.model.TipoMovimiento
import com.williammarrugo.fiao.ui.theme.LocalColoresFiao
import com.williammarrugo.fiao.ui.theme.estiloMontoFila
import com.williammarrugo.fiao.util.aPesos
import com.williammarrugo.fiao.util.aPesosConSigno

/**
 * Un monto de plata, con el color que le corresponde.
 *
 * Existe para que el formato y el color del dinero se decidan en un solo
 * lugar. Si mañana cambia el formato de pesos o el verde de las ventas,
 * se toca aquí y no en siete pantallas.
 */
@Composable
fun MontoTexto(
    monto: Long,
    modifier: Modifier = Modifier,
    estilo: TextStyle = estiloMontoFila,
    color: Color = MaterialTheme.colorScheme.onSurface,
    conSigno: Boolean = false
) {
    Text(
        text = if (conSigno) monto.aPesosConSigno() else monto.aPesos(),
        style = estilo,
        color = color,
        modifier = modifier
    )
}

/**
 * El monto de un movimiento: verde si entra plata, terracota si sale,
 * y siempre con su signo.
 *
 * El signo y el color dicen lo mismo a propósito. Quien no distinga bien
 * los colores lee el signo; quien mira rápido ve el color. Nunca se
 * depende solo del color para comunicar algo.
 */
@Composable
fun MontoMovimiento(
    tipo: TipoMovimiento,
    monto: Long,
    modifier: Modifier = Modifier,
    estilo: TextStyle = estiloMontoFila
) {
    val colores = LocalColoresFiao.current
    val esVenta = tipo == TipoMovimiento.VENTA
    MontoTexto(
        monto = if (esVenta) monto else -monto,
        modifier = modifier,
        estilo = estilo,
        color = if (esVenta) colores.venta else colores.gasto,
        conSigno = true
    )
}
