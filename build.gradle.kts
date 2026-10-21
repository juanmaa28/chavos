plugins {
    alias(libs.plugins.android.application) apply false
    // Fija la versión de Kotlin: AGP 9 trae Kotlin integrado (2.2.10) y el compilador de Compose debe coincidir.
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
