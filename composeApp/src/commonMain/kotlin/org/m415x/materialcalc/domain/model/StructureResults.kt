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

data class SlabResult(
    val totalWeightKg: Double,          // Peso total para el presupuesto
    val totalMeters: Double,            // Metros lineales totales
    val countX: Int,                    // Cantidad de varillas en dirección X
    val countY: Int,                    // Cantidad de varillas en dirección Y
    val lengthX: Double,                // Largo de cada varilla X (con ganchos)
    val lengthY: Double,                // Largo de cada varilla Y (con ganchos)
    val commercialBars12m: Int,         // Cantidad de barras de 12m a comprar
    val wasteAmountKg: Double,          // Cuánto del peso es desperdicio
    val suggestedMesh: String? = null,  // Ej: "Q188"
    val meshPanelsNeeded: Int? = null   // Cantidad de paneles de 2x5m
)