package com.williammarrugo.fiao.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.williammarrugo.fiao.domain.model.ClienteConSaldo
import com.williammarrugo.fiao.domain.model.DatosDemo
import com.williammarrugo.fiao.ui.theme.FiaoTheme
import com.williammarrugo.fiao.ui.theme.LocalColoresFiao
import com.williammarrugo.fiao.ui.theme.estiloMontoFila

/**
 * Una fila de la lista de clientes: inicial, nombre y saldo.
 *
 * El saldo en ocre cuando debe, y en gris apagado cuando está en cero.
 * Así la vista responde de un vistazo a la pregunta que de verdad hace
 * el tendero: quién me debe.
 */
@Composable
fun FilaCliente(
    clienteConSaldo: ClienteConSaldo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = LocalColoresFiao.current
    val cliente = clienteConSaldo.cliente
    val debe = clienteConSaldo.saldo > 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (debe) colores.fiadoSuave else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cliente.nombre.trim().take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = if (debe) colores.fiado else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = cliente.nombre,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = cliente.telefono ?: "Sin teléfono",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.tintaSuave
            )
        }

        if (debe) {
            MontoTexto(
                monto = clienteConSaldo.saldo,
                estilo = estiloMontoFila,
                color = colores.fiado
            )
        } else {
            Text(
                text = "Al día",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.tintaSuave
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilaClientePreview() {
    FiaoTheme {
        Column {
            FilaCliente(DatosDemo.clientes[0], onClick = {})
            SeparadorFila()
            FilaCliente(DatosDemo.clientes[4], onClick = {})
        }
    }
}
