package com.williammarrugo.fiao.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de Fiao.
 *
 * La referencia visual son las fachadas pintadas del Caribe colombiano:
 * teal, terracota y ocre. No es el morado que trae la plantilla de
 * Android Studio.
 *
 * Tres colores cargan información, no decoración:
 *   venta  -> verde teal  (entra plata)
 *   gasto  -> terracota   (sale plata)
 *   fiado  -> ocre        (plata prometida, todavía no entra)
 *
 * IMPORTANTE: los tres valores semánticos no se eligieron a ojo. Se
 * validaron con un script de contraste y daltonismo. El par verde/rojo
 * que uno usaría por instinto (#1C7A3E y #B23A1F) falla para
 * deuteranopía, el tipo de daltonismo más común: un 8% de los hombres
 * los vería casi iguales. Los valores de abajo mantienen una separación
 * suficiente tanto en visión normal como en deuteranopía y protanopía.
 *
 * Aun así, en toda la app el color va acompañado de texto o de signo
 * (+/-, "Fiado", "Abono"). El color nunca es la única señal.
 */

// --- Marca ---------------------------------------------------------------
val TealProfundo = Color(0xFF0E4A54)
val TealMedio = Color(0xFF266C74)
val TealClaro = Color(0xFF9CCBD2)
val TealMuyClaro = Color(0xFFDCEBEE)
val TealMuyOscuro = Color(0xFF0A2B31)
val TealContenedorOscuro = Color(0xFF17454D)

// --- Semánticos, tema claro (validados sobre superficie clara) -----------
val VerdeVenta = Color(0xFF157F63)
val VerdeVentaSuave = Color(0xFFD6EDE5)
val TerracotaGasto = Color(0xFF9C3B16)
val TerracotaGastoSuave = Color(0xFFF6E0D6)
val OcreFiado = Color(0xFFB08200)
val OcreFiadoSuave = Color(0xFFF8EACA)

// --- Semánticos, tema oscuro (validados sobre superficie oscura) ---------
val VerdeVentaOscuro = Color(0xFF2E9A7A)
val VerdeVentaSuaveOscuro = Color(0xFF13302A)
val TerracotaGastoOscuro = Color(0xFFC75638)
val TerracotaGastoSuaveOscuro = Color(0xFF361C14)
val OcreFiadoOscuro = Color(0xFFB28C22)
val OcreFiadoSuaveOscuro = Color(0xFF322814)

// --- Neutros, tema claro -------------------------------------------------
val FondoClaro = Color(0xFFF5F7F7)
val SuperficieClara = Color(0xFFFFFFFF)
val TintaFuerte = Color(0xFF111E21)
val TintaSuave = Color(0xFF566165)
val BordeClaro = Color(0xFFDCE4E5)

// --- Neutros, tema oscuro ------------------------------------------------
val FondoOscuro = Color(0xFF0D1416)
val SuperficieOscura = Color(0xFF161F21)
val TintaClara = Color(0xFFE5EDEE)
val TintaClaraSuave = Color(0xFFA2AEB1)
val BordeOscuro = Color(0xFF283538)
