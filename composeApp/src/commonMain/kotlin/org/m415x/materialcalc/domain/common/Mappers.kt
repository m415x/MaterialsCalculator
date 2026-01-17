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

package org.m415x.materialcalc.domain.common

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.ceil

/**
 * Data class para transportar la cantidad y el recurso de string (singular/plural).
 */
data class PresentationUnit(
    val quantity: Int,
    val unitRes: StringResource
)

/**
 * Convierte una cantidad total en un objeto PresentationUnit que contiene la cantidad
 * de contenedores y el recurso de string correcto (singular o plural).
 *
 * @param quantityContainer Tamaño de la unidad (ej: 50 para bolsa de cemento).
 * @param singularRes Recurso para el nombre en singular.
 * @param pluralRes Recurso para el nombre en plural.
 * @return Objeto PresentationUnit.
 */
fun Double.toPresentationUnit(
    quantityContainer: Number = 1,
    singularRes: StringResource,
    pluralRes: StringResource
): PresentationUnit {
    val quantity = ceil(this / quantityContainer.toDouble()).toInt()
    val unitRes = if (quantity == 1) singularRes else pluralRes
    return PresentationUnit(quantity, unitRes)
}

/**
 * Composable que toma un PresentationUnit y lo muestra como un string formateado.
 *
 * @param unit Objeto PresentationUnit.
 * @return String formateado.
 */
@Composable
fun DisplayUnit(unit: PresentationUnit): String {
    return "${unit.quantity} ${stringResource(unit.unitRes)}"
}