package com.jmarias.chavos.dominio

import com.jmarias.chavos.data.local.Categoria
import com.jmarias.chavos.data.local.MovimientoEntity
import com.jmarias.chavos.data.local.TipoMovimiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

class CalculosTest {
    private val utc = ZoneOffset.UTC

    private fun mov(fecha: LocalDate, monto: Long, categoria: Categoria) = MovimientoEntity(
        usuarioId = 1,
        tipo = categoria.tipo,
        monto = monto,
        categoria = categoria,
        fecha = fecha.atTime(12, 0).toInstant(utc).toEpochMilli(),
        nota = "",
    )

    private val movimientos = listOf(
        mov(LocalDate.of(2026, 9, 1), 2_500_000, Categoria.SALARIO),
        mov(LocalDate.of(2026, 9, 2), 120_000, Categoria.ALIMENTACION),
        mov(LocalDate.of(2026, 9, 3), 35_000, Categoria.TRANSPORTE),
        mov(LocalDate.of(2026, 9, 9), 80_000, Categoria.ALIMENTACION),
        mov(LocalDate.of(2026, 8, 5), 300_000, Categoria.SALUD),
    )

    @Test
    fun `resumen del mes separa ingresos y gastos`() {
        val r = Calculos.resumen(Calculos.delMes(movimientos, YearMonth.of(2026, 9), utc))
        assertEquals(2_500_000, r.ingresos)
        assertEquals(235_000, r.gastos)
        assertEquals(2_265_000, r.saldo)
    }

    @Test
    fun `gasto por mes termina en el mes pedido y rellena con ceros`() {
        val meses = Calculos.gastosPorMes(movimientos, YearMonth.of(2026, 9), meses = 3, zona = utc)
        assertEquals(listOf(YearMonth.of(2026, 7), YearMonth.of(2026, 8), YearMonth.of(2026, 9)), meses.map { it.first })
        assertEquals(listOf(0L, 300_000L, 235_000L), meses.map { it.second })
    }

    @Test
    fun `promedio ignora meses sin gasto`() {
        assertEquals(267_500L, Calculos.promedio(listOf(0, 300_000, 235_000)))
        assertNull(Calculos.promedio(listOf(0, 0)))
    }

    @Test
    fun `variacion contra el promedio`() {
        assertEquals(14, Calculos.variacion(1_250_000, 1_095_000))
        assertEquals(-20, Calculos.variacion(80, 100))
        assertNull(Calculos.variacion(100, null))
    }

    @Test
    fun `gastos por categoria ordenados de mayor a menor`() {
        val r = Calculos.gastosPorCategoria(Calculos.delMes(movimientos, YearMonth.of(2026, 9), utc))
        assertEquals(listOf(Categoria.ALIMENTACION to 200_000L, Categoria.TRANSPORTE to 35_000L), r)
    }

    @Test
    fun `aporte mensual cuenta el mes actual y el de la fecha limite`() {
        val hoy = LocalDate.of(2026, 9, 24)
        // Faltan 1.900.000 entre septiembre y diciembre: cuatro aportes.
        assertEquals(475_000L, Calculos.aporteMensual(5_000_000, 3_100_000, LocalDate.of(2026, 12, 1), hoy))
        // Se redondea hacia arriba a miles.
        assertEquals(334_000L, Calculos.aporteMensual(1_000_000, 0, LocalDate.of(2026, 11, 30), hoy))
        assertNull(Calculos.aporteMensual(1_000, 1_000, LocalDate.of(2026, 12, 1), hoy))
        assertNull(Calculos.aporteMensual(1_000, 0, null, hoy))
    }

    @Test
    fun `tipo de movimiento coincide con la categoria`() {
        assertEquals(TipoMovimiento.INGRESO, mov(LocalDate.now(), 1, Categoria.SALARIO).tipo)
        assertEquals(TipoMovimiento.GASTO, mov(LocalDate.now(), 1, Categoria.METAS).tipo)
    }
}
