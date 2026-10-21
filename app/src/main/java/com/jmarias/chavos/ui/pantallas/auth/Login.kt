package com.jmarias.chavos.ui.pantallas.auth

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.jmarias.chavos.R
import com.jmarias.chavos.data.AuthRepository
import com.jmarias.chavos.data.SesionRepository
import com.jmarias.chavos.dominio.Validacion
import com.jmarias.chavos.ui.Huella
import com.jmarias.chavos.ui.componentes.Avisos
import com.jmarias.chavos.ui.componentes.Boton
import com.jmarias.chavos.ui.componentes.CampoClave
import com.jmarias.chavos.ui.componentes.CampoTexto
import com.jmarias.chavos.ui.componentes.Casilla
import com.jmarias.chavos.ui.componentes.LogoChavos
import com.jmarias.chavos.ui.componentes.TipoBoton
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.Texto
import com.jmarias.chavos.ui.vistaModelo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LoginEstado(
    val correo: String = "",
    val clave: String = "",
    val recordar: Boolean = true,
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val cargando: Boolean = false,
)

class LoginViewModel(private val auth: AuthRepository, private val sesion: SesionRepository) : ViewModel() {
    var estado by mutableStateOf(LoginEstado())
        private set

    val huellaUsuarioId: StateFlow<Long?> = sesion.huellaUsuarioId.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun cambiarCorreo(valor: String) { estado = estado.copy(correo = valor, errorCorreo = null) }
    fun cambiarClave(valor: String) { estado = estado.copy(clave = valor, errorClave = null) }
    fun cambiarRecordar(valor: Boolean) { estado = estado.copy(recordar = valor) }

    fun iniciarSesion() {
        val actual = estado
        val errorCorreo = when {
            actual.correo.isBlank() -> "Escribe tu correo"
            !Validacion.correoValido(actual.correo) -> "Revisa el formato del correo"
            else -> null
        }
        val errorClave = if (actual.clave.isEmpty()) "Escribe tu contraseña" else null
        if (errorCorreo != null || errorClave != null) {
            estado = actual.copy(errorCorreo = errorCorreo, errorClave = errorClave)
            return
        }
        estado = actual.copy(cargando = true)
        viewModelScope.launch {
            val usuario = auth.autenticar(actual.correo, actual.clave)
            if (usuario == null) {
                estado = estado.copy(cargando = false, errorClave = "Correo o contraseña incorrectos")
            } else {
                sesion.iniciar(usuario.id, actual.recordar)
            }
        }
    }

    fun entrarConHuella(usuarioId: Long) {
        viewModelScope.launch {
            if (auth.existe(usuarioId)) sesion.iniciar(usuarioId, recordar = true)
        }
    }
}

@Composable
fun LoginPantalla(onCrearCuenta: () -> Unit) {
    val vm = vistaModelo { c, _ -> LoginViewModel(c.auth, c.sesion) }
    val e = vm.estado
    val huellaUsuarioId by vm.huellaUsuarioId.collectAsStateWithLifecycle()
    val c = Chavos.colores
    val contexto = LocalContext.current
    val actividad = LocalActivity.current as? FragmentActivity
    val foco = LocalFocusManager.current
    val avisos = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    var mostrarAyudaClave by remember { mutableStateOf(false) }

    fun avisar(texto: String) { alcance.launch { avisos.showSnackbar(texto) } }

    Box(Modifier.fillMaxSize().background(c.fondo)) {
        BoxWithConstraints(Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
            val altoMinimo = maxHeight
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(min = altoMinimo).padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.padding(top = 22.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LogoChavos()
                            Text("Chavos", style = Texto.marca, color = c.tinta)
                        }
                        Spacer(Modifier.height(56.dp))
                        Text("Tus finanzas,\nen calma.", style = Texto.titularLogin, color = c.tinta)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Registra lo que gastas, ahorra para tus metas y entiende tu mes de un vistazo.",
                            style = Texto.entradilla,
                            color = c.tintaSecundaria,
                            modifier = Modifier.widthIn(max = 300.dp),
                        )
                    }

                    Column(Modifier.padding(top = 40.dp, bottom = 34.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
                        CampoTexto(
                            valor = e.correo,
                            onCambio = vm::cambiarCorreo,
                            etiqueta = "Correo electrónico",
                            placeholder = "tucorreo@ejemplo.com",
                            iconoInicial = R.drawable.ic_mail,
                            error = e.errorCorreo,
                            teclado = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next, autoCorrectEnabled = false),
                        )
                        CampoClave(
                            valor = e.clave,
                            onCambio = vm::cambiarClave,
                            etiqueta = "Contraseña",
                            error = e.errorClave,
                            accionTeclado = ImeAction.Done,
                            onListo = { foco.clearFocus(); vm.iniciarSesion() },
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(
                                modifier = Modifier.toggleable(e.recordar, role = Role.Checkbox, onValueChange = vm::cambiarRecordar),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(9.dp),
                            ) {
                                Casilla(e.recordar)
                                Text("Recordarme", style = Texto.medio135, color = c.tintaSecundaria)
                            }
                            Text(
                                "¿Olvidaste tu contraseña?",
                                style = Texto.enlace135.copy(textDecoration = TextDecoration.Underline),
                                color = c.tinta,
                                modifier = Modifier.clickable(role = Role.Button) { mostrarAyudaClave = true }.padding(vertical = 6.dp),
                            )
                        }
                        Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Boton("Iniciar sesión", { foco.clearFocus(); vm.iniciarSesion() }, cargando = e.cargando)
                            Boton(
                                texto = "Entrar con huella",
                                tipo = TipoBoton.Secundario,
                                icono = R.drawable.ic_fingerprint,
                                onClick = {
                                    val id = huellaUsuarioId
                                    when {
                                        id == null -> avisar("Activa el desbloqueo con huella en Ajustes después de iniciar sesión.")
                                        actividad == null || !Huella.disponible(contexto) -> avisar("Este teléfono no tiene una huella configurada.")
                                        else -> Huella.autenticar(
                                            actividad, "Entrar a Chavos", "Usa tu huella para continuar",
                                            onExito = { vm.entrarConHuella(id) },
                                            onError = ::avisar,
                                        )
                                    }
                                },
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Text("¿Aún no tienes cuenta? ", style = Texto.cuerpo, color = c.tintaSecundaria)
                            Text(
                                "Crear cuenta",
                                style = Texto.cuerpo.copy(fontWeight = FontWeight.Bold),
                                color = c.tinta,
                                modifier = Modifier.clickable(role = Role.Button, onClick = onCrearCuenta),
                            )
                        }
                    }
                }
            }
        }
        Avisos(avisos, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }

    if (mostrarAyudaClave) {
        AlertDialog(
            onDismissRequest = { mostrarAyudaClave = false },
            confirmButton = { TextButton(onClick = { mostrarAyudaClave = false }) { Text("Entendido") } },
            title = { Text("Recuperar contraseña") },
            text = {
                Text(
                    "Tus datos se guardan solo en este teléfono, así que no podemos enviarte un enlace por correo. " +
                        "Si no recuerdas tu contraseña, puedes crear una cuenta nueva.",
                )
            },
            containerColor = c.superficie,
        )
    }
}
