package com.jmarias.chavos.ui.componentes

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jmarias.chavos.R
import com.jmarias.chavos.dominio.Validacion
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.Texto

/** Campo con etiqueta arriba y solo línea inferior: gris en reposo, negra con foco, roja con error. */
@Composable
fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    @DrawableRes iconoInicial: Int? = null,
    final: (@Composable () -> Unit)? = null,
    error: String? = null,
    teclado: KeyboardOptions = KeyboardOptions.Default,
    acciones: KeyboardActions = KeyboardActions.Default,
    transformacion: VisualTransformation = VisualTransformation.None,
    unaLinea: Boolean = true,
) {
    val c = Chavos.colores
    val interaccion = remember { MutableInteractionSource() }
    val enfocado by interaccion.collectIsFocusedAsState()
    val linea by animateColorAsState(
        when {
            error != null -> c.negativo
            enfocado -> c.tinta
            else -> c.bordeCampo
        },
        tween(150), label = "linea",
    )
    Column(modifier.fillMaxWidth()) {
        Text(etiqueta, style = Texto.etiqueta, color = c.tintaTerciaria)
        Spacer(Modifier.height(9.dp))
        BasicTextField(
            value = valor,
            onValueChange = onCambio,
            singleLine = unaLinea,
            textStyle = Texto.campo.copy(color = c.tinta),
            cursorBrush = SolidColor(c.tinta),
            keyboardOptions = teclado,
            keyboardActions = acciones,
            visualTransformation = transformacion,
            interactionSource = interaccion,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = etiqueta },
            decorationBox = { campo ->
                Column {
                    Row(
                        modifier = Modifier.heightIn(min = 32.dp).padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (iconoInicial != null) Icono(iconoInicial, tamano = 18.dp, color = c.iconoCampo)
                        Box(Modifier.weight(1f)) {
                            if (valor.isEmpty() && placeholder != null) {
                                Text(placeholder, style = Texto.campo, color = c.tintaTerciaria, maxLines = 1)
                            }
                            campo()
                        }
                        final?.invoke()
                    }
                    Box(Modifier.fillMaxWidth().height(1.5.dp).background(linea))
                }
            },
        )
        if (error != null) {
            Spacer(Modifier.height(6.dp))
            Text(error, style = Texto.leyenda, color = c.negativo)
        }
    }
}

@Composable
fun CampoClave(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    accionTeclado: ImeAction = ImeAction.Next,
    onListo: () -> Unit = {},
    extraFinal: (@Composable () -> Unit)? = null,
) {
    val c = Chavos.colores
    var visible by rememberSaveable { mutableStateOf(false) }
    CampoTexto(
        valor = valor,
        onCambio = onCambio,
        etiqueta = etiqueta,
        modifier = modifier,
        error = error,
        teclado = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = accionTeclado, autoCorrectEnabled = false),
        acciones = if (accionTeclado == ImeAction.Done) KeyboardActions(onDone = { onListo() }) else KeyboardActions.Default,
        transformacion = if (visible) VisualTransformation.None else PasswordVisualTransformation('•'),
        final = {
            extraFinal?.invoke()
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    // Sin foco de teclado: "Siguiente" debe saltar al próximo campo, no a este botón.
                    .focusProperties { canFocus = false }
                    .clickable(role = Role.Button) { visible = !visible }
                    .semantics { contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña" },
                contentAlignment = Alignment.Center,
            ) {
                Icono(if (visible) R.drawable.ic_eye_off else R.drawable.ic_eye, tamano = 18.dp, color = c.tintaTerciaria)
            }
        },
    )
}

/**
 * Agrupa los dígitos con puntos de miles mientras se escribe ("1250000" → "1.250.000"),
 * con prefijo y marcador opcionales cuando el campo está vacío.
 */
class MilesTransformacion(
    private val prefijo: String = "",
    private val estiloPrefijo: SpanStyle? = null,
    private val marcador: String? = null,
    private val estiloMarcador: SpanStyle? = null,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text
        val p = prefijo.length
        if (digitos.isEmpty()) {
            if (p == 0 && marcador == null) return TransformedText(text, OffsetMapping.Identity)
            val vacio = buildAnnotatedString {
                agregar(prefijo, estiloPrefijo)
                if (marcador != null) agregar(marcador, estiloMarcador)
            }
            return TransformedText(vacio, object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = p
                override fun transformedToOriginal(offset: Int) = 0
            })
        }
        val agrupado = StringBuilder()
        val posiciones = IntArray(digitos.length)
        digitos.forEachIndexed { i, ch ->
            if (i > 0 && (digitos.length - i) % 3 == 0) agrupado.append('.')
            posiciones[i] = agrupado.length
            agrupado.append(ch)
        }
        val transformado = buildAnnotatedString {
            agregar(prefijo, estiloPrefijo)
            append(agrupado.toString())
        }
        return TransformedText(transformado, object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset <= 0) p else p + posiciones[(offset - 1).coerceAtMost(digitos.length - 1)] + 1

            override fun transformedToOriginal(offset: Int): Int {
                val o = offset - p
                return if (o <= 0) 0 else posiciones.count { it < o }
            }
        })
    }

    private fun AnnotatedString.Builder.agregar(texto: String, estilo: SpanStyle?) {
        if (texto.isEmpty()) return
        if (estilo != null) withStyle(estilo) { append(texto) } else append(texto)
    }

    override fun equals(other: Any?): Boolean =
        other is MilesTransformacion && other.prefijo == prefijo && other.marcador == marcador &&
            other.estiloPrefijo == estiloPrefijo && other.estiloMarcador == estiloMarcador

    override fun hashCode(): Int = listOf(prefijo, marcador, estiloPrefijo, estiloMarcador).hashCode()
}

/** Solo dígitos, sin ceros a la izquierda y como máximo 12 cifras. */
fun limpiarDigitos(texto: String): String = texto.filter(Char::isDigit).trimStart('0').take(12)

/** Monto grande y centrado para el formulario de movimiento. */
@Composable
fun CampoMonto(digitos: String, onCambio: (String) -> Unit, enfocarAlIniciar: Boolean, modifier: Modifier = Modifier) {
    val c = Chavos.colores
    val foco = remember { FocusRequester() }
    val transformacion = remember(c) {
        MilesTransformacion(
            prefijo = "$",
            estiloPrefijo = SpanStyle(color = c.tintaTerciaria, fontSize = 26.sp),
            marcador = "0",
            estiloMarcador = SpanStyle(color = c.pistaClave),
        )
    }
    LaunchedEffect(Unit) { if (enfocarAlIniciar) foco.requestFocus() }
    BasicTextField(
        value = digitos,
        onValueChange = { onCambio(limpiarDigitos(it)) },
        singleLine = true,
        textStyle = Texto.montoEntrada.copy(color = c.tinta, textAlign = TextAlign.Center),
        cursorBrush = SolidColor(c.tinta),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        visualTransformation = transformacion,
        modifier = modifier.fillMaxWidth().focusRequester(foco).semantics { contentDescription = "Monto" },
    )
}

/** Tres segmentos y una etiqueta con la fortaleza de la contraseña. */
@Composable
fun IndicadorClave(clave: String) {
    val c = Chavos.colores
    val puntos = Validacion.fortalezaClave(clave)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i < puntos) c.tintaSecundaria else c.pistaClave),
                )
            }
        }
        Text(Validacion.etiquetaFortaleza(puntos), style = Texto.porcentaje, color = c.tintaSecundaria)
    }
}

/** Indicador de pasos del registro. */
@Composable
fun IndicadorPasos(actual: Int, total: Int) {
    val c = Chavos.colores
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i < actual) c.tinta else c.pistaPaso),
            )
        }
    }
}
