package com.williammarrugo.fiao.ui.componentes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.williammarrugo.fiao.ui.theme.FiaoTheme
import com.williammarrugo.fiao.ui.theme.estiloMontoTitular
import com.williammarrugo.fiao.util.aPesosSinSimbolo

/**
 * Campo para escribir plata.
 *
 * Tres decisiones:
 *
 * 1. Teclado numérico. El usuario está de pie, con una mano, atendiendo.
 *    Que salga el teclado de letras sería un estorbo.
 * 2. Solo dígitos. El filtro descarta cualquier otro carácter antes de
 *    avisar del cambio, así que el estado de arriba nunca recibe basura.
 * 3. Se muestra con separador de miles mientras se escribe. "45.000" se
 *    lee de un golpe; "45000" hay que contarlo.
 *
 * El valor viaja hacia arriba como Long. El formato es cosa de la vista,
 * nunca del dato.
 */
@Composable
fun CampoMonto(
    valor: Long?,
    onValorCambia: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    etiqueta: String = "Monto",
    error: String? = null,
    imeAction: ImeAction = ImeAction.Next
) {
    val texto = valor?.aPesosSinSimbolo() ?: ""

    OutlinedTextField(
        value = texto,
        onValueChange = { nuevo ->
            val soloDigitos = nuevo.filter { it.isDigit() }
            onValorCambia(if (soloDigitos.isEmpty()) null else soloDigitos.toLongOrNull())
        },
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        prefix = { Text("$ ") },
        textStyle = estiloMontoTitular,
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction
        )
    )
}

/** Campo de texto normal, con el mismo manejo de error que CampoMonto. */
@Composable
fun CampoTexto(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    ayuda: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        textStyle = MaterialTheme.typography.bodyLarge,
        singleLine = true,
        isError = error != null,
        supportingText = when {
            error != null -> ({ Text(error) })
            ayuda != null -> ({ Text(ayuda) })
            else -> null
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun CampoMontoPreview() {
    FiaoTheme {
        CampoMonto(
            valor = 45_000,
            onValorCambia = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
