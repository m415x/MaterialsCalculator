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

import kotlinx.serialization.Serializable

/**
 * Representa una mezcla personalizada.
 *
 * @Serializable permite convertirlo a JSON automáticamente.
 * @property id Identificador único (usaremos UUID o Timestamp)
 * @property name Nombre descriptivo de la mezcla
 * @property type Tipo de mezcla (MORTAR o CONCRETE)
 * @property cementKg Cantidad de cemento en kilogramos
 * @property limeKg Cantidad de cal en kilogramos
 * @property sandM3 Cantidad de arena en metros cúbicos
 * @property gravelM3 Cantidad de piedra en metros cúbicos
 * @property waterLiters Cantidad de agua en litros
 * @property waterCementRatio Relación de agua con cemento
 * @property uses Uso de la mezcla
 * @property isStructural Indica si es apto para uso estructural
 * @property isCustom Indica si es una mezcla personalizada
 * @property isProportion Indica si es una proporción personalizada
 * @property partCement Cantidad de cemento en la proporción
 * @property partLime Cantidad de cal en la proporción
 * @property partSand Cantidad de arena en la proporción
 * @property partGravel Cantidad de piedra en la proporción
 * @property partWater Cantidad de agua en la proporción
 */
@Serializable
data class CustomRecipe(
    val id: String,
    val name: String,
    val type: String,
    override val cementKg: Double,
    override val limeKg: Double,
    override val sandM3: Double,
    override val gravelM3: Double,
    override val waterCementRatio: Double,
    override val waterLiters: Double = 0.0,
    val uses: String = "",
    val isStructural: Boolean = false,
    val isCustom: Boolean = true,
    val isProportion: Boolean = false,
    val partCement: Double = 0.0,
    val partLime: Double = 0.0,
    val partSand: Double = 0.0,
    val partGravel: Double = 0.0,
    val partWater: Double = 0.0
) : WetMixRecipe
