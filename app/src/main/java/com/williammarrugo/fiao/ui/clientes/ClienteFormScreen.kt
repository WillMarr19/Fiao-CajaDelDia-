package com.williammarrugo.fiao.ui.clientes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.williammarrugo.fiao.ui.componentes.BarraSuperior
import com.williammarrugo.fiao.ui.componentes.CampoTexto
import com.williammarrugo.fiao.ui.theme.FiaoTheme

/**
 * Crear y editar clientes (F6).
 *
 * El botón de eliminar solo aparece si el cliente no debe nada
 * (regla R6). Cuando debe, en lugar del botón se muestra una
 * explicación: esconder un control sin decir por qué deja al usuario
 * pensando que la app está rota.
 */
@Composable
fun ClienteFormScreen(
    estado: ClienteFormUiState,
    onNombreCambia: (String) -> Unit,
    onTelefonoCambia: (String) -> Unit,
    onGuardar: () -> Unit,
    onEliminar: () -> Unit,
    onVolver: () -> Unit,
    onErrorMostrado: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrandoDialogo by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(estado.guardado, estado.eliminado) {
        if (estado.guardado || estado.eliminado) onVolver()
    }

    LaunchedEffect(estado.errorGeneral) {
        val mensaje = estado.errorGeneral
        if (mensaje != null) {
            snackbarHostState.showSnackbar(mensaje)
            onErrorMostrado()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BarraSuperior(
                titulo = if (estado.esEdicion) "Editar cliente" else "Nuevo cliente",
                onVolver = onVolver,
                acciones = {
                    if (estado.esEdicion && estado.puedeEliminar) {
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
                enabled = !estado.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (estado.guardando) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else {
                    Text(
                        text = if (estado.esEdicion) "Guardar cambios" else "Agregar cliente",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (estado.esEdicion && !estado.puedeEliminar) {
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
            estado = ClienteFormUiState(
                esEdicion = true,
                nombre = "Rosa Herrera",
                telefono = "3001234567",
                puedeEliminar = false
            ),
            onNombreCambia = {},
            onTelefonoCambia = {},
            onGuardar = {},
            onEliminar = {},
            onVolver = {},
            onErrorMostrado = {}
        )
    }
}
