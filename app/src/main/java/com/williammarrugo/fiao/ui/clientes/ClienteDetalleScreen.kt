package com.williammarrugo.fiao.ui.clientes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.williammarrugo.fiao.domain.model.Cliente
import com.williammarrugo.fiao.domain.model.DatosDemo
import com.williammarrugo.fiao.domain.model.EntradaHistorial
import com.williammarrugo.fiao.ui.componentes.BarraSuperior
import com.williammarrugo.fiao.ui.componentes.EstadoVacio
import com.williammarrugo.fiao.ui.componentes.MontoTexto
import com.williammarrugo.fiao.ui.componentes.SeparadorFila
import com.williammarrugo.fiao.ui.inicio.EncabezadoSeccion
import com.williammarrugo.fiao.ui.theme.FiaoTheme
import com.williammarrugo.fiao.ui.theme.LocalColoresFiao
import com.williammarrugo.fiao.ui.theme.estiloMontoFila
import com.williammarrugo.fiao.ui.theme.estiloMontoHeroe
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatoFecha =
    DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-CO"))

/**
 * La cuenta de un cliente.
 *
 * El saldo manda, y debajo van las dos acciones que el tendero hace a
 * diario: fiar y abonar. Abonar es la acción principal (botón lleno)
 * porque es la que cierra el ciclo del negocio.
 */
@Composable
fun ClienteDetalleScreen(
    cliente: Cliente,
    saldo: Long,
    historial: List<EntradaHistorial>,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    onFiar: () -> Unit,
    onAbonar: () -> Unit,
    onRecordar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = LocalColoresFiao.current

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(
                titulo = cliente.nombre,
                onVolver = onVolver,
                acciones = {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar cliente")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (saldo > 0) "Debe" else "Está al día",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colores.tintaSuave
                    )
                    MontoTexto(
                        monto = saldo,
                        estilo = estiloMontoHeroe,
                        color = if (saldo > 0) colores.fiado else MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(Modifier.height(18.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onAbonar,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            enabled = saldo > 0
                        ) { Text("Abonar") }

                        OutlinedButton(
                            onClick = onFiar,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) { Text("Fiar") }
                    }

                    if (cliente.telefono != null && saldo > 0) {
                        TextButton(
                            onClick = onRecordar,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Mandar recordatorio por WhatsApp")
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }

            item { EncabezadoSeccion(titulo = "Movimientos de la cuenta") }

            if (historial.isEmpty()) {
                item {
                    EstadoVacio(
                        titulo = "Sin movimientos todavía",
                        descripcion = "Cuando le fíes algo, aparecerá aquí."
                    )
                }
            } else {
                items(
                    items = historial,
                    key = { entrada ->
                        when (entrada) {
                            is EntradaHistorial.DeFiado -> "f${entrada.fiado.id}"
                            is EntradaHistorial.DeAbono -> "a${entrada.abono.id}"
                        }
                    }
                ) { entrada ->
                    FilaHistorial(entrada)
                    SeparadorFila()
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

/**
 * Una entrada del historial.
 *
 * El `when` sobre la sealed class no lleva `else`: el compilador sabe
 * que solo hay dos casos y, si mañana se agrega un tercero, marcará
 * error justo aquí. Es la ventaja de modelar con sealed en vez de con
 * un campo de texto.
 */
@Composable
private fun FilaHistorial(entrada: EntradaHistorial) {
    val colores = LocalColoresFiao.current

    val (etiqueta, descripcion, color) = when (entrada) {
        is EntradaHistorial.DeFiado ->
            Triple("Fiado", entrada.fiado.descripcion, colores.fiado)
        is EntradaHistorial.DeAbono ->
            Triple("Abono", "Pago recibido", colores.venta)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$etiqueta · ${entrada.fecha.format(formatoFecha)}",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.tintaSuave
            )
        }
        MontoTexto(
            monto = entrada.monto,
            estilo = estiloMontoFila,
            color = color
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ClienteDetallePreview() {
    FiaoTheme {
        ClienteDetalleScreen(
            cliente = DatosDemo.clientes[0].cliente,
            saldo = DatosDemo.clientes[0].saldo,
            historial = DatosDemo.historialCliente,
            onVolver = {},
            onEditar = {},
            onFiar = {},
            onAbonar = {},
            onRecordar = {}
        )
    }
}
