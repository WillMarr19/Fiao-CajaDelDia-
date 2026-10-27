package com.williammarrugo.fiao.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colores que Material 3 no define pero esta app sí necesita.
 *
 * El ColorScheme de Material trae primary, surface, error y demás, pero no
 * tiene un concepto de "venta", "gasto" o "fiado". En vez de escribir el
 * hexadecimal suelto en cada pantalla (que rompería el tema oscuro), se
 * declaran aquí y se exponen por un CompositionLocal.
 *
 * Uso dentro de un composable:
 *   val colores = LocalColoresFiao.current
 *   Text(text = "...", color = colores.venta)
 */
@Immutable
data class ColoresFiao(
    val venta: Color,
    val ventaSuave: Color,
    val gasto: Color,
    val gastoSuave: Color,
    val fiado: Color,
    val fiadoSuave: Color,
    val borde: Color,
    val tintaSuave: Color
)

val coloresFiaoClaro = ColoresFiao(
    venta = VerdeVenta,
    ventaSuave = VerdeVentaSuave,
    gasto = TerracotaGasto,
    gastoSuave = TerracotaGastoSuave,
    fiado = OcreFiado,
    fiadoSuave = OcreFiadoSuave,
    borde = BordeClaro,
    tintaSuave = TintaSuave
)

val coloresFiaoOscuro = ColoresFiao(
    venta = VerdeVentaOscuro,
    ventaSuave = VerdeVentaSuaveOscuro,
    gasto = TerracotaGastoOscuro,
    gastoSuave = TerracotaGastoSuaveOscuro,
    fiado = OcreFiadoOscuro,
    fiadoSuave = OcreFiadoSuaveOscuro,
    borde = BordeOscuro,
    tintaSuave = TintaClaraSuave
)

/**
 * staticCompositionLocalOf y no compositionLocalOf: este valor cambia muy
 * pocas veces (solo al alternar claro/oscuro). La versión "static" no
 * rastrea lecturas individuales y recompone todo el subárbol cuando cambia,
 * lo que resulta más barato para algo tan estable como un tema.
 */
val LocalColoresFiao = staticCompositionLocalOf { coloresFiaoClaro }
