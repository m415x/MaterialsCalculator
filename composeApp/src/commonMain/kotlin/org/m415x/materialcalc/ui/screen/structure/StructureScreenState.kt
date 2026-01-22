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

package org.m415x.materialcalc.ui.screen.structure

import androidx.compose.runtime.*
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.label_error
import materialscalculator.composeapp.generated.resources.message_error_validation
import org.m415x.materialcalc.domain.model.*
import org.m415x.materialcalc.domain.usecase.CalculateStructureUseCase
import org.m415x.materialcalc.ui.common.inputs.ConcreteOptionUi
import org.m415x.materialcalc.ui.common.inputs.IronOptionUi
import org.m415x.materialcalc.ui.common.utils.areValidDimensions
import org.m415x.materialcalc.ui.common.utils.roundToDecimals
import org.m415x.materialcalc.ui.common.utils.toSafeDoubleOrNull

/**
 * State Holder para la pantalla de Estructuras.
 * Gestiona el estado de la UI y la lógica de negocio (cálculos).
 */
class StructureScreenState(
    private val appSettings: AppSettingsState,
    private val calculateStructure: CalculateStructureUseCase,
    val concreteOptions: List<ConcreteOptionUi>,
    val ironOptions: List<IronOptionUi>
) {
    // --- ESTADO GENERAL ---
    var selectedStructureType by mutableStateOf(StructureType.BEAM)
    var result by mutableStateOf<StructureResult?>(null)
    var slabResult by mutableStateOf<SlabResult?>(null)
    var errorMsg by mutableStateOf<TextSource?>(null)
    var showResultSheet by mutableStateOf(false)

    // --- ESTADO VIGAS / COLUMNAS ---
    var isCircular by mutableStateOf(false)
    var length by mutableStateOf("")
    var sideA by mutableStateOf("")
    var sideB by mutableStateOf("")
    var quantityIronRods by mutableStateOf("4")
    var stirrupSpacingM by mutableStateOf("0.20")

    var selectedConcreteOption by mutableStateOf(
        concreteOptions.find { it.id == appSettings.defaultConcreteStrId } ?: concreteOptions.firstOrNull()
    )

    var selectedMainIron by mutableStateOf(
        ironOptions.find { it.id == IronDiameter.HIERRO_10.name } ?: ironOptions.firstOrNull()
    )
    var selectedStirrup by mutableStateOf(
        ironOptions.find { it.id == IronDiameter.HIERRO_6.name } ?: ironOptions.firstOrNull()
    )

    var selectedStartTermination by mutableStateOf(RebarTerminationType.STRAIGHT)
    var selectedEndTermination by mutableStateOf(RebarTerminationType.STRAIGHT)
    var startHookLength by mutableStateOf("")
    var endHookLength by mutableStateOf("")

    // --- ESTADO LOSAS ---
    var slabWidth by mutableStateOf("")
    var slabLength by mutableStateOf("")
    var slabThickness by mutableStateOf("")
    var isManualRebar by mutableStateOf(true)
    var selectedMeshId by mutableStateOf("q131")
    var separationX by mutableStateOf("0.15")
    var separationY by mutableStateOf("0.15")

    var selectedPhiX by mutableStateOf(
        ironOptions.find { it.id == IronDiameter.HIERRO_8.name } ?: ironOptions.firstOrNull()
    )
    var selectedPhiY by mutableStateOf(
        ironOptions.find { it.id == IronDiameter.HIERRO_8.name } ?: ironOptions.firstOrNull()
    )

    var selectedSlabTermination by mutableStateOf(RebarTerminationType.STRAIGHT)
    var slabHookLength by mutableStateOf("")

    // --- ACCIONES QUE MODIFICAN ESTADO CON EFECTOS SECUNDARIOS ---

    fun onStructureTypeChange(type: StructureType) {
        selectedStructureType = type
        if (type == StructureType.BEAM) {
            isCircular = false
        }
    }

    fun onMainIronChange(option: IronOptionUi) {
        selectedMainIron = option
        updateStartHook()
        updateEndHook()
    }

    fun onStartTerminationChange(type: RebarTerminationType) {
        selectedStartTermination = type
        updateStartHook()
    }

    fun onEndTerminationChange(type: RebarTerminationType) {
        selectedEndTermination = type
        updateEndHook()
    }

    fun onPhiXChange(option: IronOptionUi) {
        selectedPhiX = option
        updateSlabHook()
    }

    fun onPhiYChange(option: IronOptionUi) {
        selectedPhiY = option
        updateSlabHook()
    }

    fun onSlabTerminationChange(type: RebarTerminationType) {
        selectedSlabTermination = type
        updateSlabHook()
    }

    // --- LÓGICA PRIVADA DE ACTUALIZACIÓN DE GANCHOS ---

    private fun updateStartHook() {
        if (selectedStartTermination != RebarTerminationType.STRAIGHT) {
            val diameter = selectedMainIron?.iron?.milimeters ?: 10.0
            val defaultLen = selectedStartTermination.getDefaultLengthMeters(diameter)
            startHookLength = defaultLen.roundToDecimals(2).toString()
        } else {
            startHookLength = ""
        }
    }

    private fun updateEndHook() {
        if (selectedEndTermination != RebarTerminationType.STRAIGHT) {
            val diameter = selectedMainIron?.iron?.milimeters ?: 10.0
            val defaultLen = selectedEndTermination.getDefaultLengthMeters(diameter)
            endHookLength = defaultLen.roundToDecimals(2).toString()
        } else {
            endHookLength = ""
        }
    }

    private fun updateSlabHook() {
        if (selectedSlabTermination != RebarTerminationType.STRAIGHT) {
            val dX = selectedPhiX?.iron?.milimeters ?: 8.0
            val dY = selectedPhiY?.iron?.milimeters ?: 8.0
            val maxDiameter = maxOf(dX, dY)
            val defaultLen = selectedSlabTermination.getDefaultLengthMeters(maxDiameter)
            slabHookLength = defaultLen.roundToDecimals(2).toString()
        } else {
            slabHookLength = ""
        }
    }

    // --- LÓGICA DE CÁLCULO ---

    fun calculate() {
        if (selectedStructureType == StructureType.SLAB) {
            calculateSlab()
        } else {
            calculateBeamOrColumn()
        }
    }

    private fun calculateSlab() {
        val w = slabWidth.toSafeDoubleOrNull()
        val l = slabLength.toSafeDoubleOrNull()
        val t = slabThickness.toSafeDoubleOrNull()
        val sepX = separationX.toSafeDoubleOrNull()
        val sepY = separationY.toSafeDoubleOrNull()
        val hookL = slabHookLength.toSafeDoubleOrNull() ?: 0.0

        if (areValidDimensions(w, l, t) &&
            (!isManualRebar || areValidDimensions(sepX, sepY))
        ) {
            val typeForCalculation = try {
                ConcreteType.valueOf(selectedConcreteOption?.id ?: "")
            } catch (e: Exception) {
                ConcreteType.H21
            }

            val result = if (isManualRebar) {
                calculateStructure.calculateSlab(
                    widthX = w!!,
                    lengthY = l!!,
                    thickness = t!!,
                    sepXm = sepX!!,
                    sepYm = sepY!!,
                    phiX = selectedPhiX!!.iron,
                    phiY = selectedPhiY!!.iron,
                    wastePct = appSettings.wasteIronMainPct / 100.0,
                    hookLengthMeters = hookL,
                    concreteType = typeForCalculation,
                    cementBagWeightKg = appSettings.bagCementKg,
                    limeBagWeightKg = appSettings.bagLimeKg,
                    percentageConcreteWaste = appSettings.wasteConcretePct / 100.0
                )
            } else {
                calculateStructure.calculateSlabWithMesh(
                    widthX = w!!,
                    lengthY = l!!,
                    thickness = t!!,
                    meshId = selectedMeshId,
                    concreteType = typeForCalculation,
                    cementBagWeightKg = appSettings.bagCementKg,
                    limeBagWeightKg = appSettings.bagLimeKg,
                    percentageConcreteWaste = appSettings.wasteConcretePct / 100.0
                )
            }

            result.fold(
                onSuccess = {
                    slabResult = it
                    errorMsg = null
                    showResultSheet = true
                },
                onFailure = {
                    errorMsg = TextSource.ResourceArgs(Res.string.label_error, listOf(": ", it.message ?: ""))
                    slabResult = null
                }
            )
        } else {
            errorMsg = TextSource.Resource(Res.string.message_error_validation)
            slabResult = null
        }
    }

    private fun calculateBeamOrColumn() {
        val l = length.toSafeDoubleOrNull()
        val a = sideA.toSafeDoubleOrNull()
        val b = if (isCircular) 1.0 else sideB.toSafeDoubleOrNull()
        val quantityRods = quantityIronRods.toIntOrNull()
        val spM = stirrupSpacingM.toSafeDoubleOrNull()
        val startHook = startHookLength.toSafeDoubleOrNull() ?: 0.0
        val endHook = endHookLength.toSafeDoubleOrNull() ?: 0.0

        if (areValidDimensions(l, a, b, quantityRods, spM) &&
            selectedMainIron != null && selectedStirrup != null
        ) {
            val typeForCalculate = try {
                ConcreteType.valueOf(selectedConcreteOption?.id ?: "")
            } catch (e: Exception) {
                ConcreteType.H21
            }

            val customMainIron = if (selectedMainIron!!.isCustom) {
                appSettings.customIrons.find { it.id == selectedMainIron!!.id }
            } else null

            val customStirrupIron = if (selectedStirrup!!.isCustom) {
                appSettings.customIrons.find { it.id == selectedStirrup!!.id }
            } else null

            val result = calculateStructure(
                lengthMeters = l!!,
                sideAMeters = a!!,
                sideBMeters = if (isCircular) 0.0 else b!!,
                isCircular = isCircular,
                concreteType = typeForCalculate,
                mainIronDiameter = selectedMainIron!!.iron,
                mainIronQuantity = quantityRods!!,
                stirrupIronDiameter = selectedStirrup!!.iron,
                stirrupSpacingMeters = spM!!,
                cementBagWeightKg = appSettings.bagCementKg,
                limeBagWeightKg = appSettings.bagLimeKg,
                percentageCementWaste = appSettings.wasteConcretePct / 100.0,
                percentageMainIronWaste = appSettings.wasteIronMainPct / 100.0,
                percentageStirrupIronWaste = appSettings.wasteIronStirrupPct / 100.0,
                customMainIron = customMainIron,
                customStirrupIron = customStirrupIron,
                startHookLengthMeters = startHook,
                endHookLengthMeters = endHook
            )

            result.fold(
                onSuccess = {
                    this.result = it
                    errorMsg = null
                    showResultSheet = true
                },
                onFailure = {
                    errorMsg = TextSource.ResourceArgs(Res.string.label_error, listOf(": ", it.message ?: ""))
                    this.result = null
                }
            )
        } else {
            errorMsg = TextSource.Resource(Res.string.message_error_validation)
            result = null
        }
    }
}

@Composable
fun rememberStructureScreenState(
    appSettings: AppSettingsState,
    calculateStructure: CalculateStructureUseCase,
    concreteOptions: List<ConcreteOptionUi>,
    ironOptions: List<IronOptionUi>
): StructureScreenState {
    return remember(appSettings, calculateStructure, concreteOptions, ironOptions) {
        StructureScreenState(appSettings, calculateStructure, concreteOptions, ironOptions)
    }
}