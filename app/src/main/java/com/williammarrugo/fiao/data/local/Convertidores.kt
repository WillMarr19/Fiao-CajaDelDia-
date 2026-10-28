package com.williammarrugo.fiao.data.local

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * SQLite no sabe qué es un LocalDateTime.
 *
 * Solo guarda números, textos y blobs. Estos conversores le dicen a
 * Room cómo pasar de LocalDateTime a algo guardable y de vuelta.
 *
 * Se guarda como milisegundos desde 1970 (un Long) y no como texto,
 * por dos razones: ocupa menos y permite comparar rangos con BETWEEN
 * en SQL, que es justo lo que necesita el filtro por día (F5).
 *
 * La conversión usa la zona horaria del dispositivo, que es la regla
 * R7: "hoy" es el día del tendero, no el de un servidor en otro país.
 */
class Convertidores {

    @TypeConverter
    fun deFechaAMillis(fecha: LocalDateTime?): Long? =
        fecha?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()

    @TypeConverter
    fun deMillisAFecha(millis: Long?): LocalDateTime? =
        millis?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        }
}
