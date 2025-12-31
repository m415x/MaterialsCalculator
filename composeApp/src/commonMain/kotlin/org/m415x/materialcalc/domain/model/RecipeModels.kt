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
 * @property cementoKg Cantidad de cemento en kilogramos
 * @property calKg Cantidad de cal en kilogramos
 * @property arenaM3 Cantidad de arena en metros cúbicos
 * @property piedraM3 Cantidad de piedra en metros cúbicos
 * @property relacionAgua Relación de agua con cemento
 */
interface MaterialRecipe {
    val cementoKg: Double
    val calKg: Double
    val arenaM3: Double
    val piedraM3: Double
    val aguaLitros: Double
    val relacionAgua: Double
}

/**
 * Contiene las constantes de materiales para 1 m³ de hormigón. (Valores promedio de tablas
 * estándar)
 *
 * @property nombre Nombre de la mezcla (ej: "H21", "Mi Mezcla")
 * @property descripcionProporcion Descripción de la proporción (ej: "1:3:3")
 * @property cementoKg Cantidad de cemento en kilogramos
 * @property arenaM3 Cantidad de arena en metros cúbicos
 * @property piedraM3 Cantidad de piedra en metros cúbicos
 * @property relacionAgua Relación de agua con cemento
 * @property calKg Cantidad de cal en kilogramos
 */
data class DosificacionHormigon(
    val nombre: String,
    val descripcionProporcion: String,
    override val cementoKg: Double,
    override val arenaM3: Double,
    override val piedraM3: Double,
    override val aguaLitros: Double,
    override val relacionAgua: Double,
    override val calKg: Double = 0.0
) : MaterialRecipe

/**
 * Contiene las constantes de materiales para 1 m³ de mortero. (Valores promedio de tablas estándar)
 *
 * @property proporcionMezcla Proporcion de la mezcla
 * @property cementoKg Cantidad de cemento en kilogramos
 * @property calKg Cantidad de cal en kilogramos
 * @property arenaM3 Cantidad de arena en metros cúbicos
 * @property relacionAgua Relación de agua con cemento
 * @property piedraM3 Cantidad de piedra en metros cúbicos
 */
data class DosificacionMortero(
    val proporcionMezcla: String,
    override val cementoKg: Double,
    override val calKg: Double,
    override val arenaM3: Double,
    override val aguaLitros: Double,
    override val relacionAgua: Double,
    override val piedraM3: Double = 0.0,
    val partes: String? = null
) : MaterialRecipe
