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

object MaterialIds {
    // Materiales Básicos
    const val CEMENT = "MAT_CEMENT"
    const val HYDRATED_LIME = "MAT_HYDRATED_LIME" // Cal Hidratada (Común)
    const val AERIAL_LIME = "MAT_AERIAL_LIME" // Cal Aérea (Para finos)
    const val SAND = "MAT_SAND"
    const val STONE = "MAT_STONE"
    const val WATER = "MAT_WATER"
    const val PREMIX = "MAT_PREMIX"
}

object LaborIds {
    // Mano de Obra
    const val CONCRETE_M3 = "LABOR_CONCRETE_M3"
    const val WALL_M2 = "LABOR_WALL_M2"
    const val PLASTER_M2 = "LABOR_PLASTER_M2"
    const val STRUCTURE_ML = "LABOR_STRUCTURE_ML"
    const val STRUCTURE_SLAB = "LABOR_STRUCTURE_SLAB"
}