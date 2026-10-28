package com.williammarrugo.fiao.ui.movimientos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williammarrugo.fiao.domain.model.Movimiento
import com.williammarrugo.fiao.domain.model.TipoMovimiento
import com.williammarrugo.fiao.domain.repository.MovimientoRepository
import com.williammarrugo.fiao.domain.rules.esValido
import com.williammarrugo.fiao.domain.rules.mensajeONull
import com.williammarrugo.fiao.domain.rules.validarDescripcion
import com.williammarrugo.fiao.domain.rules.validarMonto
import com.williammarrugo.fiao.ui.navegacion.Rutas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * El estado del formulario.
 *
 * `guardado` y `eliminado` son señales de una sola vez: la pantalla las
 * observa y, cuando se vuelven true, navega hacia atrás. Es la forma
 * sencilla de resolverlo. En apps grandes se usa un Channel o un
 * SharedFlow de eventos para que la señal no se repita si la pantalla
 * se recompone, pero para este caso alcanza.
 */
data class MovimientoFormUiState(
    val cargando: Boolean = false,
    val esEdicion: Boolean = false,
    val tipo: TipoMovimiento = TipoMovimiento.VENTA,
    val monto: Long? = null,
    val descripcion: String = "",
    val errorMonto: String? = null,
    val errorDescripcion: String? = null,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val eliminado: Boolean = false,
    val errorGeneral: String? = null
)

/**
 * Crear (F1), editar y eliminar (F3) un movimiento.
 *
 * Sobre `SavedStateHandle`: es la bolsa donde Navigation deja los
 * argumentos de la ruta. Aquí sale el id del movimiento a editar. Y
 * sobrevive incluso a que el sistema mate el proceso por falta de
 * memoria, cosa que el ViewModel por sí solo no hace.
 *
 * Sobre `viewModelScope`: el alcance de corrutinas que muere con el
 * ViewModel. Si el usuario sale de la pantalla a mitad de un guardado,
 * la corrutina se cancela sola y no queda trabajo huérfano escribiendo
 * sobre un objeto que ya nadie mira.
 */
class MovimientoFormViewModel(
    private val repository: MovimientoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movimientoId: Long =
        savedStateHandle.get<Long>(Rutas.ARG_MOVIMIENTO_ID) ?: SIN_ID

    private val _uiState = MutableStateFlow(MovimientoFormUiState())
    val uiState: StateFlow<MovimientoFormUiState> = _uiState.asStateFlow()

    init {
        if (movimientoId != SIN_ID) cargarExistente()
    }

    private fun cargarExistente() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val movimiento = repository.obtenerPorId(movimientoId)
            if (movimiento == null) {
                _uiState.update {
                    it.copy(cargando = false, errorGeneral = "No se encontró el movimiento")
                }
            } else {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        esEdicion = true,
                        tipo = movimiento.tipo,
                        monto = movimiento.monto,
                        descripcion = movimiento.descripcion
                    )
                }
            }
        }
    }

    // --- Entradas del usuario -------------------------------------------
    //
    // Cada cambio limpia el error de SU campo. Mostrar "el monto está
    // mal" mientras el usuario ya lo está corrigiendo es molesto.

    fun cambiarTipo(tipo: TipoMovimiento) {
        _uiState.update { it.copy(tipo = tipo) }
    }

    fun cambiarMonto(monto: Long?) {
        _uiState.update { it.copy(monto = monto, errorMonto = null) }
    }

    fun cambiarDescripcion(texto: String) {
        _uiState.update { it.copy(descripcion = texto, errorDescripcion = null) }
    }

    // --- Acciones --------------------------------------------------------

    /**
     * Valida y guarda.
     *
     * La validación no vive aquí: vive en `domain/rules/Reglas.kt` como
     * funciones puras que tienen sus propios tests. El ViewModel solo
     * las llama y traduce el resultado al estado de la pantalla. Si la
     * regla cambia, se cambia en un solo lugar.
     */
    fun guardar() {
        val estado = _uiState.value
        val validacionMonto = validarMonto(estado.monto)
        val validacionDescripcion = validarDescripcion(estado.descripcion)

        if (!validacionMonto.esValido || !validacionDescripcion.esValido) {
            _uiState.update {
                it.copy(
                    errorMonto = validacionMonto.mensajeONull(),
                    errorDescripcion = validacionDescripcion.mensajeONull()
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorGeneral = null) }
            try {
                val movimiento = Movimiento(
                    id = if (estado.esEdicion) movimientoId else 0,
                    tipo = estado.tipo,
                    monto = estado.monto!!,
                    descripcion = estado.descripcion.trim(),
                    fecha = LocalDateTime.now()
                )
                if (estado.esEdicion) {
                    repository.actualizar(movimiento)
                } else {
                    repository.insertar(movimiento)
                }
                _uiState.update { it.copy(guardando = false, guardado = true) }
            } catch (e: Exception) {
                // No se deja reventar la app por un fallo de escritura:
                // se le dice al usuario y la pantalla sigue en pie.
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errorGeneral = "No se pudo guardar. Intenta de nuevo."
                    )
                }
            }
        }
    }

    fun eliminar() {
        if (movimientoId == SIN_ID) return
        viewModelScope.launch {
            try {
                repository.eliminar(movimientoId)
                _uiState.update { it.copy(eliminado = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorGeneral = "No se pudo eliminar.") }
            }
        }
    }

    /** La pantalla avisa que ya atendió el error y se puede ocultar. */
    fun errorMostrado() {
        _uiState.update { it.copy(errorGeneral = null) }
    }

    companion object {
        /** El valor que Navigation deja cuando la ruta no trae id. */
        const val SIN_ID = -1L
    }
}
