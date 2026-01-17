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

package org.m415x.materialcalc.domain.registry

/**
 * Registro de mallas SIMA (Steel Industrial Mesh Alliance).
 * Contiene las especificaciones de las mallas estándar utilizadas en la industria de la construcción.
 *
 * @property id Identificador único de la malla.
 * @property name Nombre de la malla.
 * @property phiMm Diámetro del alambre en milímetros.
 * @property separationCm Separación entre alambres en centímetros.
 * @property weightKgM2 Peso de la malla en kilogramos por metro cuadrado.
 * @property panelWidthM Ancho del panel en metros.
 * @property panelLengthM Longitud del panel en metros.
 */
data class SimaMesh(
    val id: String,
    val name: String,
    val phiMm: Double,
    val separationCm: Int,
    val weightKgM2: Double,
    val panelWidthM: Double = 2.0,
    val panelLengthM: Double = 5.0
)

/** 
 * Registro de mallas SIMA (Steel Industrial Mesh Alliance).
 * Contiene una lista de mallas estándar y métodos para acceder a ellas.
 */
object SimaMeshRegistry {
    val standardMeshes = listOf(
        SimaMesh("q92", "Q-92", 4.2, 15, 1.48),
        SimaMesh("q131", "Q-131", 5.0, 15, 2.09),
        SimaMesh("q188", "Q-188", 6.0, 15, 3.02),
        SimaMesh("q257", "Q-257", 7.0, 15, 4.11),
        SimaMesh("q335", "Q-335", 8.0, 15, 5.37)
    )

    fun getMeshById(id: String) = standardMeshes.find { it.id == id } ?: standardMeshes[1] // Default Q131
}
