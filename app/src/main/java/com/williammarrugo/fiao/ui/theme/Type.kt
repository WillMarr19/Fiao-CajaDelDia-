package com.williammarrugo.fiao.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tipografía de Fiao.
 *
 * Una sola familia (la del sistema). La decisión tipográfica que importa
 * en esta app no es el typeface, es el tratamiento de las cifras:
 * un tendero lee montos, no párrafos.
 *
 * Por eso hay tres estilos de dinero con "tnum" (tabular numbers), que
 * hace que todos los dígitos ocupen el mismo ancho. Sin eso, una columna
 * de montos queda desalineada porque el 1 es más angosto que el 8.
 */

private val Sistema = FontFamily.Default

/** Cifras tabulares: obligatorio en cualquier texto que muestre plata. */
private const val CIFRAS_TABULARES = "tnum"

/** El número protagonista de la pantalla de inicio. */
val estiloMontoHeroe = TextStyle(
    fontFamily = Sistema,
    fontWeight = FontWeight.SemiBold,
    fontSize = 40.sp,
    lineHeight = 46.sp,
    letterSpacing = (-0.5).sp,
    fontFeatureSettings = CIFRAS_TABULARES
)

/** Totales de sección y saldos de cliente. */
val estiloMontoTitular = TextStyle(
    fontFamily = Sistema,
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    fontFeatureSettings = CIFRAS_TABULARES
)

/** Montos dentro de una fila de lista. */
val estiloMontoFila = TextStyle(
    fontFamily = Sistema,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 20.sp,
    fontFeatureSettings = CIFRAS_TABULARES
)

val Tipografia = Typography(
    displaySmall = TextStyle(
        fontFamily = Sistema,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Sistema,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Sistema,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Sistema,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Sistema,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Sistema,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Sistema,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Sistema,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
)
