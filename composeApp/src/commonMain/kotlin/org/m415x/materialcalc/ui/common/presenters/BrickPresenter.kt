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

import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.model.BrickType
import org.m415x.materialcalc.domain.model.CustomBrick
import org.m415x.materialcalc.domain.model.TextSource
import org.m415x.materialcalc.domain.model.toProperties
import org.m415x.materialcalc.ui.common.inputs.BrickOptionUi

/**
 * Clase encargada de preparar los datos para la UI de selección de ladrillos.
 */
class BrickPresenter(
    private val staticRepo: StaticMaterialRepository = StaticMaterialRepository()
) {

    /**
     * Genera la lista de opciones de ladrillo para la UI.
     */
    fun getOptions(
        customBricks: List<CustomBrick>,
        hiddenIds: Set<String>
    ): List<BrickOptionUi> {
        val factoryOptions = mutableListOf<Pair<Int, BrickOptionUi>>()
        val customOptions = mutableListOf<BrickOptionUi>()

        // A. Estáticos (Si no están ocultos)
        BrickType.entries.forEach { type ->
            if (type.name !in hiddenIds) {
                factoryOptions.add(
                    type.ordinal to BrickOptionUi(
                        id = type.name,
                        label = TextSource.Resource(type.brickNameRes),
                        isBearing = type.isBearing,
                        isCustom = false,
                        description = TextSource.Resource(type.descriptionRes),
                        props = staticRepo.getBrickProps(type)!!,
                        recipe = staticRepo.getMortarDosing(type)
                    )
                )
            }
        }

        // B. Custom
        customBricks.sortedBy { it.name }.forEach { custom ->
            val defaultRecipe = staticRepo.getMortarDosing(BrickType.COMUN)
            customOptions.add(
                BrickOptionUi(
                    id = custom.id,
                    label = TextSource.Raw(custom.name),
                    isBearing = custom.isBearing,
                    isCustom = true,
                    description = TextSource.Raw(custom.description),
                    props = custom.toProperties(),
                    recipe = defaultRecipe
                )
            )
        }

        val sortedFactory = factoryOptions.sortedBy { it.first }.map { it.second }

        return sortedFactory + customOptions
    }
}