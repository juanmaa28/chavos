package com.jmarias.chavos.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jmarias.chavos.R
import com.jmarias.chavos.data.local.MetaConAhorro
import com.jmarias.chavos.dominio.Calculos
import com.jmarias.chavos.dominio.Formato
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.Texto
import java.time.LocalDate

/** Tarjeta negra de saldo: el único bloque oscuro grande de la app. Tocarla abre el informe. */
@Composable
fun TarjetaSaldo(etiqueta: String, saldo: Long, ingresos: Long, gastos: Long, onInforme: () -> Unit) {
    val c = Chavos.colores
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.heroFondo)
            .clickable(role = Role.Button, onClickLabel = "Ver informe", onClick = onInforme)
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 18.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(etiqueta, style = Texto.meta, color = c.heroTintaSuave)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Informe", style = Texto.etiqueta, color = c.heroTinta)
                Icono(R.drawable.ic_chevron_right, tamano = 14.dp, color = c.heroTinta)
            }
        }
        Spacer(Modifier.height(14.dp))
        BasicText(
            text = Formato.pesos(saldo),
            style = Texto.montoHero.copy(color = c.heroTinta),
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 26.sp, maxFontSize = 48.sp, stepSize = 2.sp),
        )
        Spacer(Modifier.height(22.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.heroDivisor))
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth()) {
            DatoSaldo("Ingresos", ingresos, Modifier.weight(1f))
            DatoSaldo("Gastos", gastos, Modifier.weight(1f))
        }
    }
}

@Composable
private fun DatoSaldo(etiqueta: String, valor: Long, modifier: Modifier) {
    val c = Chavos.colores
    Column(modifier) {
        Text(etiqueta, style = Texto.heroDivision, color = c.heroTintaSuave)
        Spacer(Modifier.height(4.dp))
        Text(Formato.pesos(valor), style = Texto.valor16, color = c.heroTinta, maxLines = 1)
    }
}

@Composable
fun TarjetaMeta(meta: MetaConAhorro, onClick: () -> Unit, hoy: LocalDate = LocalDate.now()) {
    val c = Chavos.colores
    val fecha = meta.meta.fechaLimite?.let(LocalDate::ofEpochDay)
    val cumplida = meta.ahorrado >= meta.meta.objetivo
    val derecha = when {
        cumplida -> "Meta cumplida"
        fecha == null -> "Aporte libre"
        else -> Calculos.aporteMensual(meta.meta.objetivo, meta.ahorrado, fecha, hoy)?.let { "${Formato.pesos(it)} al mes" } ?: ""
    }
    Tarjeta(relleno = PaddingValues(16.dp), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconoCategoria(meta.meta.icono.icono, 40.dp)
            Column(Modifier.weight(1f)) {
                Text(meta.meta.nombre, style = Texto.fila15, color = c.tinta, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(fecha?.let(Formato::plazo) ?: "Sin fecha límite", style = Texto.leyenda, color = c.tintaTerciaria)
            }
            Text("${meta.porcentaje} %", style = Texto.seccion, color = c.tinta)
        }
        Spacer(Modifier.height(16.dp))
        BarraProgreso(meta.fraccion, alto = 8.dp)
        Spacer(Modifier.height(11.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = c.tinta)) { append(Formato.pesos(meta.ahorrado)) }
                    withStyle(SpanStyle(color = c.tintaTerciaria)) { append(" de ${Formato.pesos(meta.meta.objetivo)}") }
                },
                style = Texto.nota,
            )
            Text(derecha, style = Texto.nota, color = if (cumplida) c.positivo else c.tintaTerciaria)
        }
    }
}
