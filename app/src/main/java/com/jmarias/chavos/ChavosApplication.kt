package com.jmarias.chavos

import android.app.Application

class ChavosApplication : Application() {
    lateinit var contenedor: Contenedor
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = Contenedor(this)
    }
}
