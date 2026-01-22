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

import org.m415x.materialcalc.domain.model.ConcreteDosing
import org.m415x.materialcalc.domain.model.MortarDosing
import org.m415x.materialcalc.domain.model.TextSource
import org.m415x.materialcalc.domain.utils.ConstructionConstants.APPARENT_CEMENT_DENSITY
import org.m415x.materialcalc.domain.utils.ConstructionConstants.APPARENT_LIME_DENSITY
import org.m415x.materialcalc.domain.utils.ConstructionConstants.formatPart

/**
 * Convierte la dosificación técnica de hormigón a proporción volumétrica (1:3:3).
 *
 * @param ConcreteDosing Objeto que contiene la dosificación en kg y m³.
 * @return Un TextSource representando la proporción volumétrica.
 */
fun ConcreteDosing.estimateProportionTxt(): TextSource {
    // 1. SI TENEMOS EL DATO ORIGINAL, LO USAMOS (Prioridad Absoluta)
    // Si es un recurso, lo devolvemos tal cual. Si es Raw y no está vacío, también.
    if (descriptionProportion is TextSource.Resource) return descriptionProportion
    if (descriptionProportion is TextSource.Raw && descriptionProportion.text.isNotBlank()) return descriptionProportion

    // 2. Calcular volumen aparente del cemento (el "1" de la fórmula)
    val cementVolume = this.cementKg / APPARENT_CEMENT_DENSITY

    if (cementVolume <= 0.001) return TextSource.Raw("Sin Cemento")

    // 3. Calcular partes relativas
    // Como arenaM3 y piedraM3 ya son volumen, solo dividimos por el volumen del cemento
    val sandPart = this.sandM3 / cementVolume
    val gravelPart = this.gravelM3 / cementVolume

    val text = buildString {
        append("1") // Cemento
        append(":${formatPart(sandPart)}")
        append(":${formatPart(gravelPart)}")
        append(" (Cem:Arena:Piedra)")
    }
    return TextSource.Raw(text)
}

/**
 * Intenta convertir la dosificación técnica (kg) a una proporción volumétrica legible (1:3).
 *
 * @param MortarDosing Objeto que contiene la dosificación en kg.
 * @return Un TextSource representando la proporción volumétrica.
 */
fun MortarDosing.estimateProportionTxt(): TextSource {
    // 1. SI TENEMOS EL DATO ORIGINAL, LO USAMOS (Prioridad Absoluta)
    // Nota: MortarDosing.mixingRatio es el equivalente a descriptionProportion
    if (mixingRatio is TextSource.Resource) return mixingRatio
    if (mixingRatio is TextSource.Raw && mixingRatio.text.isNotBlank()) return mixingRatio

    // 2. Si no (ej: receta vieja o manual), usamos la estimación matemática
    val cementVolume = this.cementKg / APPARENT_CEMENT_DENSITY

    // Si no hay cemento, es raro, devolvemos vacío o manejo especial
    if (cementVolume <= 0.001) return TextSource.Raw("Sin Cemento")

    // 3. Normalizamos dividiendo todo por el volumen del cemento (El cemento es el "1")
    val cementPart = 1.0
    val limePart = if (this.limeKg > 0) (this.limeKg / APPARENT_LIME_DENSITY) / cementVolume else 0.0
    val sandPart = this.sandM3 / cementVolume // La arena ya está en m3
    // val parteAgua = ... (Generalmente no se pone en el 1:3:3, es a ojo)

    val text = buildString {
        append(formatPart(cementPart))

        if (limePart > 0.1) {
            append(":${formatPart(limePart)}")
        }

        append(":${formatPart(sandPart)}")

        // Agregamos leyenda
        append(" (Cem")
        if (limePart > 0.1) append(":Cal")
        append(":Arena)")
    }
    return TextSource.Raw(text)
}