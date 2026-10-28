package com.jmarias.chavos.ui.pantallas.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmarias.chavos.R
import com.jmarias.chavos.data.AuthRepository
import com.jmarias.chavos.data.FinanzasRepository
import com.jmarias.chavos.data.SesionRepository
import com.jmarias.chavos.data.local.MetaEntity
import com.jmarias.chavos.dominio.Validacion
import com.jmarias.chavos.dominio.iconoSugerido
import com.jmarias.chavos.ui.componentes.BarraSuperior
import com.jmarias.chavos.ui.componentes.Boton
import com.jmarias.chavos.ui.componentes.CampoClave
import com.jmarias.chavos.ui.componentes.CampoTexto
import com.jmarias.chavos.ui.componentes.Casilla
import com.jmarias.chavos.ui.componentes.Icono
import com.jmarias.chavos.ui.componentes.IndicadorClave
import com.jmarias.chavos.ui.componentes.IndicadorPasos
import com.jmarias.chavos.ui.componentes.MilesTransformacion
import com.jmarias.chavos.ui.componentes.TipoBoton
import com.jmarias.chavos.ui.componentes.limpiarDigitos
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.Texto
import com.jmarias.chavos.ui.vistaModelo
import kotlinx.coroutines.launch

/** Estado del formulario de registro. */
data class RegistroEstado(
    val paso: Int = 1,
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val confirmar: String = "",
    val terminos: Boolean = false,
    val metaNombre: String = "",
    val metaDigitos: String = "",
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val errorConfirmar: String? = null,
    val errorTerminos: String? = null,
    val errorMeta: String? = null,
    val cargando: Boolean = false,
)

class RegistroViewModel(
    private val auth: AuthRepository,
    private val finanzas: FinanzasRepository,
    private val sesion: SesionRepository,
) : ViewModel() {
    var estado by mutableStateOf(RegistroEstado())
        private set

    fun cambiarNombre(v: String) { estado = estado.copy(nombre = v, errorNombre = null) }
    fun cambiarCorreo(v: String) { estado = estado.copy(correo = v, errorCorreo = null) }
    fun cambiarClave(v: String) { estado = estado.copy(clave = v, errorClave = null) }
    fun cambiarConfirmar(v: String) { estado = estado.copy(confirmar = v, errorConfirmar = null) }
    fun cambiarTerminos(v: Boolean) { estado = estado.copy(terminos = v, errorTerminos = null) }
    fun cambiarMetaNombre(v: String) { estado = estado.copy(metaNombre = v, errorMeta = null) }
    fun cambiarMetaMonto(v: String) { estado = estado.copy(metaDigitos = limpiarDigitos(v), errorMeta = null) }
    fun volverAlPaso1() { estado = estado.copy(paso = 1) }

    fun continuar() {
        val e = estado
        val conErrores = e.copy(
            errorNombre = if (e.nombre.trim().length < 2) "Escribe tu nombre" else null,
            errorCorreo = when {
                e.correo.isBlank() -> "Escribe tu correo"
                !Validacion.correoValido(e.correo) -> "Revisa el formato del correo"
                else -> null
            },
            errorClave = if (e.clave.length < Validacion.LARGO_MINIMO_CLAVE) "Usa al menos ${Validacion.LARGO_MINIMO_CLAVE} caracteres" else null,
            errorConfirmar = if (e.confirmar != e.clave || e.confirmar.isEmpty()) "Las contraseñas no coinciden" else null,
            errorTerminos = if (!e.terminos) "Acepta los términos para continuar" else null,
        )
        val hayErrores = listOf(conErrores.errorNombre, conErrores.errorCorreo, conErrores.errorClave,
            conErrores.errorConfirmar, conErrores.errorTerminos).any { it != null }
        if (hayErrores) {
            estado = conErrores
            return
        }
        estado = e.copy(cargando = true)
        viewModelScope.launch {
            estado = if (auth.correoRegistrado(e.correo)) {
                estado.copy(cargando = false, errorCorreo = "Ya existe una cuenta con este correo")
            } else {
                estado.copy(cargando = false, paso = 2)
            }
        }
    }

    fun crearCuenta(conMeta: Boolean) {
        val e = estado
        val monto = e.metaDigitos.toLongOrNull() ?: 0L
        val guardarMeta = conMeta && (e.metaNombre.isNotBlank() || monto > 0)
        if (guardarMeta && (e.metaNombre.isBlank() || monto <= 0)) {
            estado = e.copy(errorMeta = "Completa el nombre y el monto, o crea la cuenta sin meta")
            return
        }
        estado = e.copy(cargando = true)
        viewModelScope.launch {
            val usuarioId = auth.registrar(e.nombre, e.correo, e.clave)
            if (guardarMeta) {
                finanzas.crearMeta(
                    MetaEntity(usuarioId = usuarioId, nombre = e.metaNombre.trim(), objetivo = monto,
                        fechaLimite = null, icono = iconoSugerido(e.metaNombre), creadaEn = System.currentTimeMillis()),
                )
            }
            sesion.iniciar(usuarioId, recordar = true)
        }
    }
}

@Composable
fun RegistroPantalla(onVolver: () -> Unit) {
    val vm = vistaModelo { c, _ -> RegistroViewModel(c.auth, c.finanzas, c.sesion) }
    val e = vm.estado
    val c = Chavos.colores
    var mostrarTerminos by remember { mutableStateOf(false) }

    BackHandler(enabled = e.paso == 2) { vm.volverAlPaso1() }

    BoxWithConstraints(Modifier.fillMaxSize().background(c.fondo).systemBarsPadding().imePadding()) {
        val altoMinimo = maxHeight
        Column(Modifier.fillMaxSize()) {
            BarraSuperior(
                titulo = "",
                onVolver = { if (e.paso == 2) vm.volverAlPaso1() else onVolver() },
                derecha = {
                    Text("Paso ${e.paso} de 2", style = Texto.etiqueta, color = c.tintaTerciaria, modifier = Modifier.padding(end = 5.dp))
                },
            )
            AnimatedContent(
                targetState = e.paso,
                transitionSpec = {
                    val adelante = targetState > initialState
                    (slideInHorizontally(tween(260)) { if (adelante) it / 6 else -it / 6 } + fadeIn(tween(200))) togetherWith
                        (slideOutHorizontally(tween(200)) { if (adelante) -it / 6 else it / 6 } + fadeOut(tween(150)))
                },
                label = "paso",
            ) { paso ->
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Column(
                        modifier = Modifier.fillMaxWidth().heightIn(min = altoMinimo - 56.dp).padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        if (paso == 1) {
                            Paso1(e, vm, onTerminos = { mostrarTerminos = true }, onIniciarSesion = onVolver)
                        } else {
                            Paso2(e, vm)
                        }
                    }
                }
            }
        }
    }

    if (mostrarTerminos) {
        AlertDialog(
            onDismissRequest = { mostrarTerminos = false },
            confirmButton = { TextButton(onClick = { mostrarTerminos = false; vm.cambiarTerminos(true) }) { Text("Aceptar") } },
            dismissButton = { TextButton(onClick = { mostrarTerminos = false }) { Text("Cerrar") } },
            title = { Text("Términos de uso") },
            text = {
                Text(
                    "Chavos es un proyecto académico. Tus movimientos, metas y tu contraseña cifrada se guardan " +
                        "únicamente en este teléfono; no se envían a ningún servidor. Si desinstalas la app, " +
                        "los datos se borran. Puedes exportar tus movimientos en cualquier momento desde Ajustes.",
                )
            },
            containerColor = c.superficie,
        )
    }
}

@Composable
private fun Paso1(e: RegistroEstado, vm: RegistroViewModel, onTerminos: () -> Unit, onIniciarSesion: () -> Unit) {
    val c = Chavos.colores
    val foco = LocalFocusManager.current
    Column(Modifier.padding(top = 4.dp)) {
        IndicadorPasos(actual = 1, total = 2)
        Spacer(Modifier.height(26.dp))
        Text("Crea tu cuenta", style = Texto.tituloPagina, color = c.tinta)
        Spacer(Modifier.height(10.dp))
        Text("Empieza a ordenar tu dinero hoy mismo.", style = Texto.entradilla, color = c.tintaSecundaria)
        Spacer(Modifier.height(30.dp))
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            CampoTexto(
                valor = e.nombre, onCambio = vm::cambiarNombre, etiqueta = "Nombre completo",
                placeholder = "Como quieres que te saludemos", iconoInicial = R.drawable.ic_user, error = e.errorNombre,
                teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            )
            CampoTexto(
                valor = e.correo, onCambio = vm::cambiarCorreo, etiqueta = "Correo electrónico",
                placeholder = "tucorreo@ejemplo.com", iconoInicial = R.drawable.ic_mail, error = e.errorCorreo,
                teclado = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next, autoCorrectEnabled = false),
            )
            Column {
                CampoClave(valor = e.clave, onCambio = vm::cambiarClave, etiqueta = "Contraseña", error = e.errorClave)
                if (e.clave.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    IndicadorClave(e.clave)
                }
            }
            CampoClave(
                valor = e.confirmar, onCambio = vm::cambiarConfirmar, etiqueta = "Confirmar contraseña",
                // Cierra el teclado para que se vea el error de los términos si aún no se aceptan.
                error = e.errorConfirmar, accionTeclado = ImeAction.Done, onListo = { foco.clearFocus(); vm.continuar() },
                extraFinal = if (e.confirmar.isNotEmpty() && e.confirmar == e.clave) {
                    { Icono(R.drawable.ic_check, tamano = 18.dp, color = c.positivo, descripcion = "Las contraseñas coinciden") }
                } else {
                    null
                },
            )
        }
    }
    Column(Modifier.padding(top = 32.dp, bottom = 34.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column {
            Row(
                modifier = Modifier.toggleable(e.terminos, role = Role.Checkbox, onValueChange = vm::cambiarTerminos),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Casilla(e.terminos, Modifier.padding(top = 1.dp))
                Text(
                    buildAnnotatedString {
                        append("Acepto los ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = c.tinta, textDecoration = TextDecoration.Underline)) {
                            append("términos de uso")
                        }
                        append(" y el tratamiento de mis datos.")
                    },
                    style = Texto.pequeno,
                    color = c.tintaSecundaria,
                    modifier = Modifier.clickable(role = Role.Button, onClick = onTerminos),
                )
            }
            if (e.errorTerminos != null) {
                Spacer(Modifier.height(6.dp))
                Text(e.errorTerminos, style = Texto.leyenda, color = c.negativo, modifier = Modifier.padding(start = 30.dp))
            }
        }
        Boton("Continuar", vm::continuar, cargando = e.cargando)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("¿Ya tienes cuenta? ", style = Texto.cuerpo, color = c.tintaSecundaria)
            Text(
                "Inicia sesión",
                style = Texto.cuerpo.copy(fontWeight = FontWeight.Bold),
                color = c.tinta,
                modifier = Modifier.clickable(role = Role.Button, onClick = onIniciarSesion),
            )
        }
    }
}

@Composable
private fun Paso2(e: RegistroEstado, vm: RegistroViewModel) {
    val c = Chavos.colores
    val transformacion = remember { MilesTransformacion(prefijo = "$") }
    Column(Modifier.padding(top = 4.dp)) {
        IndicadorPasos(actual = 2, total = 2)
        Spacer(Modifier.height(26.dp))
        Text("¿Para qué quieres ahorrar?", style = Texto.tituloPagina, color = c.tinta)
        Spacer(Modifier.height(10.dp))
        Text(
            "Crea tu primera meta y empieza a separar dinero para ella. Puedes cambiarla después.",
            style = Texto.entradilla,
            color = c.tintaSecundaria,
        )
        Spacer(Modifier.height(30.dp))
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            CampoTexto(
                valor = e.metaNombre, onCambio = vm::cambiarMetaNombre, etiqueta = "Nombre de la meta",
                placeholder = "Viaje, portátil, fondo de emergencia…", iconoInicial = R.drawable.ic_target,
                teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            )
            CampoTexto(
                valor = e.metaDigitos, onCambio = vm::cambiarMetaMonto, etiqueta = "¿Cuánto necesitas?",
                placeholder = "$0", iconoInicial = R.drawable.ic_coin, error = e.errorMeta,
                teclado = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                transformacion = transformacion,
            )
        }
    }
    Column(Modifier.padding(top = 32.dp, bottom = 34.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Boton("Crear cuenta", { vm.crearCuenta(conMeta = true) }, cargando = e.cargando)
        Boton("Omitir por ahora", { vm.crearCuenta(conMeta = false) }, tipo = TipoBoton.Secundario, habilitado = !e.cargando)
    }
}
