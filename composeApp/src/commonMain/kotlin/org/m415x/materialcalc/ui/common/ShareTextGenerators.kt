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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.domain.common.metersToCm
import org.m415x.materialcalc.domain.model.*
import kotlin.math.ceil

/**
 * Genera un texto para compartir el resultado de un cálculo de concreto.
 *
 * @param result Resultado del cálculo de concreto.
 * @param width Ancho del concreto.
 * @param length Largo del concreto.
 * @param thickness Espesor del concreto.
 * @param quantity Cantidad de concreto.
 * @param nameConcrete Nombre del concreto.
 * @param proportionConcrete Proporción del concreto.
 * @param appName Nombre de la aplicación.
 * @return Texto para compartir.
 */
@Composable
fun rememberConcreteShareText(
    result: ConcreteResult,
    width: Double,
    length: Double,
    thickness: Double,
    quantity: Int,
    nameConcrete: String,
    proportionConcrete: String,
    appName: String
): String {
    val titleStr = stringResource(Res.string.share_concrete_title)
    val sectionDetailsStr = stringResource(Res.string.share_section_details)
    val sectionMaterialsStr = stringResource(Res.string.share_section_materials)
    val generatedByStr = stringResource(Res.string.share_generated_by, appName)
    
    val unitBag = stringResource(Res.string.unit_bag)
    val unitBags = stringResource(Res.string.unit_bags)
    val unitM = stringResource(Res.string.unit_meters)
    val unitCm = stringResource(Res.string.unit_centimeters)
//    val unitU = stringResource(Res.string.unit_units)
    val unitKg = stringResource(Res.string.unit_kilograms)
    val unitM3 = stringResource(Res.string.unit_cubic_meters)
    val unitLt = stringResource(Res.string.unit_liters)

    val dimensionsStr = stringResource(Res.string.share_common_dimensions)
    val thicknessStr = stringResource(Res.string.share_concrete_thickness)
//    val quantityStr = stringResource(Res.string.share_concrete_quantity)
    val totalVolumeStr = stringResource(Res.string.share_concrete_total_volume)
    val concreteStr = stringResource(Res.string.concrete_title)
    val cementStr = stringResource(Res.string.share_common_cement)
    val aroundStr = stringResource(Res.string.share_common_around)
    val sandStr = stringResource(Res.string.share_common_sand)
    val gravelStr = stringResource(Res.string.share_concrete_gravel)
    val waterStr = stringResource(Res.string.share_common_water)
    val proportionStr = stringResource(Res.string.share_common_proportion)

    return remember(result, width, length, thickness, quantity, nameConcrete, appName, titleStr) {
        val bagQty = ceil(result.cementKg / result.cementBagKg.toDouble()).toInt()
        val bagStr = if (bagQty == 1) unitBag else unitBags
        val cementInBags = "$bagQty $bagStr"

        buildString {
            appendLine(titleStr)
            appendLine("=========================")
            appendLine()
            appendLine(sectionDetailsStr)
            appendLine("-------------------------")
            appendLine("*$dimensionsStr* ${width}x$length $unitM")
            appendLine("*$thicknessStr* ${thickness.metersToCm} $unitCm")
//            appendLine("*$quantityStr* $quantity $unitU")
            appendLine()
            appendLine("*$totalVolumeStr* ${result.totalVolumeM3.roundToDecimals(2)} $unitM3")
            appendLine("*$concreteStr:* $nameConcrete")
            appendLine()
            appendLine(sectionMaterialsStr)
            appendLine("-------------------------")
            appendLine("● *$cementStr* ${result.cementKg.roundToDecimals(1)} $unitKg")
            appendLine("    └ $aroundStr $cementInBags")
            appendLine("● *$sandStr* ${result.sandM3.roundToDecimals(2)} $unitM3")
            appendLine("● *$gravelStr* ${result.gravelM3.roundToDecimals(2)} $unitM3")
            appendLine("● *$waterStr* ${result.waterLiters.roundToDecimals(1)} $unitLt")
            appendLine()
            if (proportionConcrete.isNotBlank()) {
                appendLine("*$proportionStr*")
                appendLine(proportionConcrete)
                appendLine()
            }
            appendLine("_________________________")
            append("_${generatedByStr}_")
        }
    }
}

/**
 * Genera un texto para compartir el resultado de un cálculo de pared.
 *
 * @param result Resultado del cálculo de pared.
 * @param length Largo de la pared.
 * @param height Altura de la pared.
 * @param brickType Tipo de ladrillos.
 * @param brickDetail Detalles del tipo de ladrillos.
 * @param openings Lista de aberturas en la pared.
 * @param mixDetail Detalles de la mezcla.
 * @param appName Nombre de la aplicación.
 * @return Texto para compartir.
 */
@Composable
fun rememberWallShareText(
    result: WallResult,
    length: Double,
    height: Double,
    brickType: String,
    brickDetail: String,
    openings: List<Aperture>,
    mixDetail: String,
    appName: String
): String {
    val titleStr = stringResource(Res.string.share_wall_title)
    val sectionDetailsStr = stringResource(Res.string.share_section_details)
    val sectionMaterialsStr = stringResource(Res.string.share_section_materials)
    val generatedByStr = stringResource(Res.string.share_generated_by, appName)

    val unitBag = stringResource(Res.string.unit_bag)
    val unitBags = stringResource(Res.string.unit_bags)
    val unitM = stringResource(Res.string.unit_meters)
    val unitM2 = stringResource(Res.string.unit_square_meters)
    val unitM3 = stringResource(Res.string.unit_cubic_meters)
    val unitKg = stringResource(Res.string.unit_kilograms)
    val unitLt = stringResource(Res.string.unit_liters)
    val unitU = stringResource(Res.string.unit_units)

    val dimensionsStr = stringResource(Res.string.share_common_dimensions)
    val brickStr = stringResource(Res.string.share_wall_brick)
    val surfacesStr = stringResource(Res.string.share_wall_surfaces)
    val totalSurfaceStr = stringResource(Res.string.share_wall_total_surface)
    val openingsSurfaceStr = stringResource(Res.string.share_wall_openings_surface)
    val netSurfaceStr = stringResource(Res.string.share_wall_net_surface)
    val openingsTitleStr = stringResource(Res.string.share_wall_openings_title)
    val noOpeningsStr = stringResource(Res.string.share_wall_no_openings)
    val bricksQtyStr = stringResource(Res.string.share_wall_bricks_qty)
    val mortarQtyFormat = stringResource(
        Res.string.share_wall_mortar_qty,
        result.mortarM3.roundToDecimals(2),
        stringResource(Res.string.unit_cubic_meters)
    )
    val cementStr = stringResource(Res.string.share_common_cement)
    val limeStr = stringResource(Res.string.share_common_lime)
    val sandStr = stringResource(Res.string.share_common_sand)
    val waterStr = stringResource(Res.string.share_common_water)
    val aroundStr = stringResource(Res.string.share_common_around)
    val proportionStr = stringResource(Res.string.share_common_proportion)

    return remember(result, length, height, brickType, brickDetail, openings, mixDetail, appName) {
        val grossArea = length * height
        val grossOpenings = openings.sumOf { it.widthMeters * it.heightMeters * it.quantity }

        val openingDetail = if (openings.isEmpty()) {
            "    $noOpeningsStr"
        } else {
            openings.joinToString("\n") { "    ‣ ${it.quantity} x ${it.name}: ${it.widthMeters}x${it.heightMeters} $unitM" }
        }

        val cementBags = ceil(result.cementKg / result.cementBagKg.toDouble()).toInt()
        val cementBagStr = if (cementBags == 1) unitBag else unitBags

        val limeBags = if (result.limeKg > 0) ceil(result.limeKg / result.limeBagKg.toDouble()).toInt() else 0
        val limeBagStr = if (limeBags == 1) unitBag else unitBags

        buildString {
            appendLine(titleStr)
            appendLine("=========================")
            appendLine()
            appendLine(sectionDetailsStr)
            appendLine("-------------------------")
            appendLine("*$dimensionsStr* ${length}x$height $unitM")
            appendLine("*$brickStr* $brickType")
            appendLine("    └ $brickDetail")
            appendLine()
            appendLine(openingsTitleStr)
            appendLine(openingDetail)
            appendLine()
            appendLine(surfacesStr)
            appendLine("● $totalSurfaceStr ${grossArea.roundToDecimals(2)} $unitM2")
            appendLine("● $openingsSurfaceStr ${grossOpenings.roundToDecimals(2)} $unitM2")
            appendLine("● $netSurfaceStr ${result.netAreaM2.roundToDecimals(2)} $unitM2")
            appendLine()
            appendLine(sectionMaterialsStr)
            appendLine("-------------------------")
            appendLine("● $bricksQtyStr ${result.quantityBricks} $unitU")
            appendLine("● $mortarQtyFormat")
            appendLine("    ‣ $cementStr ${result.cementKg.roundToDecimals(1)} $unitKg")
            appendLine("        └ $aroundStr $cementBags $cementBagStr")
            if (result.limeKg > 0) {
                appendLine("    ‣ $limeStr ${result.limeKg.roundToDecimals(1)} $unitKg")
                appendLine("        └ $aroundStr $limeBags $limeBagStr")
            }
            appendLine("    ‣ $sandStr ${result.sandM3.roundToDecimals(2)} $unitM3")
            appendLine("    ‣ $waterStr ${result.waterLiters.roundToDecimals(1)} $unitLt")
            appendLine()
            if (mixDetail.isNotBlank()) {
                appendLine("*$proportionStr*")
                appendLine(mixDetail)
                appendLine()
            }
            appendLine("_________________________")
            append("_${generatedByStr}_")
        }
    }
}

/**
 * Genera un texto para compartir el resultado de un cálculo de estructura.
 */
@Composable
fun rememberStructureShareText(
    result: StructureResult,
    length: Double,
    sideA: Double,
    sideB: Double,
    isCircular: Boolean,
    concreteType: ConcreteType,
    stirrupSpacingCm: Double,
    appName: String
): String {
    val titleStr = stringResource(Res.string.share_structure_title)
    val sectionDetailsStr = stringResource(Res.string.share_section_details)
    val lengthStr = stringResource(Res.string.share_structure_length)
    val concreteSectionFormat = stringResource(
        Res.string.share_structure_concrete_section,
        result.volumeConcreteM3.roundToDecimals(2),
        stringResource(Res.string.unit_cubic_meters)
    )
    val typeStr = stringResource(Res.string.share_structure_type)
    val cementStr = stringResource(Res.string.share_common_cement)
    val sandStr = stringResource(Res.string.share_common_sand)
    val gravelStr = stringResource(Res.string.share_concrete_gravel)
    val waterStr = stringResource(Res.string.share_common_water)
    val aroundStr = stringResource(Res.string.share_common_around)
    val ironSectionStr = stringResource(Res.string.share_structure_iron_section)
    val mainIronStr = stringResource(Res.string.share_structure_main_iron)
    val stirrupIronStr = stringResource(Res.string.share_structure_stirrup_iron)
    val rodsStr = stringResource(Res.string.share_structure_rods)
    val totalWeightStr = stringResource(Res.string.share_structure_total_weight)
    val buyStr = stringResource(Res.string.share_structure_buy)
    val separationStr = stringResource(Res.string.share_structure_separation)
    val eachStr = stringResource(Res.string.share_structure_each)
    val bars12mStr = stringResource(Res.string.share_structure_bars_12m)
    val generatedByStr = stringResource(Res.string.share_generated_by, appName)

    val unitM = stringResource(Res.string.unit_meters)
    val unitCm = stringResource(Res.string.unit_centimeters)
    val unitMm = stringResource(Res.string.unit_millimeters)
    val unitKg = stringResource(Res.string.unit_kilograms)
    val unitM3 = stringResource(Res.string.unit_cubic_meters)
    val unitLt = stringResource(Res.string.unit_liters)
    val unitBag = stringResource(Res.string.unit_bag)
    val unitBags = stringResource(Res.string.unit_bags)

    return remember(result, length, sideA, sideB, isCircular, concreteType, stirrupSpacingCm, appName) {
        val geometryDetail =
            if (isCircular) "Columna Circular: Ø $sideA $unitM" else "Rectangular: $sideA x $sideB $unitM"

        val bagQty = ceil(result.cementKg / result.cementBagKg.toDouble()).toInt()
        val bagStr = if (bagQty == 1) unitBag else unitBags

        buildString {
            appendLine(titleStr)
            appendLine("=========================")
            appendLine()
            appendLine(sectionDetailsStr)
            appendLine("-------------------------")
            appendLine("*$lengthStr* $length $unitM")
            appendLine(geometryDetail)
            appendLine()
            appendLine(concreteSectionFormat)
            appendLine("-------------------------")
            appendLine("$typeStr ${concreteType.name}")
            appendLine()
            appendLine("● $cementStr ${result.cementKg.roundToDecimals(1)} $unitKg")
            appendLine("    └ $aroundStr $bagQty $bagStr")
            appendLine("● $sandStr ${result.sandM3.roundToDecimals(2)} $unitM3")
            appendLine("● $gravelStr ${result.gravelM3.roundToDecimals(2)} $unitM3")
            appendLine("● $waterStr ${result.waterLiters.roundToDecimals(0)} $unitLt")
            appendLine()
            appendLine(ironSectionStr)
            appendLine("-------------------------")
            appendLine(mainIronStr)
            appendLine("● $rodsStr Ø ${result.mainDiameter.milimeters} $unitMm")
            appendLine("● $totalWeightStr ${result.mainIronKg.roundToDecimals(1)} $unitKg")
            appendLine("    └ $buyStr ${result.mainIronAmount} $bars12mStr")
            appendLine()
            appendLine(stirrupIronStr)
            appendLine("● $rodsStr Ø ${result.stirrupDiameter.milimeters} $unitMm")
            appendLine("● $separationStr $eachStr ${stirrupSpacingCm.roundToDecimals(0)} $unitCm")
            appendLine("● $totalWeightStr ${result.stirrupIronKg.roundToDecimals(1)} $unitKg")
            appendLine("    └ $buyStr ${result.stirrupIronAmount} $bars12mStr")
            appendLine()
            appendLine("_________________________")
            append("_${generatedByStr}_")
        }
    }
}

/**
 * Genera un texto para compartir el resultado de un cálculo de losa.
 */
@Composable
fun rememberSlabShareText(
    result: SlabResult,
    width: Double,
    length: Double,
    thickness: Double,
    concreteType: ConcreteType,
    appName: String
): String {
    val titleStr = stringResource(Res.string.share_slab_title)
    val sectionDetailsStr = stringResource(Res.string.share_section_details)
    val dimensionsStr = stringResource(Res.string.share_common_dimensions)
    val thicknessStr = stringResource(Res.string.share_concrete_thickness)
    val concreteSectionFormat = stringResource(
        Res.string.share_structure_concrete_section,
        result.volumeConcreteM3.roundToDecimals(2),
        stringResource(Res.string.unit_cubic_meters)
    )
    val typeStr = stringResource(Res.string.share_structure_type)
    val cementStr = stringResource(Res.string.share_common_cement)
    val sandStr = stringResource(Res.string.share_common_sand)
    val gravelStr = stringResource(Res.string.share_concrete_gravel)
    val waterStr = stringResource(Res.string.share_common_water)
    val aroundStr = stringResource(Res.string.share_common_around)
    val ironSectionStr = stringResource(Res.string.share_structure_iron_section)
    val meshSectionStr = stringResource(Res.string.share_slab_mesh_section)
    val meshSuggestedStr = stringResource(Res.string.share_slab_mesh_suggested)
    val meshPanelsStr = stringResource(Res.string.share_slab_mesh_panels)
    val ironXStr = stringResource(Res.string.share_slab_iron_x)
    val ironYStr = stringResource(Res.string.share_slab_iron_y)
    val rodsStr = stringResource(Res.string.share_structure_rods)
    val totalWeightStr = stringResource(Res.string.share_structure_total_weight)
    val buyStr = stringResource(Res.string.share_structure_buy)
    val bars12mStr = stringResource(Res.string.share_structure_bars_12m)
    val generatedByStr = stringResource(Res.string.share_generated_by, appName)

    val unitM = stringResource(Res.string.unit_meters)
    val unitCm = stringResource(Res.string.unit_centimeters)
    val unitMm = stringResource(Res.string.unit_millimeters)
    val unitKg = stringResource(Res.string.unit_kilograms)
    val unitM3 = stringResource(Res.string.unit_cubic_meters)
    val unitLt = stringResource(Res.string.unit_liters)
    val unitBag = stringResource(Res.string.unit_bag)
    val unitBags = stringResource(Res.string.unit_bags)

    return remember(result, width, length, thickness, concreteType, appName) {
        val bagQty = ceil(result.cementKg / result.cementBagKg.toDouble()).toInt()
        val bagStr = if (bagQty == 1) unitBag else unitBags

        buildString {
            appendLine(titleStr)
            appendLine("=========================")
            appendLine()
            appendLine(sectionDetailsStr)
            appendLine("-------------------------")
            appendLine("*$dimensionsStr* ${width}x$length $unitM")
            appendLine("*$thicknessStr* ${thickness.metersToCm} $unitCm")
            appendLine()
            appendLine(concreteSectionFormat)
            appendLine("-------------------------")
            appendLine("$typeStr ${concreteType.name}")
            appendLine()
            appendLine("● $cementStr ${result.cementKg.roundToDecimals(1)} $unitKg")
            appendLine("    └ $aroundStr $bagQty $bagStr")
            appendLine("● $sandStr ${result.sandM3.roundToDecimals(2)} $unitM3")
            appendLine("● $gravelStr ${result.gravelM3.roundToDecimals(2)} $unitM3")
            appendLine("● $waterStr ${result.waterLiters.roundToDecimals(0)} $unitLt")
            appendLine()
            appendLine(ironSectionStr)
            appendLine("-------------------------")
            
            if (result.suggestedMesh != null) {
                appendLine(meshSectionStr)
                appendLine("● $meshSuggestedStr ${result.suggestedMesh}")
                if (result.meshPanelsNeeded != null) {
                    appendLine("● $meshPanelsStr ${result.meshPanelsNeeded}")
                }
            } else {
                appendLine(ironXStr)
                appendLine("● $rodsStr ${result.countX} x Ø ${result.diameterX} $unitMm")
                appendLine("● $totalWeightStr ${result.weightX.roundToDecimals(1)} $unitKg")
                appendLine()
                appendLine(ironYStr)
                appendLine("● $rodsStr ${result.countY} x Ø ${result.diameterY} $unitMm")
                appendLine("● $totalWeightStr ${result.weightY.roundToDecimals(1)} $unitKg")
                appendLine()
                appendLine("● Total Hierro: ${result.totalWeightKg.roundToDecimals(1)} $unitKg")
                appendLine("    └ $buyStr ${result.commercialBars12m} $bars12mStr")
            }

            appendLine()
            appendLine("_________________________")
            append("_${generatedByStr}_")
        }
    }
}

/**
 * Genera un texto para compartir el resultado de un cálculo de revoque.
 */
@Composable
fun rememberPlasterShareText(
    result: PlasterResult,
    length: Double,
    height: Double,
    thicknessMeters: Double,
    bothSides: Boolean,
    appName: String
): String {
    val titleStr = stringResource(Res.string.share_plaster_title)
    val sectionDetailsStr = stringResource(Res.string.share_section_details)
    val wallStr = stringResource(Res.string.share_plaster_wall)
    val totalSurfaceStr = stringResource(Res.string.share_plaster_total_surface)
    val thickSectionStr = stringResource(Res.string.share_plaster_thick_section)
    val thicknessStr = stringResource(Res.string.share_concrete_thickness)
    val volumeStr = stringResource(Res.string.share_concrete_total_volume) // Reutilizamos
    val cementStr = stringResource(Res.string.share_common_cement)
    val limeStr = stringResource(Res.string.share_common_lime)
    val sandStr = stringResource(Res.string.share_common_sand)
    val aroundStr = stringResource(Res.string.share_common_around)
    val proportionStr = stringResource(Res.string.share_common_proportion)
    val fineSectionStr = stringResource(Res.string.share_plaster_fine_section)
    val optionAStr = stringResource(Res.string.share_plaster_option_a)
    val optionBStr = stringResource(Res.string.share_plaster_option_b)
    val premixStr = stringResource(Res.string.share_plaster_premix)
    val fineSandStr = stringResource(Res.string.share_plaster_fine_sand)
    val aerialLimeStr = stringResource(Res.string.share_plaster_aerial_lime)
    val cementMinStr = stringResource(Res.string.share_plaster_cement_min)
    val generatedByStr = stringResource(Res.string.share_generated_by, appName)
    val bothSideStr = stringResource(Res.string.share_plaster_both_sides)
    val singleSideStr = stringResource(Res.string.share_plaster_single_side)

    val unitM = stringResource(Res.string.unit_meters)
    val unitM2 = stringResource(Res.string.unit_square_meters)
    val unitM3 = stringResource(Res.string.unit_cubic_meters)
    val unitCm = stringResource(Res.string.unit_centimeters)
    val unitKg = stringResource(Res.string.unit_kilograms)
    val unitBag = stringResource(Res.string.unit_bag)
    val unitBags = stringResource(Res.string.unit_bags)

    return remember(result, length, height, thicknessMeters, bothSides, appName) {
        val detailFaces = if (bothSides) bothSideStr else singleSideStr

        val cementBags = ceil(result.thickCementKg / result.cementBagKg.toDouble()).toInt()
        val cementBagStr = if (cementBags == 1) unitBag else unitBags

        val limeBags = ceil(result.thickLimeKg / result.limeBagKg.toDouble()).toInt()
        val limeBagStr = if (limeBags == 1) unitBag else unitBags

        val premixBags = ceil(result.finePremixKg / result.premixBagKg.toDouble()).toInt()
        val premixBagStr = if (premixBags == 1) unitBag else unitBags

        val aerialLimeBags = ceil(result.fineLimeKg / result.limeBagKg.toDouble()).toInt()
        val aerialLimeBagStr = if (aerialLimeBags == 1) unitBag else unitBags

        buildString {
            appendLine(titleStr)
            appendLine("=========================")
            appendLine()
            appendLine(sectionDetailsStr)
            appendLine("-------------------------")
            appendLine("*$wallStr* ${length}x$height $unitM")
            appendLine("*$totalSurfaceStr* ${result.totalAreaM2.roundToDecimals(2)} $unitM2")
            appendLine(detailFaces)
            appendLine()
            appendLine(thickSectionStr)
            appendLine("-------------------------")
            appendLine("*$thicknessStr* ${thicknessMeters.metersToCm} $unitCm")
            appendLine("*$volumeStr* ${result.thickVolumeM3.roundToDecimals(2)} $unitM3")
            appendLine()
            appendLine("● $cementStr ${result.thickCementKg.roundToDecimals(1)} $unitKg")
            appendLine("    └ $aroundStr $cementBags $cementBagStr")
            appendLine("● $limeStr ${result.thickLimeKg.roundToDecimals(1)} $unitKg")
            appendLine("    └ $aroundStr $limeBags $limeBagStr")
            appendLine("● $sandStr ${result.thickSandKg.roundToDecimals(2)} $unitM3")
            appendLine()
            if (result.thickDosage.isNotBlank()) {
                appendLine("*$proportionStr*")
                appendLine(result.thickDosage)
                appendLine()
            }
            appendLine(fineSectionStr)
            appendLine("-------------------------")
            appendLine(optionAStr)
            appendLine("● $premixStr ${result.finePremixKg.roundToDecimals(1)} $unitKg")
            appendLine("    └ $aroundStr $premixBags $premixBagStr")
            appendLine()
            appendLine(optionBStr)
            appendLine("● $aerialLimeStr ${result.fineLimeKg.roundToDecimals(1)} $unitKg")
            appendLine("    └ $aroundStr $aerialLimeBags $aerialLimeBagStr")
            appendLine("● $fineSandStr ${result.fineSandM3.roundToDecimals(2)} $unitM3")
            appendLine("● $cementMinStr")
            appendLine()
            if (result.fineDosage.isNotBlank()) {
                appendLine("$proportionStr ")
                appendLine(result.fineDosage)
                appendLine()
            }
            appendLine("_________________________")
            append("_${generatedByStr}_")
        }
    }
}
