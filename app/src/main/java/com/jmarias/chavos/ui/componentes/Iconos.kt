package com.jmarias.chavos.ui.componentes

import androidx.annotation.DrawableRes
import com.jmarias.chavos.R
import com.jmarias.chavos.data.local.Categoria
import com.jmarias.chavos.data.local.IconoMeta

@get:DrawableRes
val Categoria.icono: Int
    get() = when (this) {
        Categoria.ALIMENTACION -> R.drawable.ic_cart
        Categoria.TRANSPORTE -> R.drawable.ic_bus
        Categoria.OCIO -> R.drawable.ic_play
        Categoria.SALUD -> R.drawable.ic_heart
        Categoria.HOGAR -> R.drawable.ic_home_alt
        Categoria.EDUCACION -> R.drawable.ic_book
        Categoria.METAS -> R.drawable.ic_target
        Categoria.OTROS_GASTOS -> R.drawable.ic_more
        Categoria.SALARIO -> R.drawable.ic_wallet
        Categoria.VENTAS -> R.drawable.ic_tag
        Categoria.REGALOS -> R.drawable.ic_gift
        Categoria.OTROS_INGRESOS -> R.drawable.ic_more
    }

@get:DrawableRes
val IconoMeta.icono: Int
    get() = when (this) {
        IconoMeta.VIAJE -> R.drawable.ic_send
        IconoMeta.EMERGENCIA -> R.drawable.ic_shield
        IconoMeta.TECNOLOGIA -> R.drawable.ic_laptop
        IconoMeta.HOGAR -> R.drawable.ic_home_alt
        IconoMeta.ESTUDIO -> R.drawable.ic_book
        IconoMeta.REGALO -> R.drawable.ic_gift
        IconoMeta.OTRA -> R.drawable.ic_target
    }
