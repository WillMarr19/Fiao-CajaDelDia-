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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.williammarrugo.fiao.domain.model.ClienteConSaldo
import com.williammarrugo.fiao.domain.model.DatosDemo
import com.williammarrugo.fiao.ui.componentes.BarraSuperior
import com.williammarrugo.fiao.ui.componentes.EstadoVacio
import com.williammarrugo.fiao.ui.componentes.FilaCliente
import com.williammarrugo.fiao.ui.componentes.MontoTexto
import com.williammarrugo.fiao.ui.componentes.SeparadorFila
import com.williammarrugo.fiao.ui.theme.FiaoTheme
import com.williammarrugo.fiao.ui.theme.LocalColoresFiao
import com.williammarrugo.fiao.ui.theme.estiloMontoTitular

/**
 * Quién debe.
 *
 * Ordenada por mayor deuda, porque esa es la pregunta que trae al
 * usuario a esta pantalla. La búsqueda aparece arriba porque con
 * treinta clientes la lista deja de servir para encontrar a uno.
 */
@Composable
fun ClientesScreen(
    clientes: List<ClienteConSaldo>,
    totalPorCobrar: Long,
    busqueda: String,
    onBusquedaCambia: (String) -> Unit,
    onVolver: () -> Unit,
    onClienteClick: (Long) -> Unit,
    onNuevoCliente: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = LocalColoresFiao.current

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(titulo = "Clientes", onVolver = onVolver)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNuevoCliente,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo cliente")
            }
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Total por cobrar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colores.tintaSuave,
                            modifier = Modifier.weight(1f)
                        )
                        MontoTexto(
                            monto = totalPorCobrar,
                            estilo = estiloMontoTitular,
                            color = colores.fiado
                        )
                    }

                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = onBusquedaCambia,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar cliente") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        singleLine = true
                    )
                    Spacer(Modifier.height(2.dp))
                }
            }

            if (clientes.isEmpty()) {
                item {
                    if (busqueda.isBlank()) {
                        EstadoVacio(
                            titulo = "Todavía no tienes clientes",
                            descripcion = "Agrega a quien le fías y lleva la cuenta de lo que debe.",
                            textoAccion = "Agregar cliente",
                            onAccion = onNuevoCliente
                        )
                    } else {
                        EstadoVacio(
                            titulo = "Ningún cliente se llama así",
                            descripcion = "Revisa cómo lo escribiste o agrégalo."
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items = clientes,
                    key = { _, item -> item.cliente.id }
                ) { indice, item ->
                    if (indice > 0) SeparadorFila()
                    FilaCliente(
                        clienteConSaldo = item,
                        onClick = { onClienteClick(item.cliente.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(96.dp)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClientesPreview() {
    FiaoTheme {
        ClientesScreen(
            clientes = DatosDemo.clientes,
            totalPorCobrar = DatosDemo.totalPorCobrar,
            busqueda = "",
            onBusquedaCambia = {},
            onVolver = {},
            onClienteClick = {},
            onNuevoCliente = {}
        )
    }
}
