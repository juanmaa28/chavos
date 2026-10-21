package com.jmarias.chavos.ui.pantallas.ajustes

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.jmarias.chavos.R
import com.jmarias.chavos.data.AuthRepository
import com.jmarias.chavos.data.FinanzasRepository
import com.jmarias.chavos.data.SesionRepository
import com.jmarias.chavos.dominio.Csv
import com.jmarias.chavos.dominio.Validacion
import com.jmarias.chavos.ui.Huella
import com.jmarias.chavos.ui.componentes.Avatar
import com.jmarias.chavos.ui.componentes.Avisos
import com.jmarias.chavos.ui.componentes.CampoTexto
import com.jmarias.chavos.ui.componentes.Chevron
import com.jmarias.chavos.ui.componentes.ColumnaPantalla
import com.jmarias.chavos.ui.componentes.EtiquetaGrupo
import com.jmarias.chavos.ui.componentes.FilaAjuste
import com.jmarias.chavos.ui.componentes.Interruptor
import com.jmarias.chavos.ui.componentes.Tarjeta
import com.jmarias.chavos.ui.componentes.Tono
import com.jmarias.chavos.ui.componentes.ValorAjuste
import com.jmarias.chavos.ui.compartirCsv
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.Texto
import com.jmarias.chavos.ui.vistaModelo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AjustesEstado(
    val nombre: String = "",
    val correo: String = "",
    val temaOscuro: Boolean = false,
    val huellaActiva: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class AjustesViewModel(
    private val sesion: SesionRepository,
    private val auth: AuthRepository,
    private val finanzas: FinanzasRepository,
) : ViewModel() {
    val estado: StateFlow<AjustesEstado> = sesion.usuarioId.filterNotNull().flatMapLatest { id ->
        combine(auth.usuario(id), sesion.temaOscuro, sesion.huellaUsuarioId) { usuario, oscuro, huella ->
            AjustesEstado(usuario?.nombre.orEmpty(), usuario?.correo.orEmpty(), oscuro, huella == id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AjustesEstado())

    private suspend fun usuarioId(): Long = sesion.usuarioId.filterNotNull().first()

    fun cambiarTema(oscuro: Boolean) { viewModelScope.launch { sesion.cambiarTema(oscuro) } }

    fun configurarHuella(activar: Boolean) {
        viewModelScope.launch { sesion.configurarHuella(if (activar) usuarioId() else null) }
    }

    fun cambiarNombre(nombre: String) { viewModelScope.launch { auth.actualizarNombre(usuarioId(), nombre) } }

    fun cerrarSesion() { viewModelScope.launch { sesion.cerrar() } }

    /** CSV con todos los movimientos, o null si no hay nada que exportar. */
    suspend fun csv(): String? {
        val id = usuarioId()
        val movimientos = finanzas.todosLosMovimientos(id)
        if (movimientos.isEmpty()) return null
        val metas = finanzas.todasLasMetas(id).associate { it.id to it.nombre }
        return Csv.movimientos(movimientos, metas)
    }
}

@Composable
fun AjustesPantalla(onCreditos: () -> Unit) {
    val vm = vistaModelo { c, _ -> AjustesViewModel(c.sesion, c.auth, c.finanzas) }
    val e by vm.estado.collectAsStateWithLifecycle()
    val c = Chavos.colores
    val contexto = LocalContext.current
    val actividad = LocalActivity.current as? FragmentActivity
    val avisos = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    var editarNombre by remember { mutableStateOf(false) }
    var confirmarSalida by remember { mutableStateOf(false) }

    fun avisar(texto: String) { alcance.launch { avisos.showSnackbar(texto) } }

    Box(Modifier.fillMaxSize()) {
        ColumnaPantalla {
            Text("Ajustes", style = Texto.tituloGrande, color = c.tinta, modifier = Modifier.padding(top = 6.dp, bottom = 18.dp))

            Tarjeta(relleno = PaddingValues(horizontal = 16.dp, vertical = 14.dp), onClick = { editarNombre = true }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                    Avatar(Validacion.iniciales(e.nombre), 46.dp)
                    Column(Modifier.weight(1f)) {
                        Text(e.nombre, style = Texto.fila16, color = c.tinta, maxLines = 1)
                        Spacer(Modifier.height(2.dp))
                        Text(e.correo, style = Texto.leyenda, color = c.tintaTerciaria, maxLines = 1)
                    }
                    Chevron()
                }
            }

            Spacer(Modifier.height(16.dp))
            EtiquetaGrupo("Preferencias")
            Tarjeta {
                FilaAjuste(R.drawable.ic_coin, "Moneda", conDivisor = false) { ValorAjuste("COP $") }
                FilaAjuste(
                    R.drawable.ic_moon, "Tema oscuro", conDivisor = true,
                    modifier = Modifier.toggleable(e.temaOscuro, role = Role.Switch, onValueChange = vm::cambiarTema),
                ) { Interruptor(e.temaOscuro) }
            }

            Spacer(Modifier.height(16.dp))
            EtiquetaGrupo("Seguridad y datos")
            Tarjeta {
                FilaAjuste(
                    R.drawable.ic_fingerprint, "Desbloqueo con huella", conDivisor = false,
                    modifier = Modifier.toggleable(e.huellaActiva, role = Role.Switch) { activar ->
                        when {
                            !activar -> vm.configurarHuella(false)
                            actividad == null || !Huella.disponible(contexto) ->
                                avisar("Configura una huella en los ajustes del teléfono para usar esta opción.")
                            else -> Huella.autenticar(
                                actividad, "Activar huella", "Confirma con tu huella para entrar más rápido",
                                onExito = { vm.configurarHuella(true) },
                                onError = ::avisar,
                            )
                        }
                    },
                ) { Interruptor(e.huellaActiva) }
                FilaAjuste(
                    R.drawable.ic_export, "Exportar movimientos", conDivisor = true,
                    onClick = {
                        alcance.launch {
                            val csv = vm.csv()
                            if (csv == null) avisar("Todavía no tienes movimientos para exportar.") else compartirCsv(contexto, csv)
                        }
                    },
                ) {
                    ValorAjuste("CSV")
                    Chevron()
                }
            }

            Spacer(Modifier.height(16.dp))
            Tarjeta {
                FilaAjuste(R.drawable.ic_info, "Créditos", conDivisor = false, onClick = onCreditos) { Chevron() }
                FilaAjuste(R.drawable.ic_logout, "Cerrar sesión", conDivisor = true, tono = Tono.Peligro, onClick = { confirmarSalida = true })
            }
            Spacer(Modifier.height(28.dp))
        }
        Avisos(avisos, Modifier.align(Alignment.BottomCenter))
    }

    if (editarNombre) {
        var nombre by remember { mutableStateOf(e.nombre) }
        AlertDialog(
            onDismissRequest = { editarNombre = false },
            title = { Text("Tu nombre") },
            text = {
                CampoTexto(
                    valor = nombre, onCambio = { nombre = it }, etiqueta = "Nombre completo",
                    teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    error = if (nombre.isBlank()) "Escribe tu nombre" else null,
                )
            },
            confirmButton = {
                TextButton(enabled = nombre.isNotBlank(), onClick = { vm.cambiarNombre(nombre); editarNombre = false }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { editarNombre = false }) { Text("Cancelar") } },
            containerColor = c.superficie,
        )
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tus movimientos y metas se quedan guardados en este teléfono.") },
            confirmButton = { TextButton(onClick = { confirmarSalida = false; vm.cerrarSesion() }) { Text("Cerrar sesión", color = c.negativo) } },
            dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") } },
            containerColor = c.superficie,
        )
    }
}
