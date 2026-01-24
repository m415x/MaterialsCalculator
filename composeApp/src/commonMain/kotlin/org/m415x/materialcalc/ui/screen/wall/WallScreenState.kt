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

package org.m415x.materialcalc.ui.screen.wall

import androidx.compose.runtime.*
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.message_error_unknown
import materialscalculator.composeapp.generated.resources.message_error_validation_input
import org.m415x.materialcalc.domain.model.*
import org.m415x.materialcalc.domain.usecase.CalculateWallUseCase
import org.m415x.materialcalc.ui.common.inputs.BrickOptionUi
import org.m415x.materialcalc.ui.common.inputs.MortarOptionUi
import org.m415x.materialcalc.ui.common.utils.toSafeDoubleOrNull

/**
 * State Holder para la pantalla de Muros.
 */
class WallScreenState(
    private val appSettings: AppSettingsState,
    private val calculateWall: CalculateWallUseCase,
    val brickOptions: List<BrickOptionUi>,
    val mortarOptions: List<MortarOptionUi>
) {
    // --- ESTADO DE UI ---
    var wallLength by mutableStateOf("")
    var wallHeight by mutableStateOf("")
    val openings = mutableStateListOf<Aperture>()

    // --- ESTADO DE ERRORES DE VALIDACIÓN ---
    var lengthError by mutableStateOf<TextSource?>(null)
    var heightError by mutableStateOf<TextSource?>(null)

    var selectedBrickOption by mutableStateOf(
        brickOptions.find { it.id == appSettings.defaultBrickId } ?: brickOptions.firstOrNull()
    )

    // Selección de Mezcla (Objeto de dominio MortarDosing)
    // Nota: WallScreen tiene una lógica especial donde la mezcla cambia según el ladrillo.
    var selectedMix by mutableStateOf<MortarDosing?>(null)

    var showMezclaDialog by mutableStateOf(false)
    var result by mutableStateOf<WallResult?>(null)
    var errorMsg by mutableStateOf<TextSource?>(null)
    var showResultSheet by mutableStateOf(false)

    // --- LÓGICA DE INICIALIZACIÓN Y REACTIVIDAD ---
    init {
        // Inicializar mezcla basada en el ladrillo seleccionado si no hay una seleccionada
        if (selectedBrickOption != null) {
            selectedMix = selectedBrickOption!!.recipe
        }
    }

    fun onBrickSelected(option: BrickOptionUi) {
        selectedBrickOption = option
        // Cuando cambia el ladrillo, cambiamos la mezcla a la sugerida por defecto
        // si la actual es diferente a la nueva sugerida (para evitar sobrescribir si el usuario ya eligió otra igual)
        // O simplemente forzamos el cambio como comportamiento por defecto
        selectedMix = option.recipe
    }

    fun onMixSelected(dosing: MortarDosing) {
        selectedMix = dosing
        showMezclaDialog = false
    }

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
        val l = wallLength.toSafeDoubleOrNull()
        if (l == null || l <= 0) {
            lengthError = TextSource.Resource(Res.string.message_error_validation_input)
            isValid = false
        } else {
            lengthError = null
        }

        // Validar Alto
        val h = wallHeight.toSafeDoubleOrNull()
        if (h == null || h <= 0) {
            heightError = TextSource.Resource(Res.string.message_error_validation_input)
            isValid = false
        } else {
            heightError = null
        }

        return isValid
    }

    // --- LÓGICA DE CÁLCULO ---
    fun calculate() {
        // Primero validamos
        if (!validate()) {
            errorMsg = TextSource.Resource(Res.string.message_error_validation_input)
            return
        }

        val l = wallLength.toSafeDoubleOrNull()!!
        val h = wallHeight.toSafeDoubleOrNull()!!

        if (selectedBrickOption != null && selectedMix != null) {
            // Obtenemos los precios actuales del estado global
            val prices = appSettings.priceSettings

            val calcResult = calculateWall(
                lengthMeters = l,
                heightMeters = h,
                brickProps = selectedBrickOption!!.props,
                mortarDosing = selectedMix!!,
                openingList = openings.toList(),
                cementBagWeightKg = appSettings.bagCementKg,
                limeBagWeightKg = appSettings.bagLimeKg,
                percentageBrickWaste = appSettings.wasteBrickPct / 100.0,
                percentageMortarWaste = appSettings.wasteMortarPct / 100.0,
                // Pasamos los precios al UseCase
                materialPrices = prices.materialPrices,
                laborPrices = prices.laborPrices
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
fun rememberWallScreenState(
    appSettings: AppSettingsState,
    calculateWall: CalculateWallUseCase,
    brickOptions: List<BrickOptionUi>,
    mortarOptions: List<MortarOptionUi>
): WallScreenState {
    return remember(appSettings, calculateWall, brickOptions, mortarOptions) {
        WallScreenState(appSettings, calculateWall, brickOptions, mortarOptions)
    }
}