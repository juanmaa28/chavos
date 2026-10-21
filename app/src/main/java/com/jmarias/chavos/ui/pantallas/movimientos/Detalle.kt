package com.jmarias.chavos.ui.pantallas.movimientos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.jmarias.chavos.R
import com.jmarias.chavos.data.FinanzasRepository
import com.jmarias.chavos.data.local.MovimientoEntity
import com.jmarias.chavos.dominio.Calculos
import com.jmarias.chavos.dominio.Formato
import com.jmarias.chavos.ui.componentes.BarraSuperior
import com.jmarias.chavos.ui.componentes.Boton
import com.jmarias.chavos.ui.componentes.FilaDato
import com.jmarias.chavos.ui.componentes.IconoCategoria
import com.jmarias.chavos.ui.componentes.Tarjeta
import com.jmarias.chavos.ui.componentes.TipoBoton
import com.jmarias.chavos.ui.componentes.Tono
import com.jmarias.chavos.ui.componentes.icono
import com.jmarias.chavos.ui.componentes.montoConSigno
import com.jmarias.chavos.ui.componentes.titulo
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.Texto
import com.jmarias.chavos.ui.vistaModelo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetalleEstado(
    val movimiento: MovimientoEntity? = null,
    val nombreMeta: String? = null,
    val cargando: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleViewModel(private val finanzas: FinanzasRepository, guardado: SavedStateHandle) : ViewModel() {
    private val id: Long = checkNotNull(guardado.get<Long>("id"))

    val estado: StateFlow<DetalleEstado> = finanzas.movimiento(id).flatMapLatest { m ->
        val metaId = m?.metaId
        if (metaId == null) {
            flowOf(DetalleEstado(m, null, cargando = false))
        } else {
            finanzas.meta(metaId).map { meta -> DetalleEstado(m, meta?.nombre, cargando = false) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleEstado())

    fun eliminar(alTerminar: () -> Unit) {
        viewModelScope.launch {
            finanzas.eliminarMovimiento(id)
            alTerminar()
        }
    }
}

@Composable
fun DetallePantalla(onVolver: () -> Unit, onEditar: (Long) -> Unit) {
    val vm = vistaModelo { c, s -> DetalleViewModel(c.finanzas, s) }
    val e by vm.estado.collectAsStateWithLifecycle()
    val c = Chavos.colores
    var confirmarEliminar by remember { mutableStateOf(false) }
    var eliminando by remember { mutableStateOf(false) }

    // Si el movimiento desaparece (lo borraron), no hay nada que mostrar.
    LaunchedEffect(e.cargando, e.movimiento) {
        if (!e.cargando && e.movimiento == null && !eliminando) onVolver()
    }

    Column(Modifier.fillMaxSize().background(c.fondo).statusBarsPadding()) {
        BarraSuperior("Detalle", onVolver)
        val m = e.movimiento ?: return@Column
        val fecha = Calculos.fecha(m.fecha)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Column(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                IconoCategoria(m.categoria.icono, 46.dp, Tono.Suave)
                Spacer(Modifier.height(16.dp))
                BasicText(
                    text = m.montoConSigno(),
                    style = Texto.cifra.copy(color = if (m.esIngreso) c.positivo else c.negativo, textAlign = TextAlign.Center),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(minFontSize = 24.sp, maxFontSize = 40.sp, stepSize = 2.sp),
                )
                Spacer(Modifier.height(10.dp))
                Text(m.titulo(), style = Texto.fila16, color = c.tinta, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text("${Formato.fechaLarga(fecha)}, ${Formato.hora(m.fecha)}", style = Texto.leyenda, color = c.tintaTerciaria)
            }
            Spacer(Modifier.height(26.dp))
            Tarjeta(relleno = PaddingValues(vertical = 2.dp)) {
                FilaDato("Tipo", if (m.esIngreso) "Ingreso" else "Gasto", conDivisor = false)
                FilaDato("Categoría", m.categoria.etiqueta, conDivisor = true)
                FilaDato("Fecha", Formato.fechaCompleta(fecha), conDivisor = true)
                if (e.nombreMeta != null) FilaDato("Meta", e.nombreMeta!!, conDivisor = true)
            }
            Spacer(Modifier.height(24.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Boton("Editar", { onEditar(m.id) }, Modifier.weight(1f), tipo = TipoBoton.Secundario, icono = R.drawable.ic_edit)
            Boton("Eliminar", { confirmarEliminar = true }, Modifier.weight(1f), tipo = TipoBoton.Peligro, icono = R.drawable.ic_trash)
        }
    }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("¿Eliminar este movimiento?") },
            text = { Text("Se quitará de tus movimientos y de los totales del mes. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    eliminando = true
                    vm.eliminar(onVolver)
                }) { Text("Eliminar", color = c.negativo) }
            },
            dismissButton = { TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") } },
            containerColor = c.superficie,
        )
    }
}
