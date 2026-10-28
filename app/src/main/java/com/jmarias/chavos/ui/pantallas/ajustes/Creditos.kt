package com.jmarias.chavos.ui.pantallas.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jmarias.chavos.BuildConfig
import com.jmarias.chavos.R
import com.jmarias.chavos.dominio.Validacion
import com.jmarias.chavos.ui.componentes.Avatar
import com.jmarias.chavos.ui.componentes.BarraSuperior
import com.jmarias.chavos.ui.componentes.Divisor
import com.jmarias.chavos.ui.componentes.FilaDato
import com.jmarias.chavos.ui.componentes.LogoChavos
import com.jmarias.chavos.ui.componentes.Tarjeta
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.Texto

/** Pantalla de créditos con los integrantes del equipo. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreditosPantalla(onVolver: () -> Unit) {
    val c = Chavos.colores
    val integrantes = stringArrayResource(R.array.integrantes)

    Column(Modifier.fillMaxSize().background(c.fondo).statusBarsPadding()) {
        BarraSuperior("Créditos", onVolver)
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 20.dp),
        ) {
            Text("Equipo", style = Texto.etiqueta, color = c.tintaTerciaria, modifier = Modifier.padding(top = 10.dp))
            Spacer(Modifier.height(10.dp))
            Tarjeta(relleno = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                integrantes.forEachIndexed { i, nombre ->
                    if (i > 0) Divisor()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Avatar(Validacion.iniciales(nombre), 46.dp)
                        Text(nombre, style = Texto.fila16, color = c.tinta)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Tarjeta(relleno = PaddingValues(vertical = 2.dp)) {
                FilaDato("Programa", stringResource(R.string.programa), conDivisor = false, icono = R.drawable.ic_user)
                FilaDato("Universidad", stringResource(R.string.universidad), conDivisor = true, icono = R.drawable.ic_book)
                FilaDato("Curso", stringResource(R.string.curso), conDivisor = true, icono = R.drawable.ic_laptop)
                FilaDato("Periodo", stringResource(R.string.periodo), conDivisor = true, icono = R.drawable.ic_calendar)
                FilaDato("Entrega", stringResource(R.string.entrega), conDivisor = true, icono = R.drawable.ic_target)
            }

            Spacer(Modifier.height(24.dp))
            Text("Construido con", style = Texto.etiqueta, color = c.tintaTerciaria)
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Kotlin", "Jetpack Compose", "Room", "DataStore", "Material 3", "Android Studio").forEach { Etiqueta(it) }
            }

            Spacer(Modifier.height(36.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LogoChavos(22.dp)
                    Text("Chavos", style = Texto.subtitulo, color = c.tinta)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Versión ${BuildConfig.VERSION_NAME}\nHecho con Kotlin en Medellín, Colombia",
                    style = Texto.nota,
                    color = c.tintaTerciaria,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun Etiqueta(texto: String) {
    val c = Chavos.colores
    val forma = RoundedCornerShape(8.dp)
    Box(
        Modifier.clip(forma).background(c.superficie).border(1.dp, c.bordeTarjeta, forma).padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(texto, style = Texto.enlace13, color = c.tinta)
    }
}
