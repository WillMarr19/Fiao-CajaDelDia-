package com.williammarrugo.fiao.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.williammarrugo.fiao.ui.theme.FiaoTheme
import com.williammarrugo.fiao.ui.theme.LocalColoresFiao

/**
 * Pantalla vacía.
 *
 * Una lista sin datos no es un error ni un hueco: es el estado normal
 * del primer día. El texto dice qué hacer, no se disculpa ni explica
 * que "no hay datos disponibles".
 */
@Composable
fun EstadoVacio(
    titulo: String,
    descripcion: String,
    modifier: Modifier = Modifier,
    textoAccion: String? = null,
    onAccion: (() -> Unit)? = null
) {
    val colores = LocalColoresFiao.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = descripcion,
            style = MaterialTheme.typography.bodyMedium,
            color = colores.tintaSuave,
            textAlign = TextAlign.Center
        )
        if (textoAccion != null && onAccion != null) {
            TextButton(onClick = onAccion) {
                Text(textoAccion)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EstadoVacioPreview() {
    FiaoTheme {
        EstadoVacio(
            titulo = "Todavía no hay movimientos hoy",
            descripcion = "Registra la primera venta del día y aparecerá aquí.",
            textoAccion = "Registrar venta",
            onAccion = {}
        )
    }
}
