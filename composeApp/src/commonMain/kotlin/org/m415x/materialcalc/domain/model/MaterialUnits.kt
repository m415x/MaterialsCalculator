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

import materialscalculator.composeapp.generated.resources.*

/**
 * Constantes para unidades de medida estandarizadas en la aplicación.
 * Retornan TextSource para permitir localización en la UI.
 */
object MaterialUnits {
    val UNIT = TextSource.Resource(Res.string.unit_units)
    val BAG = TextSource.Resource(Res.string.unit_bag)
    val BAGS = TextSource.Resource(Res.string.unit_bags)
    val KG = TextSource.Resource(Res.string.unit_kilograms)
    val L = TextSource.Resource(Res.string.unit_liters)
    val M = TextSource.Resource(Res.string.unit_meters)
    val M2 = TextSource.Resource(Res.string.unit_square_meters)
    val M3 = TextSource.Resource(Res.string.unit_cubic_meters)
    val CM = TextSource.Resource(Res.string.unit_centimeters)
    val CM2 = TextSource.Resource(Res.string.unit_square_centimeters)
    val CM3 = TextSource.Resource(Res.string.unit_cubic_centimeters)
    val MM = TextSource.Resource(Res.string.unit_millimeters)
    val KGM = TextSource.Resource(Res.string.unit_kg_m)
    val ML = TextSource.Resource(Res.string.unit_linear_meter)
    val BAR = TextSource.Resource(Res.string.unit_bar)
    val BARS = TextSource.Resource(Res.string.unit_bars)
    val MESH = TextSource.Resource(Res.string.unit_mesh)
    val MESHES = TextSource.Resource(Res.string.unit_meshes)

    fun bag(weight: Int): TextSource {
        // Enviamos el "ticket" con el dato del peso, sin resolverlo aún
        return TextSource.ResourceArgs(Res.string.unit_bag_with_weight, listOf(weight))
    }
}