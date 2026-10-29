package com.williammarrugo.fiao.di

import android.content.Context
import com.williammarrugo.fiao.data.AbonoRepositoryRoom
import com.williammarrugo.fiao.data.ClienteRepositoryRoom
import com.williammarrugo.fiao.data.FiadoRepositoryRoom
import com.williammarrugo.fiao.data.MovimientoRepositoryRoom
import com.williammarrugo.fiao.data.local.FiaoDatabase
import com.williammarrugo.fiao.domain.repository.AbonoRepository
import com.williammarrugo.fiao.domain.repository.ClienteRepository
import com.williammarrugo.fiao.domain.repository.FiadoRepository
import com.williammarrugo.fiao.domain.repository.MovimientoRepository

/**
 * El contenedor de dependencias.
 *
 * Alguien tiene que crear la base de datos y los repositorios y
 * entregárselos a quien los necesite. Eso es inyección de dependencias,
 * y aquí está hecha a mano.
 *
 * ¿Por qué no Hilt o Koin? Porque para una app de este tamaño agregan
 * más de lo que resuelven, y porque entender el problema a mano hace
 * que luego las librerías tengan sentido. Si en la entrevista preguntan
 * por Hilt, la respuesta honesta es: hace esto mismo pero generando el
 * código con anotaciones, lo cual se agradece cuando el grafo de
 * dependencias crece.
 *
 * `by lazy` significa que cada objeto se crea la primera vez que se
 * pide, no al arrancar la app. Abrir la base de datos tiene costo y no
 * tiene sentido pagarlo si el usuario todavía no ha pedido datos.
 *
 * Fíjate en los tipos declarados: `MovimientoRepository`, no
 * `MovimientoRepositoryRoom`. Lo que sale de aquí es la interfaz; la
 * clase concreta queda escondida dentro del contenedor. Ese es el
 * único lugar de la app que sabe que los datos vienen de Room.
 */
interface Contenedor {
    val movimientoRepository: MovimientoRepository
    val clienteRepository: ClienteRepository
    val fiadoRepository: FiadoRepository
    val abonoRepository: AbonoRepository
}

class ContenedorApp(private val context: Context) : Contenedor {

    private val baseDeDatos: FiaoDatabase by lazy {
        FiaoDatabase.obtener(context)
    }

    override val movimientoRepository: MovimientoRepository by lazy {
        MovimientoRepositoryRoom(baseDeDatos.movimientoDao())
    }

    override val clienteRepository: ClienteRepository by lazy {
        ClienteRepositoryRoom(
            clienteDao = baseDeDatos.clienteDao(),
            fiadoDao = baseDeDatos.fiadoDao(),
            abonoDao = baseDeDatos.abonoDao()
        )
    }

    override val fiadoRepository: FiadoRepository by lazy {
        FiadoRepositoryRoom(baseDeDatos.fiadoDao())
    }

    override val abonoRepository: AbonoRepository by lazy {
        AbonoRepositoryRoom(baseDeDatos.abonoDao())
    }
}
