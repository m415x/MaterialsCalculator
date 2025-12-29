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
    val nombre: String,          // Ej: "H21 Reforzado"
    override val cementoKg: Double,       // por m3
    override val arenaM3: Double,         // por m3
    override val piedraM3: Double = 0.0,  // por m3 (0 si es mortero)
    override val calKg: Double = 0.0,     // por m3 (0 si es hormigón)
    override val relacionAgua: Double,    // Relación A/C
    val tipo: String,             // "CONCRETE", "MORTAR", "PLASTER"
    val usos: String = "",           // Ej: "Vigas y Losas"
    val isEstructural: Boolean = false, // Solo relevante para Hormigón
    val isProportion: Boolean = false, // ¿Fue creado por partes?
    val partCemento: Double = 0.0,
    val partCal: Double = 0.0,
    val partArena: Double = 0.0,
    val partPiedra: Double = 0.0,
    val partAgua: Double = 0.0
) : MaterialRecipe {
    // Propiedad 'proporcionMezcla' requerida por la interfaz
    // La generamos dinámicamente o la guardamos como campo
    val proporcionMezcla: String
    get() = "$nombre (Personalizado)"
}

