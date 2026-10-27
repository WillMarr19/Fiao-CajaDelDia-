package com.williammarrugo.fiao.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val EsquemaClaro = lightColorScheme(
    primary = TealProfundo,
    onPrimary = SuperficieClara,
    primaryContainer = TealMuyClaro,
    onPrimaryContainer = TealProfundo,
    secondary = TealMedio,
    onSecondary = SuperficieClara,
    secondaryContainer = TealClaro,
    onSecondaryContainer = TealProfundo,
    background = FondoClaro,
    onBackground = TintaFuerte,
    surface = SuperficieClara,
    onSurface = TintaFuerte,
    surfaceVariant = TealMuyClaro,
    onSurfaceVariant = TintaSuave,
    outline = BordeClaro,
    outlineVariant = BordeClaro,
    error = TerracotaGasto,
    onError = SuperficieClara,
    errorContainer = TerracotaGastoSuave,
    onErrorContainer = TerracotaGasto
)

private val EsquemaOscuro = darkColorScheme(
    primary = TealClaro,
    onPrimary = TealMuyOscuro,
    primaryContainer = TealContenedorOscuro,
    onPrimaryContainer = TealClaro,
    secondary = TealMedio,
    onSecondary = TintaClara,
    secondaryContainer = TealContenedorOscuro,
    onSecondaryContainer = TealClaro,
    background = FondoOscuro,
    onBackground = TintaClara,
    surface = SuperficieOscura,
    onSurface = TintaClara,
    surfaceVariant = TealContenedorOscuro,
    onSurfaceVariant = TintaClaraSuave,
    outline = BordeOscuro,
    outlineVariant = BordeOscuro,
    error = TerracotaGastoOscuro,
    onError = FondoOscuro,
    errorContainer = TerracotaGastoSuaveOscuro,
    onErrorContainer = TerracotaGastoOscuro
)

/**
 * Tema de la app.
 *
 * Decisión deliberada: NO se usa dynamic color (Material You), aunque el
 * dispositivo lo soporte. Dynamic color toma los colores del fondo de
 * pantalla del usuario y reemplazaría toda la paleta, con lo cual el verde
 * de "venta" y el ocre de "fiado" podrían volverse indistinguibles.
 * Cuando el color carga información, no se puede delegar al fondo de
 * pantalla.
 *
 * La plantilla de Android Studio trae dynamic color activado; aquí se
 * quitó a propósito.
 */
@Composable
fun FiaoTheme(
    enOscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val esquema = if (enOscuro) EsquemaOscuro else EsquemaClaro
    val coloresFiao = if (enOscuro) coloresFiaoOscuro else coloresFiaoClaro

    CompositionLocalProvider(LocalColoresFiao provides coloresFiao) {
        MaterialTheme(
            colorScheme = esquema,
            typography = Tipografia,
            content = content
        )
    }
}
