package org.m415x.materialscalculator.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

// Necesitamos un contexto global o inyectado.
// Para simplificar este ejemplo, usaremos una variable estática inicializada en MainActivity.
// (En una app real, usa Koin o Hilt).
@SuppressLint("StaticFieldLeak")
object AndroidContext {
    lateinit var context: Context
}

class AndroidShareManager : ShareManager {

    private val context = AndroidContext.context

    override fun shareText(content: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartir cálculo")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    override fun generateAndSharePdf(title: String, content: String) {
        // 1. Crear documento
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // 2. Dibujar contenido (Muy básico, línea por línea)
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText(title, 40f, 50f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        var y = 90f

        // Dividimos el texto por líneas para dibujarlo
        content.lines().forEach { line ->
            // Lógica simple para negritas simuladas (si empieza con *)
            if (line.trim().startsWith("*")) paint.isFakeBoldText = true else paint.isFakeBoldText = false
            val cleanLine = line.replace("*", "") // Limpiamos markdown

            canvas.drawText(cleanLine, 40f, y, paint)
            y += 25f
        }

        pdfDocument.finishPage(page)

        // 3. Guardar archivo temporal
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Calculo_Materiales.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfDocument.close()

        // 4. Compartir PDF
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartir PDF"))
    }
}

// Implementación del expect
actual fun getShareManager(): ShareManager = AndroidShareManager()