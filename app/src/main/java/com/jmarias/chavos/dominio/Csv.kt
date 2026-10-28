package com.jmarias.chavos.dominio

import com.jmarias.chavos.data.local.MovimientoEntity
import java.time.Instant
import java.time.ZoneId

/** Conversión de movimientos a texto CSV para exportarlos. */
object Csv {
    /** CSV con BOM para que Excel reconozca las tildes. Los montos van sin formato para poder sumarlos. */
    fun movimientos(lista: List<MovimientoEntity>, nombresMeta: Map<Long, String>, zona: ZoneId = ZoneId.systemDefault()): String =
        buildString {
            append('﻿')
            appendLine("Fecha,Hora,Tipo,Categoría,Monto,Nota,Meta")
            lista.sortedBy { it.fecha }.forEach { m ->
                val instante = Instant.ofEpochMilli(m.fecha).atZone(zona)
                appendLine(
                    listOf(
                        instante.toLocalDate().toString(),
                        Formato.hora(m.fecha, zona),
                        if (m.esIngreso) "Ingreso" else "Gasto",
                        m.categoria.etiqueta,
                        m.monto.toString(),
                        m.nota,
                        m.metaId?.let { nombresMeta[it] }.orEmpty(),
                    ).joinToString(",") { campo(it) },
                )
            }
        }

    private fun campo(valor: String): String =
        if (valor.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"" + valor.replace("\"", "\"\"") + "\"" else valor
}
