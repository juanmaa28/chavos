package com.jmarias.chavos

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.jmarias.chavos.data.AuthRepository
import com.jmarias.chavos.data.DatosDeEjemplo
import com.jmarias.chavos.data.FinanzasRepository
import com.jmarias.chavos.data.SesionRepository
import com.jmarias.chavos.data.local.ChavosDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val Context.almacen: DataStore<Preferences> by preferencesDataStore(name = "preferencias")

/** Dependencias de la app, creadas una sola vez en [ChavosApplication]. */
class Contenedor(contexto: Context) {
    private val app = contexto.applicationContext
    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val bd: ChavosDatabase =
        Room.databaseBuilder(app, ChavosDatabase::class.java, "chavos.db").build()

    val sesion = SesionRepository(app.almacen)
    val auth = AuthRepository(bd.usuarioDao())
    val finanzas = FinanzasRepository(bd.movimientoDao(), bd.metaDao())

    init {
        alcance.launch { DatosDeEjemplo.sembrarSiVacio(bd) }
    }
}
