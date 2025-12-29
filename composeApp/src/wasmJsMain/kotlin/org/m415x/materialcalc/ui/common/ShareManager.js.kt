package org.m415x.materialcalc.ui.common

import kotlinx.browser.window

actual fun getShareManager(): ShareManager = object : ShareManager {
    override fun shareText(text: String) {
        // En Wasm, lo más seguro sin librerías externas de interop
        // es copiar al portapapeles.
        window.navigator.clipboard.writeText(text)
        window.alert("Copiado al portapapeles")
    }

    override fun generateAndSharePdf(title: String, content: String) {
        // Al igual que en JS, abrimos el diálogo de impresión
        window.print()
    }
}