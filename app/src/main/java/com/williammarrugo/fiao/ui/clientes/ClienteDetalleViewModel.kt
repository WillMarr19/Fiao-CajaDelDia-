package com.williammarrugo.fiao.ui.clientes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williammarrugo.fiao.domain.model.Cliente
import com.williammarrugo.fiao.domain.model.EntradaHistorial
import com.williammarrugo.fiao.domain.repository.ClienteRepository
import com.williammarrugo.fiao.ui.navegacion.Rutas
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ClienteDetalleUiState(
    val cargando: Boolean = true,
    val cliente: Cliente? = null,
    val saldo: Long = 0,
    val historial: List<EntradaHistorial> = emptyList()
)

/**
 * La cuenta de un cliente.
 *
 * Se incluye aunque registrar fiados y abonos (F7 y F8) todavía no
 * exista, porque sin esto la pantalla mostraría datos de ejemplo
 * mientras la lista de al lado muestra clientes reales. Un dato falso
 * entre datos verdaderos confunde más que una pantalla vacía.
 *
 * Por ahora el saldo saldrá en cero y el historial vacío, que es la
 * verdad: no hay fiados registrados todavía.
 */
class ClienteDetalleViewModel(
    repository: ClienteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clienteId: Long = savedStateHandle.get<Long>(Rutas.ARG_CLIENTE_ID) ?: -1L

    val uiState: StateFlow<ClienteDetalleUiState> = combine(
        repository.observarCliente(clienteId),
        repository.observarSaldo(clienteId),
        repository.observarHistorial(clienteId)
    ) { cliente, saldo, historial ->
        ClienteDetalleUiState(
            cargando = false,
            cliente = cliente,
            saldo = saldo,
            historial = historial
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClienteDetalleUiState()
    )
}
