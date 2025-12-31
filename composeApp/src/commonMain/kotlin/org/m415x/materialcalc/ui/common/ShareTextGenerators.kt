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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.domain.common.metersToCm
import org.m415x.materialcalc.domain.model.ResultadoHormigon
import kotlin.math.ceil

/**
 * Genera el texto para compartir el resultado de un cálculo de hormigón.
 * Esta función es @Composable para poder acceder a los recursos de string.
 */
@Composable
fun rememberConcreteShareText(
    result: ResultadoHormigon,
    width: Double,
    length: Double,
    high: Double,
    quantity: Int,
    nameConcrete: String,
    proportionConcrete: String,
    appName: String
): String {
    // 1. Resolvemos todos los strings necesarios fuera del remember.
    // El remember se recalculará si cambia el idioma, lo cual es correcto.
    val titleStr = stringResource(Res.string.share_concrete_title)
    val sectionDetailsStr = stringResource(Res.string.share_section_details)
    val sectionMaterialsStr = stringResource(Res.string.share_section_materials)
    val generatedByStr = stringResource(Res.string.share_generated_by, appName)
    
    val unitBag = stringResource(Res.string.unit_bag)
    val unitBags = stringResource(Res.string.unit_bags)
    val unitM = stringResource(Res.string.unit_meters)
    val unitCm = stringResource(Res.string.unit_centimeters)
    val unitU = stringResource(Res.string.unit_units)
    val unitKg = stringResource(Res.string.unit_kilograms)
    val unitM3 = stringResource(Res.string.unit_cubic_meters)
    val unitLt = stringResource(Res.string.unit_liters)

    val dimensionsStr = stringResource(Res.string.share_concrete_dimensions)
    val highStr = stringResource(Res.string.share_concrete_high)
    val quantityStr = stringResource(Res.string.share_concrete_quantity)
    val totalVolumeStr = stringResource(Res.string.share_concrete_total_volume)
    val concreteStr = stringResource(Res.string.concrete_title)
    val cementStr = stringResource(Res.string.share_concrete_cement)
    val aroundStr = stringResource(Res.string.share_concrete_around)
    val sandStr = stringResource(Res.string.share_concrete_sand)
    val gravelStr = stringResource(Res.string.share_concrete_gravel)
    val waterStr = stringResource(Res.string.share_concrete_water)
    val proportionStr = stringResource(Res.string.share_concrete_proportion)

    // 2. El remember ahora solo depende de los datos y los strings ya resueltos.
    return remember(result, width, length, high, quantity, nameConcrete, appName, titleStr,
        sectionDetailsStr, sectionMaterialsStr, generatedByStr, unitBag, unitBags, unitM, unitCm, unitU, unitKg,
        unitM3, unitLt, dimensionsStr, highStr, quantityStr, totalVolumeStr, concreteStr, cementStr, aroundStr,
        sandStr, gravelStr, waterStr, proportionStr) {

        val bagQty = ceil(result.cementoKg / result.bolsaCementoKg.toDouble()).toInt()
        val bagStr = if (quantity == 1) unitBag else unitBags
        val cementInBags = "$bagQty $bagStr"

        buildString {
            appendLine(titleStr)
            appendLine("=========================")
            appendLine()
            appendLine(sectionDetailsStr)
            appendLine("-------------------------")
            appendLine("*$dimensionsStr* $width x $length $unitM")
            appendLine("*$highStr* ${high.metersToCm} $unitCm")
            appendLine("*$quantityStr* $quantity $unitU")
            appendLine()
            appendLine("*$totalVolumeStr* ${result.volumenTotalM3.roundToDecimals(2)} $unitM3")
            appendLine()
            appendLine("*$concreteStr:* $nameConcrete")
            appendLine()
            appendLine(sectionMaterialsStr)
            appendLine("-------------------------")
            appendLine("● *$cementStr* ${result.cementoKg.roundToDecimals(1)} $unitKg")
            appendLine("    └ $aroundStr $cementInBags")
            appendLine("● *$sandStr* ${result.arenaM3.roundToDecimals(2)} $unitM3")
            appendLine("● *$gravelStr* ${result.piedraM3.roundToDecimals(2)} $unitM3")
            appendLine("● *$waterStr* ${result.aguaLitros.roundToDecimals(1)} $unitLt")
            appendLine()
            appendLine("*$proportionStr*")
            appendLine(proportionConcrete)
            appendLine()
            appendLine("_________________________")
            append("_${generatedByStr}_")
        }
    }
}
