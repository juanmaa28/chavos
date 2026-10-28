package com.jmarias.chavos

import android.app.Application

/** Punto de entrada de la app: crea el contenedor de dependencias al arrancar. */
class ChavosApplication : Application() {
    lateinit var contenedor: Contenedor
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = Contenedor(this)
    }
}
