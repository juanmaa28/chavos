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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.jmarias.chavos.data.FinanzasRepository
import com.jmarias.chavos.data.SesionRepository
import com.jmarias.chavos.data.local.MovimientoEntity
import com.jmarias.chavos.dominio.Calculos
import com.jmarias.chavos.dominio.Formato
import com.jmarias.chavos.dominio.ResumenMes
import com.jmarias.chavos.ui.componentes.Chip
import com.jmarias.chavos.ui.componentes.EstadoVacio
import com.jmarias.chavos.ui.componentes.FilaMovimiento
import com.jmarias.chavos.ui.componentes.SelectorMes
import com.jmarias.chavos.ui.componentes.Tarjeta
import com.jmarias.chavos.ui.componentes.icono
import com.jmarias.chavos.ui.componentes.montoConSigno
import com.jmarias.chavos.ui.componentes.titulo
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.Texto
import com.jmarias.chavos.ui.vistaModelo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

enum class Filtro(val etiqueta: String) { Todos("Todos"), Ingresos("Ingresos"), Gastos("Gastos") }

data class GrupoDia(val fecha: LocalDate, val neto: Long, val movimientos: List<MovimientoEntity>)

data class MovimientosEstado(
    val mes: YearMonth = YearMonth.now(),
    val filtro: Filtro = Filtro.Todos,
    val resumen: ResumenMes = ResumenMes(),
    val grupos: List<GrupoDia> = emptyList(),
    val cargando: Boolean = true,
) {
    val puedeAvanzar: Boolean get() = mes < YearMonth.now()
}

@OptIn(ExperimentalCoroutinesApi::class)
class MovimientosViewModel(sesion: SesionRepository, finanzas: FinanzasRepository) : ViewModel() {
    private val mes = MutableStateFlow(YearMonth.now())
    private val filtro = MutableStateFlow(Filtro.Todos)

    val estado: StateFlow<MovimientosEstado> = combine(
        sesion.usuarioId.filterNotNull().flatMapLatest { finanzas.movimientos(it) },
        mes,
        filtro,
    ) { movimientos, mesActual, filtroActual ->
        val delMes = Calculos.delMes(movimientos, mesActual)
        val visibles = when (filtroActual) {
            Filtro.Todos -> delMes
            Filtro.Ingresos -> delMes.filter { it.esIngreso }
            Filtro.Gastos -> delMes.filterNot { it.esIngreso }
        }
        val grupos = visibles.groupBy { Calculos.fecha(it.fecha) }
            .toSortedMap(compareByDescending { it })
            .map { (fecha, lista) -> GrupoDia(fecha, lista.sumOf { if (it.esIngreso) it.monto else -it.monto }, lista) }
        MovimientosEstado(mesActual, filtroActual, Calculos.resumen(delMes), grupos, cargando = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MovimientosEstado())

    fun mesAnterior() = mes.update { it.minusMonths(1) }
    fun mesSiguiente() = mes.update { if (it < YearMonth.now()) it.plusMonths(1) else it }
    fun filtrar(nuevo: Filtro) { filtro.value = nuevo }
}

@Composable
fun MovimientosPantalla(onMovimiento: (Long) -> Unit, onRegistrar: () -> Unit) {
    val vm = vistaModelo { c, _ -> MovimientosViewModel(c.sesion, c.finanzas) }
    val e by vm.estado.collectAsStateWithLifecycle()
    val c = Chavos.colores
    val hoy = LocalDate.now()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(c.fondo).statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
    ) {
        item(key = "titulo") {
            Text("Movimientos", style = Texto.tituloGrande, color = c.tinta, modifier = Modifier.padding(top = 6.dp, bottom = 14.dp))
        }
        item(key = "mes") {
            SelectorMes(e.mes, vm::mesAnterior, vm::mesSiguiente, e.puedeAvanzar)
            Spacer(Modifier.height(12.dp))
        }
        item(key = "resumen") {
            Tarjeta(relleno = PaddingValues(16.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    Resumen("Ingresos", Formato.pesos(e.resumen.ingresos), Modifier.weight(1f))
                    Resumen("Gastos", Formato.pesos(e.resumen.gastos), Modifier.weight(1f))
                    Resumen("Balance", Formato.pesos(e.resumen.saldo), Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(14.dp))
        }
        item(key = "filtros") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Filtro.entries.forEach { f -> Chip(f.etiqueta, f == e.filtro) { vm.filtrar(f) } }
            }
            Spacer(Modifier.height(20.dp))
        }
        if (!e.cargando && e.grupos.isEmpty()) {
            item(key = "vacio") {
                val texto = when (e.filtro) {
                    Filtro.Todos -> "movimientos"
                    Filtro.Ingresos -> "ingresos"
                    Filtro.Gastos -> "gastos"
                }
                EstadoVacio(
                    titulo = "Sin $texto en ${Formato.mes(e.mes)}",
                    descripcion = "Cuando registres uno, aparecerá aquí agrupado por día.",
                    accion = if (e.mes == YearMonth.now()) "Registrar movimiento" else null,
                    onAccion = onRegistrar,
                )
            }
        }
        e.grupos.forEach { grupo ->
            item(key = "dia-${grupo.fecha}") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 2.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(Formato.encabezadoDia(grupo.fecha, hoy), style = Texto.etiqueta, color = c.tintaSecundaria)
                    Text(
                        Formato.pesosConSigno(kotlin.math.abs(grupo.neto), grupo.neto >= 0),
                        style = Texto.leyenda,
                        color = c.tintaTerciaria,
                    )
                }
                Tarjeta(relleno = PaddingValues(vertical = 2.dp)) {
                    grupo.movimientos.forEachIndexed { i, m ->
                        FilaMovimiento(
                            icono = m.categoria.icono,
                            titulo = m.titulo(),
                            subtitulo = if (m.nota.isBlank()) Formato.hora(m.fecha) else "${m.categoria.etiqueta}, ${Formato.hora(m.fecha)}",
                            monto = m.montoConSigno(),
                            esIngreso = m.esIngreso,
                            conDivisor = i > 0,
                            onClick = { onMovimiento(m.id) },
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun Resumen(etiqueta: String, valor: String, modifier: Modifier) {
    val c = Chavos.colores
    Column(modifier) {
        Text(etiqueta, style = Texto.etiqueta, color = c.tintaTerciaria)
        Spacer(Modifier.height(4.dp))
        Text(valor, style = Texto.categoria, color = c.tinta, maxLines = 1)
    }
}
