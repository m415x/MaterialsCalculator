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

/**
 * Representa un ladrillo creado por el usuario.
 * @Serializable permite convertirlo a JSON automáticamente.
 */
@Serializable
data class CustomBrick(
    val id: String,                  // Identificador único (usaremos UUID o Timestamp)
    val nombre: String,              // Ej: "Bloque San Juan"
    val ancho: Double,               // Metros
    val alto: Double,                // Metros
    val largo: Double,               // Metros
    val junta: Double,               // Metros (Espesor de mezcla sugerido)
    val isPortante: Boolean = false, // Por defecto false
    val descripcion: String = ""     // Por defecto vacío
)

// Extensión útil para convertir este ladrillo a las propiedades que usa el cálculo
fun CustomBrick.toProperties() = BrickProps(
    width = this.ancho,
    height = this.alto,
    length = this.largo,
    gasketThickness = this.junta
)