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

package org.m415x.materialcalc.domain.common

import androidx.compose.runtime.Composable
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.unit_bag
import materialscalculator.composeapp.generated.resources.unit_bags
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.domain.model.*
import org.m415x.materialcalc.ui.common.roundToDecimals
import kotlin.math.ceil

/**
 * Data class para transportar la cantidad y el recurso de string (singular/plural).
 */
data class PresentationUnit(
    val quantity: Int,
    val unitRes: StringResource
)

/**
 * Convierte una cantidad total en un objeto PresentationUnit que contiene la cantidad
 * de contenedores y el recurso de string correcto (singular o plural).
 *
 * @param quantityContainer Tamaño de la unidad (ej: 50 para bolsa de cemento).
 * @param singularRes Recurso para el nombre en singular.
 * @param pluralRes Recurso para el nombre en plural.
 * @return Objeto PresentationUnit.
 */
fun Double.toPresentationUnit(
    quantityContainer: Number = 1,
    singularRes: StringResource,
    pluralRes: StringResource
): PresentationUnit {
    val quantity = ceil(this / quantityContainer.toDouble()).toInt()
    val unitRes = if (quantity == 1) singularRes else pluralRes
    return PresentationUnit(quantity, unitRes)
}

/**
 * Composable que toma un PresentationUnit y lo muestra como un string formateado.
 *
 * @param unit Objeto PresentationUnit.
 * @return String formateado.
 */
@Composable
fun DisplayUnit(unit: PresentationUnit): String {
    return "${unit.quantity} ${stringResource(unit.unitRes)}"
}

/**
 * Genera un texto para compartir el resultado de un cálculo de muro.
 *
 * @param largo Largo del muro.
 * @param alto Alto del muro.
 * @param tipoLadrillo Tipo de ladrillo.
 * @param detalleLadrillo Detalle del tipo de ladrillo.
 * @param aberturas Listado de aberturas.
 * @param detalleMezcla Detalle de la mezcla.
 * @param appName Nombre de la app.
 * @return Texto para compartir.
 */
fun WallResult.toShareText(
    largo: Double,
    alto: Double,
    tipoLadrillo: String,
    detalleLadrillo: String,
    aberturas: List<Aperture>,
    detalleMezcla: String,
    appName: String
): String {
    val superficieBruta = largo * alto
    val superficieAberturas = aberturas.sumOf { it.widthMeters * it.heightMeters * it.quantity }

    val sb = StringBuilder()

    val detalleAberturas =
        if (aberturas.isEmpty()) {
            "    (Sin aberturas)"
        } else aberturas.joinToString("\n") {
            "    ‣ ${it.quantity} x ${it.name}: ${it.widthMeters} x ${it.heightMeters} m"
        }

    sb.append("*CÁLCULO DE MURO*\n")
    sb.append("=========================\n\n")
    sb.append("*DETALLE DE OBRA*\n")
    sb.append("-------------------------\n")
    sb.append("*Dimensiones:* $largo x $alto m\n")
    sb.append("*Ladrillo:* $tipoLadrillo \n")
    sb.append("    └ Dimensiones $detalleLadrillo\n\n")
    sb.append("*Superficies:*\n")
    sb.append("● Total Muro: ${superficieBruta.roundToDecimals(2)} m²\n")
    sb.append("● Aberturas:  ${superficieAberturas.roundToDecimals(2)} m²\n")
    sb.append("● Real a cubrir: ${netAreaM2.roundToDecimals(2)} m²\n\n")
    sb.append("*Aberturas:*\n")
    sb.append("$detalleAberturas\n\n")
    sb.append("*MATERIALES ESTIMADOS*\n")
    sb.append("-------------------------\n")
    sb.append("● *Ladrillos:* $quantityBricks U\n")
    sb.append("● *Mortero* (${mortarM3.roundToDecimals(2)} m³):\n")
    sb.append("    ‣ Cemento: ${cementKg.roundToDecimals(1)} kg\n")
    sb.append(
        "    └ aprox. ${
            cementKg.toPresentationUnit(
                cementBagKg,
                Res.string.unit_bag,
                Res.string.unit_bags
            )
        }\n"
    )
    if (limeKg > 0) {
        sb.append("    ‣ Cal: ${limeKg.roundToDecimals(1)} kg\n")
        sb.append(
            "        └ aprox. ${
                limeKg.toPresentationUnit(
                    limeBagKg,
                    Res.string.unit_bag,
                    Res.string.unit_bags
                )
            }\n"
        )
    }
    sb.append("    ‣ Arena: ${sandM3.roundToDecimals(2)} m³\n")
    sb.append("    ‣ Agua: ${waterLiters.roundToDecimals(1)} Lt\n\n")
    sb.append("*Proporción estimada:* \n")
    sb.append("$detalleMezcla\n\n")
    sb.append("_________________________\n")
    sb.append("_Generado con ${appName}_")

    return sb.toString()
}

/**
 * Genera un texto para compartir el resultado de un cálculo de estructura.
 */
fun ResultadoEstructura.toShareText(
    largo: Double,
    ladoA: Double,
    ladoB: Double,
    isCircular: Boolean,
    concreteType: ConcreteType,
    separacionEstribosCm: Double,
    appName: String
): String {
    val sb = StringBuilder()
    val detalleGeometria = if (isCircular) "*Columna Circular:* Ø $ladoA m" else "*Rectangular:* $ladoA x $ladoB m"

    sb.append("*CÁLCULO DE ARMADURA*\n")
    sb.append("=========================\n\n")
    sb.append("*DETALLE DE OBRA*\n")
    sb.append("-------------------------\n")
    sb.append("*Largo Total:* $largo m\n")
    sb.append("$detalleGeometria\n\n")
    sb.append("*1. HORMIGÓN (${volumenHormigonM3.roundToDecimals(2)} m³)*\n")
    sb.append("-------------------------\n")
    sb.append("Tipo: ${concreteType.name} (${concreteType.resistanceKgCm2})\n\n")
    sb.append("• Cemento: ${cementoKg.roundToDecimals(1)} kg\n")
    sb.append(
        "    └ aprox. ${
            cementoKg.toPresentationUnit(
                bolsaCementoKg,
                Res.string.unit_bag,
                Res.string.unit_bags
            )
        }\n"
    )
    sb.append("• Arena: ${arenaM3.roundToDecimals(2)} m³\n")
    sb.append("• Piedra: ${piedraM3.roundToDecimals(2)} m³\n")
    sb.append("• Agua: ${aguaLitros.roundToDecimals(0)} Lt\n\n")
    sb.append("*2. ARMADURA (HIERROS)*\n")
    sb.append("-------------------------\n")
    sb.append("*Principal (Longitudinal):*\n")
    sb.append("    Varillas: Ø ${diametroPrincipal.mm} mm\n")
    sb.append("    Total Peso: ${hierroPrincipalKg.roundToDecimals(1)} kg\n")
    sb.append("        *Comprar:* $cantidadHierroPrincipal barras de 12 m\n\n")
    sb.append("*Estribos (Transversal):*\n")
    sb.append("    Hierro: Ø ${diametroEstribo.mm} mm\n")
    sb.append("    Separación: cada ${separacionEstribosCm.roundToDecimals(0)} cm\n")
    sb.append("    Total Peso: ${hierroEstribosKg.roundToDecimals(1)} kg\n")
    sb.append("        *Comprar:* $cantidadHierroEstribos barras de 12 m\n\n")
    sb.append("_________________________\n")
    sb.append("_Generado con ${appName}_")

    return sb.toString()
}

/**
 * Genera un texto para compartir el resultado de un cálculo de revoque.
 */
fun ResultadoRevoque.toShareText(
    largo: Double,
    alto: Double,
    espesorGruesoMetros: Double,
    ambasCaras: Boolean,
    appName: String
): String {
    val detalleCaras = if (ambasCaras) "(Ambas caras)" else "(Una sola cara)"

    val sb = StringBuilder()

    sb.append("*CÁLCULO DE REVOQUE*\n")
    sb.append("=========================\n\n")
    sb.append("*DETALLE DE OBRA*\n")
    sb.append("-------------------------\n")
    sb.append("*Pared:* $largo x $alto m\n")
    sb.append("*Superficie Total:* ${areaTotalM2.roundToDecimals(2)} m²\n")
    sb.append("$detalleCaras\n\n")
    sb.append("*1. REVOQUE GRUESO (Jaharro)*\n")
    sb.append("-------------------------\n")
    sb.append("*Espesor:* ${espesorGruesoMetros.metersToCm} cm\n")
    sb.append("*Volumen:* ${volumenGruesoM3.roundToDecimals(2)} m³\n\n")
    sb.append("• Cemento: ${gruesoCementoKg.roundToDecimals(1)} kg\n")
    sb.append(
        "    └ aprox. ${
            gruesoCementoKg.toPresentationUnit(
                bolsaCementoKg,
                Res.string.unit_bag,
                Res.string.unit_bags
            )
        }\n"
    )
    sb.append("• Cal Hidratada: ${gruesoCalKg.roundToDecimals(1)} kg\n")
    sb.append(
        "    └ aprox. ${
            gruesoCalKg.toPresentationUnit(
                bolsaCalKg,
                Res.string.unit_bag,
                Res.string.unit_bags
            )
        }\n"
    )
    sb.append("• Arena Común: ${gruesoArenaM3.roundToDecimals(2)} m³\n\n")
    sb.append("*Proporción estimada:*\n")
    sb.append("$dosificacionGrueso\n\n")
    sb.append("*2. REVOQUE FINO (Enlucido)*\n")
    sb.append("-------------------------\n")
    sb.append("*Opción A*\n")
    sb.append("• Premezcla: ${finoPremezclaKg.roundToDecimals(1)} kg\n")
    sb.append(
        "    └ aprox. ${
            finoPremezclaKg.toPresentationUnit(
                bolsaFinoPremezclaKg,
                Res.string.unit_bag,
                Res.string.unit_bags
            )
        }\n\n"
    )
    sb.append("*Opción B (Tradicional: A la cal)*\n")
    sb.append("• Cal Aérea: ${finoCalKg.roundToDecimals(1)} kg\n")
    sb.append(
        "    └ aprox. ${
            finoCalKg.toPresentationUnit(
                bolsaCalKg,
                Res.string.unit_bag,
                Res.string.unit_bags
            )
        }\n\n"
    )
    sb.append("• Arena Fina: ${finoArenaM3.roundToDecimals(2)} m³\n")
    sb.append("• Cemento: (Mínimo para ligar)\n")
    sb.append("Proporción estimada: \n")
    sb.append("$dosificacionFino\n\n")
    sb.append("_________________________\n")
    sb.append("_Generado con ${appName}_")

    return sb.toString()
}