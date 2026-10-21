package com.jmarias.chavos.dominio

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Derivación de contraseñas con PBKDF2: nunca se guarda la contraseña, solo su hash y una sal. */
object Claves {
    private const val ITERACIONES = 60_000
    private const val BITS = 256

    fun nuevaSal(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun hash(clave: String, sal: String): String {
        val spec = PBEKeySpec(clave.toCharArray(), Base64.getDecoder().decode(sal), ITERACIONES, BITS)
        try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            return Base64.getEncoder().encodeToString(bytes)
        } finally {
            spec.clearPassword()
        }
    }

    fun verificar(clave: String, sal: String, hashGuardado: String): Boolean =
        MessageDigest.isEqual(hash(clave, sal).toByteArray(), hashGuardado.toByteArray())
}
