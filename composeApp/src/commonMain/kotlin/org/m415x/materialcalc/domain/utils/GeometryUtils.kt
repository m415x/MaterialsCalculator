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

import org.m415x.materialcalc.domain.model.Aperture
import org.m415x.materialcalc.ui.common.roundToDecimals // Asegúrate de tener acceso a esta extensión o muévela a domain/common

/**
 * Calcula la superficie neta de una pared descontando aberturas.
 * Realiza validaciones para asegurar que las aberturas no excedan la pared.
 *
 * @param length Largo de la pared en metros.
 * @param height Alto de la pared en metros.
 * @param openingsList Lista de aberturas a descontar.
 * @return Superficie neta de la pared en metros cuadrados.
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
        throw IllegalArgumentException(
            "El área de aberturas (${openingArea.roundToDecimals(2)} m²) supera el área del muro (${
                grossArea.roundToDecimals(
                    2
                )
            } m²)."
        )
    }

    if (openingArea == grossArea && grossArea > 0) {
        throw IllegalArgumentException(
            "El área de aberturas es igual al área del muro. No hay superficie para calcular."
        )
    }

    // Retornamos el área neta segura
    return (grossArea - openingArea).coerceAtLeast(0.0)
}