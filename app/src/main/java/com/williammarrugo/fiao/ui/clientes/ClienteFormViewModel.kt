package com.williammarrugo.fiao.ui.clientes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williammarrugo.fiao.domain.model.Cliente
import com.williammarrugo.fiao.domain.repository.ClienteRepository
import com.williammarrugo.fiao.domain.rules.esValido
import com.williammarrugo.fiao.domain.rules.mensajeONull
import com.williammarrugo.fiao.domain.rules.puedeEliminarCliente
import com.williammarrugo.fiao.domain.rules.validarNombreCliente
import com.williammarrugo.fiao.ui.navegacion.Rutas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClienteFormUiState(
    val cargando: Boolean = false,
    val esEdicion: Boolean = false,
    val nombre: String = "",
    val telefono: String = "",
    val errorNombre: String? = null,
    val puedeEliminar: Boolean = true,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val eliminado: Boolean = false,
    val errorGeneral: String? = null
)

/**
 * Crear y editar clientes (F6).
 *
 * Igual que el formulario de movimientos, con una diferencia: aquí
 * entra la regla R6, que prohíbe borrar un cliente que todavía debe.
 *
 * Fíjate en que la regla se aplica en dos momentos:
 *
 * 1. Al cargar, para decidir si el botón de eliminar siquiera aparece.
 * 2. Al eliminar, volviendo a consultar el saldo del momento.
 *
 * Lo segundo parece redundante pero no lo es: entre que la pantalla se
 * abrió y el usuario tocó eliminar pudo registrarse un fiado. Una
 * comprobación que solo vive en la interfaz no es una regla de
 * negocio, es una sugerencia.
 */
class ClienteFormViewModel(
    private val repository: ClienteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clienteId: Long =
        savedStateHandle.get<Long>(Rutas.ARG_CLIENTE_ID) ?: SIN_ID

    private val _uiState = MutableStateFlow(ClienteFormUiState())
    val uiState: StateFlow<ClienteFormUiState> = _uiState.asStateFlow()

    init {
        if (clienteId != SIN_ID) cargarExistente()
    }

    private fun cargarExistente() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val cliente = repository.obtenerPorId(clienteId)
            val saldo = repository.observarSaldo(clienteId).first()

            if (cliente == null) {
                _uiState.update {
                    it.copy(cargando = false, errorGeneral = "No se encontró el cliente")
                }
            } else {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        esEdicion = true,
                        nombre = cliente.nombre,
                        telefono = cliente.telefono.orEmpty(),
                        puedeEliminar = puedeEliminarCliente(saldo)
                    )
                }
            }
        }
    }

    fun cambiarNombre(texto: String) {
        _uiState.update { it.copy(nombre = texto, errorNombre = null) }
    }

    fun cambiarTelefono(texto: String) {
        // Solo dígitos, espacios y el signo +. Evita que entre basura
        // en un campo que después alimenta un enlace de WhatsApp.
        val limpio = texto.filter { it.isDigit() || it == ' ' || it == '+' }
        _uiState.update { it.copy(telefono = limpio) }
    }

    fun guardar() {
        val estado = _uiState.value
        val validacion = validarNombreCliente(estado.nombre)

        if (!validacion.esValido) {
            _uiState.update { it.copy(errorNombre = validacion.mensajeONull()) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorGeneral = null) }
            try {
                val cliente = Cliente(
                    id = if (estado.esEdicion) clienteId else 0,
                    nombre = estado.nombre.trim(),
                    telefono = estado.telefono.trim().ifBlank { null }
                )
                if (estado.esEdicion) {
                    repository.actualizar(cliente)
                } else {
                    repository.insertar(cliente)
                }
                _uiState.update { it.copy(guardando = false, guardado = true) }
            } catch (e: Exception) {
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
        if (clienteId == SIN_ID) return
        viewModelScope.launch {
            try {
                // Regla R6, verificada contra el saldo de este momento.
                val saldo = repository.observarSaldo(clienteId).first()
                if (!puedeEliminarCliente(saldo)) {
                    _uiState.update {
                        it.copy(
                            puedeEliminar = false,
                            errorGeneral = "No se puede eliminar: el cliente todavía debe."
                        )
                    }
                    return@launch
                }
                repository.eliminar(clienteId)
                _uiState.update { it.copy(eliminado = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorGeneral = "No se pudo eliminar.") }
            }
        }
    }

    fun errorMostrado() {
        _uiState.update { it.copy(errorGeneral = null) }
    }

    companion object {
        const val SIN_ID = -1L
    }
}
