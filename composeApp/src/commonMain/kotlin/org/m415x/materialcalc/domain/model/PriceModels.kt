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
 * Representa el precio unitario de un material.
 *
 * @property id Identificador único del material (ej: "CEMENT", "SAND", "BRICK_LADRILLON")
 * @property name Nombre descriptivo del material
 * @property unit Unidad de medida (ej: "bolsa", "m3", "u", "barra")
 * @property price Precio unitario
 */
@Serializable
data class MaterialPrice(
    val id: String,
    val name: String,
    val unit: String,
    val price: Double
)

/**
 * Representa el precio de mano de obra por unidad de trabajo.
 *
 * @property id Identificador único del trabajo (ej: "WALL_BUILD", "PLASTER_INT")
 * @property name Nombre descriptivo del trabajo
 * @property unit Unidad de medida (ej: "m2", "m3", "ml")
 * @property price Precio unitario
 */
@Serializable
data class LaborPrice(
    val id: String,
    val name: String,
    val unit: String,
    val price: Double
)

/**
 * Contenedor para la configuración de precios.
 */
@Serializable
data class PriceSettings(
    val materialPrices: List<MaterialPrice> = emptyList(),
    val laborPrices: List<LaborPrice> = emptyList()
)
