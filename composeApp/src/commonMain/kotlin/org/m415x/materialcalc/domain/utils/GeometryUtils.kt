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

package org.m415x.materialcalc.domain.utils

import org.m415x.materialcalc.domain.model.Abertura
import org.m415x.materialcalc.ui.common.roundToDecimals // Asegúrate de tener acceso a esta extensión o muévela a domain/common

/**
 * Calcula la superficie neta de una pared descontando aberturas.
 * Realiza validaciones para asegurar que las aberturas no excedan la pared.
 */
fun calculateNetSurface(
    largo: Double,
    alto: Double,
    aberturas: List<Abertura>
): Double {
    val areaBruta = largo * alto
    val areaAberturas = aberturas.sumOf { it.anchoMetros * it.altoMetros * it.cantidad }

    // VALIDACIONES CENTRALIZADAS
    if (areaAberturas > areaBruta) {
        throw IllegalArgumentException(
            "El área de aberturas (${areaAberturas.roundToDecimals(2)} m²) supera el área del muro (${areaBruta.roundToDecimals(2)} m²)."
        )
    }

    if (areaAberturas == areaBruta && areaBruta > 0) {
        throw IllegalArgumentException(
            "El área de aberturas es igual al área del muro. No hay superficie para calcular."
        )
    }

    // Retornamos el área neta segura
    return (areaBruta - areaAberturas).coerceAtLeast(0.0)
}