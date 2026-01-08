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
    val unitU = stringResource(Res.string.unit_units)
    val unitKg = stringResource(Res.string.unit_kilograms)
    val unitM3 = stringResource(Res.string.unit_cubic_meters)
    val unitLt = stringResource(Res.string.unit_liters)

    val dimensionsStr = stringResource(Res.string.share_common_dimensions)
    val thicknessStr = stringResource(Res.string.share_concrete_thickness)
    val quantityStr = stringResource(Res.string.share_concrete_quantity)
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
            appendLine("*$dimensionsStr* $width x $length $unitM")
            appendLine("*$thicknessStr* ${thickness.metersToCm} $unitCm")
            appendLine("*$quantityStr* $quantity $unitU")
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

@Composable
fun rememberWallShareText(
    resultado: WallResult,
    largo: Double,
    alto: Double,
    tipoLadrillo: String,
    detalleLadrillo: String,
    aberturas: List<Aperture>,
    detalleMezcla: String,
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
    val mortarQtyFormat = stringResource(Res.string.share_wall_mortar_qty)
    val cementStr = stringResource(Res.string.share_common_cement)
    val limeStr = stringResource(Res.string.share_common_lime)
    val sandStr = stringResource(Res.string.share_common_sand)
    val waterStr = stringResource(Res.string.share_common_water)
    val aroundStr = stringResource(Res.string.share_common_around)
    val proportionStr = stringResource(Res.string.share_common_proportion)

    return remember(resultado, largo, alto, tipoLadrillo, detalleLadrillo, aberturas, detalleMezcla, appName) {
        val superficieBruta = largo * alto
        val superficieAberturas = aberturas.sumOf { it.widthMeters * it.heightMeters * it.quantity }

        val detalleAberturas = if (aberturas.isEmpty()) {
            "    $noOpeningsStr"
        } else {
            aberturas.joinToString("\n") { "    ‣ ${it.quantity} x ${it.name}: ${it.widthMeters} x ${it.heightMeters} $unitM" }
        }

        val cementBags = ceil(resultado.cementKg / resultado.cementBagKg.toDouble()).toInt()
        val cementBagStr = if (cementBags == 1) unitBag else unitBags

        val limeBags = if (resultado.limeKg > 0) ceil(resultado.limeKg / resultado.limeBagKg.toDouble()).toInt() else 0
        val limeBagStr = if (limeBags == 1) unitBag else unitBags

        buildString {
            appendLine(titleStr)
            appendLine("=========================")
            appendLine()
            appendLine(sectionDetailsStr)
            appendLine("-------------------------")
            appendLine("*$dimensionsStr* $largo x $alto $unitM")
            appendLine("*$brickStr* $tipoLadrillo")
            appendLine("    └ $detalleLadrillo")
            appendLine()
            appendLine(surfacesStr)
            appendLine("$totalSurfaceStr ${superficieBruta.roundToDecimals(2)} $unitM2")
            appendLine("$openingsSurfaceStr ${superficieAberturas.roundToDecimals(2)} $unitM2")
            appendLine("$netSurfaceStr ${resultado.netAreaM2.roundToDecimals(2)} $unitM2")
            appendLine()
            appendLine(openingsTitleStr)
            appendLine(detalleAberturas)
            appendLine()
            appendLine(sectionMaterialsStr)
            appendLine("-------------------------")
            appendLine("$bricksQtyStr ${resultado.quantityBricks} $unitU")
            appendLine(mortarQtyFormat.replace("%1\$s", resultado.mortarM3.roundToDecimals(2)))
            appendLine("    ‣ $cementStr ${resultado.cementKg.roundToDecimals(1)} $unitKg")
            appendLine("        └ $aroundStr $cementBags $cementBagStr")
            if (resultado.limeKg > 0) {
                appendLine("    ‣ $limeStr ${resultado.limeKg.roundToDecimals(1)} $unitKg")
                appendLine("        └ $aroundStr $limeBags $limeBagStr")
            }
            appendLine("    ‣ $sandStr ${resultado.sandM3.roundToDecimals(2)} $unitM3")
            appendLine("    ‣ $waterStr ${resultado.waterLiters.roundToDecimals(1)} $unitLt")
            appendLine()
            if (detalleMezcla.isNotBlank()) {
                appendLine("*$proportionStr*")
                appendLine(detalleMezcla)
                appendLine()
            }
            appendLine("_________________________")
            append("_${generatedByStr}_")
        }
    }
}

@Composable
fun rememberStructureShareText(
    resultado: ResultadoEstructura,
    largo: Double,
    ladoA: Double,
    ladoB: Double,
    isCircular: Boolean,
    concreteType: ConcreteType,
    separacionEstribosCm: Double,
    appName: String
): String {
    val titleStr = stringResource(Res.string.share_structure_title)
    val sectionDetailsStr = stringResource(Res.string.share_section_details)
    val lengthStr = stringResource(Res.string.share_structure_length)
    val concreteSectionFormat = stringResource(Res.string.share_structure_concrete_section)
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
    val unitKg = stringResource(Res.string.unit_kilograms)
    val unitM3 = stringResource(Res.string.unit_cubic_meters)
    val unitLt = stringResource(Res.string.unit_liters)
    val unitBag = stringResource(Res.string.unit_bag)
    val unitBags = stringResource(Res.string.unit_bags)

    return remember(resultado, largo, ladoA, ladoB, isCircular, concreteType, separacionEstribosCm, appName) {
        val detalleGeometria =
            if (isCircular) "Columna Circular: Ø $ladoA $unitM" else "Rectangular: $ladoA x $ladoB $unitM"

        val bagQty = ceil(resultado.cementoKg / resultado.bolsaCementoKg.toDouble()).toInt()
        val bagStr = if (bagQty == 1) unitBag else unitBags

        buildString {
            appendLine(titleStr)
            appendLine("=========================")
            appendLine()
            appendLine(sectionDetailsStr)
            appendLine("-------------------------")
            appendLine("*$lengthStr* $largo $unitM")
            appendLine(detalleGeometria)
            appendLine()
            appendLine(concreteSectionFormat.replace("%1\$s", resultado.volumenHormigonM3.roundToDecimals(2)))
            appendLine("-------------------------")
            appendLine("$typeStr ${concreteType.name} (${concreteType.resistanceKgCm2})")
            appendLine()
            appendLine("• $cementStr ${resultado.cementoKg.roundToDecimals(1)} $unitKg")
            appendLine("    └ $aroundStr $bagQty $bagStr (${resultado.bolsaCementoKg} $unitKg)")
            appendLine("• $sandStr ${resultado.arenaM3.roundToDecimals(2)} $unitM3")
            appendLine("• $gravelStr ${resultado.piedraM3.roundToDecimals(2)} $unitM3")
            appendLine("• $waterStr ${resultado.aguaLitros.roundToDecimals(0)} $unitLt")
            appendLine()
            appendLine(ironSectionStr)
            appendLine("-------------------------")
            appendLine(mainIronStr)
            appendLine("    $rodsStr Ø ${resultado.diametroPrincipal.mm} mm")
            appendLine("    $totalWeightStr ${resultado.hierroPrincipalKg.roundToDecimals(1)} $unitKg")
            appendLine("        $buyStr ${resultado.cantidadHierroPrincipal} $bars12mStr")
            appendLine()
            appendLine(stirrupIronStr)
            appendLine("    Hierro: Ø ${resultado.diametroEstribo.mm} mm")
            appendLine("    $separationStr $eachStr ${separacionEstribosCm.roundToDecimals(0)} $unitCm")
            appendLine("    $totalWeightStr ${resultado.hierroEstribosKg.roundToDecimals(1)} $unitKg")
            appendLine("        $buyStr ${resultado.cantidadHierroEstribos} $bars12mStr")
            appendLine()
            appendLine("_________________________")
            append("_${generatedByStr}_")
        }
    }
}

@Composable
fun rememberPlasterShareText(
    resultado: ResultadoRevoque,
    largo: Double,
    alto: Double,
    espesorGruesoMetros: Double,
    ambasCaras: Boolean,
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

    val unitM = stringResource(Res.string.unit_meters)
    val unitM2 = stringResource(Res.string.unit_square_meters)
    val unitM3 = stringResource(Res.string.unit_cubic_meters)
    val unitCm = stringResource(Res.string.unit_centimeters)
    val unitKg = stringResource(Res.string.unit_kilograms)
    val unitBag = stringResource(Res.string.unit_bag)
    val unitBags = stringResource(Res.string.unit_bags)

    return remember(resultado, largo, alto, espesorGruesoMetros, ambasCaras, appName) {
        val detalleCaras = if (ambasCaras) "(Ambas caras)" else "(Una sola cara)"

        val cementBags = ceil(resultado.gruesoCementoKg / resultado.bolsaCementoKg.toDouble()).toInt()
        val cementBagStr = if (cementBags == 1) unitBag else unitBags

        val limeBags = ceil(resultado.gruesoCalKg / resultado.bolsaCalKg.toDouble()).toInt()
        val limeBagStr = if (limeBags == 1) unitBag else unitBags

        val premixBags = ceil(resultado.finoPremezclaKg / resultado.bolsaFinoPremezclaKg.toDouble()).toInt()
        val premixBagStr = if (premixBags == 1) unitBag else unitBags

        val aerialLimeBags = ceil(resultado.finoCalKg / resultado.bolsaCalKg.toDouble()).toInt()
        val aerialLimeBagStr = if (aerialLimeBags == 1) unitBag else unitBags

        buildString {
            appendLine(titleStr)
            appendLine("=========================")
            appendLine()
            appendLine(sectionDetailsStr)
            appendLine("-------------------------")
            appendLine("*$wallStr* $largo x $alto $unitM")
            appendLine("*$totalSurfaceStr* ${resultado.areaTotalM2.roundToDecimals(2)} $unitM2")
            appendLine(detalleCaras)
            appendLine()
            appendLine(thickSectionStr)
            appendLine("-------------------------")
            appendLine("*$thicknessStr* ${espesorGruesoMetros.metersToCm} $unitCm")
            appendLine("*$volumeStr* ${resultado.volumenGruesoM3.roundToDecimals(2)} $unitM3")
            appendLine()
            appendLine("• $cementStr ${resultado.gruesoCementoKg.roundToDecimals(1)} $unitKg")
            appendLine("    └ $aroundStr $cementBags $cementBagStr (${resultado.bolsaCementoKg} $unitKg)")
            appendLine("• $limeStr ${resultado.gruesoCalKg.roundToDecimals(1)} $unitKg")
            appendLine("    └ $aroundStr $limeBags $limeBagStr (${resultado.bolsaCalKg} $unitKg)")
            appendLine("• $sandStr ${resultado.gruesoArenaM3.roundToDecimals(2)} $unitM3")
            appendLine()
            if (resultado.dosificacionGrueso.isNotBlank()) {
                appendLine("*$proportionStr*")
                appendLine(resultado.dosificacionGrueso)
                appendLine()
            }
            appendLine(fineSectionStr)
            appendLine("-------------------------")
            appendLine(optionAStr)
            appendLine("• $premixStr ${resultado.finoPremezclaKg.roundToDecimals(1)} $unitKg")
            appendLine("    └ $aroundStr $premixBags $premixBagStr (${resultado.bolsaFinoPremezclaKg} $unitKg)")
            appendLine()
            appendLine(optionBStr)
            appendLine("• $aerialLimeStr ${resultado.finoCalKg.roundToDecimals(1)} $unitKg")
            appendLine("    └ $aroundStr $aerialLimeBags $aerialLimeBagStr (${resultado.bolsaCalKg} $unitKg)")
            appendLine("• $fineSandStr ${resultado.finoArenaM3.roundToDecimals(2)} $unitM3")
            appendLine("• $cementMinStr")
            appendLine()
            if (resultado.dosificacionFino.isNotBlank()) {
                appendLine("$proportionStr ")
                appendLine(resultado.dosificacionFino)
                appendLine()
            }
            appendLine("_________________________")
            append("_${generatedByStr}_")
        }
    }
}
