package com.williammarrugo.fiao.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.williammarrugo.fiao.domain.model.DatosDemo
import com.williammarrugo.fiao.ui.theme.FiaoTheme
import com.williammarrugo.fiao.ui.theme.LocalColoresFiao
import com.williammarrugo.fiao.ui.theme.estiloMontoTitular

/**
 * Lo que deben los clientes.
 *
 * Esta sí es una tarjeta, a diferencia de las filas de movimiento. La
 * plata fiada es otra clase de dinero: no está en la caja, está en la
 * calle. Separarla visualmente evita el error de sumarla al balance del
 * día, que es justo lo que dice la regla R3.
 *
 * La franja ocre del borde izquierdo es el único adorno de la pantalla,
 * y está ahí para marcar esa diferencia.
 */
@Composable
fun TarjetaPorCobrar(
    total: Long,
    cantidadClientes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = LocalColoresFiao.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colores.fiadoSuave),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                    .background(colores.fiado)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Por cobrar",
                    style = MaterialTheme.typography.labelLarge,
                    color = colores.fiado
                )
                Text(
                    text = if (cantidadClientes == 1) "1 cliente debe"
                    else "$cantidadClientes clientes deben",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colores.fiado
                )
            }
            MontoTexto(
                monto = total,
                estilo = estiloMontoTitular,
                color = colores.fiado,
                modifier = Modifier.padding(end = 18.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaPorCobrarPreview() {
    FiaoTheme {
        TarjetaPorCobrar(
            total = DatosDemo.totalPorCobrar,
            cantidadClientes = DatosDemo.clientesQueDeben,
            onClick = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}
