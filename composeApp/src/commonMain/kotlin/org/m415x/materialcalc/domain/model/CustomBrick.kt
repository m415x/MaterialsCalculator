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
 * Representa un ladrillo creado por el usuario.
 *
 * @Serializable permite convertirlo a JSON automáticamente.
 * @property id Identificador único (usaremos UUID o Timestamp)
 * @property name Nombre descriptivo del ladrillo
 * @property width Ancho del ladrillo en metros
 * @property height Alto del ladrillo en metros
 * @property length Largo del ladrillo en metros
 * @property joint Espesor de la junta del ladrillo en metros
 * @property isBearing Indica si el ladrillo es portante
 * @property description Descripción y usos del ladrillo
 */
@Serializable
data class CustomBrick(
    val id: String,
    val name: String,                
    val width: Double,               
    val height: Double,              
    val length: Double,              
    val joint: Double,               
    val isBearing: Boolean = false, 
    val description: String = "" 
)

/**
 * Extensión útil para convertir este ladrillo a las propiedades que usa el cálculo.
 *
 * @return BrickProps
 */
fun CustomBrick.toProperties() = BrickProps(
    id = this.id,
    width = this.width,
    height = this.height,
    length = this.length,
    gasketThickness = this.joint
)
