package com.jmarias.chavos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Consultas sobre la tabla de usuarios. */
@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun porCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun porId(id: Long): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun observar(id: Long): Flow<UsuarioEntity?>

    @Query("UPDATE usuarios SET nombre = :nombre WHERE id = :id")
    suspend fun actualizarNombre(id: Long, nombre: String)

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int
}

@Dao
interface MovimientoDao {
    @Query("SELECT * FROM movimientos WHERE usuarioId = :usuarioId ORDER BY fecha DESC, id DESC")
    fun observarTodos(usuarioId: Long): Flow<List<MovimientoEntity>>

    @Query("SELECT * FROM movimientos WHERE usuarioId = :usuarioId ORDER BY fecha DESC, id DESC")
    suspend fun todos(usuarioId: Long): List<MovimientoEntity>

    @Query("SELECT * FROM movimientos WHERE id = :id")
    fun observar(id: Long): Flow<MovimientoEntity?>

    @Query("SELECT * FROM movimientos WHERE id = :id")
    suspend fun porId(id: Long): MovimientoEntity?

    @Insert
    suspend fun insertar(movimiento: MovimientoEntity): Long

    @Insert
    suspend fun insertarTodos(movimientos: List<MovimientoEntity>)

    @Update
    suspend fun actualizar(movimiento: MovimientoEntity)

    @Query("DELETE FROM movimientos WHERE id = :id")
    suspend fun eliminar(id: Long)

    @Query(
        "SELECT metaId, SUM(monto) AS total FROM movimientos " +
            "WHERE usuarioId = :usuarioId AND metaId IS NOT NULL GROUP BY metaId",
    )
    fun totalesPorMeta(usuarioId: Long): Flow<List<TotalMeta>>
}

@Dao
interface MetaDao {
    @Query("SELECT * FROM metas WHERE usuarioId = :usuarioId ORDER BY creadaEn ASC, id ASC")
    fun observar(usuarioId: Long): Flow<List<MetaEntity>>

    @Query("SELECT * FROM metas WHERE usuarioId = :usuarioId ORDER BY creadaEn ASC, id ASC")
    suspend fun todas(usuarioId: Long): List<MetaEntity>

    @Query("SELECT * FROM metas WHERE id = :id")
    fun observarPorId(id: Long): Flow<MetaEntity?>

    @Insert
    suspend fun insertar(meta: MetaEntity): Long

    @Query("DELETE FROM metas WHERE id = :id")
    suspend fun eliminar(id: Long)
}
