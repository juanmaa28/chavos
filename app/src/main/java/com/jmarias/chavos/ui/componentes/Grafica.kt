package com.jmarias.chavos.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jmarias.chavos.dominio.Formato
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.Texto

data class BarraMes(val etiqueta: String, val valor: Long)

/**
 * Barras de gasto por mes: el último mes en negro, los anteriores en gris y el promedio como
 * línea punteada. Tocar una barra muestra su valor; por defecto se rotula el mes actual.
 */
@Composable
fun GraficaMensual(barras: List<BarraMes>, promedio: Long?, modifier: Modifier = Modifier) {
    val c = Chavos.colores
    val medidor = rememberTextMeasurer()
    var seleccion by remember(barras) { mutableIntStateOf(barras.lastIndex) }
    val descripcion = barras.joinToString { "${it.etiqueta}: ${Formato.pesos(it.valor)}" } +
        (promedio?.let { ". Promedio ${Formato.pesos(it)}" } ?: "")

    Column(modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(116.dp)
                .semantics { contentDescription = descripcion }
                .pointerInput(barras) {
                    detectTapGestures { toque ->
                        if (barras.isEmpty()) return@detectTapGestures
                        val hueco = 12.dp.toPx()
                        val ancho = (size.width - hueco * (barras.size - 1)) / barras.size
                        seleccion = (toque.x / (ancho + hueco)).toInt().coerceIn(0, barras.lastIndex)
                    }
                },
        ) {
            if (barras.isEmpty()) return@Canvas
            val n = barras.size
            val espacioEtiqueta = 20.dp.toPx()
            val area = size.height - espacioEtiqueta
            val maximo = maxOf(barras.maxOf { it.valor }, promedio ?: 0L, 1L).toFloat()
            val hueco = 12.dp.toPx()
            val ancho = (size.width - hueco * (n - 1)) / n
            val radio = CornerRadius(4.dp.toPx())

            fun alto(valor: Long) = valor / maximo * area

            barras.forEachIndexed { i, barra ->
                val h = alto(barra.valor)
                if (h <= 0f) return@forEachIndexed
                val x = i * (ancho + hueco)
                val ruta = Path().apply {
                    addRoundRect(RoundRect(Rect(x, size.height - h, x + ancho, size.height), topLeft = radio, topRight = radio))
                }
                drawPath(ruta, if (i == n - 1) c.tinta else c.barraGrafica)
            }

            if (promedio != null && promedio > 0) {
                val y = size.height - alto(promedio)
                drawLine(
                    color = c.lineaPromedio,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
                )
            }

            val elegida = barras[seleccion]
            val texto = medidor.measure(Formato.compacto(elegida.valor), Texto.grafica.copy(color = c.tinta))
            val centro = seleccion * (ancho + hueco) + ancho / 2
            val x = (centro - texto.size.width / 2f).coerceIn(0f, size.width - texto.size.width)
            val y = (size.height - alto(elegida.valor) - texto.size.height - 4.dp.toPx()).coerceAtLeast(0f)
            drawText(texto, topLeft = Offset(x, y))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            barras.forEachIndexed { i, barra ->
                val actual = i == barras.lastIndex
                Text(
                    barra.etiqueta,
                    style = if (actual) Texto.eje.copy(fontWeight = FontWeight.Bold) else Texto.eje,
                    color = if (actual) c.tinta else c.tintaTerciaria,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
