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

package org.m415x.materialcalc.domain.utils

import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.openings_message_error_equal_wall
import materialscalculator.composeapp.generated.resources.openings_message_error_exceed_wall
import org.m415x.materialcalc.domain.model.Aperture
import org.m415x.materialcalc.domain.model.CalculationException
import org.m415x.materialcalc.domain.model.TextSource
import org.m415x.materialcalc.ui.common.utils.roundToDecimals

/**
 * Calcula la superficie neta de una pared descontando aberturas.
 * Realiza validaciones para asegurar que las aberturas no excedan la pared.
 *
 * @param length Largo de la pared en metros.
 * @param height Alto de la pared en metros.
 * @param openingsList Lista de aberturas a descontar.
 * @return Superficie neta de la pared en metros cuadrados.
 * @throws CalculationException Si las aberturas superan o igualan al muro.
 */
fun calculateNetSurface(
    length: Double,
    height: Double,
    openingsList: List<Aperture>
): Double {
    val grossArea = length * height
    val openingArea = openingsList.sumOf { it.widthMeters * it.heightMeters * it.quantity }

    // VALIDACIONES CENTRALIZADAS
    if (openingArea > grossArea) {
        throw CalculationException(
            TextSource.ResourceArgs(
                Res.string.openings_message_error_exceed_wall,
                listOf(
                    openingArea.roundToDecimals(2).toString(),
                    grossArea.roundToDecimals(2).toString()
                )
            )
        )
    }

    if (openingArea == grossArea && grossArea > 0) {
        throw CalculationException(
            TextSource.Resource(Res.string.openings_message_error_equal_wall)
        )
    }

    // Retornamos el área neta segura
    return (grossArea - openingArea).coerceAtLeast(0.0)
}