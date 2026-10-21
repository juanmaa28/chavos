package com.jmarias.chavos

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.jmarias.chavos.data.SesionRepository
import com.jmarias.chavos.ui.LocalContenedor
import com.jmarias.chavos.ui.navegacion.AppAutenticacion
import com.jmarias.chavos.ui.navegacion.AppPrincipal
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.TemaChavos
import com.jmarias.chavos.ui.vistaModelo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

/** FragmentActivity (y no ComponentActivity) porque el aviso de huella la necesita. */
class MainActivity : FragmentActivity() {
    /** La app está escrita en español; así el calendario y el aviso de huella no salen en el idioma del sistema. */
    override fun attachBaseContext(base: Context) {
        val configuracion = Configuration(base.resources.configuration).apply { setLocale(ESPANOL) }
        super.attachBaseContext(base.createConfigurationContext(configuracion))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val contenedor = (application as ChavosApplication).contenedor
        setContent {
            CompositionLocalProvider(LocalContenedor provides contenedor) {
                AppRaiz()
            }
        }
    }
}

private val ESPANOL: Locale = Locale.forLanguageTag("es-CO")

sealed interface EstadoSesion {
    data object Cargando : EstadoSesion
    data object SinSesion : EstadoSesion
    data class Activa(val usuarioId: Long) : EstadoSesion
}

class AppViewModel(sesion: SesionRepository) : ViewModel() {
    val sesion: StateFlow<EstadoSesion> = sesion.usuarioId
        .map { id -> if (id == null) EstadoSesion.SinSesion else EstadoSesion.Activa(id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EstadoSesion.Cargando)

    val temaOscuro: StateFlow<Boolean> = sesion.temaOscuro.stateIn(viewModelScope, SharingStarted.Eagerly, false)
}

@Composable
private fun AppRaiz() {
    val vm = vistaModelo { c, _ -> AppViewModel(c.sesion) }
    val sesion by vm.sesion.collectAsStateWithLifecycle()
    val oscuro by vm.temaOscuro.collectAsStateWithLifecycle()

    TemaChavos(oscuro) {
        Box(Modifier.fillMaxSize().background(Chavos.colores.fondo)) {
            Crossfade(targetState = sesion, animationSpec = tween(220), label = "sesion") { estado ->
                when (estado) {
                    EstadoSesion.Cargando -> Unit
                    EstadoSesion.SinSesion -> AppAutenticacion()
                    is EstadoSesion.Activa -> key(estado.usuarioId) { AppPrincipal() }
                }
            }
        }
    }
}
