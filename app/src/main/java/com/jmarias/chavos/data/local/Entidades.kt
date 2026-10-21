package com.jmarias.chavos.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TipoMovimiento { INGRESO, GASTO }

enum class Categoria(val etiqueta: String, val corta: String, val tipo: TipoMovimiento) {
    ALIMENTACION("Alimentación", "Alimentos", TipoMovimiento.GASTO),
    TRANSPORTE("Transporte", "Transporte", TipoMovimiento.GASTO),
    OCIO("Ocio y suscripciones", "Ocio", TipoMovimiento.GASTO),
    SALUD("Salud", "Salud", TipoMovimiento.GASTO),
    HOGAR("Hogar", "Hogar", TipoMovimiento.GASTO),
    EDUCACION("Educación", "Estudio", TipoMovimiento.GASTO),
    METAS("Aportes a metas", "Metas", TipoMovimiento.GASTO),
    OTROS_GASTOS("Otros", "Otros", TipoMovimiento.GASTO),
    SALARIO("Salario", "Salario", TipoMovimiento.INGRESO),
    VENTAS("Ventas", "Ventas", TipoMovimiento.INGRESO),
    REGALOS("Regalos", "Regalos", TipoMovimiento.INGRESO),
    OTROS_INGRESOS("Otros ingresos", "Otros", TipoMovimiento.INGRESO);

    companion object {
        fun de(tipo: TipoMovimiento): List<Categoria> = entries.filter { it.tipo == tipo }
    }
}

enum class IconoMeta { VIAJE, EMERGENCIA, TECNOLOGIA, HOGAR, ESTUDIO, REGALO, OTRA }

@Entity(tableName = "usuarios", indices = [Index(value = ["correo"], unique = true)])
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val hash: String,
    val sal: String,
    val creadoEn: Long,
)

@Entity(
    tableName = "metas",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("usuarioId")],
)
data class MetaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val nombre: String,
    val objetivo: Long,
    /** Día de la fecha límite (epochDay); null si la meta no tiene fecha. */
    val fechaLimite: Long?,
    val icono: IconoMeta,
    val creadaEn: Long,
)

@Entity(
    tableName = "movimientos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE,
        ),
        // Si se borra una meta, sus aportes siguen contando como gasto: solo se pierde el vínculo.
        ForeignKey(
            entity = MetaEntity::class,
            parentColumns = ["id"],
            childColumns = ["metaId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("usuarioId"), Index("metaId")],
)
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val tipo: TipoMovimiento,
    /** Monto en pesos, siempre positivo; el signo lo da [tipo]. */
    val monto: Long,
    val categoria: Categoria,
    /** Instante del movimiento en milisegundos desde epoch. */
    val fecha: Long,
    val nota: String,
    val metaId: Long? = null,
) {
    val esIngreso: Boolean get() = tipo == TipoMovimiento.INGRESO
}

data class TotalMeta(val metaId: Long, val total: Long)

data class MetaConAhorro(val meta: MetaEntity, val ahorrado: Long) {
    val fraccion: Float get() = if (meta.objetivo <= 0) 0f else (ahorrado.toFloat() / meta.objetivo).coerceIn(0f, 1f)
    val porcentaje: Int get() = if (meta.objetivo <= 0) 0 else (ahorrado * 100 / meta.objetivo).toInt().coerceIn(0, 100)
}
