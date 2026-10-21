package com.jmarias.chavos.ui.pantallas.informe

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.jmarias.chavos.data.FinanzasRepository
import com.jmarias.chavos.data.SesionRepository
import com.jmarias.chavos.data.local.Categoria
import com.jmarias.chavos.dominio.Calculos
import com.jmarias.chavos.dominio.Formato
import com.jmarias.chavos.ui.componentes.BarraMes
import com.jmarias.chavos.ui.componentes.BarraSuperior
import com.jmarias.chavos.ui.componentes.EstadoVacio
import com.jmarias.chavos.ui.componentes.FilaCategoria
import com.jmarias.chavos.ui.componentes.GraficaMensual
import com.jmarias.chavos.ui.componentes.SelectorMes
import com.jmarias.chavos.ui.componentes.Tarjeta
import com.jmarias.chavos.ui.componentes.icono
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
import java.time.YearMonth
import kotlin.math.abs

data class CategoriaResumen(val categoria: Categoria, val total: Long, val fraccion: Float, val porcentaje: Int)

data class InformeEstado(
    val mes: YearMonth = YearMonth.now(),
    val total: Long = 0,
    val variacion: Int? = null,
    val barras: List<BarraMes> = emptyList(),
    val promedio: Long? = null,
    val categorias: List<CategoriaResumen> = emptyList(),
    val cargando: Boolean = true,
) {
    val puedeAvanzar: Boolean get() = mes < YearMonth.now()
}

@OptIn(ExperimentalCoroutinesApi::class)
class InformeViewModel(sesion: SesionRepository, finanzas: FinanzasRepository) : ViewModel() {
    private val mes = MutableStateFlow(YearMonth.now())

    val estado: StateFlow<InformeEstado> = combine(
        sesion.usuarioId.filterNotNull().flatMapLatest { finanzas.movimientos(it) },
        mes,
    ) { movimientos, mesActual ->
        val porMes = Calculos.gastosPorMes(movimientos, mesActual)
        val total = porMes.last().second
        val promedio = Calculos.promedio(porMes.map { it.second })
        val porCategoria = Calculos.gastosPorCategoria(Calculos.delMes(movimientos, mesActual))
        val mayor = porCategoria.firstOrNull()?.second ?: 0L
        InformeEstado(
            mes = mesActual,
            total = total,
            variacion = if (porMes.count { it.second > 0 } >= 2) Calculos.variacion(total, promedio) else null,
            barras = porMes.map { (m, v) -> BarraMes(Formato.mesCorto(m), v) },
            promedio = promedio,
            categorias = porCategoria.map { (categoria, monto) ->
                CategoriaResumen(
                    categoria = categoria,
                    total = monto,
                    fraccion = if (mayor == 0L) 0f else monto.toFloat() / mayor,
                    porcentaje = if (total == 0L) 0 else (monto * 100.0 / total).toInt(),
                )
            },
            cargando = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InformeEstado())

    fun mesAnterior() = mes.update { it.minusMonths(1) }
    fun mesSiguiente() = mes.update { if (it < YearMonth.now()) it.plusMonths(1) else it }
}

@Composable
fun InformePantalla(onVolver: () -> Unit) {
    val vm = vistaModelo { c, _ -> InformeViewModel(c.sesion, c.finanzas) }
    val e by vm.estado.collectAsStateWithLifecycle()
    val c = Chavos.colores

    Column(Modifier.fillMaxSize().background(c.fondo).statusBarsPadding()) {
        BarraSuperior("Informe", onVolver)
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 20.dp),
        ) {
            SelectorMes(e.mes, vm::mesAnterior, vm::mesSiguiente, e.puedeAvanzar)
            Spacer(Modifier.height(14.dp))
            Text("Gastaste", style = Texto.meta, color = c.tintaTerciaria)
            Spacer(Modifier.height(6.dp))
            BasicText(
                text = Formato.pesos(e.total),
                style = Texto.cifra.copy(color = c.tinta),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 24.sp, maxFontSize = 40.sp, stepSize = 2.sp),
            )
            Spacer(Modifier.height(9.dp))
            Text(textoVariacion(e.variacion), style = Texto.nota, color = c.tintaSecundaria)
            Spacer(Modifier.height(18.dp))

            Tarjeta(relleno = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 14.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text("Últimos seis meses", style = Texto.tituloTarjeta, color = c.tinta)
                    if (e.promedio != null) {
                        Text("Promedio ${Formato.compacto(e.promedio!!)}", style = Texto.heroDivision, color = c.tintaTerciaria)
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (!e.cargando && e.barras.all { it.valor == 0L }) {
                    Text(
                        "Cuando registres gastos, aquí verás cuánto gastas cada mes.",
                        style = Texto.nota,
                        color = c.tintaSecundaria,
                        modifier = Modifier.padding(vertical = 20.dp),
                    )
                } else {
                    GraficaMensual(e.barras, e.promedio)
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Por categoría", style = Texto.subtitulo, color = c.tinta)
            Spacer(Modifier.height(10.dp))
            if (!e.cargando && e.categorias.isEmpty()) {
                EstadoVacio(
                    titulo = "Sin gastos en ${Formato.mes(e.mes)}",
                    descripcion = "Cuando registres gastos, aquí verás en qué se va tu dinero.",
                )
            } else {
                Tarjeta(relleno = PaddingValues(vertical = 2.dp)) {
                    e.categorias.forEachIndexed { i, cat ->
                        FilaCategoria(
                            icono = cat.categoria.icono,
                            nombre = cat.categoria.etiqueta,
                            monto = Formato.pesos(cat.total),
                            fraccion = cat.fraccion,
                            porcentaje = "${cat.porcentaje} %",
                            conDivisor = i > 0,
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun textoVariacion(variacion: Int?): String = when {
    variacion == null -> "Aún no hay meses anteriores para comparar"
    variacion == 0 -> "Igual que tu promedio de seis meses"
    variacion > 0 -> "${abs(variacion)} % por encima de tu promedio de seis meses"
    else -> "${abs(variacion)} % por debajo de tu promedio de seis meses"
}
