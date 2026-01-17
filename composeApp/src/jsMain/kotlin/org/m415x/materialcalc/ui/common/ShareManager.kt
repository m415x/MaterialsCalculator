/*
 * materialCalc
 * Copyright (C) 2026 M415X
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

import kotlinx.browser.window

actual fun getShareManager(): ShareManager = object : ShareManager {
    override fun shareText(content: String) {
        val nav = window.navigator.asDynamic()
        if (nav.share != null) {
            // Corregido: 'content' en lugar de 'text' que no existe
            nav.share(js("({text: content})")) 
        } else {
            window.navigator.clipboard.writeText(content)
            window.alert("Copiado al portapapeles")
        }
    }

    override fun generateAndSharePdf(title: String, content: String) {
        window.print()
    }
}