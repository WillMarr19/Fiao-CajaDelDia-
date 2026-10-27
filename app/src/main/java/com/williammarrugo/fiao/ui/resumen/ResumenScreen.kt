package com.williammarrugo.fiao.ui.resumen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.williammarrugo.fiao.domain.model.DatosDemo
import com.williammarrugo.fiao.domain.model.DiaResumen
import com.williammarrugo.fiao.ui.componentes.BarraSuperior
import com.williammarrugo.fiao.ui.componentes.EstadoVacio
import com.williammarrugo.fiao.ui.componentes.MontoTexto
import com.williammarrugo.fiao.ui.theme.FiaoTheme
import com.williammarrugo.fiao.ui.theme.LocalColoresFiao
import com.williammarrugo.fiao.ui.theme.estiloMontoTitular
import com.williammarrugo.fiao.util.aPesos

/**
 * Resumen de los últimos 7 días.
 *
 * Dos series (ventas y gastos) por día, en barras agrupadas. Se eligió
 * barra y no línea porque cada día es una categoría cerrada, no una
 * medición continua: no existe "medio martes".
 *
 * La gráfica está dibujada a mano con Canvas, sin librerías externas.
 * Para dos series y siete días no se justifica traer una dependencia.
 */
@Composable
fun ResumenScreen(
    dias: List<DiaResumen>,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = LocalColoresFiao.current
    val totalVentas = dias.sumOf { it.ventas }
    val totalGastos = dias.sumOf { it.gastos }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Estos 7 días", onVolver = onVolver) }
    ) { padding ->
        if (dias.isEmpty()) {
            EstadoVacio(
                titulo = "Sin datos de la semana",
                descripcion = "Registra movimientos y aquí verás cómo va el negocio.",
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Balance de la semana",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colores.tintaSuave
                )
                MontoTexto(
                    monto = totalVentas - totalGastos,
                    estilo = estiloMontoTitular,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Leyenda: con dos series siempre va, para que la identidad
            // no dependa solo del color.
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                ItemLeyenda("Ventas", colores.venta, totalVentas)
                ItemLeyenda("Gastos", colores.gasto, totalGastos)
            }

            GraficaSemana(
                dias = dias,
                colorVentas = colores.venta,
                colorGastos = colores.gasto,
                colorGuias = colores.borde,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                dias.forEach { dia ->
                    Text(
                        text = dia.etiqueta,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colores.tintaSuave,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            TablaDias(dias = dias)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ItemLeyenda(etiqueta: String, color: Color, total: Long) {
    val colores = LocalColoresFiao.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                color = colores.tintaSuave
            )
            Text(
                text = total.aPesos(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/**
 * Barras agrupadas dibujadas con Canvas.
 *
 * Detalles que no son casualidad:
 * - Una sola escala en el eje vertical. Dos escalas distintas para
 *   ventas y gastos harían que las alturas no se puedan comparar.
 * - Las barras nacen en la línea base y llevan las puntas redondeadas
 *   solo arriba.
 * - Hay 2dp de separación entre la barra de ventas y la de gastos del
 *   mismo día, para que se lean como dos marcas y no como una sola.
 * - Las guías horizontales van en un gris tenue: son referencia, no
 *   contenido.
 */
@Composable
private fun GraficaSemana(
    dias: List<DiaResumen>,
    colorVentas: Color,
    colorGastos: Color,
    colorGuias: Color,
    modifier: Modifier = Modifier
) {
    val maximo = dias.maxOf { maxOf(it.ventas, it.gastos) }.coerceAtLeast(1L)

    Canvas(modifier = modifier) {
        val anchoGrupo = size.width / dias.size
        val separacion = 2.dp.toPx()
        val anchoBarra = ((anchoGrupo * 0.62f) - separacion) / 2f
        val radio = CornerRadius(4.dp.toPx(), 4.dp.toPx())

        // Guías: base, mitad y tope.
        listOf(0f, 0.5f, 1f).forEach { fraccion ->
            val y = size.height * (1f - fraccion)
            drawLine(
                color = colorGuias,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        dias.forEachIndexed { indice, dia ->
            val centro = anchoGrupo * indice + anchoGrupo / 2f
            val izquierdaVentas = centro - separacion / 2f - anchoBarra
            val izquierdaGastos = centro + separacion / 2f

            dibujarBarra(
                x = izquierdaVentas,
                ancho = anchoBarra,
                valor = dia.ventas,
                maximo = maximo,
                color = colorVentas,
                radio = radio
            )
            dibujarBarra(
                x = izquierdaGastos,
                ancho = anchoBarra,
                valor = dia.gastos,
                maximo = maximo,
                color = colorGastos,
                radio = radio
            )
        }
    }
}

private fun DrawScope.dibujarBarra(
    x: Float,
    ancho: Float,
    valor: Long,
    maximo: Long,
    color: Color,
    radio: CornerRadius
) {
    if (valor <= 0L) return
    val alto = size.height * (valor.toFloat() / maximo.toFloat())
    drawRoundRect(
        color = color,
        topLeft = Offset(x, size.height - alto),
        size = Size(ancho, alto),
        cornerRadius = radio
    )
}

/**
 * La misma información en texto.
 *
 * Una gráfica se lee rápido pero es imprecisa, y quien use lector de
 * pantalla no la ve. La tabla da los números exactos y hace que el
 * color deje de ser indispensable.
 */
@Composable
private fun TablaDias(dias: List<DiaResumen>) {
    val colores = LocalColoresFiao.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Día por día",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        dias.forEach { dia ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dia.etiqueta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colores.tintaSuave,
                    modifier = Modifier.weight(1f)
                )
                MontoTexto(
                    monto = dia.ventas,
                    color = colores.venta,
                    modifier = Modifier.padding(end = 16.dp)
                )
                MontoTexto(
                    monto = dia.gastos,
                    color = colores.gasto
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResumenPreview() {
    FiaoTheme {
        ResumenScreen(dias = DatosDemo.semana, onVolver = {})
    }
}
