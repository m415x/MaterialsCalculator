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

import kotlinx.serialization.Serializable

@Serializable
data class CustomRecipe(
    val id: String,
    val nombre: String,
    val tipo: String, // "MORTAR" o "CONCRETE"
    override val cementKg: Double,
    override val limeKg: Double,
    override val sandM3: Double,
    override val gravelM3: Double,
    override val waterCementRatio: Double,
    override val waterLiters: Double = 0.0, // Añadido para persistencia directa si se desea, aunque se puede calcular
    val usos: String = "",
    val isEstructural: Boolean = false,
    val isCustom: Boolean = true,
    val isProportion: Boolean = false,
    val partCemento: Double = 0.0,
    val partCal: Double = 0.0,
    val partArena: Double = 0.0,
    val partPiedra: Double = 0.0,
    val partAgua: Double = 0.0 // Añadido
) : MaterialRecipe
