package com.jmarias.chavos.data

import com.jmarias.chavos.data.local.Categoria
import com.jmarias.chavos.data.local.ChavosDatabase
import com.jmarias.chavos.data.local.IconoMeta
import com.jmarias.chavos.data.local.MetaEntity
import com.jmarias.chavos.data.local.MovimientoEntity
import com.jmarias.chavos.data.local.TipoMovimiento
import com.jmarias.chavos.data.local.UsuarioEntity
import com.jmarias.chavos.dominio.Claves
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.roundToLong

/**
 * Cuenta de demostración con seis meses de movimientos, creada solo si la base está vacía.
 * Las fechas son relativas a hoy para que la app siempre muestre un mes actual con datos.
 */
object DatosDeEjemplo {
    const val CORREO = "demo@chavos.app"
    const val CLAVE = "Chavos2026"

    private data class Plantilla(val dia: Int, val nota: String, val categoria: Categoria, val monto: Long, val varia: Boolean = true)

    private val GASTOS_MES = listOf(
        Plantilla(2, "Mercado quincenal", Categoria.ALIMENTACION, 180_000),
        Plantilla(3, "Metro y buses", Categoria.TRANSPORTE, 90_000),
        Plantilla(5, "Netflix", Categoria.OCIO, 35_000, varia = false),
        Plantilla(6, "Ahorro viaje", Categoria.METAS, 200_000, varia = false),
        Plantilla(7, "Spotify", Categoria.OCIO, 17_000, varia = false),
        Plantilla(9, "Frutas y verduras", Categoria.ALIMENTACION, 45_000),
        Plantilla(10, "Servicios del hogar", Categoria.HOGAR, 120_000),
        Plantilla(12, "Farmacia", Categoria.SALUD, 60_000),
        Plantilla(14, "Libros y fotocopias", Categoria.EDUCACION, 40_000),
        Plantilla(16, "Mercado del mes", Categoria.ALIMENTACION, 160_000),
        Plantilla(18, "Recarga cívica", Categoria.TRANSPORTE, 85_000),
        Plantilla(20, "Almuerzo con amigos", Categoria.ALIMENTACION, 40_000),
        Plantilla(22, "Cine", Categoria.OCIO, 48_000),
    )

    /** Variación del gasto en los cinco meses anteriores al actual, del más antiguo al más reciente. */
    private val FACTORES = listOf(0.88, 1.0, 0.78, 1.11, 0.99)

    suspend fun sembrarSiVacio(bd: ChavosDatabase, hoy: LocalDate = LocalDate.now(), zona: ZoneId = ZoneId.systemDefault()) {
        if (bd.usuarioDao().contar() > 0) return

        val ahora = System.currentTimeMillis()
        val sal = Claves.nuevaSal()
        val usuarioId = bd.usuarioDao().insertar(
            UsuarioEntity(nombre = "Juan Manuel Arias", correo = CORREO, hash = Claves.hash(CLAVE, sal), sal = sal, creadoEn = ahora),
        )

        val metas = bd.metaDao()
        val viaje = metas.insertar(
            MetaEntity(usuarioId = usuarioId, nombre = "Viaje a Cartagena", objetivo = 5_000_000,
                fechaLimite = hoy.plusMonths(3).withDayOfMonth(1).toEpochDay(), icono = IconoMeta.VIAJE, creadaEn = ahora),
        )
        val emergencia = metas.insertar(
            MetaEntity(usuarioId = usuarioId, nombre = "Fondo de emergencia", objetivo = 1_500_000,
                fechaLimite = null, icono = IconoMeta.EMERGENCIA, creadaEn = ahora + 1),
        )
        val portatil = metas.insertar(
            MetaEntity(usuarioId = usuarioId, nombre = "Portátil nuevo", objetivo = 3_200_000,
                fechaLimite = hoy.plusMonths(6).withDayOfMonth(1).toEpochDay(), icono = IconoMeta.TECNOLOGIA, creadaEn = ahora + 2),
        )

        fun instante(fecha: LocalDate, hora: Int) = fecha.atTime(hora, (fecha.dayOfMonth * 7) % 60).atZone(zona).toInstant().toEpochMilli()

        val lista = mutableListOf<MovimientoEntity>()

        // Ahorro previo, fuera de la ventana de seis meses para no distorsionar las gráficas.
        val inicio = hoy.minusMonths(7).withDayOfMonth(15)
        listOf(viaje to 1_900_000L, emergencia to 750_000L, portatil to 1_200_000L).forEach { (meta, monto) ->
            lista += MovimientoEntity(usuarioId = usuarioId, tipo = TipoMovimiento.GASTO, monto = monto,
                categoria = Categoria.METAS, fecha = instante(inicio, 11), nota = "Ahorro inicial", metaId = meta)
        }

        val mesActual = YearMonth.from(hoy)
        for (atras in 5 downTo 0) {
            val mes = mesActual.minusMonths(atras.toLong())
            val factor = if (atras == 0) 1.0 else FACTORES[5 - atras]
            val ultimoDia = if (atras == 0) hoy.dayOfMonth else mes.lengthOfMonth()

            lista += MovimientoEntity(usuarioId = usuarioId, tipo = TipoMovimiento.INGRESO, monto = 2_500_000,
                categoria = Categoria.SALARIO, fecha = instante(mes.atDay(1), 9), nota = "Salario")
            if (atras == 3) {
                lista += MovimientoEntity(usuarioId = usuarioId, tipo = TipoMovimiento.INGRESO, monto = 120_000,
                    categoria = Categoria.VENTAS, fecha = instante(mes.atDay(19), 16), nota = "Venta de libros usados")
            }
            GASTOS_MES.filter { it.dia <= ultimoDia }.forEach { p ->
                val monto = if (p.varia) ((p.monto * factor) / 1_000).roundToLong() * 1_000 else p.monto
                lista += MovimientoEntity(usuarioId = usuarioId, tipo = TipoMovimiento.GASTO, monto = monto,
                    categoria = p.categoria, fecha = instante(mes.atDay(p.dia), 8 + p.dia % 10), nota = p.nota,
                    metaId = if (p.categoria == Categoria.METAS) viaje else null)
            }
        }
        bd.movimientoDao().insertarTodos(lista)
    }
}
