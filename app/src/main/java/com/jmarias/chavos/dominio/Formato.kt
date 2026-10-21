package com.jmarias.chavos.dominio

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Textos de fechas y montos en español de Colombia. Los nombres de meses y días son propios
 * porque los datos de idioma de Android varían entre versiones ("sep." vs "sept.").
 */
object Formato {
    private val MESES = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
    )
    private val MESES_CORTOS = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
    private val DIAS = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")

    /** Signo menos tipográfico (U+2212), no el guion. */
    const val MENOS = "−"

    fun miles(valor: Long): String {
        val digitos = abs(valor).toString()
        val texto = StringBuilder(digitos.length + digitos.length / 3)
        digitos.forEachIndexed { i, c ->
            if (i > 0 && (digitos.length - i) % 3 == 0) texto.append('.')
            texto.append(c)
        }
        return texto.toString()
    }

    fun pesos(valor: Long): String = (if (valor < 0) MENOS else "") + "$" + miles(valor)

    fun pesosConSigno(monto: Long, esIngreso: Boolean): String = (if (esIngreso) "+" else MENOS) + "$" + miles(monto)

    /** Etiqueta compacta para gráficas: "$1,25 M", "$850 mil". */
    fun compacto(valor: Long): String = when {
        valor >= 1_000_000 -> {
            val texto = String.format(Locale.US, "%.2f", valor / 1_000_000.0).trimEnd('0').trimEnd('.')
            "$" + texto.replace('.', ',') + " M"
        }
        valor >= 1_000 -> "$" + (valor / 1_000.0).roundToLong() + " mil"
        else -> "$$valor"
    }

    fun mes(mes: YearMonth): String = MESES[mes.monthValue - 1]

    fun mesAnio(mes: YearMonth): String = mes(mes).capitalizar() + " " + mes.year

    fun mesCorto(mes: YearMonth): String = MESES_CORTOS[mes.monthValue - 1]

    /** "Jueves 24 de septiembre" */
    fun fechaLarga(fecha: LocalDate): String =
        "${DIAS[fecha.dayOfWeek.value - 1]} ${fecha.dayOfMonth} de ${MESES[fecha.monthValue - 1]}".capitalizar()

    /** "24 de septiembre de 2026" */
    fun fechaCompleta(fecha: LocalDate): String = "${fecha.dayOfMonth} de ${MESES[fecha.monthValue - 1]} de ${fecha.year}"

    /** "24 sep 2026" */
    fun fechaCorta(fecha: LocalDate): String = "${fecha.dayOfMonth} ${MESES_CORTOS[fecha.monthValue - 1]} ${fecha.year}"

    /** "hoy", "ayer", "21 sep" o "21 sep 2025" */
    fun relativa(fecha: LocalDate, hoy: LocalDate = LocalDate.now()): String = when (fecha) {
        hoy -> "hoy"
        hoy.minusDays(1) -> "ayer"
        else -> "${fecha.dayOfMonth} ${MESES_CORTOS[fecha.monthValue - 1]}" + if (fecha.year != hoy.year) " ${fecha.year}" else ""
    }

    /** Encabezado de un grupo de movimientos: "Hoy", "Ayer" o "Lunes 21 de septiembre". */
    fun encabezadoDia(fecha: LocalDate, hoy: LocalDate = LocalDate.now()): String = when (fecha) {
        hoy -> "Hoy"
        hoy.minusDays(1) -> "Ayer"
        else -> fechaLarga(fecha) + if (fecha.year != hoy.year) " de ${fecha.year}" else ""
    }

    /** "Para diciembre de 2026" */
    fun plazo(fecha: LocalDate): String = "Para ${MESES[fecha.monthValue - 1]} de ${fecha.year}"

    fun hora(milis: Long, zona: ZoneId = ZoneId.systemDefault()): String {
        val t = Instant.ofEpochMilli(milis).atZone(zona).toLocalTime()
        return String.format(Locale.US, "%02d:%02d", t.hour, t.minute)
    }

    private fun String.capitalizar(): String = replaceFirstChar { it.uppercase() }
}
