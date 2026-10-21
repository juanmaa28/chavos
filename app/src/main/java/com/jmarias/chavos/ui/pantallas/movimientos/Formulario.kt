package com.jmarias.chavos.ui.pantallas.movimientos

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmarias.chavos.R
import com.jmarias.chavos.data.FinanzasRepository
import com.jmarias.chavos.data.SesionRepository
import com.jmarias.chavos.data.local.Categoria
import com.jmarias.chavos.data.local.MetaEntity
import com.jmarias.chavos.data.local.MovimientoEntity
import com.jmarias.chavos.data.local.TipoMovimiento
import com.jmarias.chavos.dominio.Calculos
import com.jmarias.chavos.dominio.Formato
import com.jmarias.chavos.ui.componentes.BarraSuperior
import com.jmarias.chavos.ui.componentes.Boton
import com.jmarias.chavos.ui.componentes.CampoMonto
import com.jmarias.chavos.ui.componentes.CampoTexto
import com.jmarias.chavos.ui.componentes.Chevron
import com.jmarias.chavos.ui.componentes.Chip
import com.jmarias.chavos.ui.componentes.FilaAjuste
import com.jmarias.chavos.ui.componentes.IconoCategoria
import com.jmarias.chavos.ui.componentes.Segmentado
import com.jmarias.chavos.ui.componentes.Tarjeta
import com.jmarias.chavos.ui.componentes.Tono
import com.jmarias.chavos.ui.componentes.ValorAjuste
import com.jmarias.chavos.ui.componentes.icono
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.Texto
import com.jmarias.chavos.ui.vistaModelo
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

data class FormularioEstado(
    val listo: Boolean = false,
    val usuarioId: Long = 0,
    val id: Long = 0,
    val tipo: TipoMovimiento = TipoMovimiento.GASTO,
    val digitos: String = "",
    val categoria: Categoria? = null,
    val metaId: Long? = null,
    val fecha: LocalDate = LocalDate.now(),
    val horaOriginal: LocalTime? = null,
    val nota: String = "",
    val metas: List<MetaEntity> = emptyList(),
    val soloAporte: Boolean = false,
    val errorMonto: String? = null,
    val errorCategoria: String? = null,
    val errorMeta: String? = null,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
) {
    val esEdicion: Boolean get() = id != 0L
}

class FormularioViewModel(
    private val sesion: SesionRepository,
    private val finanzas: FinanzasRepository,
    private val argumentos: SavedStateHandle,
) : ViewModel() {
    var estado by mutableStateOf(FormularioEstado())
        private set

    init {
        viewModelScope.launch {
            val usuarioId = sesion.usuarioId.filterNotNull().first()
            val metas = finanzas.todasLasMetas(usuarioId)
            val id = argumentos.get<Long>("id") ?: 0L
            val existente = if (id != 0L) finanzas.cargarMovimiento(id) else null
            estado = if (existente != null) {
                val zona = ZoneId.systemDefault()
                FormularioEstado(
                    listo = true,
                    usuarioId = usuarioId,
                    id = existente.id,
                    tipo = existente.tipo,
                    digitos = existente.monto.toString(),
                    categoria = existente.categoria,
                    metaId = existente.metaId,
                    fecha = Calculos.fecha(existente.fecha, zona),
                    horaOriginal = Instant.ofEpochMilli(existente.fecha).atZone(zona).toLocalTime(),
                    nota = existente.nota,
                    metas = metas,
                )
            } else {
                val tipo = argumentos.get<String>("tipo")?.let(TipoMovimiento::valueOf) ?: TipoMovimiento.GASTO
                val categoria = argumentos.get<String>("categoria")?.takeIf { it.isNotEmpty() }?.let(Categoria::valueOf)
                val meta = argumentos.get<Long>("meta")?.takeIf { it > 0 }
                    ?: if (categoria == Categoria.METAS) metas.singleOrNull()?.id else null
                FormularioEstado(
                    listo = true,
                    usuarioId = usuarioId,
                    tipo = tipo,
                    categoria = categoria,
                    metaId = meta,
                    metas = metas,
                    soloAporte = categoria == Categoria.METAS,
                )
            }
        }
    }

    fun cambiarTipo(tipo: TipoMovimiento) {
        estado = estado.copy(
            tipo = tipo,
            categoria = estado.categoria?.takeIf { it.tipo == tipo },
            metaId = if (tipo == TipoMovimiento.INGRESO) null else estado.metaId,
            errorCategoria = null,
        )
    }

    fun cambiarMonto(digitos: String) { estado = estado.copy(digitos = digitos, errorMonto = null) }

    fun cambiarCategoria(categoria: Categoria) {
        estado = estado.copy(
            categoria = categoria,
            metaId = if (categoria == Categoria.METAS) estado.metaId ?: estado.metas.singleOrNull()?.id else null,
            errorCategoria = null,
            errorMeta = null,
        )
    }

    fun cambiarMeta(id: Long) { estado = estado.copy(metaId = id, errorMeta = null) }
    fun cambiarFecha(fecha: LocalDate) { estado = estado.copy(fecha = fecha) }
    fun cambiarNota(nota: String) { estado = estado.copy(nota = nota) }

    fun guardar() {
        val e = estado
        val monto = e.digitos.toLongOrNull() ?: 0L
        val conErrores = e.copy(
            errorMonto = if (monto <= 0) "Escribe un monto mayor a cero" else null,
            errorCategoria = if (e.categoria == null) "Elige una categoría" else null,
            errorMeta = if (e.categoria == Categoria.METAS && e.metaId == null) "Elige la meta a la que va este aporte" else null,
        )
        if (conErrores.errorMonto != null || conErrores.errorCategoria != null || conErrores.errorMeta != null) {
            estado = conErrores
            return
        }
        val categoria = e.categoria ?: return
        val hora = e.horaOriginal ?: if (e.fecha == LocalDate.now()) LocalTime.now() else LocalTime.NOON
        // Un aporte sin nota se titula con su meta; así se reconoce en la lista aunque la meta se borre después.
        val nota = e.nota.trim().ifEmpty {
            if (categoria == Categoria.METAS) e.metas.firstOrNull { it.id == e.metaId }?.let { "Aporte a ${it.nombre}" }.orEmpty() else ""
        }
        estado = e.copy(guardando = true)
        viewModelScope.launch {
            finanzas.guardar(
                MovimientoEntity(
                    id = e.id,
                    usuarioId = e.usuarioId,
                    tipo = categoria.tipo,
                    monto = monto,
                    categoria = categoria,
                    fecha = e.fecha.atTime(hora).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    nota = nota,
                    metaId = if (categoria == Categoria.METAS) e.metaId else null,
                ),
            )
            estado = estado.copy(guardando = false, guardado = true)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioPantalla(onCerrar: () -> Unit) {
    val vm = vistaModelo { c, s -> FormularioViewModel(c.sesion, c.finanzas, s) }
    val e = vm.estado
    val c = Chavos.colores
    var elegirFecha by remember { mutableStateOf(false) }

    LaunchedEffect(e.guardado) { if (e.guardado) onCerrar() }

    val titulo = when {
        e.esEdicion -> "Editar movimiento"
        e.soloAporte -> "Aporte a una meta"
        e.tipo == TipoMovimiento.INGRESO -> "Nuevo ingreso"
        else -> "Nuevo gasto"
    }
    val textoBoton = when {
        e.esEdicion -> "Guardar cambios"
        e.soloAporte -> "Guardar aporte"
        e.tipo == TipoMovimiento.INGRESO -> "Guardar ingreso"
        else -> "Guardar gasto"
    }

    Column(Modifier.fillMaxSize().background(c.fondo).statusBarsPadding().imePadding()) {
        BarraSuperior(titulo, onCerrar, icono = R.drawable.ic_x, descripcion = "Cerrar")
        if (!e.listo) return@Column

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            if (!e.soloAporte) {
                Spacer(Modifier.height(4.dp))
                Segmentado(
                    opciones = listOf("Gasto", "Ingreso"),
                    seleccion = if (e.tipo == TipoMovimiento.GASTO) 0 else 1,
                    onSeleccion = { vm.cambiarTipo(if (it == 0) TipoMovimiento.GASTO else TipoMovimiento.INGRESO) },
                )
            }
            Spacer(Modifier.height(24.dp))
            Text("Monto", style = Texto.etiqueta, color = c.tintaTerciaria, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            CampoMonto(e.digitos, vm::cambiarMonto, enfocarAlIniciar = !e.esEdicion)
            Text(
                e.errorMonto ?: "Pesos colombianos",
                style = Texto.leyenda,
                color = if (e.errorMonto != null) c.negativo else c.tintaTerciaria,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            if (!e.soloAporte) {
                Spacer(Modifier.height(28.dp))
                Text("Categoría", style = Texto.etiqueta, color = c.tintaTerciaria)
                Spacer(Modifier.height(12.dp))
                RejillaCategorias(Categoria.de(e.tipo), e.categoria, vm::cambiarCategoria)
                if (e.errorCategoria != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(e.errorCategoria, style = Texto.leyenda, color = c.negativo)
                }
            }

            if (e.categoria == Categoria.METAS) {
                Spacer(Modifier.height(24.dp))
                Text("Meta", style = Texto.etiqueta, color = c.tintaTerciaria)
                Spacer(Modifier.height(10.dp))
                if (e.metas.isEmpty()) {
                    Text("Aún no tienes metas. Créalas en la pestaña Metas.", style = Texto.nota, color = c.tintaSecundaria)
                } else {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        e.metas.forEach { meta -> Chip(meta.nombre, meta.id == e.metaId) { vm.cambiarMeta(meta.id) } }
                    }
                }
                if (e.errorMeta != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(e.errorMeta, style = Texto.leyenda, color = c.negativo)
                }
            }

            Spacer(Modifier.height(24.dp))
            Tarjeta(relleno = PaddingValues(vertical = 2.dp)) {
                FilaAjuste(R.drawable.ic_calendar, "Fecha", conDivisor = false, onClick = { elegirFecha = true }) {
                    ValorAjuste(Formato.fechaCorta(e.fecha))
                    Chevron()
                }
            }
            Spacer(Modifier.height(22.dp))
            CampoTexto(
                valor = e.nota,
                onCambio = vm::cambiarNota,
                etiqueta = "Nota",
                placeholder = when {
                    e.categoria == Categoria.METAS -> "Ej. ahorro de la quincena"
                    e.tipo == TipoMovimiento.INGRESO -> "Ej. pago de la quincena"
                    else -> "Ej. mercado quincenal"
                },
                teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            )
            Spacer(Modifier.height(24.dp))
        }

        Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)) {
            Boton(textoBoton, vm::guardar, cargando = e.guardando)
        }
    }

    if (elegirFecha) {
        val estadoFecha = rememberDatePickerState(
            initialSelectedDateMillis = e.fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { elegirFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoFecha.selectedDateMillis?.let { vm.cambiarFecha(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                    elegirFecha = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { elegirFecha = false }) { Text("Cancelar") } },
        ) {
            DatePicker(state = estadoFecha)
        }
    }
}

@Composable
private fun RejillaCategorias(categorias: List<Categoria>, seleccion: Categoria?, onElegir: (Categoria) -> Unit) {
    val c = Chavos.colores
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        categorias.chunked(4).forEach { fila ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fila.forEach { categoria ->
                    val activa = categoria == seleccion
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .selectable(selected = activa, role = Role.RadioButton) { onElegir(categoria) }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        IconoCategoria(categoria.icono, 46.dp, if (activa) Tono.Oscuro else Tono.Suave)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            categoria.corta,
                            style = Texto.categoriaCorta,
                            color = if (activa) c.tinta else c.tintaTerciaria,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                repeat(4 - fila.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
