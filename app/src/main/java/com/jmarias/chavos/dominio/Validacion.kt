package com.jmarias.chavos.dominio

/** Reglas de validación de los campos de los formularios. */
object Validacion {
    private val CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val ESPACIOS = Regex("\\s+")

    const val LARGO_MINIMO_CLAVE = 8

    fun correoValido(correo: String): Boolean = CORREO.matches(correo.trim())

    fun normalizarCorreo(correo: String): String = correo.trim().lowercase()

    /** De 0 a 3: largo mínimo, mezcla de letras y números, y largo amplio o símbolos. */
    fun fortalezaClave(clave: String): Int {
        if (clave.isEmpty()) return 0
        var puntos = 0
        if (clave.length >= LARGO_MINIMO_CLAVE) puntos++
        if (clave.any(Char::isLetter) && clave.any(Char::isDigit)) puntos++
        if (clave.length >= 12 || clave.any { !it.isLetterOrDigit() }) puntos++
        return puntos
    }

    fun etiquetaFortaleza(puntos: Int): String = when (puntos) {
        0, 1 -> "Contraseña débil"
        2 -> "Contraseña aceptable"
        else -> "Contraseña segura"
    }

    fun iniciales(nombre: String): String =
        nombre.trim().split(ESPACIOS).filter { it.isNotEmpty() }.take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }

    fun primerNombre(nombre: String): String = nombre.trim().split(ESPACIOS).firstOrNull().orEmpty()
}
