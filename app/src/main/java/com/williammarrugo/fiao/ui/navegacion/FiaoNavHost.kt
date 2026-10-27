package com.williammarrugo.fiao.ui.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.williammarrugo.fiao.domain.model.DatosDemo
import com.williammarrugo.fiao.ui.clientes.ClienteDetalleScreen
import com.williammarrugo.fiao.ui.clientes.ClienteFormScreen
import com.williammarrugo.fiao.ui.clientes.ClientesScreen
import com.williammarrugo.fiao.ui.clientes.EstadoFormularioCliente
import com.williammarrugo.fiao.ui.inicio.InicioScreen
import com.williammarrugo.fiao.ui.movimientos.EstadoFormularioMovimiento
import com.williammarrugo.fiao.ui.movimientos.MovimientoFormScreen
import com.williammarrugo.fiao.ui.movimientos.MovimientosScreen
import com.williammarrugo.fiao.ui.resumen.ResumenScreen
import java.time.LocalDate

/**
 * El grafo de navegación.
 *
 * Por ahora todas las pantallas se alimentan de DatosDemo y los
 * formularios guardan su estado aquí mismo con remember. Es un andamio:
 * sirve para recorrer la app completa en el celular antes de que exista
 * la base de datos.
 *
 * Cuando lleguen los ViewModel (día 6) y Room (día 7), lo único que
 * cambia es de dónde salen los datos. Las pantallas no se tocan, porque
 * ninguna sabe quién se los pasa.
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
            InicioScreen(
                resumen = DatosDemo.resumenHoy,
                movimientos = DatosDemo.movimientos,
                totalPorCobrar = DatosDemo.totalPorCobrar,
                clientesQueDeben = DatosDemo.clientesQueDeben,
                onNuevoMovimiento = {
                    navController.navigate(Rutas.movimientoForm())
                },
                onMovimientoClick = { id ->
                    navController.navigate(Rutas.movimientoForm(id))
                },
                onVerMovimientos = { navController.navigate(Rutas.MOVIMIENTOS) },
                onVerClientes = { navController.navigate(Rutas.CLIENTES) },
                onVerResumen = { navController.navigate(Rutas.RESUMEN) }
            )
        }

        composable(Rutas.MOVIMIENTOS) {
            MovimientosScreen(
                fecha = LocalDate.now(),
                resumen = DatosDemo.resumenHoy,
                movimientos = DatosDemo.movimientos,
                onVolver = { navController.popBackStack() },
                onCambiarFecha = { /* El selector de fecha llega el día 8 */ },
                onNuevoMovimiento = { navController.navigate(Rutas.movimientoForm()) },
                onMovimientoClick = { id -> navController.navigate(Rutas.movimientoForm(id)) }
            )
        }

        composable(
            route = Rutas.MOVIMIENTO_FORM_RUTA,
            arguments = listOf(
                navArgument(Rutas.ARG_MOVIMIENTO_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { entrada ->
            val id = entrada.arguments?.getLong(Rutas.ARG_MOVIMIENTO_ID) ?: -1L
            val existente = DatosDemo.movimientos.firstOrNull { it.id == id }

            var estado by remember(id) {
                mutableStateOf(
                    EstadoFormularioMovimiento(
                        tipo = existente?.tipo ?: EstadoFormularioMovimiento().tipo,
                        monto = existente?.monto,
                        descripcion = existente?.descripcion.orEmpty()
                    )
                )
            }

            MovimientoFormScreen(
                estado = estado,
                esEdicion = existente != null,
                onTipoCambia = { estado = estado.copy(tipo = it) },
                onMontoCambia = { estado = estado.copy(monto = it, errorMonto = null) },
                onDescripcionCambia = {
                    estado = estado.copy(descripcion = it, errorDescripcion = null)
                },
                onGuardar = { navController.popBackStack() },
                onEliminar = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.CLIENTES) {
            var busqueda by remember { mutableStateOf("") }
            val lista = DatosDemo.clientes
                .filter { it.cliente.nombre.contains(busqueda, ignoreCase = true) }
                .sortedByDescending { it.saldo }

            ClientesScreen(
                clientes = lista,
                totalPorCobrar = DatosDemo.totalPorCobrar,
                busqueda = busqueda,
                onBusquedaCambia = { busqueda = it },
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
            val id = entrada.arguments?.getLong(Rutas.ARG_CLIENTE_ID) ?: return@composable
            val item = DatosDemo.clientes.firstOrNull { it.cliente.id == id }
                ?: return@composable

            ClienteDetalleScreen(
                cliente = item.cliente,
                saldo = item.saldo,
                historial = DatosDemo.historialCliente,
                onVolver = { navController.popBackStack() },
                onEditar = { navController.navigate(Rutas.clienteForm(id)) },
                onFiar = { /* Hoja de registro de fiado: día 9 */ },
                onAbonar = { /* Hoja de registro de abono: día 10 */ },
                onRecordar = { /* Intent de WhatsApp: día 10 */ }
            )
        }

        composable(
            route = Rutas.CLIENTE_FORM_RUTA,
            arguments = listOf(
                navArgument(Rutas.ARG_CLIENTE_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { entrada ->
            val id = entrada.arguments?.getLong(Rutas.ARG_CLIENTE_ID) ?: -1L
            val existente = DatosDemo.clientes.firstOrNull { it.cliente.id == id }

            var estado by remember(id) {
                mutableStateOf(
                    EstadoFormularioCliente(
                        nombre = existente?.cliente?.nombre.orEmpty(),
                        telefono = existente?.cliente?.telefono.orEmpty(),
                        puedeEliminar = (existente?.saldo ?: 0L) == 0L
                    )
                )
            }

            ClienteFormScreen(
                estado = estado,
                esEdicion = existente != null,
                onNombreCambia = { estado = estado.copy(nombre = it, errorNombre = null) },
                onTelefonoCambia = { estado = estado.copy(telefono = it) },
                onGuardar = { navController.popBackStack() },
                onEliminar = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.RESUMEN) {
            ResumenScreen(
                dias = DatosDemo.semana,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
