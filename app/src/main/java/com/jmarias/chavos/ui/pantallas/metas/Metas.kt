package com.jmarias.chavos.ui.pantallas.metas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.jmarias.chavos.R
import com.jmarias.chavos.data.FinanzasRepository
import com.jmarias.chavos.data.SesionRepository
import com.jmarias.chavos.data.local.IconoMeta
import com.jmarias.chavos.data.local.MetaConAhorro
import com.jmarias.chavos.data.local.MetaEntity
import com.jmarias.chavos.dominio.Formato
import com.jmarias.chavos.dominio.iconoSugerido
import com.jmarias.chavos.ui.componentes.Asa
import com.jmarias.chavos.ui.componentes.BarraProgreso
import com.jmarias.chavos.ui.componentes.Boton
import com.jmarias.chavos.ui.componentes.BotonCerrar
import com.jmarias.chavos.ui.componentes.BotonIcono
import com.jmarias.chavos.ui.componentes.CampoTexto
import com.jmarias.chavos.ui.componentes.Chevron
import com.jmarias.chavos.ui.componentes.ColumnaPantalla
import com.jmarias.chavos.ui.componentes.EstadoVacio
import com.jmarias.chavos.ui.componentes.FilaAjuste
import com.jmarias.chavos.ui.componentes.IconoCategoria
import com.jmarias.chavos.ui.componentes.MilesTransformacion
import com.jmarias.chavos.ui.componentes.Tarjeta
import com.jmarias.chavos.ui.componentes.TarjetaMeta
import com.jmarias.chavos.ui.componentes.TipoBoton
import com.jmarias.chavos.ui.componentes.Tono
import com.jmarias.chavos.ui.componentes.ValorAjuste
import com.jmarias.chavos.ui.componentes.icono
import com.jmarias.chavos.ui.componentes.limpiarDigitos
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.Texto
import com.jmarias.chavos.ui.vistaModelo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Estado de la pantalla de metas de ahorro. */
data class MetasEstado(
    val metas: List<MetaConAhorro> = emptyList(),
    val totalAhorrado: Long = 0,
    val totalObjetivo: Long = 0,
    val cargando: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class MetasViewModel(private val sesion: SesionRepository, private val finanzas: FinanzasRepository) : ViewModel() {
    val estado: StateFlow<MetasEstado> = sesion.usuarioId.filterNotNull()
        .flatMapLatest { finanzas.metas(it) }
        .map { metas ->
            MetasEstado(
                metas = metas,
                totalAhorrado = metas.sumOf { minOf(it.ahorrado, it.meta.objetivo) },
                totalObjetivo = metas.sumOf { it.meta.objetivo },
                cargando = false,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MetasEstado())

    fun crear(nombre: String, objetivo: Long, fechaLimite: LocalDate?, icono: IconoMeta) {
        viewModelScope.launch {
            val usuarioId = sesion.usuarioId.filterNotNull().first()
            finanzas.crearMeta(
                MetaEntity(usuarioId = usuarioId, nombre = nombre.trim(), objetivo = objetivo,
                    fechaLimite = fechaLimite?.toEpochDay(), icono = icono, creadaEn = System.currentTimeMillis()),
            )
        }
    }

    fun eliminar(id: Long) {
        viewModelScope.launch { finanzas.eliminarMeta(id) }
    }
}

@Composable
fun MetasPantalla(onAportar: (Long) -> Unit) {
    val vm = vistaModelo { c, _ -> MetasViewModel(c.sesion, c.finanzas) }
    val e by vm.estado.collectAsStateWithLifecycle()
    val c = Chavos.colores
    var creando by rememberSaveable { mutableStateOf(false) }
    var elegidaId by rememberSaveable { mutableStateOf<Long?>(null) }

    ColumnaPantalla {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Metas", style = Texto.tituloGrande, color = c.tinta)
            BotonIcono(R.drawable.ic_plus, "Crear meta", { creando = true })
        }

        if (!e.cargando && e.metas.isEmpty()) {
            EstadoVacio(
                titulo = "Aún no tienes metas",
                descripcion = "Define para qué estás ahorrando y Chavos te dirá cuánto separar cada mes.",
                accion = "Crear meta",
                onAccion = { creando = true },
            )
        } else if (e.metas.isNotEmpty()) {
            Text("Llevas ahorrado", style = Texto.meta, color = c.tintaTerciaria)
            Spacer(Modifier.height(6.dp))
            Text(Formato.pesos(e.totalAhorrado), style = Texto.cifra, color = c.tinta, maxLines = 1)
            Spacer(Modifier.height(9.dp))
            val reparto = if (e.metas.size == 1) "en tu meta" else "entre ${e.metas.size} metas"
            Text("de ${Formato.pesos(e.totalObjetivo)} $reparto", style = Texto.nota, color = c.tintaSecundaria)
            Spacer(Modifier.height(14.dp))
            BarraProgreso(
                fraccion = if (e.totalObjetivo == 0L) 0f else e.totalAhorrado.toFloat() / e.totalObjetivo,
                alto = 8.dp,
                pista = c.pistaSobreFondo,
            )
            Spacer(Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                e.metas.forEach { meta -> TarjetaMeta(meta, onClick = { elegidaId = meta.meta.id }) }
            }
        }
        Spacer(Modifier.height(28.dp))
    }

    if (creando) {
        HojaNuevaMeta(
            onCerrar = { creando = false },
            onCrear = { nombre, objetivo, fecha, icono ->
                vm.crear(nombre, objetivo, fecha, icono)
                creando = false
            },
        )
    }

    val elegida = e.metas.firstOrNull { it.meta.id == elegidaId }
    if (elegida != null) {
        HojaMeta(
            meta = elegida,
            onCerrar = { elegidaId = null },
            onAportar = { elegidaId = null; onAportar(elegida.meta.id) },
            onEliminar = { elegidaId = null; vm.eliminar(elegida.meta.id) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaNuevaMeta(onCerrar: () -> Unit, onCrear: (String, Long, LocalDate?, IconoMeta) -> Unit) {
    val c = Chavos.colores
    var nombre by rememberSaveable { mutableStateOf("") }
    var digitos by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf<Long?>(null) }
    var icono by rememberSaveable { mutableStateOf<IconoMeta?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var elegirFecha by remember { mutableStateOf(false) }
    val transformacion = remember { MilesTransformacion(prefijo = "$") }
    val iconoVisible = icono ?: iconoSugerido(nombre)

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.superficie,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        dragHandle = { Asa() },
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).navigationBarsPadding().imePadding().padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Nueva meta", style = Texto.tituloHoja, color = c.tinta)
                BotonCerrar(onCerrar)
            }
            CampoTexto(
                valor = nombre, onCambio = { nombre = it; error = null }, etiqueta = "Nombre",
                placeholder = "Viaje, portátil, fondo de emergencia…",
                teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            )
            CampoTexto(
                valor = digitos, onCambio = { digitos = limpiarDigitos(it); error = null }, etiqueta = "Monto objetivo",
                placeholder = "$0", transformacion = transformacion, error = error,
                teclado = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            )
            Column {
                Text("Ícono", style = Texto.etiqueta, color = c.tintaTerciaria)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    IconoMeta.entries.forEach { opcion ->
                        val activa = opcion == iconoVisible
                        Column(
                            Modifier.clip(RoundedCornerShape(10.dp)).selectable(activa, role = Role.RadioButton) { icono = opcion },
                        ) {
                            IconoCategoria(opcion.icono, 40.dp, if (activa) Tono.Oscuro else Tono.Suave)
                        }
                    }
                }
            }
            Tarjeta(relleno = PaddingValues(vertical = 2.dp)) {
                FilaAjuste(R.drawable.ic_calendar, "Fecha límite", conDivisor = false, onClick = { elegirFecha = true }) {
                    ValorAjuste(fecha?.let { Formato.fechaCorta(LocalDate.ofEpochDay(it)) } ?: "Sin fecha")
                    Chevron()
                }
            }
            Boton("Crear meta", {
                val objetivo = digitos.toLongOrNull() ?: 0L
                when {
                    nombre.isBlank() -> error = "Ponle un nombre a la meta"
                    objetivo <= 0 -> error = "Escribe cuánto necesitas"
                    else -> onCrear(nombre, objetivo, fecha?.let(LocalDate::ofEpochDay), iconoVisible)
                }
            })
        }
    }

    if (elegirFecha) {
        val estadoFecha = rememberDatePickerState(
            initialSelectedDateMillis = (fecha?.let(LocalDate::ofEpochDay) ?: LocalDate.now().plusMonths(3))
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { elegirFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoFecha.selectedDateMillis?.let { fecha = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay() }
                    elegirFecha = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { fecha = null; elegirFecha = false }) { Text("Sin fecha") }
            },
        ) {
            DatePicker(state = estadoFecha)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaMeta(meta: MetaConAhorro, onCerrar: () -> Unit, onAportar: () -> Unit, onEliminar: () -> Unit) {
    val c = Chavos.colores
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val alcance = rememberCoroutineScope()
    var confirmar by remember { mutableStateOf(false) }

    fun cerrarY(accion: () -> Unit) {
        alcance.launch { estadoHoja.hide() }.invokeOnCompletion { accion() }
    }

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = estadoHoja,
        containerColor = c.superficie,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        dragHandle = { Asa() },
    ) {
        Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding().padding(bottom = 24.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    IconoCategoria(meta.meta.icono.icono, 40.dp)
                    Text(meta.meta.nombre, style = Texto.tituloHoja, color = c.tinta, maxLines = 2)
                }
                BotonCerrar(onCerrar)
            }
            Spacer(Modifier.height(18.dp))
            BarraProgreso(meta.fraccion, alto = 8.dp)
            Spacer(Modifier.height(10.dp))
            Text(
                "${Formato.pesos(meta.ahorrado)} de ${Formato.pesos(meta.meta.objetivo)} (${meta.porcentaje} %)",
                style = Texto.nota,
                color = c.tintaSecundaria,
            )
            Spacer(Modifier.height(24.dp))
            Boton("Aportar a esta meta", { cerrarY(onAportar) }, icono = R.drawable.ic_plus)
            Spacer(Modifier.height(10.dp))
            Boton("Eliminar meta", { confirmar = true }, tipo = TipoBoton.Peligro, icono = R.drawable.ic_trash)
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("¿Eliminar esta meta?") },
            text = { Text("Los aportes que hiciste se conservan en tus movimientos como gastos de la categoría Metas.") },
            confirmButton = {
                TextButton(onClick = { confirmar = false; cerrarY(onEliminar) }) { Text("Eliminar", color = c.negativo) }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } },
            containerColor = c.superficie,
        )
    }
}
