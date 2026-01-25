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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.domain.common.toPresentationUnit
import org.m415x.materialcalc.domain.model.AppSettingsState
import org.m415x.materialcalc.domain.model.ConcreteResult
import org.m415x.materialcalc.domain.model.PriceSettings
import org.m415x.materialcalc.domain.model.asString
import org.m415x.materialcalc.domain.usecase.CalculateConcreteUseCase
import org.m415x.materialcalc.ui.common.display.*
import org.m415x.materialcalc.ui.common.inputs.CmInput
import org.m415x.materialcalc.ui.common.inputs.ConcreteSelectorField
import org.m415x.materialcalc.ui.common.inputs.NumericInput
import org.m415x.materialcalc.ui.common.layout.InputRow
import org.m415x.materialcalc.ui.common.layout.InputSection
import org.m415x.materialcalc.ui.common.presenters.ConcretePresenter
import org.m415x.materialcalc.ui.common.utils.*

/**
 * Pantalla principal de la calculadora de hormigón.
 *
 * @param appSettings El estado global de la configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcreteScreen(appSettings: AppSettingsState, repository: SettingsRepository) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val appName = stringResource(Res.string.app_name)

    // Observamos los precios en tiempo real
    val materialPrices by repository.materialPrices.collectAsState(initial = emptyList())
    val laborPrices by repository.laborPrices.collectAsState(initial = emptyList())

    // Creamos un objeto PriceSettings actualizado
    val currentPriceSettings = remember(materialPrices, laborPrices) {
        PriceSettings(materialPrices, laborPrices)
    }

    val calculateConcrete = remember { CalculateConcreteUseCase() }

    // Instanciamos el Presenter
    val concretePresenter = remember { ConcretePresenter() }

    // --- LÓGICA DE PREPARACIÓN DE DATOS (Delegada al Presenter) ---
    val resLabel = stringResource(Res.string.recipe_section_resistance)
    val resUnit = stringResource(Res.string.recipe_unit_kilogram_per_square_centimeters)
    val propLabel = stringResource(Res.string.concrete_result_proportion)
    val techLabel = stringResource(Res.string.concrete_result_technical, "")
    val unitKg = stringResource(Res.string.unit_kilograms)

    // Obtenemos la lista de opciones desde el Presenter
    val concreteOptions = remember(appSettings.customRecipes, appSettings.hiddenRecipeIds) {
        concretePresenter.getOptions(
            customRecipes = appSettings.customRecipes,
            hiddenIds = appSettings.hiddenRecipeIds,
            filterStructuralOnly = false,
            resLabel = resLabel,
            resUnit = resUnit,
            propLabel = propLabel,
            techLabel = techLabel,
            unitKg = unitKg
        )
    }

    // --- INICIALIZACIÓN DEL STATE HOLDER ---
    val state = rememberConcreteScreenState(
        appSettings = appSettings,
        calculateConcrete = calculateConcrete,
        concreteOptions = concreteOptions
    )

    val shareManager = remember { getShareManager() }

    val focusWidth = remember { FocusRequester() }
    val focusLength = remember { FocusRequester() }
    val focusHigh = remember { FocusRequester() }
    val focusConcrete = remember { FocusRequester() }

    RequestFocusOnStart(focusWidth, enabled = appSettings.requestFocusOnStart)

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    keyboardController?.hide()
                    state.calculate(currentPriceSettings)
                },
                icon = { Icon(Icons.Default.Calculate, null) },
                text = { Text(stringResource(Res.string.button_calculate)) }
            )
        }
    ) { paddingLocal ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingLocal)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InputSection(title = stringResource(Res.string.label_dimensions)) {
                InputRow {
                    NumericInput(
                        value = state.width,
                        onValueChange = { state.width = it },
                        label = stringResource(
                            Res.string.label_width,
                            stringResource(Res.string.unit_meters)
                        ),
                        errorText = state.widthError, // Conectamos el error
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusWidth,
                        nextFocusRequester = focusLength
                    )
                    NumericInput(
                        value = state.length,
                        onValueChange = { state.length = it },
                        label = stringResource(
                            Res.string.label_length,
                            stringResource(Res.string.unit_meters)
                        ),
                        errorText = state.lengthError, // Conectamos el error
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusLength,
                        nextFocusRequester = focusHigh
                    )
                }

                InputRow {
                    CmInput(
                        value = state.high,
                        onValueChange = { state.high = it },
                        label = stringResource(
                            Res.string.label_thickness,
                            stringResource(Res.string.unit_meters)
                        ),
                        errorText = state.highError, // Conectamos el error
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusHigh,
                        nextFocusRequester = focusConcrete
                    )
                }
            }

            InputSection(title = stringResource(Res.string.concrete_section_resistance), showDivider = false) {
                ConcreteSelectorField(
                    options = state.concreteOptions,
                    selectedOption = state.selectedConcrete,
                    onOptionSelected = { state.selectedConcrete = it },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusConcrete)
                )
            }

            ErrorMessageCard(state.errorMsg)

            Spacer(Modifier.height(80.dp))
        }
    }

    if (state.showResultSheet && state.result != null) {
        val shareText = rememberConcreteShareText(
            result = state.result!!,
            width = state.width.toSafeDoubleOrNull() ?: 0.0,
            length = state.length.toSafeDoubleOrNull() ?: 0.0,
            thickness = state.high.toSafeDoubleOrNull() ?: 0.0,
            quantity = state.quantity.toIntOrNull() ?: 1,
            nameConcrete = state.selectedConcrete?.label?.asString() ?: "N/A",
            proportionConcrete = state.selectedConcrete?.recipe?.descriptionProportion?.asString() ?: "N/A",
            appName = appName,
            materialCost = state.result!!.materialCost,
            laborCost = state.result!!.laborCost
        )

        AppResultBottomSheet(
            onDismissRequest = { state.showResultSheet = false },
            onSave = { /* TODO */ },
            onEdit = { state.showResultSheet = false },
            onShare = { shareManager.shareText(shareText) }
        ) {
            ConcreteResultContent(state.result!!)
        }
    }
}

@Composable
fun ConcreteResultContent(res: ConcreteResult) {
    val unitM3 = stringResource(Res.string.unit_cubic_meters)
    val unitKg = stringResource(Res.string.unit_kilograms)
    val unitLt = stringResource(Res.string.unit_liters)

    ResultSection(
        title = stringResource(
            Res.string.concrete_result_total_volume,
            res.totalVolumeM3.roundToDecimals(2),
            unitM3
        ),
        subTitle = stringResource(
            Res.string.label_result_waste_included,
            (res.percentageConcreteWaste * 100).toInt()
        )
    ) {
        ResultRow(
            label = stringResource(Res.string.concrete_result_cement),
            subLabel = stringResource(
                Res.string.label_result_subtitle_unit,
                res.cementKg.roundToDecimals(1),
                unitKg
            ),
            value = res.cementKg.toPresentationUnit(
                res.cementBagKg,
                Res.string.unit_bag,
                Res.string.unit_bags
            ),
        )

        ResultRow(
            label = stringResource(Res.string.concrete_result_sand),
            value = stringResource(
                Res.string.label_result_unit,
                res.sandM3.roundToDecimals(2),
                unitM3
            )
        )

        ResultRow(
            label = stringResource(Res.string.concrete_result_gravel),
            value = stringResource(
                Res.string.label_result_unit,
                res.gravelM3.roundToDecimals(2),
                unitM3
            )
        )

        ResultRow(
            label = stringResource(Res.string.concrete_result_water),
            value = stringResource(
                Res.string.label_result_unit,
                res.waterLiters.roundToDecimals(1),
                unitLt
            )
        )
    }

    PriceResultSection(res.materialCost, res.laborCost)
}
