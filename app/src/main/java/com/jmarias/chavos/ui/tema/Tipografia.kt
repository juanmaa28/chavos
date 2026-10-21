package com.jmarias.chavos.ui.tema

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jmarias.chavos.R

val SchibstedGrotesk = FontFamily(
    Font(R.font.schibsted_grotesk_regular, FontWeight.Normal),
    Font(R.font.schibsted_grotesk_medium, FontWeight.Medium),
    Font(R.font.schibsted_grotesk_semibold, FontWeight.SemiBold),
    Font(R.font.schibsted_grotesk_bold, FontWeight.Bold),
)

private fun estilo(tamano: Float, peso: FontWeight, interlineado: Float? = null, letras: Float = 0f) = TextStyle(
    fontFamily = SchibstedGrotesk,
    fontWeight = peso,
    fontSize = tamano.sp,
    lineHeight = interlineado?.sp ?: TextUnit.Unspecified,
    letterSpacing = letras.em,
)

/** Estilos de texto de la especificación de Monocromo (sección 4). */
object Texto {
    val montoHero = estilo(48f, FontWeight.Bold, 48f, -0.045f)
    val titularLogin = estilo(46f, FontWeight.Bold, 45f, -0.05f)
    val montoEntrada = estilo(44f, FontWeight.Bold, 48f, -0.035f)
    val cifra = estilo(40f, FontWeight.Bold, 40f, -0.045f)
    val tituloPagina = estilo(34f, FontWeight.Bold, 35f, -0.045f)
    val tituloGrande = estilo(30f, FontWeight.Bold, letras = -0.04f)
    val saludo = estilo(26f, FontWeight.Bold, letras = -0.035f)
    // Con interlineado propio: sin él heredan los 24 sp de bodyLarge, que MaterialTheme pone por defecto.
    val tituloHoja = estilo(24f, FontWeight.Bold, 28f, -0.03f)
    val marca = estilo(20f, FontWeight.Bold, letras = -0.02f)
    val seccion = estilo(18f, FontWeight.Bold, letras = -0.02f)
    val subtitulo = estilo(16f, FontWeight.Bold, letras = -0.01f)
    val tituloTarjeta = estilo(15f, FontWeight.Bold)
    val fila16 = estilo(16f, FontWeight.SemiBold, letras = -0.01f)
    val fila15 = estilo(15f, FontWeight.SemiBold, letras = -0.01f)
    val valor16 = estilo(16f, FontWeight.SemiBold, letras = -0.01f)
    val campo = estilo(16f, FontWeight.Normal)
    val entradilla = estilo(15f, FontWeight.Normal, 22.5f)
    val boton = estilo(15.5f, FontWeight.SemiBold)
    val categoria = estilo(14f, FontWeight.SemiBold, letras = -0.01f)
    val mes = estilo(14f, FontWeight.Bold)
    val cuerpo = estilo(14f, FontWeight.Normal, 20f)
    val enlace135 = estilo(13.5f, FontWeight.SemiBold)
    val medio135 = estilo(13.5f, FontWeight.Medium)
    val pequeno = estilo(13.5f, FontWeight.Normal, 20f)
    val nota = estilo(13f, FontWeight.Normal, 18f)
    val enlace13 = estilo(13f, FontWeight.SemiBold)
    val etiqueta = estilo(12.5f, FontWeight.SemiBold)
    val meta = estilo(12.5f, FontWeight.Medium)
    val leyenda = estilo(12f, FontWeight.Normal)
    val porcentaje = estilo(12f, FontWeight.SemiBold)
    val insignia = estilo(12f, FontWeight.Bold)
    val heroDivision = estilo(11.5f, FontWeight.Medium)
    val grafica = estilo(11.5f, FontWeight.Bold)
    val categoriaCorta = estilo(11.5f, FontWeight.SemiBold)
    val eje = estilo(11f, FontWeight.Medium)
    val pestana = estilo(10.5f, FontWeight.SemiBold)
}

/** Tipografía para los componentes de Material (diálogos, selector de fecha, avisos). */
val TipografiaMaterial: Typography = Typography().run {
    Typography(
        displayLarge = displayLarge.copy(fontFamily = SchibstedGrotesk),
        displayMedium = displayMedium.copy(fontFamily = SchibstedGrotesk),
        displaySmall = displaySmall.copy(fontFamily = SchibstedGrotesk),
        headlineLarge = headlineLarge.copy(fontFamily = SchibstedGrotesk),
        headlineMedium = headlineMedium.copy(fontFamily = SchibstedGrotesk),
        headlineSmall = Texto.tituloHoja,
        titleLarge = Texto.tituloHoja,
        titleMedium = titleMedium.copy(fontFamily = SchibstedGrotesk),
        titleSmall = titleSmall.copy(fontFamily = SchibstedGrotesk),
        bodyLarge = bodyLarge.copy(fontFamily = SchibstedGrotesk),
        bodyMedium = Texto.cuerpo,
        bodySmall = bodySmall.copy(fontFamily = SchibstedGrotesk),
        labelLarge = Texto.enlace135,
        labelMedium = labelMedium.copy(fontFamily = SchibstedGrotesk),
        labelSmall = labelSmall.copy(fontFamily = SchibstedGrotesk),
    )
}
