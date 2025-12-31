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

import org.m415x.materialcalc.domain.model.DosificacionHormigon
import org.m415x.materialcalc.domain.model.DosificacionMortero
import org.m415x.materialcalc.domain.utils.ConstructionConstants.DENSIDAD_CAL_SUELTA
import org.m415x.materialcalc.domain.utils.ConstructionConstants.DENSIDAD_CEMENTO_SUELTO
import org.m415x.materialcalc.domain.utils.ConstructionConstants.formatPart

/**
 * Convierte la dosificación técnica de hormigón a proporción volumétrica (1:3:3).
 */
fun DosificacionHormigon.estimarProporcionTexto(): String {
    // 1. SI TENEMOS EL DATO ORIGINAL, LO USAMOS (Prioridad Absoluta)
    if (descripcionProporcion.isNotBlank()) return descripcionProporcion

    // 2. Calcular volumen aparente del cemento (el "1" de la fórmula)
    val volCemento = this.cementoKg / DENSIDAD_CEMENTO_SUELTO

    if (volCemento <= 0.001) return "Sin Cemento"

    // 3. Calcular partes relativas
    // Como arenaM3 y piedraM3 ya son volumen, solo dividimos por el volumen del cemento
    val parteArena = this.arenaM3 / volCemento
    val partePiedra = this.piedraM3 / volCemento

    return buildString {
        append("1") // Cemento
        append(":${formatPart(parteArena)}")
        append(":${formatPart(partePiedra)}")
        append(" (Cem:Arena:Piedra)")
    }
}

/**
 * Intenta convertir la dosificación técnica (kg) a una proporción volumétrica legible (1:3).
 */
fun DosificacionMortero.estimarProporcionTexto(): String {
    // 1. SI TENEMOS EL DATO ORIGINAL, LO USAMOS (Prioridad Absoluta)
    if (!partes.isNullOrBlank()) return partes

    // 2. Si no (ej: receta vieja o manual), usamos la estimación matemática
    val volCemento = this.cementoKg / DENSIDAD_CEMENTO_SUELTO

    // Si no hay cemento, es raro, devolvemos vacío o manejo especial
    if (volCemento <= 0.001) return "Sin Cemento"

    // 3. Normalizamos dividiendo todo por el volumen del cemento (El cemento es el "1")
    val parteCemento = 1.0
    val parteCal = if (this.calKg > 0) (this.calKg / DENSIDAD_CAL_SUELTA) / volCemento else 0.0
    val parteArena = this.arenaM3 / volCemento // La arena ya está en m3
    // val parteAgua = ... (Generalmente no se pone en el 1:3:3, es a ojo)

    return buildString {
        append(formatPart(parteCemento))

        if (parteCal > 0.1) {
            append(":${formatPart(parteCal)}")
        }

        append(":${formatPart(parteArena)}")

        // Agregamos leyenda
        append(" (Cem")
        if (parteCal > 0.1) append(":Cal")
        append(":Arena)")
    }
}