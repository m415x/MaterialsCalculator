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
import materialscalculator.composeapp.generated.resources.*
import org.m415x.materialcalc.domain.model.AppSettingsState
import org.m415x.materialcalc.domain.model.ConcreteResult
import org.m415x.materialcalc.domain.model.PriceSettings
import org.m415x.materialcalc.domain.model.TextSource
import org.m415x.materialcalc.domain.usecase.CalculateConcreteUseCase
import org.m415x.materialcalc.ui.common.inputs.ConcreteOptionUi
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

    // --- ESTADO DE ERRORES DE VALIDACIÓN ---
    var widthError by mutableStateOf<TextSource?>(null)
    var lengthError by mutableStateOf<TextSource?>(null)
    var highError by mutableStateOf<TextSource?>(null)
    var quantityError by mutableStateOf<TextSource?>(null)

    var selectedConcrete by mutableStateOf(
        concreteOptions.find { it.id == appSettings.defaultConcreteGenId } ?: concreteOptions.firstOrNull()
    )

    var result by mutableStateOf<ConcreteResult?>(null)
    var errorMsg by mutableStateOf<TextSource?>(null)
    var showResultSheet by mutableStateOf(false)

    // --- VALIDACIÓN ---
    fun validate(): Boolean {
        var isValid = true

        // Validar Ancho
        val w = width.toSafeDoubleOrNull()
        if (w == null || w <= 0) {
            widthError = TextSource.Resource(Res.string.message_error_invalid_value) // O un mensaje más específico
            isValid = false
        } else {
            widthError = null
        }

        // Validar Largo
        val l = length.toSafeDoubleOrNull()
        if (l == null || l <= 0) {
            lengthError = TextSource.Resource(Res.string.message_error_invalid_value)
            isValid = false
        } else {
            lengthError = null
        }

        // Validar Espesor
        val h = high.toSafeDoubleOrNull()
        if (h == null || h <= 0) {
            highError = TextSource.Resource(Res.string.message_error_invalid_value)
            isValid = false
        } else {
            highError = null
        }

        // Validar Cantidad
        val q = quantity.toIntOrNull()
        if (q == null || q <= 0) {
            quantityError = TextSource.Resource(Res.string.message_error_invalid_value)
            isValid = false
        } else {
            quantityError = null
        }

        return isValid
    }

    // --- LÓGICA DE CÁLCULO ---
    fun calculate(updatedPrices: PriceSettings) {
        // Primero validamos
        if (!validate()) {
            errorMsg = TextSource.Resource(Res.string.message_error_validation_input)
            return
        }

        // Si pasa la validación, procedemos (ya sabemos que no son nulos)
        val w = width.toSafeDoubleOrNull()!!
        val l = length.toSafeDoubleOrNull()!!
        val h = high.toSafeDoubleOrNull()!!
        val q = quantity.toIntOrNull()!!

        if (selectedConcrete != null) {
            val calcResult = calculateConcrete(
                widthMeters = w,
                lengthMeters = l,
                thicknessMeters = h,
                unitQuantity = q,
                concreteDosing = selectedConcrete!!.recipe,
                cementBagWeightKg = appSettings.bagCementKg,
                limeBagWeightKg = appSettings.bagLimeKg,
                percentageConcreteWaste = appSettings.wasteConcretePct / 100.0,
                priceSettings = updatedPrices
            )

            calcResult.fold(
                onSuccess = {
                    result = it
                    errorMsg = null
                    showResultSheet = true
                },
                onFailure = {
                    // Este error es de lógica de negocio o excepción inesperada
                    errorMsg = it.message?.let { msg -> TextSource.Raw(msg) }
                        ?: TextSource.Resource(Res.string.message_error_unknown)
                    result = null
                }
            )
        } else {
            errorMsg = TextSource.Resource(Res.string.message_error_concrete_selected)
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
