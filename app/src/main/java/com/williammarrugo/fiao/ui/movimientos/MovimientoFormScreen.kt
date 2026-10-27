package com.williammarrugo.fiao.ui.movimientos

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
 * Estado del formulario.
 *
 * Está aparte de los composables porque, cuando llegue el ViewModel en
 * el día 6, esta clase se muda allá tal cual y la pantalla no cambia.
 * Esa es la idea de "state hoisting": la pantalla dibuja, otro decide.
 */
data class EstadoFormularioMovimiento(
    val tipo: TipoMovimiento = TipoMovimiento.VENTA,
    val monto: Long? = null,
    val descripcion: String = "",
    val errorMonto: String? = null,
    val errorDescripcion: String? = null
)

/**
 * Crear o editar un movimiento.
 *
 * La misma pantalla sirve para las dos cosas: si llega un id, edita;
 * si no, crea. Lo único que cambia es el título y la presencia del
 * botón de borrar.
 */
@Composable
fun MovimientoFormScreen(
    estado: EstadoFormularioMovimiento,
    esEdicion: Boolean,
    onTipoCambia: (TipoMovimiento) -> Unit,
    onMontoCambia: (Long?) -> Unit,
    onDescripcionCambia: (String) -> Unit,
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
                titulo = if (esEdicion) "Editar movimiento" else "Nuevo movimiento",
                onVolver = onVolver,
                acciones = {
                    if (esEdicion) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (esEdicion) "Guardar cambios" else "Registrar",
                    style = MaterialTheme.typography.labelLarge
                )
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
                TextButton(onClick = { mostrandoDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "Nuevo movimiento")
@Composable
private fun MovimientoFormPreview() {
    FiaoTheme {
        var estado by remember { mutableStateOf(EstadoFormularioMovimiento()) }
        MovimientoFormScreen(
            estado = estado,
            esEdicion = false,
            onTipoCambia = { estado = estado.copy(tipo = it) },
            onMontoCambia = { estado = estado.copy(monto = it) },
            onDescripcionCambia = { estado = estado.copy(descripcion = it) },
            onGuardar = {},
            onEliminar = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Con errores")
@Composable
private fun MovimientoFormErrorPreview() {
    FiaoTheme {
        MovimientoFormScreen(
            estado = EstadoFormularioMovimiento(
                tipo = TipoMovimiento.GASTO,
                monto = null,
                descripcion = "",
                errorMonto = "Escribe un monto mayor que cero",
                errorDescripcion = "Falta decir de qué fue"
            ),
            esEdicion = true,
            onTipoCambia = {},
            onMontoCambia = {},
            onDescripcionCambia = {},
            onGuardar = {},
            onEliminar = {},
            onVolver = {}
        )
    }
}
