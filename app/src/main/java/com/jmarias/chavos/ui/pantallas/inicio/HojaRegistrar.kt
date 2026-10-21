package com.jmarias.chavos.ui.pantallas.inicio

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.jmarias.chavos.R
import com.jmarias.chavos.ui.componentes.Asa
import com.jmarias.chavos.ui.componentes.BotonCerrar
import com.jmarias.chavos.ui.componentes.Divisor
import com.jmarias.chavos.ui.componentes.Icono
import com.jmarias.chavos.ui.componentes.IconoCategoria
import com.jmarias.chavos.ui.componentes.Tono
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.Texto
import kotlinx.coroutines.launch

enum class OpcionRegistro { Gasto, Ingreso, Aporte }

/** Hoja del botón +: gasto (la acción más frecuente, en negro), ingreso o aporte a una meta. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaRegistrar(onCerrar: () -> Unit, onElegir: (OpcionRegistro) -> Unit) {
    val c = Chavos.colores
    val estado = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val alcance = rememberCoroutineScope()

    fun cerrarY(accion: () -> Unit) {
        alcance.launch { estado.hide() }.invokeOnCompletion { if (!estado.isVisible) accion() }
    }

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = estado,
        containerColor = c.superficie,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        dragHandle = { Asa() },
    ) {
        Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding().padding(bottom = 24.dp)) {
            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Registrar", style = Texto.tituloHoja, color = c.tinta)
                BotonCerrar(onClick = { cerrarY(onCerrar) })
            }
            Opcion(R.drawable.ic_arrow_up_right, "Gasto", "Compras, pagos y suscripciones", Tono.Oscuro, conDivisor = false) {
                cerrarY { onElegir(OpcionRegistro.Gasto) }
            }
            Opcion(R.drawable.ic_arrow_down_left, "Ingreso", "Salario, ventas o reembolsos", Tono.Suave, conDivisor = true) {
                cerrarY { onElegir(OpcionRegistro.Ingreso) }
            }
            Opcion(R.drawable.ic_target, "Aporte a una meta", "Suma dinero a tus ahorros", Tono.Suave, conDivisor = true) {
                cerrarY { onElegir(OpcionRegistro.Aporte) }
            }
        }
    }
}

@Composable
private fun Opcion(@DrawableRes icono: Int, titulo: String, subtitulo: String, tono: Tono, conDivisor: Boolean, onClick: () -> Unit) {
    val c = Chavos.colores
    Column(Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick)) {
        if (conDivisor) Divisor()
        Row(Modifier.padding(vertical = 15.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            IconoCategoria(icono, 46.dp, tono)
            Column(Modifier.weight(1f)) {
                Text(titulo, style = Texto.fila16, color = c.tinta)
                Spacer(Modifier.height(2.dp))
                Text(subtitulo, style = Texto.leyenda, color = c.tintaTerciaria)
            }
            Icono(R.drawable.ic_chevron_right, tamano = 18.dp, color = c.chevron)
        }
    }
}
