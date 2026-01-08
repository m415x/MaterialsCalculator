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

/**
 * Interfaz común para cualquier mezcla húmeda
 *
 * @property cementKg Cantidad de cemento en kilogramos
 * @property limeKg Cantidad de cal en kilogramos
 * @property sandM3 Cantidad de arena en metros cúbicos
 * @property gravelM3 Cantidad de piedra en metros cúbicos
 * @property waterLiters Cantidad de agua en litros
 * @property waterCementRatio Relación de agua con cemento
 */
interface MaterialRecipe {
    val cementKg: Double
    val limeKg: Double
    val sandM3: Double
    val gravelM3: Double
    val waterLiters: Double
    val waterCementRatio: Double
}

/**
 * Contiene las constantes de materiales para 1 m³ de hormigón. (Valores promedio de tablas
 * estándar)
 *
 * @property name Nombre de la mezcla (ej: "H21", "Mi Mezcla")
 * @property descriptionProportion Descripción de la proporción (ej: "1:3:3")
 * @property cementKg Cantidad de cemento en kilogramos
 * @property sandM3 Cantidad de arena en metros cúbicos
 * @property gravelM3 Cantidad de piedra en metros cúbicos
 * @property waterLiters Cantidad de agua en litros
 * @property waterCementRatio Relación de agua con cemento
 * @property limeKg Cantidad de cal en kilogramos
 */
data class ConcreteDosing(
    val name: String,
    val descriptionProportion: String,
    override val cementKg: Double,
    override val sandM3: Double,
    override val gravelM3: Double,
    override val waterLiters: Double,
    override val waterCementRatio: Double,
    override val limeKg: Double = 0.0
) : MaterialRecipe

/**
 * Contiene las constantes de materiales para 1 m³ de mortero. (Valores promedio de tablas estándar)
 *
 * @property mixingRatio Proporcion de la mezcla
 * @property cementKg Cantidad de cemento en kilogramos
 * @property limeKg Cantidad de cal en kilogramos
 * @property sandM3 Cantidad de arena en metros cúbicos
 * @property waterLiters Cantidad de agua en litros
 * @property waterCementRatio Relación de agua con cemento
 * @property gravelM3 Cantidad de piedra en metros cúbicos
 * @property parts Descripción de las partes de la mezcla (ej: "1:4", "1:1:6")
 */
data class MortarDosing(
    val mixingRatio: String,
    override val cementKg: Double,
    override val limeKg: Double,
    override val sandM3: Double,
    override val waterLiters: Double,
    override val waterCementRatio: Double,
    override val gravelM3: Double = 0.0,
    val parts: String? = null
) : MaterialRecipe
