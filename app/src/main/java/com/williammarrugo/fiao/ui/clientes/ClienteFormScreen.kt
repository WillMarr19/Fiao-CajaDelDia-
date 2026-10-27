package com.williammarrugo.fiao.ui.clientes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.williammarrugo.fiao.ui.componentes.BarraSuperior
import com.williammarrugo.fiao.ui.componentes.CampoTexto
import com.williammarrugo.fiao.ui.theme.FiaoTheme

data class EstadoFormularioCliente(
    val nombre: String = "",
    val telefono: String = "",
    val errorNombre: String? = null,
    /** Regla R6: no se borra un cliente que todavía debe. */
    val puedeEliminar: Boolean = true
)

/**
 * Crear o editar un cliente.
 *
 * Solo dos campos. Pedir más (cédula, dirección, correo) alargaría el
 * formulario sin que el tendero vaya a llenarlos, y el objetivo es
 * poder anotar un fiado en segundos mientras el cliente espera.
 */
@Composable
fun ClienteFormScreen(
    estado: EstadoFormularioCliente,
    esEdicion: Boolean,
    onNombreCambia: (String) -> Unit,
    onTelefonoCambia: (String) -> Unit,
    onGuardar: () -> Unit,
    onEliminar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrandoDialogo by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(
                titulo = if (esEdicion) "Editar cliente" else "Nuevo cliente",
                onVolver = onVolver,
                acciones = {
                    if (esEdicion && estado.puedeEliminar) {
                        IconButton(onClick = { mostrandoDialogo = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar cliente"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CampoTexto(
                valor = estado.nombre,
                onValorCambia = onNombreCambia,
                etiqueta = "Nombre",
                error = estado.errorNombre,
                ayuda = "Como lo tienes anotado en el cuaderno"
            )

            CampoTexto(
                valor = estado.telefono,
                onValorCambia = onTelefonoCambia,
                etiqueta = "Teléfono (opcional)",
                ayuda = "Sirve para mandarle el recordatorio por WhatsApp",
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            )

            Button(
                onClick = onGuardar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (esEdicion) "Guardar cambios" else "Agregar cliente",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (esEdicion && !estado.puedeEliminar) {
                Text(
                    text = "Este cliente no se puede eliminar mientras tenga saldo pendiente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (mostrandoDialogo) {
        AlertDialog(
            onDismissRequest = { mostrandoDialogo = false },
            title = { Text("¿Eliminar a ${estado.nombre}?") },
            text = { Text("Se borra el cliente y su historial de fiados y abonos.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrandoDialogo = false
                        onEliminar()
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrandoDialogo = false }) { Text("Cancelar") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ClienteFormPreview() {
    FiaoTheme {
        ClienteFormScreen(
            estado = EstadoFormularioCliente(
                nombre = "Rosa Herrera",
                telefono = "3001234567",
                puedeEliminar = false
            ),
            esEdicion = true,
            onNombreCambia = {},
            onTelefonoCambia = {},
            onGuardar = {},
            onEliminar = {},
            onVolver = {}
        )
    }
}
