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

/**
 * Enumeración que representa los tipos de hormigón.
 *
 * @property nameRes Recurso de nombre del hormigón.
 * @property resistanceKgCm2 Resistencia característica del hormigón en kg/cm².
 * @property usesRes Recurso de string que describe los usos comunes.
 * @property isStructural Indica si es apto para uso estructural.
 */
enum class ConcreteType(val nameRes: StringResource, val resistanceKgCm2: Int, val usesRes: StringResource, val isStructural: Boolean) {
    H8(Res.string.recipe_name_h8, 80, Res.string.recipe_uses_h8, false),
    H13(Res.string.recipe_name_h13, 130, Res.string.recipe_uses_h13, false),
    H17(Res.string.recipe_name_h17, 170, Res.string.recipe_uses_h17, true),
    H21(Res.string.recipe_name_h21, 210, Res.string.recipe_uses_h21, true),
    H25(Res.string.recipe_name_h25, 250, Res.string.recipe_uses_h25, true),
    H30(Res.string.recipe_name_h30, 300, Res.string.recipe_uses_h30, true)
}

/**
 * Representa el resultado del cálculo de hormigón.
 *
 * @property totalVolumeM3 Volumen total de hormigón en m³.
 * @property percentageConcreteWaste Porcentaje de desperdicio.
 * @property cementKg Cantidad de cemento en kg.
 * @property cementBagKg Peso de la bolsa de cemento.
 * @property sandM3 Cantidad de arena en m³.
 * @property gravelM3 Cantidad de piedra en m³.
 * @property waterLiters Cantidad de agua en litros.
 * @property mixingRatio Razón de mezcla.
 */
data class ConcreteResult(
    val totalVolumeM3: Double,
    val percentageConcreteWaste: Double,
    val cementKg: Double,
    val cementBagKg: Int,
    val sandM3: Double,
    val gravelM3: Double,
    val waterLiters: Double,
    val mixingRatio: TextSource
)