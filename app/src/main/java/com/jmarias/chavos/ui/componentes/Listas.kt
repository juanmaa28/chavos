package com.jmarias.chavos.ui.componentes

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jmarias.chavos.R
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.Texto

@Composable
fun FilaMovimiento(
    @DrawableRes icono: Int,
    titulo: String,
    subtitulo: String,
    monto: String,
    esIngreso: Boolean,
    conDivisor: Boolean,
    onClick: () -> Unit,
) {
    val c = Chavos.colores
    Column(Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick)) {
        if (conDivisor) Divisor(Modifier.padding(horizontal = 16.dp))
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconoCategoria(icono, 40.dp)
            Column(Modifier.weight(1f)) {
                Text(titulo, style = Texto.fila15, color = c.tinta, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(subtitulo, style = Texto.leyenda, color = c.tintaTerciaria, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(monto, style = Texto.fila15, color = if (esIngreso) c.positivo else c.negativo, maxLines = 1, softWrap = false)
        }
    }
}

@Composable
fun FilaAjuste(
    @DrawableRes icono: Int,
    etiqueta: String,
    conDivisor: Boolean,
    modifier: Modifier = Modifier,
    tono: Tono = Tono.Suave,
    onClick: (() -> Unit)? = null,
    final: @Composable RowScope.() -> Unit = {},
) {
    val c = Chavos.colores
    Column(modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)) {
        if (conDivisor) Divisor(Modifier.padding(horizontal = 16.dp))
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp).heightIn(min = 34.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconoCategoria(icono, 34.dp, tono)
            Text(
                etiqueta,
                style = Texto.fila15,
                color = if (tono == Tono.Peligro) c.negativo else c.tinta,
                modifier = Modifier.weight(1f),
            )
            final()
        }
    }
}

@Composable
fun ValorAjuste(texto: String) {
    Text(texto, style = Texto.cuerpo, color = Chavos.colores.tintaTerciaria)
}

@Composable
fun EncabezadoSeccion(titulo: String, accion: String? = null, onAccion: () -> Unit = {}) {
    val c = Chavos.colores
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(titulo, style = Texto.seccion, color = c.tinta)
        if (accion != null) {
            Row(
                modifier = Modifier.clickable(role = Role.Button, onClick = onAccion).padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(accion, style = Texto.enlace13, color = c.tinta)
                Icono(R.drawable.ic_chevron_right, tamano = 14.dp, color = c.tinta)
            }
        }
    }
}

@Composable
fun FilaCategoria(
    @DrawableRes icono: Int,
    nombre: String,
    monto: String,
    fraccion: Float,
    porcentaje: String,
    conDivisor: Boolean,
) {
    val c = Chavos.colores
    Column(Modifier.fillMaxWidth()) {
        if (conDivisor) Divisor(Modifier.padding(horizontal = 16.dp))
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconoCategoria(icono, 36.dp)
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(nombre, style = Texto.categoria, color = c.tinta, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text(monto, style = Texto.categoria, color = c.tinta, maxLines = 1)
                }
                Spacer(Modifier.height(7.dp))
                BarraProgreso(fraccion)
            }
            Text(porcentaje, style = Texto.porcentaje, color = c.tintaTerciaria, textAlign = TextAlign.End, modifier = Modifier.width(38.dp))
        }
    }
}

/** Fila etiqueta / valor para Detalle y Créditos. */
@Composable
fun FilaDato(etiqueta: String, valor: String, conDivisor: Boolean, @DrawableRes icono: Int? = null) {
    val c = Chavos.colores
    Column(Modifier.fillMaxWidth()) {
        if (conDivisor) Divisor(Modifier.padding(horizontal = 16.dp))
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (icono != null) Icono(icono, tamano = 16.dp, color = c.tintaSecundaria, modifier = Modifier.padding(top = 1.dp))
            Text(etiqueta, style = Texto.etiqueta, color = c.tintaTerciaria, modifier = Modifier.padding(top = 1.dp))
            Text(valor, style = Texto.categoria, color = c.tinta, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun EtiquetaGrupo(texto: String, modifier: Modifier = Modifier) {
    Text(texto, style = Texto.etiqueta, color = Chavos.colores.tintaTerciaria, modifier = modifier.padding(start = 2.dp, bottom = 8.dp))
}

/** Tarjeta para listas vacías: explica qué falta y ofrece la acción para resolverlo. */
@Composable
fun EstadoVacio(titulo: String, descripcion: String, accion: String? = null, onAccion: () -> Unit = {}) {
    val c = Chavos.colores
    Tarjeta(relleno = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 24.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(titulo, style = Texto.tituloTarjeta, color = c.tinta, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(descripcion, style = Texto.nota, color = c.tintaSecundaria, textAlign = TextAlign.Center)
            if (accion != null) {
                Spacer(Modifier.height(16.dp))
                Boton(accion, onAccion, tipo = TipoBoton.Secundario)
            }
        }
    }
}
