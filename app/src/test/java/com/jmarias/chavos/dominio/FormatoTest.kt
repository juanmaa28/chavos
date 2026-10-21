package com.jmarias.chavos.dominio

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class FormatoTest {
    @Test
    fun `agrupa miles con punto`() {
        assertEquals("$0", Formato.pesos(0))
        assertEquals("$950", Formato.pesos(950))
        assertEquals("$1.250.000", Formato.pesos(1_250_000))
        assertEquals("$12.500.000", Formato.pesos(12_500_000))
    }

    @Test
    fun `los negativos usan el signo menos tipografico`() {
        assertEquals("−$35.000", Formato.pesos(-35_000))
        assertEquals("−$120.000", Formato.pesosConSigno(120_000, esIngreso = false))
        assertEquals("+$2.500.000", Formato.pesosConSigno(2_500_000, esIngreso = true))
    }

    @Test
    fun `etiqueta compacta para graficas`() {
        assertEquals("$1,25 M", Formato.compacto(1_250_000))
        assertEquals("$1,1 M", Formato.compacto(1_100_000))
        assertEquals("$2 M", Formato.compacto(2_000_000))
        assertEquals("$850 mil", Formato.compacto(850_000))
    }

    @Test
    fun `fechas en espanol`() {
        val jueves = LocalDate.of(2026, 9, 24)
        assertEquals("Jueves 24 de septiembre", Formato.fechaLarga(jueves))
        assertEquals("24 de septiembre de 2026", Formato.fechaCompleta(jueves))
        assertEquals("24 sep 2026", Formato.fechaCorta(jueves))
        assertEquals("Septiembre 2026", Formato.mesAnio(YearMonth.of(2026, 9)))
        assertEquals("Para diciembre de 2026", Formato.plazo(LocalDate.of(2026, 12, 1)))
    }

    @Test
    fun `fecha relativa`() {
        val hoy = LocalDate.of(2026, 9, 24)
        assertEquals("hoy", Formato.relativa(hoy, hoy))
        assertEquals("ayer", Formato.relativa(hoy.minusDays(1), hoy))
        assertEquals("21 sep", Formato.relativa(LocalDate.of(2026, 9, 21), hoy))
        assertEquals("21 sep 2025", Formato.relativa(LocalDate.of(2025, 9, 21), hoy))
        assertEquals("Lunes 21 de septiembre", Formato.encabezadoDia(LocalDate.of(2026, 9, 21), hoy))
    }
}
