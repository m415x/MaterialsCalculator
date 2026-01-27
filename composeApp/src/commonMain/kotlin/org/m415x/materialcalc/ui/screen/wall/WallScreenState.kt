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
import materialscalculator.composeapp.generated.resources.*
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

    // --- NUEVOS ESTADOS PARA LAYOUT ---
    var selectedLayout by mutableStateOf(WallLayout.STRETCHER)
    var showLayoutDialog by mutableStateOf(false)
    var isSismoResistenteWarning by mutableStateOf(false)

    var showWasteDialog by mutableStateOf(false)
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
        // Resetear layout a SOGA por defecto al cambiar de ladrillo
        selectedLayout = WallLayout.STRETCHER
        isSismoResistenteWarning = false
    }

    fun onMixSelected(dosing: MortarDosing) {
        selectedMix = dosing
        showWasteDialog = false
    }

    fun onLayoutSelected(layout: WallLayout) {
        selectedLayout = layout
        showLayoutDialog = false
        // Verificar advertencia sismorresistente
        // Si es portante y se elige CANTO, es peligroso.
        // Si es portante y se elige CABEZA, es seguro pero inusual en ladrillos huecos (ya filtrado en availableLayouts).
        // La advertencia principal es para Macizo Portante puesto de Canto (Panderete).
        isSismoResistenteWarning = (selectedBrickOption?.isBearing == true && layout == WallLayout.ROWLOCK)
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
            lengthError = TextSource.Resource(Res.string.message_error_invalid_value)
            isValid = false
        } else {
            lengthError = null
        }

        // Validar Alto
        val h = wallHeight.toSafeDoubleOrNull()
        if (h == null || h <= 0) {
            heightError = TextSource.Resource(Res.string.message_error_invalid_value)
            isValid = false
        } else {
            heightError = null
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

        val l = wallLength.toSafeDoubleOrNull()!!
        val h = wallHeight.toSafeDoubleOrNull()!!

        if (selectedBrickOption != null && selectedMix != null) {
            val calcResult = calculateWall(
                lengthMeters = l,
                heightMeters = h,
                brickProps = selectedBrickOption!!.props,
                wallLayout = selectedLayout, // Pasamos el layout seleccionado
                mortarDosing = selectedMix!!,
                openingList = openings.toList(),
                cementBagWeightKg = appSettings.bagCementKg,
                limeBagWeightKg = appSettings.bagLimeKg,
                percentageBrickWaste = appSettings.wasteBrickPct / 100.0,
                percentageMortarWaste = appSettings.wasteMortarPct / 100.0,
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
            errorMsg = TextSource.Resource(Res.string.message_error_brick_selected)
            result = null
        }
    }

    // --- PROPIEDADES DERIVADAS PARA LA UI ---
    val availableLayouts: List<WallLayout>
        get() {
            val brick = selectedBrickOption ?: return emptyList()
            // Lógica de filtrado según tipo de ladrillo
            // Si es custom, no tenemos flags isHollow, así que asumimos comportamiento genérico o basado en isBearing
            // Para simplificar y ser seguros:
            return when {
                // Si es portante y es hueco (esto lo inferimos si el nombre contiene "HUECO" o "PORTANTE" y esBearing es true)
                // O mejor, usamos la lógica del enum si no es custom.
                !brick.isCustom -> {
                    val type = try {
                        BrickType.valueOf(brick.id)
                    } catch (e: Exception) {
                        null
                    }
                    when (type) {
                        BrickType.PORTANTE_12, BrickType.PORTANTE_18, BrickType.BLOQUE_13, BrickType.BLOQUE_15, BrickType.BLOQUE_20 -> listOf(
                            WallLayout.STRETCHER
                        )

                        BrickType.HUECO_8, BrickType.HUECO_12, BrickType.HUECO_18 -> listOf(
                            WallLayout.STRETCHER,
                            WallLayout.ROWLOCK
                        ) // Hueco no portante permite canto (tabique)
                        else -> listOf(WallLayout.STRETCHER, WallLayout.HEADER, WallLayout.ROWLOCK) // Macizos
                    }
                }
                // Si es custom
                brick.isBearing -> listOf(
                    WallLayout.STRETCHER,
                    WallLayout.HEADER
                ) // Asumimos que si es portante custom, al menos soga y cabeza son válidos estructuralmente (aunque cabeza sea raro en huecos)
                else -> listOf(WallLayout.STRETCHER, WallLayout.HEADER, WallLayout.ROWLOCK)
            }
        }

    val estimatedWallThicknessCm: Double
        get() {
            val brick = selectedBrickOption ?: return 0.0
            val thicknessM = when (selectedLayout) {
                WallLayout.STRETCHER -> brick.props.width
                WallLayout.HEADER -> brick.props.length
                WallLayout.ROWLOCK -> brick.props.height
            }
            // Sumamos un espesor de revoque estimado (ej. 1.5cm por lado = 3cm total)
            return (thicknessM * 100) + 3.0
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
