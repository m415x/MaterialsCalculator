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
 * Representa un hierro o malla creado por el usuario.
 *
 * @Serializable permite convertirlo a JSON automáticamente.
 * @property id Identificador único (usaremos UUID o Timestamp)
 * @property name Nombre descriptivo del hierro
 * @property diameterMm Diámetro del hierro en milímetros (0 si es malla)
 * @property linearWeight Peso lineal del hierro en kg/m (0 si es malla)
 * @property isMesh Indica si es una malla (true) o una barra (false)
 * @property meshSepX Separación X en cm (solo si es malla)
 * @property meshSepY Separación Y en cm (solo si es malla)
 * @property panelWidth Ancho del panel en metros (solo si es malla)
 * @property panelLength Largo del panel en metros (solo si es malla)
 */
@Serializable
data class CustomIron(
    val id: String,
    val name: String,
    val diameterMm: Double,
    val linearWeight: Double,
    val isMesh: Boolean = false,
    val meshSepX: Double = 0.0,
    val meshSepY: Double = 0.0,
    val panelWidth: Double = 2.4, // Valor por defecto estándar
    val panelLength: Double = 6.0 // Valor por defecto estándar
)