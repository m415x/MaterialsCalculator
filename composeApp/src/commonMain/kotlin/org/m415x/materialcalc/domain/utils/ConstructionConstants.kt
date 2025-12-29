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

package org.m415x.materialcalc.domain.utils

import kotlin.math.round

/**
 * Constantes físicas de materiales para conversiones.
 * Valores de Densidad Aparente (Suelto) en kg/m³.
 */
object ConstructionConstants {
    // Densidades Aparentes (Suelto, como se carga en balde)
    // Estos son los valores estándar para convertir Volumen <-> Peso en obra.
    const val DENSIDAD_CEMENTO_SUELTO = 1200.0 // kg/m³ (Una bolsa de 50kg ocupa aprox 0.041 m³ o 41 litros)
    const val DENSIDAD_CAL_SUELTA = 600.0      // kg/m³ (Varía mucho, 600 es promedio para cal hidratada)
    const val DENSIDAD_ARENA_SUELTA = 1500.0   // kg/m³ (Seca/Húmeda varía, 1500 es estándar)
    const val DENSIDAD_PIEDRA_SUELTA = 1600.0  // kg/m³

    // Para visualización: Si el decimal es muy cercano a .0 o .5, lo redondeamos visualmente
    fun formatPart(value: Double): String {
        val rounded = (round(value * 2) / 2.0) // Redondea a 0.5 más cercano
        return if (rounded % 1 == 0.0) {
            rounded.toInt().toString()
        } else {
            rounded.toString()
        }
    }
}