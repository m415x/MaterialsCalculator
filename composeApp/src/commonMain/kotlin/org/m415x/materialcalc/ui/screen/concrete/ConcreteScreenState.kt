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

package org.m415x.materialcalc.ui.screen.concrete

import androidx.compose.runtime.*
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.label_error
import materialscalculator.composeapp.generated.resources.message_error_validation
import org.m415x.materialcalc.domain.model.AppSettingsState
import org.m415x.materialcalc.domain.model.ConcreteResult
import org.m415x.materialcalc.domain.model.TextSource
import org.m415x.materialcalc.domain.usecase.CalculateConcreteUseCase
import org.m415x.materialcalc.ui.common.inputs.ConcreteOptionUi
import org.m415x.materialcalc.ui.common.utils.areValidDimensions
import org.m415x.materialcalc.ui.common.utils.toSafeDoubleOrNull

/**
 * State Holder para la pantalla de Hormigón.
 */
class ConcreteScreenState(
    private val appSettings: AppSettingsState,
    private val calculateConcrete: CalculateConcreteUseCase,
    val concreteOptions: List<ConcreteOptionUi>
) {
    // --- ESTADO DE UI ---
    var width by mutableStateOf("")
    var length by mutableStateOf("")
    var high by mutableStateOf("")
    var quantity by mutableStateOf("1")

    var selectedOption by mutableStateOf(
        concreteOptions.find { it.id == appSettings.defaultConcreteGenId } ?: concreteOptions.firstOrNull()
    )

    var result by mutableStateOf<ConcreteResult?>(null)
    var errorMsg by mutableStateOf<TextSource?>(null)
    var showResultSheet by mutableStateOf(false)

    // --- LÓGICA DE CÁLCULO ---
    fun calculate() {
        val w = width.toSafeDoubleOrNull()
        val l = length.toSafeDoubleOrNull()
        val h = high.toSafeDoubleOrNull()
        val q = quantity.toIntOrNull()

        if (areValidDimensions(w, l, h, q) && selectedOption != null) {
            val calcResult = calculateConcrete(
                widthMeters = w!!,
                lengthMeters = l!!,
                thicknessMeters = h!!,
                unitQuantity = q!!,
                concreteDosing = selectedOption!!.recipe,
                cementBagWeightKg = appSettings.bagCementKg,
                limeBagWeightKg = appSettings.bagLimeKg,
                percentageConcreteWaste = appSettings.wasteConcretePct / 100.0
            )

            calcResult.fold(
                onSuccess = {
                    result = it
                    errorMsg = null
                    showResultSheet = true
                },
                onFailure = {
                    errorMsg = TextSource.ResourceArgs(Res.string.label_error, listOf(": ", it.message ?: ""))
                    result = null
                }
            )
        } else {
            errorMsg = TextSource.Resource(Res.string.message_error_validation)
            result = null
        }
    }
}

@Composable
fun rememberConcreteScreenState(
    appSettings: AppSettingsState,
    calculateConcrete: CalculateConcreteUseCase,
    concreteOptions: List<ConcreteOptionUi>
): ConcreteScreenState {
    return remember(appSettings, calculateConcrete, concreteOptions) {
        ConcreteScreenState(appSettings, calculateConcrete, concreteOptions)
    }
}