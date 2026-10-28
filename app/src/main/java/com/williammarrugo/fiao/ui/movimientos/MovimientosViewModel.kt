package com.williammarrugo.fiao.ui.movimientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williammarrugo.fiao.domain.model.Movimiento
import com.williammarrugo.fiao.domain.model.ResumenDia
import com.williammarrugo.fiao.domain.repository.AbonoRepository
import com.williammarrugo.fiao.domain.repository.MovimientoRepository
import com.williammarrugo.fiao.domain.rules.calcularResumenDia
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class MovimientosUiState(
    val cargando: Boolean = true,
    val dia: LocalDate = LocalDate.now(),
    val resumen: ResumenDia = ResumenDia(0, 0, 0),
    val movimientos: List<Movimiento> = emptyList()
)

/**
 * El libro de un día, con filtro de fecha (F5).
 *
 * La pieza interesante aquí es `flatMapLatest`.
 *
 * El día seleccionado es un flujo que cambia cuando el usuario elige
 * otra fecha. Cada día distinto necesita OTRA consulta a la base de
 * datos. `flatMapLatest` hace exactamente eso: cuando el flujo de
 * arriba emite un día nuevo, cancela la consulta anterior y arranca la
 * nueva.
 *
 * El "latest" del nombre es la parte importante: si el usuario cambia
 * de fecha tres veces rápido, solo sobrevive la última consulta. Sin
 * eso podrían llegar resultados viejos después de los nuevos y la
 * pantalla mostraría el día equivocado.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MovimientosViewModel(
    private val movimientoRepository: MovimientoRepository,
    private val abonoRepository: AbonoRepository
) : ViewModel() {

    private val diaSeleccionado = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<MovimientosUiState> = diaSeleccionado
        .flatMapLatest { dia ->
            combine(
                movimientoRepository.observarPorDia(dia),
                abonoRepository.observarPorDia(dia)
            ) { movimientos, abonos ->
                MovimientosUiState(
                    cargando = false,
                    dia = dia,
                    resumen = calcularResumenDia(movimientos, abonos),
                    movimientos = movimientos
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MovimientosUiState()
        )

    /** La pantalla llama esto cuando el usuario elige otra fecha. */
    fun cambiarDia(nuevoDia: LocalDate) {
        diaSeleccionado.value = nuevoDia
    }
}
