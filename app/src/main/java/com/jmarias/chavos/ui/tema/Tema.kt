package com.jmarias.chavos.ui.tema

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LocalColores = staticCompositionLocalOf { ColoresClaros }

object Chavos {
    val colores: ColoresChavos
        @Composable @ReadOnlyComposable get() = LocalColores.current
}

private fun esquema(c: ColoresChavos): ColorScheme {
    val base = if (c.esOscuro) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = c.tinta,
        onPrimary = c.tintaInversa,
        primaryContainer = c.superficieSuave,
        onPrimaryContainer = c.tinta,
        secondary = c.tintaSecundaria,
        onSecondary = c.tintaInversa,
        secondaryContainer = c.superficieSuave,
        onSecondaryContainer = c.tinta,
        tertiary = c.tintaSecundaria,
        onTertiary = c.tintaInversa,
        background = c.fondo,
        onBackground = c.tinta,
        surface = c.superficie,
        onSurface = c.tinta,
        surfaceVariant = c.superficieSuave,
        onSurfaceVariant = c.tintaSecundaria,
        surfaceTint = Color.Transparent,
        inverseSurface = c.tinta,
        inverseOnSurface = c.tintaInversa,
        error = c.negativo,
        onError = c.tintaInversa,
        outline = c.bordeCampo,
        outlineVariant = c.bordeTarjeta,
        scrim = Color.Black,
        surfaceBright = c.superficie,
        surfaceDim = c.fondo,
        surfaceContainerLowest = c.superficie,
        surfaceContainerLow = c.superficie,
        surfaceContainer = c.superficie,
        surfaceContainerHigh = c.superficie,
        surfaceContainerHighest = c.superficieSuave,
    )
}

@Composable
fun TemaChavos(oscuro: Boolean, contenido: @Composable () -> Unit) {
    val colores = if (oscuro) ColoresOscuros else ColoresClaros
    val actividad = LocalActivity.current
    val vista = LocalView.current
    if (!vista.isInEditMode && actividad != null) {
        SideEffect {
            WindowCompat.getInsetsController(actividad.window, vista).apply {
                isAppearanceLightStatusBars = !oscuro
                isAppearanceLightNavigationBars = !oscuro
            }
        }
    }
    CompositionLocalProvider(LocalColores provides colores) {
        MaterialTheme(colorScheme = esquema(colores), typography = TipografiaMaterial, content = contenido)
    }
}
