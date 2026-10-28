package com.jmarias.chavos.dominio

import com.jmarias.chavos.data.local.Categoria
import com.jmarias.chavos.data.local.MovimientoEntity
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/** Totales de ingresos y gastos de un mes. */
data class ResumenMes(val ingresos: Long = 0, val gastos: Long = 0) {
    val saldo: Long get() = ingresos - gastos
}

object Calculos {
    fun fecha(milis: Long, zona: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(milis).atZone(zona).toLocalDate()

    fun delMes(
        movimientos: List<MovimientoEntity>,
        mes: YearMonth,
        zona: ZoneId = ZoneId.systemDefault(),
    ): List<MovimientoEntity> = movimientos.filter { YearMonth.from(fecha(it.fecha, zona)) == mes }

    fun resumen(movimientos: List<MovimientoEntity>): ResumenMes = ResumenMes(
        ingresos = movimientos.filter { it.esIngreso }.sumOf { it.monto },
        gastos = movimientos.filterNot { it.esIngreso }.sumOf { it.monto },
    )

    /** Gasto de cada mes, del más antiguo al más reciente, terminando en [hasta]. */
    fun gastosPorMes(
        movimientos: List<MovimientoEntity>,
        hasta: YearMonth,
        meses: Int = 6,
        zona: ZoneId = ZoneId.systemDefault(),
    ): List<Pair<YearMonth, Long>> {
        val porMes = movimientos.filterNot { it.esIngreso }
            .groupBy { YearMonth.from(fecha(it.fecha, zona)) }
            .mapValues { (_, lista) -> lista.sumOf { it.monto } }
        return (meses - 1 downTo 0).map { atras ->
            val mes = hasta.minusMonths(atras.toLong())
            mes to (porMes[mes] ?: 0L)
        }
    }

    /** Promedio de los meses con gasto; null si ninguno tiene datos. */
    fun promedio(valores: List<Long>): Long? =
        valores.filter { it > 0 }.takeIf { it.isNotEmpty() }?.average()?.roundToLong()

    /** Diferencia porcentual contra el promedio; null si no hay con qué comparar. */
    fun variacion(actual: Long, promedio: Long?): Int? =
        if (promedio == null || promedio == 0L) null else ((actual - promedio) * 100.0 / promedio).roundToInt()

    fun gastosPorCategoria(movimientos: List<MovimientoEntity>): List<Pair<Categoria, Long>> =
        movimientos.filterNot { it.esIngreso }
            .groupBy { it.categoria }
            .map { (categoria, lista) -> categoria to lista.sumOf { it.monto } }
            .sortedByDescending { it.second }

    /**
     * Cuánto ahorrar cada mes, contando el mes actual y el de la fecha límite,
     * redondeado hacia arriba a miles. Null si la meta no tiene fecha o ya se cumplió.
     */
    fun aporteMensual(objetivo: Long, ahorrado: Long, fechaLimite: LocalDate?, hoy: LocalDate = LocalDate.now()): Long? {
        val falta = objetivo - ahorrado
        if (falta <= 0 || fechaLimite == null) return null
        val meses = (ChronoUnit.MONTHS.between(YearMonth.from(hoy), YearMonth.from(fechaLimite)) + 1).coerceAtLeast(1)
        return (ceil(falta.toDouble() / meses / 1_000) * 1_000).toLong()
    }
}
