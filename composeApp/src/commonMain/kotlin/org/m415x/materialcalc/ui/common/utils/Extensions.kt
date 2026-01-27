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

package org.m415x.materialcalc.ui.common.utils

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlin.math.round

/**
 * Función extendida para redondear Doubles fácilmente en toda la app
 *
 * @param decimals Número de decimales a redondear
 * @return Cadena de texto con el número redondeado
 */
fun Double.roundToDecimals(decimals: Int): String {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    val rounded = round(this * multiplier) / multiplier
    return rounded.toString()
}

/**
 * Limpia y convierte el String a un formato numérico seguro para Kotlin
 *
 * @return Double? con el valor convertido o null si no es un número válido
 */
fun String.toSafeDoubleOrNull(): Double? {
    // 1. Reemplaza todas las comas por puntos.
    val cleaned = this.replace(',', '.')

    // 2. Intenta convertir el string limpio a Double.
    return cleaned.toDoubleOrNull()
}

/**
 * Extensión mágica para cerrar el teclado al tocar fuera
 *
 * @return Modifier con la funcionalidad agregada
 */
fun Modifier.clearFocusOnTap(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    this.pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus()
        })
    }
}

/**
 * Convierte un String con formato **bold** en un AnnotatedString de Compose.
 * Ideal para no ensuciar los strings.xml con placeholders de formato.
 */
fun String.toAnnotatedString(
    boldStyle: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold)
): AnnotatedString {
    val parts = this.split("**")
    return buildAnnotatedString {
        parts.forEachIndexed { index, part ->
            if (index % 2 != 0) {
                // Es una parte impar, por lo tanto estaba entre ** **
                withStyle(style = boldStyle) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }
}