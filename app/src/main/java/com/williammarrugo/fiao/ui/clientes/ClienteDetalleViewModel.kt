package com.williammarrugo.fiao.ui.clientes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williammarrugo.fiao.domain.model.Abono
import com.williammarrugo.fiao.domain.model.Cliente
import com.williammarrugo.fiao.domain.model.EntradaHistorial
import com.williammarrugo.fiao.domain.model.Fiado
import com.williammarrugo.fiao.domain.repository.AbonoRepository
import com.williammarrugo.fiao.domain.repository.ClienteRepository
import com.williammarrugo.fiao.domain.repository.FiadoRepository
import com.williammarrugo.fiao.domain.rules.esValido
import com.williammarrugo.fiao.domain.rules.mensajeONull
import com.williammarrugo.fiao.domain.rules.validarAbono
import com.williammarrugo.fiao.domain.rules.validarDescripcion
import com.williammarrugo.fiao.domain.rules.validarMonto
import com.williammarrugo.fiao.ui.navegacion.Rutas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/** Qué se está registrando en la hoja inferior. */
enum class TipoRegistro { FIADO, ABONO }

/**
 * El formulario de la hoja inferior, aparte del resto del estado.
 *
 * Se separa en su propia data class porque tiene un ciclo de vida
 * distinto: los datos del cliente vienen de la base de datos y se
 * actualizan solos, mientras que esto es lo que el usuario está
 * escribiendo en este momento.
 */
data class FormularioRegistro(
    val tipo: TipoRegistro = TipoRegistro.FIADO,
    val monto: Long? = null,
    val descripcion: String = "",
    val errorMonto: String? = null,
    val errorDescripcion: String? = null,
    val guardando: Boolean = false
)

data class ClienteDetalleUiState(
    val cargando: Boolean = true,
    val cliente: Cliente? = null,
    val saldo: Long = 0,
    val historial: List<EntradaHistorial> = emptyList(),
    /** null significa que la hoja está cerrada. */
    val formulario: FormularioRegistro? = null,
    val mensaje: String? = null
)

/**
 * La cuenta de un cliente: saldo (F9), historial (F11), y el registro
 * de fiados (F7) y abonos (F8).
 *
 * Aquí conviven dos clases de estado y conviene distinguirlas:
 *
 * - Lo que viene de la base de datos (cliente, saldo, historial) llega
 *   por `Flow` y se actualiza solo. El ViewModel no lo controla.
 * - Lo que el usuario está escribiendo (el formulario) lo controla el
 *   ViewModel con un MutableStateFlow.
 *
 * `combine` los junta en un solo UiState. Cuando se guarda un fiado,
 * Room avisa, el saldo se recalcula y la pantalla se redibuja sin que
 * nadie pida recargar.
 */
class ClienteDetalleViewModel(
    private val clienteRepository: ClienteRepository,
    private val fiadoRepository: FiadoRepository,
    private val abonoRepository: AbonoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clienteId: Long = savedStateHandle.get<Long>(Rutas.ARG_CLIENTE_ID) ?: -1L

    private val formulario = MutableStateFlow<FormularioRegistro?>(null)
    private val mensaje = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ClienteDetalleUiState> = combine(
        clienteRepository.observarCliente(clienteId),
        clienteRepository.observarSaldo(clienteId),
        clienteRepository.observarHistorial(clienteId),
        formulario,
        mensaje
    ) { cliente, saldo, historial, form, msg ->
        ClienteDetalleUiState(
            cargando = false,
            cliente = cliente,
            saldo = saldo,
            historial = historial,
            formulario = form,
            mensaje = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClienteDetalleUiState()
    )

    // --- Abrir y cerrar la hoja ------------------------------------------

    fun abrirFiado() {
        formulario.value = FormularioRegistro(tipo = TipoRegistro.FIADO)
    }

    fun abrirAbono() {
        formulario.value = FormularioRegistro(tipo = TipoRegistro.ABONO)
    }

    fun cerrarHoja() {
        formulario.value = null
    }

    // --- Entradas del usuario --------------------------------------------

    fun cambiarMonto(monto: Long?) {
        formulario.update { it?.copy(monto = monto, errorMonto = null) }
    }

    fun cambiarDescripcion(texto: String) {
        formulario.update { it?.copy(descripcion = texto, errorDescripcion = null) }
    }

    // --- Guardar ----------------------------------------------------------

    /**
     * Valida y guarda, según lo que se esté registrando.
     *
     * Las dos ramas usan reglas distintas:
     *
     * - Un fiado solo necesita monto válido y descripción (R1).
     * - Un abono además no puede superar la deuda (R5), y para eso
     *   se consulta el saldo del momento, no el que tenía la pantalla
     *   cuando se abrió. Entre una cosa y otra pudo registrarse otro
     *   movimiento.
     */
    fun confirmar() {
        val form = formulario.value ?: return
        if (form.guardando) return

        viewModelScope.launch {
            val saldoActual = clienteRepository.observarSaldo(clienteId).first()

            val validacionMonto = when (form.tipo) {
                TipoRegistro.FIADO -> validarMonto(form.monto)
                TipoRegistro.ABONO -> validarAbono(form.monto, saldoActual)
            }
            val validacionDescripcion = when (form.tipo) {
                TipoRegistro.FIADO -> validarDescripcion(form.descripcion)
                // El abono no lleva descripción: pagar es pagar.
                TipoRegistro.ABONO -> null
            }

            if (!validacionMonto.esValido ||
                (validacionDescripcion != null && !validacionDescripcion.esValido)
            ) {
                formulario.update {
                    it?.copy(
                        errorMonto = validacionMonto.mensajeONull(),
                        errorDescripcion = validacionDescripcion?.mensajeONull()
                    )
                }
                return@launch
            }

            formulario.update { it?.copy(guardando = true) }

            try {
                when (form.tipo) {
                    TipoRegistro.FIADO -> {
                        fiadoRepository.insertar(
                            Fiado(
                                clienteId = clienteId,
                                monto = form.monto!!,
                                descripcion = form.descripcion.trim(),
                                fecha = LocalDateTime.now()
                            )
                        )
                        mensaje.value = "Fiado registrado"
                    }
                    TipoRegistro.ABONO -> {
                        abonoRepository.insertar(
                            Abono(
                                clienteId = clienteId,
                                monto = form.monto!!,
                                fecha = LocalDateTime.now()
                            )
                        )
                        mensaje.value = "Abono registrado"
                    }
                }
                // Cerrar la hoja es la confirmación visual: el saldo de
                // atrás ya cambió solo.
                formulario.value = null
            } catch (e: Exception) {
                formulario.update { it?.copy(guardando = false) }
                mensaje.value = "No se pudo guardar. Intenta de nuevo."
            }
        }
    }

    fun mensajeMostrado() {
        mensaje.value = null
    }

    /**
     * Arma el enlace de WhatsApp con el recordatorio (F12).
     *
     * Devuelve null si el cliente no tiene teléfono o no debe nada.
     * El ViewModel solo construye el texto y la URL; abrir WhatsApp es
     * cosa de la pantalla, porque lanzar un Intent necesita un Context
     * y el ViewModel no debe tener uno.
     */
    fun enlaceRecordatorio(): String? {
        val estado = uiState.value
        val telefono = estado.cliente?.telefono?.filter { it.isDigit() } ?: return null
        if (telefono.isBlank() || estado.saldo <= 0) return null

        val nombre = estado.cliente.nombre.trim().substringBefore(' ')
        val texto = "Hola $nombre, te recuerdo que tienes un saldo pendiente " +
            "de ${formatearPesos(estado.saldo)}. ¡Gracias!"

        // 57 es el indicativo de Colombia. Si el número ya lo trae, no
        // se duplica.
        val numeroCompleto = if (telefono.startsWith("57")) telefono else "57$telefono"
        return "https://wa.me/$numeroCompleto?text=${java.net.URLEncoder.encode(texto, "UTF-8")}"
    }

    private fun formatearPesos(monto: Long): String {
        val formato = java.text.NumberFormat.getCurrencyInstance(
            java.util.Locale.forLanguageTag("es-CO")
        ).apply { maximumFractionDigits = 0 }
        return formato.format(monto)
    }
}
