package com.jmarias.chavos.data

import com.jmarias.chavos.data.local.MetaConAhorro
import com.jmarias.chavos.data.local.MetaDao
import com.jmarias.chavos.data.local.MetaEntity
import com.jmarias.chavos.data.local.MovimientoDao
import com.jmarias.chavos.data.local.MovimientoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Acceso a movimientos y metas de ahorro de cada usuario. */
class FinanzasRepository(
    private val movimientos: MovimientoDao,
    private val metas: MetaDao,
) {
    fun movimientos(usuarioId: Long): Flow<List<MovimientoEntity>> = movimientos.observarTodos(usuarioId)

    fun movimiento(id: Long): Flow<MovimientoEntity?> = movimientos.observar(id)

    suspend fun cargarMovimiento(id: Long): MovimientoEntity? = movimientos.porId(id)

    suspend fun guardar(movimiento: MovimientoEntity): Long =
        if (movimiento.id == 0L) {
            movimientos.insertar(movimiento)
        } else {
            movimientos.actualizar(movimiento)
            movimiento.id
        }

    suspend fun eliminarMovimiento(id: Long) = movimientos.eliminar(id)

    suspend fun todosLosMovimientos(usuarioId: Long): List<MovimientoEntity> = movimientos.todos(usuarioId)

    /** Lo ahorrado en cada meta es la suma de sus aportes: así hay una sola fuente de verdad. */
    fun metas(usuarioId: Long): Flow<List<MetaConAhorro>> =
        combine(metas.observar(usuarioId), movimientos.totalesPorMeta(usuarioId)) { lista, totales ->
            val porMeta = totales.associate { it.metaId to it.total }
            lista.map { MetaConAhorro(it, porMeta[it.id] ?: 0L) }
        }

    suspend fun todasLasMetas(usuarioId: Long): List<MetaEntity> = metas.todas(usuarioId)

    fun meta(id: Long): Flow<MetaEntity?> = metas.observarPorId(id)

    suspend fun crearMeta(meta: MetaEntity): Long = metas.insertar(meta)

    suspend fun eliminarMeta(id: Long) = metas.eliminar(id)
}
