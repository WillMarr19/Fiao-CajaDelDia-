package com.williammarrugo.fiao.ui.movimientos

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.williammarrugo.fiao.domain.model.TipoMovimiento
import com.williammarrugo.fiao.ui.componentes.BarraSuperior
import com.williammarrugo.fiao.ui.componentes.CampoMonto
import com.williammarrugo.fiao.ui.componentes.CampoTexto
import com.williammarrugo.fiao.ui.componentes.SelectorTipo
import com.williammarrugo.fiao.ui.theme.FiaoTheme

/**
 * Crear (F1), editar y eliminar (F3) un movimiento.
 *
 * La pantalla sigue sin saber nada de bases de datos: recibe el estado
 * y reporta lo que hace el usuario. Lo nuevo respecto a la versión del
 * armazón son tres cosas:
 *
 * - `LaunchedEffect` observa `guardado` y `eliminado` para navegar
 *   hacia atrás cuando la operación termina.
 * - Un indicador de progreso mientras escribe en disco.
 * - Un Snackbar para los errores que no son de un campo concreto.
 */
@Composable
fun MovimientoFormScreen(
    estado: MovimientoFormUiState,
    onTipoCambia: (TipoMovimiento) -> Unit,
    onMontoCambia: (Long?) -> Unit,
    onDescripcionCambia: (String) -> Unit,
    onGuardar: () -> Unit,
    onEliminar: () -> Unit,
    onVolver: () -> Unit,
    onErrorMostrado: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrandoDialogo by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    /**
     * LaunchedEffect corre su bloque cuando la clave cambia.
     *
     * Aquí la clave es el par (guardado, eliminado): en cuanto el
     * ViewModel pone alguno en true, se navega hacia atrás. Es la forma
     * de reaccionar a un cambio de estado sin hacerlo durante el
     * dibujado, que en Compose es un error: el cuerpo de un composable
     * puede ejecutarse muchas veces y navegar ahí dispararía varias
     * navegaciones.
     */
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
                titulo = if (estado.esEdicion) "Editar movimiento" else "Nuevo movimiento",
                onVolver = onVolver,
                acciones = {
                    if (estado.esEdicion) {
                        IconButton(onClick = { mostrandoDialogo = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar movimiento"
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
            SelectorTipo(
                seleccionado = estado.tipo,
                onSeleccionar = onTipoCambia
            )

            CampoMonto(
                valor = estado.monto,
                onValorCambia = onMontoCambia,
                error = estado.errorMonto
            )

            CampoTexto(
                valor = estado.descripcion,
                onValorCambia = onDescripcionCambia,
                etiqueta = "¿De qué fue?",
                error = estado.errorDescripcion,
                ayuda = "Por ejemplo: gaseosas, hielo, recarga",
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
                    // Mientras escribe en disco: el botón se desactiva
                    // y muestra actividad, para que nadie toque dos
                    // veces y queden dos movimientos iguales.
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else {
                    Text(
                        text = if (estado.esEdicion) "Guardar cambios" else "Registrar",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    if (mostrandoDialogo) {
        AlertDialog(
            onDismissRequest = { mostrandoDialogo = false },
            title = { Text("¿Eliminar este movimiento?") },
            text = { Text("Se quita del día y el balance se recalcula. No se puede deshacer.") },
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

@Preview(showBackground = true, name = "Nuevo movimiento")
@Composable
private fun MovimientoFormPreview() {
    FiaoTheme {
        MovimientoFormScreen(
            estado = MovimientoFormUiState(monto = 12_000, descripcion = "Gaseosas"),
            onTipoCambia = {},
            onMontoCambia = {},
            onDescripcionCambia = {},
            onGuardar = {},
            onEliminar = {},
            onVolver = {},
            onErrorMostrado = {}
        )
    }
}

@Preview(showBackground = true, name = "Con errores")
@Composable
private fun MovimientoFormErrorPreview() {
    FiaoTheme {
        MovimientoFormScreen(
            estado = MovimientoFormUiState(
                esEdicion = true,
                tipo = TipoMovimiento.GASTO,
                errorMonto = "El monto debe ser mayor que cero",
                errorDescripcion = "Escribe de qué fue"
            ),
            onTipoCambia = {},
            onMontoCambia = {},
            onDescripcionCambia = {},
            onGuardar = {},
            onEliminar = {},
            onVolver = {},
            onErrorMostrado = {}
        )
    }
}
