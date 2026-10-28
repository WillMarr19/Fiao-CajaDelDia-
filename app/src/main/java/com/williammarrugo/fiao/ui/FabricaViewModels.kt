package com.williammarrugo.fiao.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.williammarrugo.fiao.FiaoApplication
import com.williammarrugo.fiao.ui.clientes.ClienteDetalleViewModel
import com.williammarrugo.fiao.ui.clientes.ClienteFormViewModel
import com.williammarrugo.fiao.ui.clientes.ClientesViewModel
import com.williammarrugo.fiao.ui.inicio.InicioViewModel
import com.williammarrugo.fiao.ui.movimientos.MovimientoFormViewModel
import com.williammarrugo.fiao.ui.movimientos.MovimientosViewModel

/**
 * La fábrica de ViewModels.
 *
 * ¿Por qué hace falta? Porque Android crea los ViewModel por reflexión
 * y, por defecto, solo sabe llamar a constructores vacíos. Los nuestros
 * reciben repositorios, así que hay que enseñarle cómo construirlos.
 *
 * `viewModelFactory { initializer<T> { ... } }` registra una receta por
 * cada tipo. Dentro del bloque:
 *
 * - `this[APPLICATION_KEY]` da la Application, de donde sale el
 *   contenedor de dependencias.
 * - `createSavedStateHandle()` da la bolsa con los argumentos de
 *   navegación, para los ViewModel que la necesitan.
 *
 * Esto es exactamente lo que Hilt genera por ti con anotaciones. Verlo
 * a mano una vez hace que la magia de Hilt deje de ser magia.
 */
object FabricaViewModels {

    val Factory = viewModelFactory {

        initializer {
            val contenedor = aplicacion().contenedor
            InicioViewModel(
                movimientoRepository = contenedor.movimientoRepository,
                abonoRepository = contenedor.abonoRepository,
                clienteRepository = contenedor.clienteRepository
            )
        }

        initializer {
            val contenedor = aplicacion().contenedor
            MovimientosViewModel(
                movimientoRepository = contenedor.movimientoRepository,
                abonoRepository = contenedor.abonoRepository
            )
        }

        initializer {
            val contenedor = aplicacion().contenedor
            MovimientoFormViewModel(
                repository = contenedor.movimientoRepository,
                savedStateHandle = createSavedStateHandle()
            )
        }

        initializer {
            val contenedor = aplicacion().contenedor
            ClientesViewModel(
                clienteRepository = contenedor.clienteRepository
            )
        }

        initializer {
            val contenedor = aplicacion().contenedor
            ClienteFormViewModel(
                repository = contenedor.clienteRepository,
                savedStateHandle = createSavedStateHandle()
            )
        }

        initializer {
            val contenedor = aplicacion().contenedor
            ClienteDetalleViewModel(
                repository = contenedor.clienteRepository,
                savedStateHandle = createSavedStateHandle()
            )
        }
    }
}

/**
 * Atajo para sacar la Application desde dentro de un initializer.
 *
 * Si esto revienta con ClassCastException, casi seguro falta la línea
 * `android:name=".FiaoApplication"` en el AndroidManifest.
 */
private fun androidx.lifecycle.viewmodel.CreationExtras.aplicacion(): FiaoApplication =
    this[APPLICATION_KEY] as FiaoApplication
