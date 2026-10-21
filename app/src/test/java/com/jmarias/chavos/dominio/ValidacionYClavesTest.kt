package com.jmarias.chavos.dominio

import com.jmarias.chavos.data.local.Categoria
import com.jmarias.chavos.data.local.IconoMeta
import com.jmarias.chavos.data.local.MovimientoEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class ValidacionYClavesTest {
    @Test
    fun `valida correos`() {
        assertTrue(Validacion.correoValido("juan@upb.edu.co"))
        assertTrue(Validacion.correoValido("  demo@chavos.app "))
        assertFalse(Validacion.correoValido("juan@"))
        assertFalse(Validacion.correoValido("juan.upb.edu.co"))
        assertEquals("demo@chavos.app", Validacion.normalizarCorreo(" Demo@Chavos.APP "))
    }

    @Test
    fun `fortaleza de la contrasena`() {
        assertEquals(0, Validacion.fortalezaClave(""))
        assertEquals(1, Validacion.fortalezaClave("abc123"))
        assertEquals(2, Validacion.fortalezaClave("abcd1234"))
        assertEquals(3, Validacion.fortalezaClave("abcd1234!"))
        assertEquals("Contraseña segura", Validacion.etiquetaFortaleza(3))
    }

    @Test
    fun `iniciales y primer nombre`() {
        assertEquals("JM", Validacion.iniciales("Juan Manuel Arias"))
        assertEquals("A", Validacion.iniciales("ana"))
        assertEquals("?", Validacion.iniciales("  "))
        assertEquals("Juan", Validacion.primerNombre("  Juan Manuel "))
    }

    @Test
    fun `el hash verifica la clave correcta y rechaza las demas`() {
        val sal = Claves.nuevaSal()
        val hash = Claves.hash("Chavos2026", sal)
        assertTrue(Claves.verificar("Chavos2026", sal, hash))
        assertFalse(Claves.verificar("chavos2026", sal, hash))
        assertNotEquals(hash, Claves.hash("Chavos2026", Claves.nuevaSal()))
    }

    @Test
    fun `sugiere icono segun el nombre de la meta`() {
        assertEquals(IconoMeta.VIAJE, iconoSugerido("Viaje a Cartagena"))
        assertEquals(IconoMeta.TECNOLOGIA, iconoSugerido("Portátil nuevo"))
        assertEquals(IconoMeta.OTRA, iconoSugerido("Bicicleta"))
    }

    @Test
    fun `csv escapa comas y comillas`() {
        val m = MovimientoEntity(
            usuarioId = 1, tipo = Categoria.ALIMENTACION.tipo, monto = 120_000, categoria = Categoria.ALIMENTACION,
            fecha = LocalDate.of(2026, 9, 2).atTime(10, 24).toInstant(ZoneOffset.UTC).toEpochMilli(),
            nota = "Mercado, \"grande\"",
        )
        val lineas = Csv.movimientos(listOf(m), emptyMap(), ZoneOffset.UTC).removePrefix("﻿").lines()
        assertEquals("Fecha,Hora,Tipo,Categoría,Monto,Nota,Meta", lineas[0])
        assertEquals("2026-09-02,10:24,Gasto,Alimentación,120000,\"Mercado, \"\"grande\"\"\",", lineas[1])
    }
}
