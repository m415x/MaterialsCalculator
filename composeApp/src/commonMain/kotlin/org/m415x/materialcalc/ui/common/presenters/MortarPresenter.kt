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
import org.m415x.materialcalc.domain.model.CustomRecipe
import org.m415x.materialcalc.domain.model.MortarDosing
import org.m415x.materialcalc.domain.model.TextSource
import org.m415x.materialcalc.domain.utils.ConstructionConstants.formatPart
import org.m415x.materialcalc.domain.utils.estimateProportionTxt
import org.m415x.materialcalc.ui.common.inputs.MortarOptionUi

/**
 * Clase encargada de preparar los datos para la UI de selección de morteros.
 */
class MortarPresenter(
    private val staticRepo: StaticMaterialRepository = StaticMaterialRepository()
) {

    /**
     * Genera la lista de opciones de mortero para la UI.
     */
    fun getOptions(
        customRecipes: List<CustomRecipe>,
        hiddenIds: Set<String>,
        filterType: String = "MORTAR", // "MORTAR" o "PLASTER"
        // Strings inyectados
        labelCem: String,
        labelLime: String,
        labelSand: String,
        labelKg: String,
        labelRatio: String = "", // "A/C"
        labelStdJaharro: String = "" // Opcional, solo para PLASTER
    ): List<MortarOptionUi> {
        val factoryList = mutableListOf<MortarOptionUi>()
        val customList = mutableListOf<MortarOptionUi>()

        fun createOption(
            id: String,
            name: TextSource,
            recipe: MortarDosing,
            isCustom: Boolean,
            uses: TextSource
        ): MortarOptionUi {
            val proportion = recipe.estimateProportionTxt()
            val technicalDetail = buildString {
                append("${recipe.cementKg.toInt()} $labelKg $labelCem")
                if (recipe.limeKg > 0) append(" + ${recipe.limeKg.toInt()} $labelKg $labelLime")
                if (recipe.waterCementRatio > 0) {
                    append(" ($labelRatio:${recipe.waterCementRatio})")
                }
            }
            return MortarOptionUi(id, name, proportion, technicalDetail, recipe, isCustom, uses)
        }

        // 1. Estándar (Según el filtro)
        if (filterType == "PLASTER") {
            val jaharro = staticRepo.getThickPlasterRecipe()
            factoryList.add(createOption("STD_JAHARRO", jaharro.name, jaharro, false, TextSource.Raw(labelStdJaharro)))
        } else {
            // Agregamos los morteros estándar para WallScreen
            if (filterType == "MORTAR") {
                val limeMix = staticRepo.getMortarDosing(org.m415x.materialcalc.domain.model.BrickType.COMUN)
                factoryList.add(createOption("STD_CAL", limeMix.name, limeMix, false, TextSource.Raw("")))

                val cementMix = staticRepo.getMortarDosing(org.m415x.materialcalc.domain.model.BrickType.BLOQUE_20)
                factoryList.add(createOption("STD_CEM", cementMix.name, cementMix, false, TextSource.Raw("")))
            }
        }

        // 2. Custom
        customRecipes
            .filter { it.type == filterType }
            .sortedBy { it.name }
            .forEach { custom ->
                val partsText = if (custom.isProportion) {
                    buildString {
                        append(formatPart(custom.partCement))
                        if (custom.partLime > 0) append(":${formatPart(custom.partLime)}")
                        append(":${formatPart(custom.partSand)}")
                        append(" ($labelCem")
                        if (custom.partLime > 0) append(":$labelLime")
                        append(":$labelSand)")
                    }
                } else null

                val dosing = MortarDosing(
                    name = TextSource.Raw(custom.name),
                    mixingRatio = if (partsText != null) TextSource.Raw(partsText) else TextSource.Raw(""),
                    cementKg = custom.cementKg,
                    limeKg = custom.limeKg,
                    sandM3 = custom.sandM3,
                    waterCementRatio = custom.waterCementRatio,
                    waterLiters = if (custom.waterCementRatio > 0) custom.cementKg * custom.waterCementRatio else 240.0,
                    parts = partsText
                )
                customList.add(createOption(custom.id, TextSource.Raw(custom.name), dosing, true, TextSource.Raw(custom.uses)))
            }
        
        return factoryList + customList
    }
}