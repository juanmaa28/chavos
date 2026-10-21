package com.jmarias.chavos.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Escribe el CSV en la caché y abre el menú de compartir del sistema. */
suspend fun compartirCsv(contexto: Context, contenido: String) {
    val archivo = withContext(Dispatchers.IO) {
        val carpeta = File(contexto.cacheDir, "exportes").apply { mkdirs() }
        File(carpeta, "movimientos-chavos.csv").apply { writeText(contenido) }
    }
    val uri = FileProvider.getUriForFile(contexto, "${contexto.packageName}.archivos", archivo)
    val envio = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Movimientos de Chavos")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    contexto.startActivity(Intent.createChooser(envio, "Exportar movimientos"))
}
