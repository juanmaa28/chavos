package com.jmarias.chavos.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Sesión y preferencias. Con "Recordarme" la sesión se guarda en disco; sin él vive solo
 * en memoria y se pierde al cerrar la app.
 */
class SesionRepository(private val almacen: DataStore<Preferences>) {

    private object Llaves {
        val USUARIO = longPreferencesKey("usuario_id")
        val TEMA_OSCURO = booleanPreferencesKey("tema_oscuro")
        val HUELLA = longPreferencesKey("huella_usuario_id")
    }

    private val enMemoria = MutableStateFlow<Long?>(null)

    private val preferencias: Flow<Preferences> = almacen.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val usuarioId: Flow<Long?> =
        combine(enMemoria, preferencias) { memoria, guardadas -> memoria ?: guardadas[Llaves.USUARIO] }
            .distinctUntilChanged()

    val temaOscuro: Flow<Boolean> = preferencias.map { it[Llaves.TEMA_OSCURO] ?: false }.distinctUntilChanged()

    /** Usuario que activó el desbloqueo con huella en este teléfono. */
    val huellaUsuarioId: Flow<Long?> = preferencias.map { it[Llaves.HUELLA] }.distinctUntilChanged()

    suspend fun iniciar(usuarioId: Long, recordar: Boolean) {
        enMemoria.value = usuarioId
        almacen.edit { if (recordar) it[Llaves.USUARIO] = usuarioId else it.remove(Llaves.USUARIO) }
    }

    suspend fun cerrar() {
        enMemoria.value = null
        almacen.edit { it.remove(Llaves.USUARIO) }
    }

    suspend fun cambiarTema(oscuro: Boolean) {
        almacen.edit { it[Llaves.TEMA_OSCURO] = oscuro }
    }

    suspend fun configurarHuella(usuarioId: Long?) {
        almacen.edit { if (usuarioId == null) it.remove(Llaves.HUELLA) else it[Llaves.HUELLA] = usuarioId }
    }
}
