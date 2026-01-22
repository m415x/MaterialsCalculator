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
import org.m415x.materialcalc.domain.model.ConcreteDosing
import org.m415x.materialcalc.domain.model.ConcreteType
import org.m415x.materialcalc.domain.model.CustomRecipe
import org.m415x.materialcalc.domain.model.TextSource
import org.m415x.materialcalc.domain.utils.ConstructionConstants.formatPart
import org.m415x.materialcalc.domain.utils.estimateProportionTxt
import org.m415x.materialcalc.ui.common.inputs.ConcreteOptionUi

/**
 * Clase encargada de preparar los datos para la UI de selección de hormigón.
 * Aísla la lógica de negocio y transformación de datos de los Composables.
 */
class ConcretePresenter(
    private val staticRepo: StaticMaterialRepository = StaticMaterialRepository()
) {

    /**
     * Genera la lista de opciones de hormigón para la UI.
     * Combina las recetas estáticas (filtrando las ocultas) con las recetas personalizadas.
     */
    fun getOptions(
        customRecipes: List<CustomRecipe>,
        hiddenIds: Set<String>,
        filterStructuralOnly: Boolean = false,
        // Strings inyectados para evitar llamadas suspendidas o de contexto aquí
        resLabel: String,
        resUnit: String,
        propLabel: String,
        techLabel: String,
        unitKg: String
    ): List<ConcreteOptionUi> {
        val factoryOptions = mutableListOf<Pair<Int, ConcreteOptionUi>>()
        val customOptions = mutableListOf<ConcreteOptionUi>()

        val techFormat = "$techLabel $unitKg"

        fun createOption(
            id: String,
            label: TextSource,
            resistance: TextSource,
            uses: TextSource,
            isStructural: Boolean,
            isCustom: Boolean,
            recipe: ConcreteDosing
        ): ConcreteOptionUi {
            val proportion = recipe.estimateProportionTxt()
            val technical = "${recipe.cementKg.toInt()} $techFormat ${recipe.waterCementRatio}"

            return ConcreteOptionUi(
                id = id,
                label = label,
                proportion = proportion,
                technical = technical,
                resistance = resistance,
                uses = uses,
                isStructural = isStructural,
                isCustom = isCustom,
                recipe = recipe
            )
        }

        // 1. Procesar hormigones de fábrica
        ConcreteType.entries.forEach { type ->
            // Filtros: No oculto Y (No filtrar estructurales O es estructural)
            if (type.name !in hiddenIds && (!filterStructuralOnly || type.isStructural)) {
                val recipe = staticRepo.getConcreteDosing(type)!!
                val resistanceText = "$resLabel ${type.resistanceKgCm2} $resUnit"
                
                // Usamos el recurso de usos del enum
                val usesSource = TextSource.Resource(type.usesRes)

                val option = createOption(
                    id = type.name,
                    label = TextSource.Raw(type.name), // El nombre del enum (H21) es técnico, se puede dejar como Raw o buscar recurso si existiera
                    resistance = TextSource.Raw(resistanceText), // Concatenado con strings resueltos
                    uses = usesSource,
                    isStructural = type.isStructural,
                    isCustom = false,
                    recipe = recipe
                )
                factoryOptions.add(type.ordinal to option)
            }
        }

        // 2. Procesar hormigones personalizados
        customRecipes
            .filter { it.type == "CONCRETE" && (!filterStructuralOnly || it.isStructural) }
            .sortedBy { it.name }
            .forEach { custom ->
                val partsText = if (custom.isProportion) {
                    buildString {
                        append(formatPart(custom.partCement))
                        append(":${formatPart(custom.partSand)}")
                        append(":${formatPart(custom.partGravel)}")
                        append(" $propLabel")
                    }
                } else null

                val recipe = ConcreteDosing(
                    name = TextSource.Raw(custom.name),
                    descriptionProportion = if (partsText != null) TextSource.Raw(partsText) else TextSource.Raw(""),
                    cementKg = custom.cementKg,
                    sandM3 = custom.sandM3,
                    gravelM3 = custom.gravelM3,
                    waterLiters = custom.cementKg * custom.waterCementRatio,
                    waterCementRatio = custom.waterCementRatio
                )
                customOptions.add(
                    createOption(
                        id = custom.id,
                        label = TextSource.Raw(custom.name),
                        resistance = TextSource.Raw(""), // Custom no tiene resistencia calculada
                        uses = TextSource.Raw(custom.uses),
                        isStructural = custom.isStructural,
                        isCustom = true,
                        recipe = recipe
                    )
                )
            }

        // 3. Ordenar y combinar
        val sortedFactory = factoryOptions.sortedBy { it.first }.map { it.second }
        
        return sortedFactory + customOptions
    }
}