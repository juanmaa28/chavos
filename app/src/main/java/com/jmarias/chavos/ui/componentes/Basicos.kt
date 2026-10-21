package com.jmarias.chavos.ui.componentes

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jmarias.chavos.R
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.Texto

@Composable
fun Icono(
    @DrawableRes id: Int,
    modifier: Modifier = Modifier,
    tamano: Dp = 20.dp,
    color: Color = LocalContentColor.current,
    descripcion: String? = null,
) {
    Icon(painter = painterResource(id), contentDescription = descripcion, modifier = modifier.size(tamano), tint = color)
}

enum class TipoBoton { Principal, Secundario, Peligro }

/** Botón de 52 dp. Se encoge al 97 % mientras se presiona para confirmar el toque. */
@Composable
fun Boton(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tipo: TipoBoton = TipoBoton.Principal,
    @DrawableRes icono: Int? = null,
    habilitado: Boolean = true,
    cargando: Boolean = false,
) {
    val c = Chavos.colores
    val forma = RoundedCornerShape(8.dp)
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(if (presionado) 0.97f else 1f, tween(120), label = "escala")
    val fondo = if (tipo == TipoBoton.Principal) c.tinta else Color.Transparent
    val tinta = when (tipo) {
        TipoBoton.Principal -> c.tintaInversa
        TipoBoton.Secundario -> c.tinta
        TipoBoton.Peligro -> c.negativo
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .graphicsLayer { scaleX = escala; scaleY = escala }
            .clip(forma)
            .background(fondo)
            .then(if (tipo != TipoBoton.Principal) Modifier.border(1.5.dp, c.bordeCampo, forma) else Modifier)
            .alpha(if (habilitado) 1f else 0.4f)
            .clickable(
                interactionSource = interaccion,
                indication = ripple(color = tinta),
                enabled = habilitado && !cargando,
                role = Role.Button,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (cargando) {
            CircularProgressIndicator(Modifier.size(18.dp), color = tinta, strokeWidth = 2.dp)
        } else if (icono != null) {
            Icono(icono, tamano = 20.dp, color = tinta)
        }
        Text(texto, style = Texto.boton, color = tinta)
    }
}

/** Botón cuadrado de 38 dp con borde; su área táctil se amplía a 48 dp. */
@Composable
fun BotonIcono(
    @DrawableRes icono: Int,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Chavos.colores
    val forma = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(38.dp)
            .clip(forma)
            .background(c.superficie)
            .border(1.dp, c.bordeTarjeta, forma)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center,
    ) {
        Icono(icono, tamano = 20.dp, color = c.tinta)
    }
}

/** Botón circular gris con una X, para cerrar hojas. */
@Composable
fun BotonCerrar(onClick: () -> Unit, descripcion: String = "Cerrar") {
    val c = Chavos.colores
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(32.dp)
            .clip(CircleShape)
            .background(c.superficieSuave)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center,
    ) {
        Icono(R.drawable.ic_x, tamano = 16.dp, color = c.tintaSecundaria)
    }
}

@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    relleno: PaddingValues = PaddingValues(0.dp),
    onClick: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    val c = Chavos.colores
    val forma = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(c.superficie)
            .border(1.dp, c.bordeTarjeta, forma)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(relleno),
        content = contenido,
    )
}

@Composable
fun Divisor(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(Chavos.colores.divisor))
}

@Composable
fun Chevron(tamano: Dp = 16.dp) {
    Icono(R.drawable.ic_chevron_right, tamano = tamano, color = Chavos.colores.chevron)
}

@Composable
fun Avatar(iniciales: String, tamano: Dp = 38.dp, modifier: Modifier = Modifier) {
    val c = Chavos.colores
    val estilo = if (tamano >= 46.dp) Texto.subtitulo else Texto.enlace135.copy(fontWeight = Texto.insignia.fontWeight)
    Box(
        // Recorta antes del modificador externo para que el efecto de toque respete las esquinas.
        modifier = Modifier.size(tamano).clip(RoundedCornerShape(8.dp)).then(modifier).background(c.tinta),
        contentAlignment = Alignment.Center,
    ) {
        Text(iniciales, style = estilo, color = c.tintaInversa)
    }
}

/** Casilla de 20 dp (solo visual; el toque lo maneja la fila que la contiene). */
@Composable
fun Casilla(marcada: Boolean, modifier: Modifier = Modifier) {
    val c = Chavos.colores
    val forma = RoundedCornerShape(5.dp)
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(forma)
            .background(if (marcada) c.tinta else Color.Transparent)
            .then(if (marcada) Modifier else Modifier.border(1.5.dp, c.bordeCampo, forma)),
        contentAlignment = Alignment.Center,
    ) {
        if (marcada) Icono(R.drawable.ic_check, tamano = 14.dp, color = c.tintaInversa)
    }
}

/** Interruptor de 44 × 26 dp (solo visual; el toque lo maneja la fila que lo contiene). */
@Composable
fun Interruptor(activo: Boolean, modifier: Modifier = Modifier) {
    val c = Chavos.colores
    val posicion by animateDpAsState(if (activo) 21.dp else 3.dp, tween(180), label = "perilla")
    val pista by animateColorAsState(if (activo) c.tinta else c.pistaClave, tween(180), label = "pista")
    Box(modifier.size(44.dp, 26.dp).clip(RoundedCornerShape(13.dp)).background(pista)) {
        Box(
            Modifier
                .offset(x = posicion, y = 3.dp)
                .size(20.dp)
                .shadow(1.5.dp, CircleShape)
                .background(if (activo) c.tintaInversa else Color.White, CircleShape),
        )
    }
}

@Composable
fun Insignia(texto: String) {
    val c = Chavos.colores
    Box(
        Modifier.height(22.dp).widthIn(min = 22.dp).clip(RoundedCornerShape(11.dp)).background(c.tinta).padding(horizontal = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(texto, style = Texto.insignia, color = c.tintaInversa)
    }
}

@Composable
fun BarraProgreso(fraccion: Float, modifier: Modifier = Modifier, alto: Dp = 6.dp, pista: Color = Chavos.colores.pista) {
    val c = Chavos.colores
    val forma = RoundedCornerShape(alto / 2)
    Box(modifier.fillMaxWidth().height(alto).clip(forma).background(pista)) {
        if (fraccion > 0f) {
            Box(Modifier.fillMaxWidth(fraccion.coerceIn(0f, 1f)).fillMaxHeight().clip(forma).background(c.tinta))
        }
    }
}

enum class Tono { Suave, Oscuro, Peligro }

@Composable
fun IconoCategoria(
    @DrawableRes icono: Int,
    tamano: Dp = 40.dp,
    tono: Tono = Tono.Suave,
    descripcion: String? = null,
) {
    val c = Chavos.colores
    val fondo by animateColorAsState(
        when (tono) { Tono.Suave -> c.superficieSuave; Tono.Oscuro -> c.tinta; Tono.Peligro -> c.negativoSuave },
        tween(160), label = "fondo",
    )
    val tinta = when (tono) { Tono.Suave -> c.tintaSecundaria; Tono.Oscuro -> c.tintaInversa; Tono.Peligro -> c.negativo }
    val radio = if (tamano >= 46.dp) 10.dp else 8.dp
    val tamanoIcono = when { tamano >= 46.dp -> 22.dp; tamano >= 40.dp -> 20.dp; else -> 18.dp }
    Box(Modifier.size(tamano).clip(RoundedCornerShape(radio)).background(fondo), contentAlignment = Alignment.Center) {
        Icono(icono, tamano = tamanoIcono, color = tinta, descripcion = descripcion)
    }
}

/** Isotipo de tres monedas; se dibuja con los colores del tema para funcionar en claro y oscuro. */
@Composable
fun LogoChavos(tamano: Dp = 34.dp) {
    val c = Chavos.colores
    Canvas(Modifier.size(tamano)) {
        val u = size.width / 34f
        drawCircle(c.logoMoneda, radius = 9f * u, center = Offset(11f * u, 22f * u))
        drawCircle(c.tinta, radius = 9f * u, center = Offset(19f * u, 15f * u))
        drawCircle(c.superficie, radius = 6.2f * u, center = Offset(24f * u, 9f * u))
        drawCircle(c.tinta, radius = 6.2f * u, center = Offset(24f * u, 9f * u), style = Stroke(width = 1.6f * u))
    }
}

/** Filtro tipo pastilla: negro cuando está activo. */
@Composable
fun Chip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    val c = Chavos.colores
    val forma = RoundedCornerShape(8.dp)
    val fondo by animateColorAsState(if (seleccionado) c.tinta else c.superficie, tween(160), label = "chip")
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(forma)
            .background(fondo)
            .border(1.dp, if (seleccionado) c.tinta else c.bordeTarjeta, forma)
            .selectable(selected = seleccionado, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(texto, style = Texto.enlace13, color = if (seleccionado) c.tintaInversa else c.tinta)
    }
}

/** Control segmentado (Gasto | Ingreso). */
@Composable
fun Segmentado(opciones: List<String>, seleccion: Int, onSeleccion: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = Chavos.colores
    val forma = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(forma)
            .background(c.superficie)
            .border(1.dp, c.bordeTarjeta, forma)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        opciones.forEachIndexed { i, texto ->
            val activo = i == seleccion
            val fondo by animateColorAsState(if (activo) c.tinta else Color.Transparent, tween(160), label = "segmento")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(7.dp))
                    .background(fondo)
                    .selectable(selected = activo, role = Role.Tab) { onSeleccion(i) },
                contentAlignment = Alignment.Center,
            ) {
                Text(texto, style = Texto.enlace135, color = if (activo) c.tintaInversa else c.tintaSecundaria, textAlign = TextAlign.Center)
            }
        }
    }
}

/** Asa de las hojas inferiores. */
@Composable
fun Asa() {
    Box(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 16.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(38.dp, 5.dp).clip(RoundedCornerShape(3.dp)).background(Chavos.colores.asa))
    }
}
