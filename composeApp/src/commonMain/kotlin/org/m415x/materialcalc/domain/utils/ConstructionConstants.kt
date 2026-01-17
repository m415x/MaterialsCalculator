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

package org.m415x.materialcalc.domain.utils

import kotlin.math.round

/**
 * Constantes físicas de materiales para conversiones.
 * Valores de Densidad Aparente (Suelto) en kg/m³. *
 * NOTA: Estos valores son aproximados y pueden variar significativamente
 * según la humedad, granulometría, compactación y tipo específico de material.
 * Se utilizan para estimaciones y cálculos de dosificación en obra.
 */
object ConstructionConstants {
    // Densidades Aparentes (Suelto, como se carga en balde)
    // Estos son los valores estándar para convertir Volumen <-> Peso en obra.
    const val APPARENT_CEMENT_DENSITY = 1400.0 // kg/m³ (Valor estándar para cálculos de mezcla)
    const val APPARENT_LIME_DENSITY = 600.0      // kg/m³ (Varía mucho, 600 es promedio para cal hidratada)
    const val APPARENT_SAND_DENSITY = 1500.0   // kg/m³ (Seca/Húmeda varía, 1500 es estándar)
    const val APPARENT_GRAVEL_DENSITY = 1600.0  // kg/m³

    // Coeficientes de Aporte (Volumen Real / Volumen Aparente)
    // Fuente: Chandias / Manuales de Construcción
    const val COEF_CEMENT = 0.47
    const val COEF_LIME = 0.37 // Polvo
    const val COEF_SAND = 0.63
    const val COEF_GRAVEL = 0.51
    const val COEF_WATER = 1.0

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