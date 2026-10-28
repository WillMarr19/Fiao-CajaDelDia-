package com.williammarrugo.fiao

import android.app.Application
import com.williammarrugo.fiao.di.Contenedor
import com.williammarrugo.fiao.di.ContenedorApp

/**
 * La clase Application.
 *
 * Se crea una sola vez, antes que cualquier Activity, y vive mientras
 * viva el proceso. Por eso es el lugar natural para colgar el
 * contenedor de dependencias: la base de datos se abre una vez y la
 * comparten todas las pantallas.
 *
 * Para que Android use esta clase en vez de la Application por defecto
 * hay que declararla en el AndroidManifest:
 *
 *     <application android:name=".FiaoApplication" ... >
 *
 * Si se olvida esa línea, la app compila pero revienta al abrir, porque
 * el contenedor nunca se crea. Es un error clásico.
 */
class FiaoApplication : Application() {

    lateinit var contenedor: Contenedor
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = ContenedorApp(this)
    }
}
