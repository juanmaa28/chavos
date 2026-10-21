package com.jmarias.chavos.ui.pantallas.inicio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.jmarias.chavos.data.AuthRepository
import com.jmarias.chavos.data.FinanzasRepository
import com.jmarias.chavos.data.SesionRepository
import com.jmarias.chavos.data.local.MovimientoEntity
import com.jmarias.chavos.dominio.Calculos
import com.jmarias.chavos.dominio.Formato
import com.jmarias.chavos.dominio.ResumenMes
import com.jmarias.chavos.dominio.Validacion
import com.jmarias.chavos.ui.componentes.Avatar
import com.jmarias.chavos.ui.componentes.ColumnaPantalla
import com.jmarias.chavos.ui.componentes.EncabezadoSeccion
import com.jmarias.chavos.ui.componentes.EstadoVacio
import com.jmarias.chavos.ui.componentes.FilaMovimiento
import com.jmarias.chavos.ui.componentes.Tarjeta
import com.jmarias.chavos.ui.componentes.TarjetaSaldo
import com.jmarias.chavos.ui.componentes.icono
import com.jmarias.chavos.ui.componentes.montoConSigno
import com.jmarias.chavos.ui.componentes.subtitulo
import com.jmarias.chavos.ui.componentes.titulo
import com.jmarias.chavos.ui.tema.Chavos
import com.jmarias.chavos.ui.tema.Texto
import com.jmarias.chavos.ui.vistaModelo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

data class InicioEstado(
    val nombre: String = "",
    val iniciales: String = "",
    val mes: YearMonth = YearMonth.now(),
    val resumen: ResumenMes = ResumenMes(),
    val recientes: List<MovimientoEntity> = emptyList(),
    val cargando: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class InicioViewModel(sesion: SesionRepository, auth: AuthRepository, finanzas: FinanzasRepository) : ViewModel() {
    val estado: StateFlow<InicioEstado> = sesion.usuarioId.filterNotNull().flatMapLatest { id ->
        combine(auth.usuario(id), finanzas.movimientos(id)) { usuario, movimientos ->
            val mes = YearMonth.now()
            val nombre = usuario?.nombre.orEmpty()
            InicioEstado(
                nombre = Validacion.primerNombre(nombre),
                iniciales = Validacion.iniciales(nombre),
                mes = mes,
                resumen = Calculos.resumen(Calculos.delMes(movimientos, mes)),
                recientes = movimientos.take(5),
                cargando = false,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InicioEstado())
}

@Composable
fun InicioPantalla(
    onInforme: () -> Unit,
    onVerTodos: () -> Unit,
    onMovimiento: (Long) -> Unit,
    onPerfil: () -> Unit,
    onRegistrar: () -> Unit,
) {
    val vm = vistaModelo { c, _ -> InicioViewModel(c.sesion, c.auth, c.finanzas) }
    val e by vm.estado.collectAsStateWithLifecycle()
    val c = Chavos.colores
    val hoy = LocalDate.now()

    ColumnaPantalla {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(Modifier.weight(1f)) {
                Text(Formato.fechaLarga(hoy), style = Texto.meta, color = c.tintaTerciaria)
                Spacer(Modifier.height(3.dp))
                Text(if (e.nombre.isEmpty()) "Hola" else "Hola, ${e.nombre}", style = Texto.saludo, color = c.tinta)
            }
            Avatar(e.iniciales, modifier = Modifier.clickable(role = Role.Button, onClickLabel = "Abrir ajustes", onClick = onPerfil))
        }

        TarjetaSaldo(
            etiqueta = "Saldo de ${Formato.mes(e.mes)}",
            saldo = e.resumen.saldo,
            ingresos = e.resumen.ingresos,
            gastos = e.resumen.gastos,
            onInforme = onInforme,
        )

        Spacer(Modifier.height(30.dp))
        EncabezadoSeccion("Recientes", accion = if (e.recientes.isEmpty()) null else "Ver todos", onAccion = onVerTodos)
        Spacer(Modifier.height(12.dp))
        if (!e.cargando && e.recientes.isEmpty()) {
            EstadoVacio(
                titulo = "Aún no tienes movimientos",
                descripcion = "Registra tu primer gasto o ingreso y aquí verás los más recientes.",
                accion = "Registrar movimiento",
                onAccion = onRegistrar,
            )
        } else {
            Tarjeta(relleno = PaddingValues(vertical = 2.dp)) {
                e.recientes.forEachIndexed { i, m ->
                    FilaMovimiento(
                        icono = m.categoria.icono,
                        titulo = m.titulo(),
                        subtitulo = m.subtitulo(hoy),
                        monto = m.montoConSigno(),
                        esIngreso = m.esIngreso,
                        conDivisor = i > 0,
                        onClick = { onMovimiento(m.id) },
                    )
                }
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}
