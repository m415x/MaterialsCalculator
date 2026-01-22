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

package org.m415x.materialcalc.ui.common.presenters

import org.m415x.materialcalc.domain.model.CustomIron
import org.m415x.materialcalc.domain.model.IronDiameter
import org.m415x.materialcalc.ui.common.inputs.IronOptionUi

/**
 * Clase encargada de preparar los datos para la UI de selección de hierros.
 */
class IronPresenter {

    /**
     * Genera la lista de opciones de hierro para la UI.
     */
    fun getOptions(
        customIrons: List<CustomIron>,
        hiddenIds: Set<String>
    ): List<IronOptionUi> {
        val factoryOptions = mutableListOf<Pair<Int, IronOptionUi>>()
        val customOptions = mutableListOf<IronOptionUi>()

        fun createOption(
            id: String,
            label: String,
            weight: Double,
            isCustom: Boolean,
            iron: IronDiameter
        ): IronOptionUi {
            return IronOptionUi(id, label, weight, isCustom, iron)
        }

        // 1. Procesar hierros de fábrica
        IronDiameter.entries.forEach { type ->
            if (type.name !in hiddenIds) {
                val label = "Ø ${type.milimeters} mm"
                val option = createOption(type.name, label, type.linearWeightKgM, false, type)
                factoryOptions.add(type.ordinal to option)
            }
        }

        // 2. Procesar hierros personalizados
        customIrons.sortedBy { it.diameterMm }.forEach { custom ->
            val label = "Ø ${custom.diameterMm} mm"
            // Usamos HIERRO_10 como placeholder seguro para el enum, pero el ID y peso son los reales
            val option = createOption(custom.id, label, custom.linearWeight, true, IronDiameter.HIERRO_10)
            customOptions.add(option)
        }

        // 3. Ordenar y combinar
        val sortedFactory = factoryOptions.sortedBy { it.first }.map { it.second }
        
        return sortedFactory + customOptions
    }
}