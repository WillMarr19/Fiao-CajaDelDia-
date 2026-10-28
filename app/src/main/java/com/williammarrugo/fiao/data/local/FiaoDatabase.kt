package com.williammarrugo.fiao.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * La base de datos.
 *
 * `version = 1` es la versión del esquema. Cada vez que cambie la forma
 * de una tabla (una columna nueva, una tabla nueva) hay que subir ese
 * número y escribir una migración, o la app reventará al abrir una base
 * vieja.
 *
 * Mientras desarrollas basta con desinstalar la app para empezar de
 * cero. En producción eso no es opción: borraría los datos del usuario.
 *
 * `exportSchema = false` evita que Room pida una carpeta donde guardar
 * el esquema en JSON. En un proyecto serio se pone en true y ese JSON
 * se versiona, porque sirve para generar y probar migraciones.
 */
@Database(
    entities = [
        MovimientoEntity::class,
        ClienteEntity::class,
        FiadoEntity::class,
        AbonoEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Convertidores::class)
abstract class FiaoDatabase : RoomDatabase() {

    abstract fun movimientoDao(): MovimientoDao
    abstract fun clienteDao(): ClienteDao
    abstract fun fiadoDao(): FiadoDao
    abstract fun abonoDao(): AbonoDao

    companion object {

        /**
         * Una sola instancia para toda la app.
         *
         * Abrir la base de datos es caro, y dos instancias apuntando al
         * mismo archivo pueden pisarse. Este es el patrón Singleton.
         *
         * `@Volatile` asegura que, si un hilo escribe la instancia,
         * todos los demás vean ese cambio de inmediato. El bloque
         * `synchronized` evita que dos hilos la creen a la vez. A este
         * conjunto se le llama "double-checked locking".
         */
        @Volatile
        private var instancia: FiaoDatabase? = null

        fun obtener(context: Context): FiaoDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    FiaoDatabase::class.java,
                    "fiao.db"
                )
                    .build()
                    .also { instancia = it }
            }
    }
}
