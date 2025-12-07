package org.m415x.materialscalculator.ui.common

/**
 * Interfaz para compartir texto y generar PDFs.
 * Implementada por la plataforma específica.
 * 
 * @property shareText Comparte texto plano.
 * @property generateAndSharePdf Genera y comparte un PDF.
 */
interface ShareManager {
    fun shareText(content: String)
    fun generateAndSharePdf(title: String, content: String) // Simplificado por ahora
}

/**
 * Función expect para obtener la implementación
 * Implementada por la plataforma específica.
 * 
 * @return Instancia de ShareManager
 */
expect fun getShareManager(): ShareManager