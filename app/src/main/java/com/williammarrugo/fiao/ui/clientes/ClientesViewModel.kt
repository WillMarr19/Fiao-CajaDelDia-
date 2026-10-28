package com.williammarrugo.fiao.ui.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williammarrugo.fiao.domain.model.ClienteConSaldo
import com.williammarrugo.fiao.domain.repository.ClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ClientesUiState(
    val cargando: Boolean = true,
    val clientes: List<ClienteConSaldo> = emptyList(),
    val totalPorCobrar: Long = 0,
    val busqueda: String = ""
)

/**
 * La lista de clientes con búsqueda (F10, parcial).
 *
 * La búsqueda se hace en memoria y no en SQL, a propósito: un tendero
 * maneja decenas de clientes, no miles, y filtrar una lista ya cargada
 * es instantáneo. Si fueran diez mil, la consulta tendría que llevar el
 * filtro con un LIKE y un índice sobre el nombre.
 *
 * Saber cuándo una solución simple alcanza, y poder explicar a partir
 * de qué punto dejaría de alcanzar, vale más en una entrevista que
 * optimizar por reflejo.
 *
 * El orden viene de SQL (por mayor deuda) y aquí no se vuelve a tocar.
 */
class ClientesViewModel(
    clienteRepository: ClienteRepository
) : ViewModel() {

    private val busqueda = MutableStateFlow("")

    val uiState: StateFlow<ClientesUiState> = combine(
        clienteRepository.observarConSaldo(),
        clienteRepository.observarTotalPorCobrar(),
        busqueda
    ) { clientes, total, texto ->
        ClientesUiState(
            cargando = false,
            clientes = if (texto.isBlank()) {
                clientes
            } else {
                clientes.filter { it.cliente.nombre.contains(texto.trim(), ignoreCase = true) }
            },
            totalPorCobrar = total,
            busqueda = texto
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClientesUiState()
    )

    fun cambiarBusqueda(texto: String) {
        busqueda.value = texto
    }
}
