package com.williammarrugo.fiao.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williammarrugo.fiao.domain.model.Movimiento
import com.williammarrugo.fiao.domain.model.ResumenDia
import com.williammarrugo.fiao.domain.repository.AbonoRepository
import com.williammarrugo.fiao.domain.repository.ClienteRepository
import com.williammarrugo.fiao.domain.repository.MovimientoRepository
import com.williammarrugo.fiao.domain.rules.calcularResumenDia
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/**
 * El estado de la pantalla de inicio.
 *
 * Una sola data class con TODO lo que la pantalla necesita para
 * dibujarse. Esto se llama "estado único": no hay cuatro variables
 * sueltas que puedan quedar desincronizadas entre sí, hay un objeto que
 * se reemplaza completo.
 */
data class InicioUiState(
    val cargando: Boolean = true,
    val resumen: ResumenDia = ResumenDia(0, 0, 0),
    val movimientos: List<Movimiento> = emptyList(),
    val totalPorCobrar: Long = 0,
    val clientesQueDeben: Int = 0
)

/**
 * El ViewModel de la pantalla de inicio.
 *
 * Tres cosas que conviene entender bien, porque son las que preguntan:
 *
 * 1. QUÉ ES UN VIEWMODEL. Un objeto que guarda el estado de una
 *    pantalla y sobrevive a los cambios de configuración. Cuando rotas
 *    el celular, Android destruye la Activity y la vuelve a crear, pero
 *    el ViewModel sigue vivo. Por eso el estado que vive aquí no se
 *    pierde al rotar, y el que vivía en un `remember` sí se perdía.
 *
 * 2. POR QUÉ RECIBE REPOSITORIOS Y NO LA BASE DE DATOS. Depende de las
 *    interfaces del dominio. En un test le paso implementaciones falsas
 *    en memoria y pruebo la lógica sin emulador.
 *
 * 3. QUÉ HACE `combine`. Toma varios flujos y produce uno nuevo cada
 *    vez que cualquiera de ellos emite. Aquí junta los movimientos del
 *    día, los abonos del día y los datos de clientes en un solo estado.
 *    Si el usuario registra una venta, Room avisa, combine recalcula y
 *    la pantalla se redibuja sola. Nadie llama a "recargar".
 */
class InicioViewModel(
    movimientoRepository: MovimientoRepository,
    abonoRepository: AbonoRepository,
    clienteRepository: ClienteRepository
) : ViewModel() {

    private val hoy: LocalDate = LocalDate.now()

    val uiState: StateFlow<InicioUiState> = combine(
        movimientoRepository.observarPorDia(hoy),
        abonoRepository.observarPorDia(hoy),
        clienteRepository.observarTotalPorCobrar(),
        clienteRepository.observarConSaldo()
    ) { movimientos, abonos, totalPorCobrar, clientes ->
        InicioUiState(
            cargando = false,
            resumen = calcularResumenDia(movimientos, abonos),
            movimientos = movimientos,
            totalPorCobrar = totalPorCobrar,
            clientesQueDeben = clientes.count { it.saldo > 0 }
        )
    }.stateIn(
        scope = viewModelScope,
        // WhileSubscribed(5000) mantiene viva la consulta 5 segundos
        // después de que la pantalla deja de mirar. Así una rotación,
        // que desuscribe y vuelve a suscribir en milisegundos, no
        // dispara una consulta nueva a la base de datos.
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InicioUiState()
    )
}
