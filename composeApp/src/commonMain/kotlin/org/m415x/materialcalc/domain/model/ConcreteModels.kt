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

package org.m415x.materialcalc.domain.model

import org.jetbrains.compose.resources.StringResource
import materialscalculator.composeapp.generated.resources.*

/**
 * Enumeración que representa los tipos de hormigón.
 *
 * @property resistanceKgCm2 Resistencia característica del hormigón en kg/cm².
 * @property usesRes Recurso de string que describe los usos comunes.
 * @property isStructural Indica si es apto para uso estructural.
 */
enum class TipoHormigon(val resistanceKgCm2: Int, val usesRes: StringResource, val isStructural: Boolean) {
    H8(80, Res.string.recipe_uses_h8, false),
    H13(130, Res.string.recipe_uses_h13, false),
    H17(170, Res.string.recipe_uses_h17, true),
    H21(210, Res.string.recipe_uses_h21, true),
    H25(250, Res.string.recipe_uses_h25, true),
    H30(300, Res.string.recipe_uses_h30, true)
}

/**
 * Representa el resultado del cálculo de hormigón.
 *
 * @property volumenTotalM3 Volumen total de hormigón en m³.
 * @property porcentajeDesperdicioHormigon Porcentaje de desperdicio.
 * @property cementoKg Cantidad de cemento en kg.
 * @property bolsaCementoKg Peso de la bolsa de cemento.
 * @property arenaM3 Cantidad de arena en m³.
 * @property piedraM3 Cantidad de piedra en m³.
 * @property aguaLitros Cantidad de agua en litros.
 */
data class ResultadoHormigon(
    val volumenTotalM3: Double,
    val porcentajeDesperdicioHormigon: Double,
    val cementoKg: Double,
    val bolsaCementoKg: Int,
    val arenaM3: Double,
    val piedraM3: Double,
    val aguaLitros: Double,
    val proporcionMezcla: String
)