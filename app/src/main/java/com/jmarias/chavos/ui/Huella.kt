package com.jmarias.chavos.ui

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** Autenticación con huella mediante BiometricPrompt. */
object Huella {
    fun disponible(contexto: Context): Boolean =
        BiometricManager.from(contexto).canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

    fun autenticar(
        actividad: FragmentActivity,
        titulo: String,
        subtitulo: String,
        onExito: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val aviso = BiometricPrompt(
            actividad,
            ContextCompat.getMainExecutor(actividad),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onExito()

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    val cancelado = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_CANCELED
                    if (!cancelado) onError(errString.toString())
                }
            },
        )
        aviso.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(titulo)
                .setSubtitle(subtitulo)
                .setNegativeButtonText("Cancelar")
                .setAllowedAuthenticators(BIOMETRIC_WEAK)
                .build(),
        )
    }
}
