package com.williammarrugo.fiao.ui.clientes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.williammarrugo.fiao.domain.model.Cliente
import com.williammarrugo.fiao.domain.model.DatosDemo
import com.williammarrugo.fiao.domain.model.EntradaHistorial
import com.williammarrugo.fiao.ui.componentes.BarraSuperior
import com.williammarrugo.fiao.ui.componentes.CampoMonto
import com.williammarrugo.fiao.ui.componentes.CampoTexto
import com.williammarrugo.fiao.ui.componentes.EstadoVacio
import com.williammarrugo.fiao.ui.componentes.MontoTexto
import com.williammarrugo.fiao.ui.componentes.SeparadorFila
import com.williammarrugo.fiao.ui.inicio.EncabezadoSeccion
import com.williammarrugo.fiao.ui.theme.FiaoTheme
import com.williammarrugo.fiao.ui.theme.LocalColoresFiao
import com.williammarrugo.fiao.ui.theme.estiloMontoFila
import com.williammarrugo.fiao.ui.theme.estiloMontoHeroe
import com.williammarrugo.fiao.util.aPesos
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatoFecha =
    DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-CO"))

/**
 * La cuenta de un cliente.
 *
 * El saldo manda, y debajo van las dos acciones del día a día: fiar y
 * abonar. Abonar es la acción principal (botón lleno) porque cierra el
 * ciclo del negocio.
 *
 * Ambas abren una hoja inferior en vez de navegar a otra pantalla. Es
 * deliberado: al registrar un abono conviene seguir viendo el saldo
 * detrás, que es contra lo que se valida. Si fuera otra pantalla, el
 * usuario tendría que recordar la cifra.
 */
@Composable
fun ClienteDetalleScreen(
    estado: ClienteDetalleUiState,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    onFiar: () -> Unit,
    onAbonar: () -> Unit,
    onRecordar: () -> Unit,
    onMontoCambia: (Long?) -> Unit,
    onDescripcionCambia: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCerrarHoja: () -> Unit,
    onMensajeMostrado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = LocalColoresFiao.current
    val cliente = estado.cliente ?: return
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensaje) {
        val msg = estado.mensaje
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            onMensajeMostrado()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        text = if (estado.saldo > 0) "Debe" else "Está al día",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colores.tintaSuave
                    )
                    MontoTexto(
                        monto = estado.saldo,
                        estilo = estiloMontoHeroe,
                        color = if (estado.saldo > 0) colores.fiado
                        else MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(Modifier.height(18.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onAbonar,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            enabled = estado.saldo > 0
                        ) { Text("Abonar") }

                        OutlinedButton(
                            onClick = onFiar,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) { Text("Fiar") }
                    }

                    if (cliente.telefono != null && estado.saldo > 0) {
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

            if (estado.historial.isEmpty()) {
                item {
                    EstadoVacio(
                        titulo = "Sin movimientos todavía",
                        descripcion = "Cuando le fíes algo, aparecerá aquí."
                    )
                }
            } else {
                items(
                    items = estado.historial,
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

    val formulario = estado.formulario
    if (formulario != null) {
        HojaRegistro(
            formulario = formulario,
            saldoActual = estado.saldo,
            onMontoCambia = onMontoCambia,
            onDescripcionCambia = onDescripcionCambia,
            onConfirmar = onConfirmar,
            onCerrar = onCerrarHoja
        )
    }
}

/**
 * La hoja inferior donde se registra el fiado o el abono.
 *
 * Es la misma hoja para las dos cosas, con los textos cambiados. Son
 * el mismo gesto (escribir un monto) y duplicar el componente solo
 * daría dos sitios donde arreglar el mismo error.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaRegistro(
    formulario: FormularioRegistro,
    saldoActual: Long,
    onMontoCambia: (Long?) -> Unit,
    onDescripcionCambia: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCerrar: () -> Unit
) {
    val colores = LocalColoresFiao.current
    val estadoHoja = rememberModalBottomSheetState()
    val esFiado = formulario.tipo == TipoRegistro.FIADO

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = estadoHoja
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = if (esFiado) "Registrar un fiado" else "Registrar un abono",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!esFiado) {
                // Recordarle cuánto debe mientras escribe el abono
                // evita el error más común: teclear de más.
                Text(
                    text = "Debe ${saldoActual.aPesos()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colores.fiado
                )
            }

            CampoMonto(
                valor = formulario.monto,
                onValorCambia = onMontoCambia,
                error = formulario.errorMonto,
                imeAction = if (esFiado) ImeAction.Next else ImeAction.Done
            )

            if (esFiado) {
                CampoTexto(
                    valor = formulario.descripcion,
                    onValorCambia = onDescripcionCambia,
                    etiqueta = "¿Qué se llevó?",
                    error = formulario.errorDescripcion,
                    ayuda = "Por ejemplo: mercado, pañales, surtido",
                    imeAction = ImeAction.Done
                )
            }

            Button(
                onClick = onConfirmar,
                enabled = !formulario.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (formulario.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (esFiado) "Registrar fiado" else "Registrar abono",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

/**
 * Una entrada del historial.
 *
 * El `when` sobre la sealed class no lleva `else`: el compilador sabe
 * que solo hay dos casos y, si mañana se agrega un tercero, marcará
 * error justo aquí.
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
            estado = ClienteDetalleUiState(
                cargando = false,
                cliente = Cliente(1, "Rosa Herrera", "3001234567"),
                saldo = 85_000,
                historial = DatosDemo.historialCliente
            ),
            onVolver = {},
            onEditar = {},
            onFiar = {},
            onAbonar = {},
            onRecordar = {},
            onMontoCambia = {},
            onDescripcionCambia = {},
            onConfirmar = {},
            onCerrarHoja = {},
            onMensajeMostrado = {}
        )
    }
}
