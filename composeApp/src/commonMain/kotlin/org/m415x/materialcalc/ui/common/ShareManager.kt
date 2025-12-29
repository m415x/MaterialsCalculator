/*
 * materialCalc
 * Copyright (C) 2025 M415X
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.m415x.materialcalc.ui.common

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