package com.jmarias.chavos.data

import com.jmarias.chavos.data.local.UsuarioDao
import com.jmarias.chavos.data.local.UsuarioEntity
import com.jmarias.chavos.dominio.Claves
import com.jmarias.chavos.dominio.Validacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AuthRepository(private val usuarios: UsuarioDao, private val cuentaDemo: suspend () -> Long) {

    /** Id de la cuenta con datos de ejemplo, para explorar la app sin registrarse. */
    suspend fun demo(): Long = cuentaDemo()

    suspend fun correoRegistrado(correo: String): Boolean =
        usuarios.porCorreo(Validacion.normalizarCorreo(correo)) != null

    suspend fun existe(id: Long): Boolean = usuarios.porId(id) != null

    /** Crea la cuenta y devuelve su id. El hash se calcula fuera del hilo principal. */
    suspend fun registrar(nombre: String, correo: String, clave: String): Long {
        val sal = Claves.nuevaSal()
        val hash = withContext(Dispatchers.Default) { Claves.hash(clave, sal) }
        return usuarios.insertar(
            UsuarioEntity(
                nombre = nombre.trim(),
                correo = Validacion.normalizarCorreo(correo),
                hash = hash,
                sal = sal,
                creadoEn = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun autenticar(correo: String, clave: String): UsuarioEntity? {
        val usuario = usuarios.porCorreo(Validacion.normalizarCorreo(correo)) ?: return null
        val valida = withContext(Dispatchers.Default) { Claves.verificar(clave, usuario.sal, usuario.hash) }
        return if (valida) usuario else null
    }

    fun usuario(id: Long): Flow<UsuarioEntity?> = usuarios.observar(id)

    suspend fun actualizarNombre(id: Long, nombre: String) = usuarios.actualizarNombre(id, nombre.trim())
}
