package com.jmarias.chavos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UsuarioEntity::class, MetaEntity::class, MovimientoEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class ChavosDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun metaDao(): MetaDao
}
