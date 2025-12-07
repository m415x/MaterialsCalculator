package org.m415x.materialscalculator.ui.common

// Solo esta clase, sin duplicados
class WebShareManager : ShareManager {
    override fun shareText(content: String) {
        // TODO: Implementar con JS window.navigator.share
        println("Compartir: $content")
    }

    override fun generateAndSharePdf(title: String, content: String) {
        println("PDF no soportado en web aún")
    }
}

actual fun getShareManager(): ShareManager = WebShareManager()