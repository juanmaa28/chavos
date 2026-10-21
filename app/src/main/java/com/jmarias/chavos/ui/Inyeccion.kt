package com.jmarias.chavos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jmarias.chavos.Contenedor

val LocalContenedor = staticCompositionLocalOf<Contenedor> { error("Falta proveer el contenedor de dependencias") }

/** Crea (o recupera) un ViewModel con acceso al contenedor y a los argumentos de navegación. */
@Composable
inline fun <reified VM : ViewModel> vistaModelo(crossinline crear: (Contenedor, SavedStateHandle) -> VM): VM {
    val contenedor = LocalContenedor.current
    return viewModel(factory = viewModelFactory { initializer { crear(contenedor, createSavedStateHandle()) } })
}
