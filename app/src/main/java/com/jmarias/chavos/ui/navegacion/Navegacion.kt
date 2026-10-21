package com.jmarias.chavos.ui.navegacion

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jmarias.chavos.R
import com.jmarias.chavos.data.local.Categoria
import com.jmarias.chavos.data.local.TipoMovimiento
import com.jmarias.chavos.ui.componentes.BarraPestanas
import com.jmarias.chavos.ui.componentes.Pestana
import com.jmarias.chavos.ui.pantallas.ajustes.AjustesPantalla
import com.jmarias.chavos.ui.pantallas.ajustes.CreditosPantalla
import com.jmarias.chavos.ui.pantallas.auth.LoginPantalla
import com.jmarias.chavos.ui.pantallas.auth.RegistroPantalla
import com.jmarias.chavos.ui.pantallas.informe.InformePantalla
import com.jmarias.chavos.ui.pantallas.inicio.HojaRegistrar
import com.jmarias.chavos.ui.pantallas.inicio.InicioPantalla
import com.jmarias.chavos.ui.pantallas.inicio.OpcionRegistro
import com.jmarias.chavos.ui.pantallas.metas.MetasPantalla
import com.jmarias.chavos.ui.pantallas.movimientos.DetallePantalla
import com.jmarias.chavos.ui.pantallas.movimientos.FormularioPantalla
import com.jmarias.chavos.ui.pantallas.movimientos.MovimientosPantalla
import com.jmarias.chavos.ui.tema.Chavos

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val INICIO = "inicio"
    const val MOVIMIENTOS = "movimientos"
    const val METAS = "metas"
    const val AJUSTES = "ajustes"
    const val INFORME = "informe"
    const val CREDITOS = "creditos"
    const val DETALLE = "detalle/{id}"
    const val FORMULARIO = "formulario?id={id}&tipo={tipo}&categoria={categoria}&meta={meta}"

    fun detalle(id: Long) = "detalle/$id"

    fun formulario(id: Long = 0, tipo: TipoMovimiento = TipoMovimiento.GASTO, categoria: Categoria? = null, meta: Long = 0) =
        "formulario?id=$id&tipo=${tipo.name}&categoria=${categoria?.name.orEmpty()}&meta=$meta"
}

private val PESTANAS = listOf(
    Pestana(Rutas.INICIO, "Inicio", R.drawable.ic_home),
    Pestana(Rutas.MOVIMIENTOS, "Movimientos", R.drawable.ic_list),
    Pestana(Rutas.METAS, "Metas", R.drawable.ic_target),
    Pestana(Rutas.AJUSTES, "Ajustes", R.drawable.ic_gear),
)

/** Curva de salida fuerte: arranca rápido, así la pantalla responde de inmediato al toque. */
private val SALIDA_FUERTE = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.entrarDesdeDerecha(): EnterTransition =
    slideInHorizontally(tween(280, easing = SALIDA_FUERTE)) { it / 4 } + fadeIn(tween(200))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.salirHaciaDerecha(): ExitTransition =
    slideOutHorizontally(tween(200, easing = SALIDA_FUERTE)) { it / 4 } + fadeOut(tween(150))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.entrarDesdeAbajo(): EnterTransition =
    slideInVertically(tween(300, easing = SALIDA_FUERTE)) { it / 6 } + fadeIn(tween(200))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.salirHaciaAbajo(): ExitTransition =
    slideOutVertically(tween(200, easing = SALIDA_FUERTE)) { it / 6 } + fadeOut(tween(150))

@Composable
fun AppAutenticacion() {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = Rutas.LOGIN,
        enterTransition = { fadeIn(tween(200)) },
        exitTransition = { fadeOut(tween(150)) },
    ) {
        composable(Rutas.LOGIN) {
            LoginPantalla(onCrearCuenta = dropUnlessResumed { nav.navigate(Rutas.REGISTRO) })
        }
        composable(
            Rutas.REGISTRO,
            enterTransition = { entrarDesdeDerecha() },
            popExitTransition = { salirHaciaDerecha() },
        ) {
            RegistroPantalla(onVolver = dropUnlessResumed { nav.popBackStack() })
        }
    }
}

@Composable
fun AppPrincipal() {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route
    var registrar by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = Chavos.colores.fondo,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (PESTANAS.any { it.ruta == ruta }) {
                BarraPestanas(PESTANAS, ruta, onSeleccion = nav::irAPestana, onRegistrar = { registrar = true })
            }
        },
    ) { relleno ->
        NavHost(
            navController = nav,
            startDestination = Rutas.INICIO,
            modifier = Modifier.padding(relleno).consumeWindowInsets(relleno),
            enterTransition = { fadeIn(tween(180)) },
            exitTransition = { fadeOut(tween(120)) },
            popEnterTransition = { fadeIn(tween(180)) },
            popExitTransition = { fadeOut(tween(120)) },
        ) {
            composable(Rutas.INICIO) {
                InicioPantalla(
                    onInforme = dropUnlessResumed { nav.navigate(Rutas.INFORME) },
                    onVerTodos = { nav.irAPestana(Rutas.MOVIMIENTOS) },
                    onMovimiento = { nav.navigate(Rutas.detalle(it)) },
                    onPerfil = { nav.irAPestana(Rutas.AJUSTES) },
                    onRegistrar = { registrar = true },
                )
            }
            composable(Rutas.MOVIMIENTOS) {
                MovimientosPantalla(onMovimiento = { nav.navigate(Rutas.detalle(it)) }, onRegistrar = { registrar = true })
            }
            composable(Rutas.METAS) {
                MetasPantalla(onAportar = { meta -> nav.navigate(Rutas.formulario(categoria = Categoria.METAS, meta = meta)) })
            }
            composable(Rutas.AJUSTES) {
                AjustesPantalla(onCreditos = dropUnlessResumed { nav.navigate(Rutas.CREDITOS) })
            }
            composable(
                Rutas.INFORME,
                enterTransition = { entrarDesdeDerecha() },
                popExitTransition = { salirHaciaDerecha() },
            ) {
                InformePantalla(onVolver = dropUnlessResumed { nav.popBackStack() })
            }
            composable(
                Rutas.CREDITOS,
                enterTransition = { entrarDesdeDerecha() },
                popExitTransition = { salirHaciaDerecha() },
            ) {
                CreditosPantalla(onVolver = dropUnlessResumed { nav.popBackStack() })
            }
            composable(
                Rutas.DETALLE,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
                enterTransition = { entrarDesdeDerecha() },
                popExitTransition = { salirHaciaDerecha() },
            ) {
                DetallePantalla(
                    onVolver = dropUnlessResumed { nav.popBackStack() },
                    onEditar = { nav.navigate(Rutas.formulario(id = it)) },
                )
            }
            composable(
                Rutas.FORMULARIO,
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("tipo") { type = NavType.StringType; defaultValue = TipoMovimiento.GASTO.name },
                    navArgument("categoria") { type = NavType.StringType; defaultValue = "" },
                    navArgument("meta") { type = NavType.LongType; defaultValue = 0L },
                ),
                enterTransition = { entrarDesdeAbajo() },
                popExitTransition = { salirHaciaAbajo() },
            ) {
                FormularioPantalla(onCerrar = dropUnlessResumed { nav.popBackStack() })
            }
        }
    }

    if (registrar) {
        HojaRegistrar(
            onCerrar = { registrar = false },
            onElegir = { opcion ->
                registrar = false
                nav.navigate(
                    when (opcion) {
                        OpcionRegistro.Gasto -> Rutas.formulario(tipo = TipoMovimiento.GASTO)
                        OpcionRegistro.Ingreso -> Rutas.formulario(tipo = TipoMovimiento.INGRESO)
                        OpcionRegistro.Aporte -> Rutas.formulario(categoria = Categoria.METAS)
                    },
                )
            },
        )
    }
}

/** Cambia de pestaña conservando el estado de cada una, sin apilar pestañas repetidas. */
private fun NavHostController.irAPestana(ruta: String) {
    navigate(ruta) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
