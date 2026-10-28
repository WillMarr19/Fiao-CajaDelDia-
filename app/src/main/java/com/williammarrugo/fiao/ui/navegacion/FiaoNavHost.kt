package com.williammarrugo.fiao.ui.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.williammarrugo.fiao.ui.FabricaViewModels
import com.williammarrugo.fiao.ui.clientes.ClienteDetalleScreen
import com.williammarrugo.fiao.ui.clientes.ClienteDetalleViewModel
import com.williammarrugo.fiao.ui.clientes.ClienteFormScreen
import com.williammarrugo.fiao.ui.clientes.ClienteFormViewModel
import com.williammarrugo.fiao.ui.clientes.ClientesScreen
import com.williammarrugo.fiao.ui.clientes.ClientesViewModel
import com.williammarrugo.fiao.ui.inicio.InicioScreen
import com.williammarrugo.fiao.ui.inicio.InicioViewModel
import com.williammarrugo.fiao.ui.movimientos.MovimientoFormScreen
import com.williammarrugo.fiao.ui.movimientos.MovimientoFormViewModel
import com.williammarrugo.fiao.ui.movimientos.MovimientosScreen
import com.williammarrugo.fiao.ui.movimientos.MovimientosViewModel
import com.williammarrugo.fiao.ui.resumen.ResumenScreen
import com.williammarrugo.fiao.domain.model.DatosDemo

/**
 * El grafo de navegación, ya conectado a la base de datos.
 *
 * Este archivo es la costura entre dos mundos: arriba los ViewModel,
 * que saben de datos, y abajo las pantallas, que solo saben dibujar.
 *
 * Dos funciones aparecen en todos los destinos:
 *
 * - `viewModel(factory = ...)` pide el ViewModel. Si ya existe uno para
 *   este destino lo reutiliza; si no, lo crea con la fábrica. Por eso
 *   al rotar el celular no se pierde el estado: la Activity se
 *   reconstruye pero el ViewModel sigue siendo el mismo.
 *
 * - `collectAsStateWithLifecycle()` suscribe la pantalla al StateFlow.
 *   La parte "WithLifecycle" importa: cuando la app pasa a segundo
 *   plano, deja de escuchar, y así no se gasta batería recalculando
 *   una pantalla que nadie ve. Es la versión correcta en Android de
 *   `collectAsState()`.
 */
@Composable
fun FiaoNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Rutas.INICIO,
        modifier = modifier
    ) {

        composable(Rutas.INICIO) {
            val vm: InicioViewModel = viewModel(factory = FabricaViewModels.Factory)
            val estado by vm.uiState.collectAsStateWithLifecycle()

            InicioScreen(
                resumen = estado.resumen,
                movimientos = estado.movimientos,
                totalPorCobrar = estado.totalPorCobrar,
                clientesQueDeben = estado.clientesQueDeben,
                onNuevoMovimiento = { navController.navigate(Rutas.movimientoForm()) },
                onMovimientoClick = { id -> navController.navigate(Rutas.movimientoForm(id)) },
                onVerMovimientos = { navController.navigate(Rutas.MOVIMIENTOS) },
                onVerClientes = { navController.navigate(Rutas.CLIENTES) },
                onVerResumen = { navController.navigate(Rutas.RESUMEN) }
            )
        }

        composable(Rutas.MOVIMIENTOS) {
            val vm: MovimientosViewModel = viewModel(factory = FabricaViewModels.Factory)
            val estado by vm.uiState.collectAsStateWithLifecycle()

            MovimientosScreen(
                estado = estado,
                onVolver = { navController.popBackStack() },
                onCambiarFecha = vm::cambiarDia,
                onNuevoMovimiento = { navController.navigate(Rutas.movimientoForm()) },
                onMovimientoClick = { id -> navController.navigate(Rutas.movimientoForm(id)) }
            )
        }

        composable(
            route = Rutas.MOVIMIENTO_FORM_RUTA,
            arguments = listOf(
                navArgument(Rutas.ARG_MOVIMIENTO_ID) {
                    type = NavType.LongType
                    defaultValue = MovimientoFormViewModel.SIN_ID
                }
            )
        ) {
            val vm: MovimientoFormViewModel = viewModel(factory = FabricaViewModels.Factory)
            val estado by vm.uiState.collectAsStateWithLifecycle()

            MovimientoFormScreen(
                estado = estado,
                onTipoCambia = vm::cambiarTipo,
                onMontoCambia = vm::cambiarMonto,
                onDescripcionCambia = vm::cambiarDescripcion,
                onGuardar = vm::guardar,
                onEliminar = vm::eliminar,
                onVolver = { navController.popBackStack() },
                onErrorMostrado = vm::errorMostrado
            )
        }

        composable(Rutas.CLIENTES) {
            val vm: ClientesViewModel = viewModel(factory = FabricaViewModels.Factory)
            val estado by vm.uiState.collectAsStateWithLifecycle()

            ClientesScreen(
                clientes = estado.clientes,
                totalPorCobrar = estado.totalPorCobrar,
                busqueda = estado.busqueda,
                onBusquedaCambia = vm::cambiarBusqueda,
                onVolver = { navController.popBackStack() },
                onClienteClick = { id -> navController.navigate(Rutas.clienteDetalle(id)) },
                onNuevoCliente = { navController.navigate(Rutas.clienteForm()) }
            )
        }

        composable(
            route = Rutas.CLIENTE_DETALLE_RUTA,
            arguments = listOf(
                navArgument(Rutas.ARG_CLIENTE_ID) { type = NavType.LongType }
            )
        ) { entrada ->
            val vm: ClienteDetalleViewModel = viewModel(factory = FabricaViewModels.Factory)
            val estado by vm.uiState.collectAsStateWithLifecycle()
            val cliente = estado.cliente
            val id = entrada.arguments?.getLong(Rutas.ARG_CLIENTE_ID) ?: 0L

            // Mientras la consulta no responde, cliente es null. No se
            // dibuja nada en vez de inventar un cliente vacío.
            if (cliente != null) {
                ClienteDetalleScreen(
                    cliente = cliente,
                    saldo = estado.saldo,
                    historial = estado.historial,
                    onVolver = { navController.popBackStack() },
                    onEditar = { navController.navigate(Rutas.clienteForm(id)) },
                    onFiar = { /* F7: pendiente */ },
                    onAbonar = { /* F8: pendiente */ },
                    onRecordar = { /* F12: pendiente */ }
                )
            }
        }

        composable(
            route = Rutas.CLIENTE_FORM_RUTA,
            arguments = listOf(
                navArgument(Rutas.ARG_CLIENTE_ID) {
                    type = NavType.LongType
                    defaultValue = ClienteFormViewModel.SIN_ID
                }
            )
        ) {
            val vm: ClienteFormViewModel = viewModel(factory = FabricaViewModels.Factory)
            val estado by vm.uiState.collectAsStateWithLifecycle()

            ClienteFormScreen(
                estado = estado,
                onNombreCambia = vm::cambiarNombre,
                onTelefonoCambia = vm::cambiarTelefono,
                onGuardar = vm::guardar,
                onEliminar = vm::eliminar,
                onVolver = { navController.popBackStack() },
                onErrorMostrado = vm::errorMostrado
            )
        }

        composable(Rutas.RESUMEN) {
            // F13 todavía usa datos de ejemplo: el resumen semanal
            // llega el día 11 del plan.
            ResumenScreen(
                dias = DatosDemo.semana,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
