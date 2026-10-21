package com.jmarias.chavos.dominio

import com.jmarias.chavos.data.local.IconoMeta

/** Ícono probable según el nombre de la meta, para no obligar a elegirlo. */
fun iconoSugerido(nombre: String): IconoMeta {
    val n = nombre.lowercase()
    return when {
        listOf("viaje", "vacaciones", "paseo").any(n::contains) -> IconoMeta.VIAJE
        listOf("emergencia", "imprevisto").any(n::contains) -> IconoMeta.EMERGENCIA
        listOf("portátil", "portatil", "computador", "celular", "consola").any(n::contains) -> IconoMeta.TECNOLOGIA
        listOf("casa", "apartamento", "hogar", "arriendo").any(n::contains) -> IconoMeta.HOGAR
        listOf("estudio", "curso", "universidad", "semestre", "matrícula").any(n::contains) -> IconoMeta.ESTUDIO
        listOf("regalo", "navidad", "cumpleaños").any(n::contains) -> IconoMeta.REGALO
        else -> IconoMeta.OTRA
    }
}
