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

package org.m415x.materialcalc.ui.screen.plaster

import androidx.compose.runtime.*
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.message_error_unknown
import materialscalculator.composeapp.generated.resources.message_error_validation_input
import org.m415x.materialcalc.domain.model.*
import org.m415x.materialcalc.domain.usecase.CalculatePlasterUseCase
import org.m415x.materialcalc.ui.common.inputs.MortarOptionUi
import org.m415x.materialcalc.ui.common.utils.toSafeDoubleOrNull

/**
 * State Holder para la pantalla de Revoques.
 */
class PlasterScreenState(
    private val appSettings: AppSettingsState,
    private val calculatePlaster: CalculatePlasterUseCase,
    val recipeOptions: List<MortarOptionUi>
) {
    // --- ESTADO DE UI ---
    var length by mutableStateOf("")
    var height by mutableStateOf("")
    val openings = mutableStateListOf<Aperture>()
    var thickThickness by mutableStateOf("0.02") // Valor por defecto sugerido
    var bothSides by mutableStateOf(false) // Switch

    // --- ESTADO DE ERRORES DE VALIDACIÓN ---
    var lengthError by mutableStateOf<TextSource?>(null)
    var heightError by mutableStateOf<TextSource?>(null)
    var thicknessError by mutableStateOf<TextSource?>(null)

    var selectMortar by mutableStateOf<MortarDosing?>(
        recipeOptions.find { it.id == appSettings.defaultPlasterId }?.data
            ?: recipeOptions.firstOrNull()?.data
    )

    var showMixDialog by mutableStateOf(false)
    var result by mutableStateOf<PlasterResult?>(null)
    var errorMsg by mutableStateOf<TextSource?>(null)
    var showResultSheet by mutableStateOf(false)

    // --- GESTIÓN DE ABERTURAS ---
    fun addOpening(opening: Aperture) {
        openings.add(opening)
    }

    fun removeOpening(opening: Aperture) {
        openings.remove(opening)
    }

    fun updateOpening(index: Int, opening: Aperture) {
        if (index in openings.indices) {
            openings[index] = opening
        }
    }

    // --- VALIDACIÓN ---
    fun validate(): Boolean {
        var isValid = true

        // Validar Largo
        val l = length.toSafeDoubleOrNull()
        if (l == null || l <= 0) {
            lengthError = TextSource.Resource(Res.string.message_error_validation_input)
            isValid = false
        } else {
            lengthError = null
        }

        // Validar Alto
        val h = height.toSafeDoubleOrNull()
        if (h == null || h <= 0) {
            heightError = TextSource.Resource(Res.string.message_error_validation_input)
            isValid = false
        } else {
            heightError = null
        }

        // Validar Espesor
        val t = thickThickness.toSafeDoubleOrNull()
        if (t == null || t <= 0) {
            thicknessError = TextSource.Resource(Res.string.message_error_validation_input)
            isValid = false
        } else {
            thicknessError = null
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

        val l = length.toSafeDoubleOrNull()!!
        val a = height.toSafeDoubleOrNull()!!
        val e = thickThickness.toSafeDoubleOrNull()!!

        if (selectMortar != null) {
            val calcResult = calculatePlaster(
                lengthMeters = l,
                heightMeters = a,
                thickThickness = e,
                // CONVERTIMOS MM A METROS (/1000)
                thinThickness = appSettings.fineThicknessMm / 1000.0,
                isBothSides = bothSides,
                openingsList = openings.toList(),
                mortarDosing = selectMortar!!,
                cementBagWeightKg = appSettings.bagCementKg,
                limeBagWeightKg = appSettings.bagLimeKg,
                premixBagWeightKg = appSettings.bagPremixKg,
                // CONVERTIMOS PORCENTAJE A DECIMAL (/100)
                percentagePlasterWaste = appSettings.wastePlasterPct / 100.0,
                priceSettings = updatedPrices
            )

            calcResult.fold(
                onSuccess = {
                    result = it
                    errorMsg = null
                    showResultSheet = true
                },
                onFailure = {
                    // Manejo seguro de errores
                    errorMsg = if (it is CalculationException) {
                        it.textSource
                    } else {
                        it.message?.let { msg -> TextSource.Raw(msg) }
                            ?: TextSource.Resource(Res.string.message_error_unknown)
                    }
                    result = null
                }
            )
        } else {
            errorMsg = TextSource.Resource(Res.string.message_error_validation_input)
            result = null
        }
    }
}

@Composable
fun rememberPlasterScreenState(
    appSettings: AppSettingsState,
    calculatePlaster: CalculatePlasterUseCase,
    recipeOptions: List<MortarOptionUi>
): PlasterScreenState {
    return remember(appSettings, calculatePlaster, recipeOptions) {
        PlasterScreenState(appSettings, calculatePlaster, recipeOptions)
    }
}