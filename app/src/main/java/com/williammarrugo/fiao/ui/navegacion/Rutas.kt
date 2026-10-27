package com.williammarrugo.fiao.ui.navegacion

/**
 * Las rutas de navegación, centralizadas.
 *
 * Están aquí y no sueltas como cadenas en cada pantalla por una razón
 * práctica: si una ruta se escribe mal en un solo lugar, la app compila
 * igual y falla en ejecución. Con constantes, el error aparece al
 * compilar.
 */
object Rutas {
    const val INICIO = "inicio"
    const val MOVIMIENTOS = "movimientos"
    const val CLIENTES = "clientes"
    const val RESUMEN = "resumen"

    /** Formulario de movimiento. Sin id crea uno nuevo; con id lo edita. */
    const val MOVIMIENTO_FORM = "movimiento_form"
    const val ARG_MOVIMIENTO_ID = "movimientoId"
    const val MOVIMIENTO_FORM_RUTA = "$MOVIMIENTO_FORM?$ARG_MOVIMIENTO_ID={$ARG_MOVIMIENTO_ID}"
    fun movimientoForm(id: Long? = null): String =
        if (id == null) MOVIMIENTO_FORM else "$MOVIMIENTO_FORM?$ARG_MOVIMIENTO_ID=$id"

    /** Formulario de cliente. Mismo patrón. */
    const val CLIENTE_FORM = "cliente_form"
    const val ARG_CLIENTE_ID = "clienteId"
    const val CLIENTE_FORM_RUTA = "$CLIENTE_FORM?$ARG_CLIENTE_ID={$ARG_CLIENTE_ID}"
    fun clienteForm(id: Long? = null): String =
        if (id == null) CLIENTE_FORM else "$CLIENTE_FORM?$ARG_CLIENTE_ID=$id"

    /** Detalle de cliente. El id siempre es obligatorio. */
    const val CLIENTE_DETALLE = "cliente_detalle"
    const val CLIENTE_DETALLE_RUTA = "$CLIENTE_DETALLE/{$ARG_CLIENTE_ID}"
    fun clienteDetalle(id: Long): String = "$CLIENTE_DETALLE/$id"
}
