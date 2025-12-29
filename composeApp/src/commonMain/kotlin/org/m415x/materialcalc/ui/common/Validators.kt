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
 * Valida que una serie de valores numéricos (Enteros o Decimales) sean:
 * 1. No nulos (que la conversión de String a Double haya funcionado).
 * 2. Mayores a Cero (no aceptamos negativos ni dimensiones de 0).
 *
 * @param values Valores a validar.
 * @return True si todos los valores son válidos, false en caso contrario.
 */
fun areValidDimensions(vararg values: Number?): Boolean {
    // .all devuelve true solo si la condición se cumple para TODOS los elementos
    return values.all {
        // Convertimos a Double para unificar la comparación
        it != null && it.toDouble() > 0.0
    }
}