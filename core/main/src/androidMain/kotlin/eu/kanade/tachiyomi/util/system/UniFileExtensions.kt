package eu.kanade.tachiyomi.util.system

import android.content.Context
import android.os.Build
import android.os.FileUtils
import com.hippo.unifile.UniFile
import java.io.BufferedOutputStream
import java.io.File

val UniFile.nameWithoutExtension: String?
    get() = name?.substringBeforeLast('.')

val UniFile.extension: String?
    get() = name?.replace("${nameWithoutExtension.orEmpty()}.", "")

val UniFile.displayablePath: String
    get() = filePath ?: uri.toString()

/**
 * Intenta copiar el contenido de este UniFile a un archivo temporal.
 * Devuelve null si falla por falta de permisos u otro error.
 */
fun UniFile.toTempFile(context: Context): File? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val tempFile = File.createTempFile(nameWithoutExtension.orEmpty(), null)
            tempFile.outputStream().use { output ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    FileUtils.copy(inputStream, output)
                } else {
                    val buffer = ByteArray(8192)
                    var count: Int
                    while (inputStream.read(buffer).also { count = it } > 0) {
                        output.write(buffer, 0, count)
                    }
                }
            }
            tempFile
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Lee el contenido del archivo como texto. Retorna null si falla.
 */
fun UniFile.readTextSafely(context: Context): String? {
    return try {
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Escribe una cadena de texto en el archivo actual. Ejecuta [onComplete] al terminar.
 */
fun UniFile.writeText(string: String, onComplete: () -> Unit = {}) {
    try {
        this.openOutputStream()?.use {
            it.write(string.toByteArray())
            onComplete()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
