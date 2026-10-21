package com.jmarias.chavos.ui.componentes

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jmarias.chavos.R
import com.jmarias.chavos.dominio.Formato
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.Texto
import java.time.YearMonth

data class Pestana(val ruta: String, val etiqueta: String, @DrawableRes val icono: Int)

/** Barra inferior con cuatro pestañas y el botón + al centro, que sobresale 18 dp. */
@Composable
fun BarraPestanas(pestanas: List<Pestana>, actual: String?, onSeleccion: (String) -> Unit, onRegistrar: () -> Unit) {
    val c = Chavos.colores
    Column(Modifier.fillMaxWidth().background(c.superficie)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.bordeBarra))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(66.dp)
                .padding(start = 12.dp, end = 12.dp, top = 11.dp),
            verticalAlignment = Alignment.Top,
        ) {
            pestanas.take(2).forEach { ItemPestana(it, it.ruta == actual) { onSeleccion(it.ruta) } }
            Box(Modifier.width(62.dp), contentAlignment = Alignment.TopCenter) {
                Box(
                    modifier = Modifier
                        .offset(y = (-29).dp)
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(c.tinta)
                        .clickable(role = Role.Button, onClick = onRegistrar)
                        .semantics { contentDescription = "Registrar movimiento" },
                    contentAlignment = Alignment.Center,
                ) {
                    Icono(R.drawable.ic_plus, tamano = 24.dp, color = c.tintaInversa)
                }
            }
            pestanas.drop(2).forEach { ItemPestana(it, it.ruta == actual) { onSeleccion(it.ruta) } }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ItemPestana(pestana: Pestana, activa: Boolean, onClick: () -> Unit) {
    val c = Chavos.colores
    val color = if (activa) c.tinta else c.lineaPromedio
    Column(
        modifier = Modifier.weight(1f).selectable(selected = activa, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icono(pestana.icono, tamano = 24.dp, color = color)
        Spacer(Modifier.height(4.dp))
        Text(pestana.etiqueta, style = Texto.pestana, color = color, maxLines = 1)
        if (activa) {
            Spacer(Modifier.height(3.dp))
            Box(Modifier.size(4.dp).clip(CircleShape).background(c.tinta))
        }
    }
}

@Composable
fun BarraSuperior(
    titulo: String,
    onVolver: () -> Unit,
    @DrawableRes icono: Int = R.drawable.ic_chevron_left,
    descripcion: String = "Volver",
    derecha: @Composable () -> Unit = { Spacer(Modifier.size(48.dp)) },
) {
    val c = Chavos.colores
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BotonIcono(icono, descripcion, onVolver)
        Text(titulo, style = Texto.tituloTarjeta, color = c.tinta, textAlign = TextAlign.Center, maxLines = 1, modifier = Modifier.weight(1f))
        derecha()
    }
}

@Composable
fun SelectorMes(mes: YearMonth, onAnterior: () -> Unit, onSiguiente: () -> Unit, puedeAvanzar: Boolean) {
    val c = Chavos.colores
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onAnterior).semantics { contentDescription = "Mes anterior" },
            contentAlignment = Alignment.Center,
        ) { Icono(R.drawable.ic_chevron_left, tamano = 16.dp, color = c.tintaTerciaria) }
        Text(Formato.mesAnio(mes), style = Texto.mes, color = c.tinta)
        Box(
            Modifier.size(40.dp).clip(CircleShape)
                .clickable(enabled = puedeAvanzar, role = Role.Button, onClick = onSiguiente)
                .semantics { contentDescription = "Mes siguiente" },
            contentAlignment = Alignment.Center,
        ) {
            Icono(R.drawable.ic_chevron_right, tamano = 16.dp, color = if (puedeAvanzar) c.tintaTerciaria else c.pistaClave)
        }
    }
}
