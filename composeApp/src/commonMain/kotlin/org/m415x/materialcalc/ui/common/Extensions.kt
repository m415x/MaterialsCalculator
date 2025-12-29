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

import kotlin.math.round
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

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