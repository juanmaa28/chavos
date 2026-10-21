package com.jmarias.chavos.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmarias.chavos.data.local.MovimientoEntity
import com.jmarias.chavos.dominio.Calculos
import com.jmarias.chavos.dominio.Formato
import java.time.LocalDate

/** Título visible de un movimiento: su nota o, si no tiene, la categoría. */
fun MovimientoEntity.titulo(): String = nota.ifBlank { categoria.etiqueta }

/** "Alimentación, hoy" — o solo la fecha si el título ya es la categoría. */
fun MovimientoEntity.subtitulo(hoy: LocalDate = LocalDate.now()): String {
    val cuando = Formato.relativa(Calculos.fecha(fecha), hoy)
    return if (nota.isBlank()) cuando.replaceFirstChar { it.uppercase() } else "${categoria.etiqueta}, $cuando"
}

fun MovimientoEntity.montoConSigno(): String = Formato.pesosConSigno(monto, esIngreso)

/** Columna con desplazamiento, fondo de la app y margen lateral de 20 dp. */
@Composable
fun ColumnaPantalla(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(com.jmarias.chavos.ui.tema.Chavos.colores.fondo)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        content = contenido,
    )
}

@Composable
fun Avisos(estado: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(estado, modifier.padding(16.dp))
}
