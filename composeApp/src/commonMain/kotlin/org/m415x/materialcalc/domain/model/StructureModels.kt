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

package org.m415x.materialcalc.domain.model

import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import kotlin.math.max


/**
 * Enumeración que representa los tipos de estructuras.
 *
 * @property labelRes Recurso de etiqueta para mostrar en la UI.
 */
enum class StructureType(val labelRes: StringResource) {
    BEAM(Res.string.structure_type_beam),
    COLUMN(Res.string.structure_type_column),
    SLAB(Res.string.structure_type_slab)
}

/**
 * Enumeración que representa los tipos de terminaciones de armadura.
 */
enum class RebarTerminationType(val displayName: String) {
    STRAIGHT("Recta"),
    HOOK_90("Gancho 90°"),
    HOOK_135("Gancho 135°"),
    HOOK_180("Gancho 180°")
}

/**
 * Calcula la longitud por defecto del gancho en metros según el reglamento CIRSOC.
 *
 * @param rebarDiameterMm Diámetro de la barra en milímetros.
 * @return Longitud del gancho en metros.
 */
fun RebarTerminationType.getDefaultLengthMeters(rebarDiameterMm: Double): Double {
    val db = rebarDiameterMm / 1000.0 // pasar a metros
    return when (this) {
        RebarTerminationType.STRAIGHT -> 0.0
        RebarTerminationType.HOOK_90 -> 12.0 * db
        RebarTerminationType.HOOK_135 -> max(6.0 * db, 0.075)
        RebarTerminationType.HOOK_180 -> max(4.0 * db, 0.065)
    }
}

/**
 * Enumeración que representa los tipos de terminaciones (nudos) para el dibujo técnico.
 */
enum class BeamColumnJointType(val displayName: String) {
    SIMPLE_SUPPORT("Apoyo Simple"),
    RIGID_FRAME("Nudo Rígido"),
    CANTILEVER("Voladizo (Ménsula)")
}

/**
 * Enumeración que representa los diámetros de hierro.
 *
 * @property milimeters Diámetro en milímetros.
 * @property linearWeightKgM Peso lineal en kg/m.
 */
enum class IronDiameter(val milimeters: Double, val linearWeightKgM: Double) {
    HIERRO_4_2(4.2, 0.109),
    HIERRO_6(6.0, 0.222),
    HIERRO_8(8.0, 0.395),
    HIERRO_10(10.0, 0.617),
    HIERRO_12(12.0, 0.888),
    HIERRO_16(16.0, 1.578),
    HIERRO_20(20.0, 2.466),
    HIERRO_25(25.0, 3.853)
}

/**
 * Representa el resultado del cálculo de una estructura.
 *
 * @property volumeConcreteM3 Volumen de hormigón en m³.
 * @property percentageConcreteWaste Porcentaje de desperdicio de hormigón.
 * @property cementKg Cantidad de cemento en kg.
 * @property cementBagKg Peso de la bolsa de cemento.
 * @property sandM3 Cantidad de arena en m³.
 * @property gravelM3 Cantidad de piedra en m³.
 * @property waterLiters Cantidad de agua en litros.
 * @property mainDiameter Diámetro del hierro principal.
 * @property mainIronMeters Metros de hierro principal.
 * @property mainIronKg Kilos de hierro principal.
 * @property mainIronAmount Cantidad de barras de hierro principal.
 * @property percentageMainIronWaste Porcentaje de desperdicio de hierro principal.
 * @property stirrupDiameter Diámetro del hierro de estribo.
 * @property stirrupIronMeters Metros de hierro de estribo.
 * @property stirrupIronKg Kilos de hierro de estribo.
 * @property stirrupIronAmount Cantidad de barras de hierro de estribo.
 * @property percentageStirrupIronWaste Porcentaje de desperdicio de hierro de estribo.
 */
data class StructureResult(
    // Hormigón
    val volumeConcreteM3: Double,
    val percentageConcreteWaste: Double,
    val cementKg: Double,
    val cementBagKg: Int,
    val sandM3: Double,
    val gravelM3: Double,
    val waterLiters: Double,
    // Hierro Principal
    val mainDiameter: IronDiameter,
    val mainIronMeters: Double,
    val mainIronKg: Double,
    val mainIronAmount: Int,
    val percentageMainIronWaste: Double,
    // Estribos
    val stirrupDiameter: IronDiameter,
    val stirrupIronMeters: Double,
    val stirrupIronKg: Double,
    val stirrupIronAmount: Int,
    val percentageStirrupIronWaste: Double
)