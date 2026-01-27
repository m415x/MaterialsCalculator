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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.common.toPresentationUnit
import org.m415x.materialcalc.domain.model.AppSettingsState
import org.m415x.materialcalc.domain.model.PlasterResult
import org.m415x.materialcalc.domain.model.PriceSettings
import org.m415x.materialcalc.domain.model.asString
import org.m415x.materialcalc.domain.usecase.CalculatePlasterUseCase
import org.m415x.materialcalc.ui.common.dialogs.AppDialog
import org.m415x.materialcalc.ui.common.display.*
import org.m415x.materialcalc.ui.common.inputs.BaseSelectorCard
import org.m415x.materialcalc.ui.common.inputs.CmInput
import org.m415x.materialcalc.ui.common.inputs.NumericInput
import org.m415x.materialcalc.ui.common.layout.InputRow
import org.m415x.materialcalc.ui.common.layout.InputSection
import org.m415x.materialcalc.ui.common.layout.OpeningsSection
import org.m415x.materialcalc.ui.common.presenters.MortarPresenter
import org.m415x.materialcalc.ui.common.utils.*

/**
 * Pantalla principal de la calculadora de revoques.
 *
 * @param appSettings El estado global de la configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlasterScreen(appSettings: AppSettingsState, repository: SettingsRepository) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // Nombre de la app
    val appName = stringResource(Res.string.app_name)

    // Observamos los precios en tiempo real
    val materialPrices by repository.materialPrices.collectAsState(initial = emptyList())
    val laborPrices by repository.laborPrices.collectAsState(initial = emptyList())

    // Creamos un objeto PriceSettings actualizado
    val currentPriceSettings = remember(materialPrices, laborPrices) {
        PriceSettings(materialPrices, laborPrices)
    }

    val staticRepo = remember { StaticMaterialRepository() }
    val calculatePlaster = remember { CalculatePlasterUseCase(staticRepo) }

    // Instanciamos el Presenter
    val mortarPresenter = remember { MortarPresenter() }

    // --- B. LISTA DE MEZCLAS (Usando Presenter) ---
    // Obtenemos la lista de opciones UI usando el Presenter
    // Nota: PlasterScreen usa mezclas tipo "PLASTER" y "MORTAR"
    // El presenter actual filtra por un solo tipo. 
    // Para mantener la funcionalidad original (mostrar ambos), deberíamos llamar al presenter dos veces o modificarlo.
    // Como solución rápida y limpia, llamamos dos veces y unimos, ya que el presenter devuelve listas puras.
    val labelCem = stringResource(Res.string.abbr_cement)
    val labelLime = stringResource(Res.string.abbr_lime)
    val labelSand = stringResource(Res.string.abbr_sand)
    val labelKg = stringResource(Res.string.unit_kilograms)
    val labelRatio = stringResource(Res.string.abbr_water_cement_ratio)
    val labelStdJaharro = stringResource(Res.string.plaster_type_std_jaharro)

    val recipeOptions = remember(appSettings.customRecipes, appSettings.hiddenRecipeIds) {
        val plasters = mortarPresenter.getOptions(
            appSettings.customRecipes,
            appSettings.hiddenRecipeIds,
            filterType = "PLASTER",
            labelCem = labelCem,
            labelLime = labelLime,
            labelSand = labelSand,
            labelKg = labelKg,
            labelRatio = labelRatio,
            labelStdJaharro = labelStdJaharro
        )
        plasters
    }

    // --- INICIALIZACIÓN DEL STATE HOLDER ---
    val state = rememberPlasterScreenState(
        appSettings = appSettings,
        calculatePlaster = calculatePlaster,
        recipeOptions = recipeOptions
    )

    val shareManager = remember { getShareManager() }

    // Focos
    val focusLength = remember { FocusRequester() }
    val focusHeight = remember { FocusRequester() }
    val focusThickness = remember { FocusRequester() }
    val focusOpeningWith =
        remember { FocusRequester() } // Foco puente pertenecerá al input "Ancho" dentro de OpeningsSection

    // Auto-Foco al abrir
    RequestFocusOnStart(focusLength, enabled = appSettings.requestFocusOnStart)

    Scaffold(
        // El FAB vive aquí, donde tiene acceso a las variables 'largo' y 'alto'
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
                .verticalScroll(rememberScrollState()), // Permite scrollear si el teclado tapa
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            InputSection(title = stringResource(Res.string.label_dimensions)) {
                InputRow {
                    NumericInput(
                        value = state.length,
                        onValueChange = { state.length = it },
                        label = stringResource(
                            Res.string.plaster_label_length,
                            stringResource(Res.string.unit_meters)
                        ),
                        errorText = state.lengthError, // Conectamos el error
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusLength,
                        nextFocusRequester = focusHeight
                    )
                    NumericInput(
                        value = state.height,
                        onValueChange = { state.height = it },
                        label = stringResource(
                            Res.string.plaster_label_height,
                            stringResource(Res.string.unit_meters)
                        ),
                        errorText = state.heightError, // Conectamos el error
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusHeight,
                        nextFocusRequester = focusOpeningWith
                    )
                }
            }

            InputSection(
                title = stringResource(Res.string.plaster_section_openings),
                attenuatedTitle = stringResource(Res.string.plaster_section_openings_attenuated)
            ) {
                OpeningsSection(
                    openings = state.openings,
                    onAddOpening = { state.addOpening(it) },
                    onRemoveOpening = { state.removeOpening(it) },
                    onEditOpening = { index, newOpening -> state.updateOpening(index, newOpening) },
                    focusRequesterWidth = focusOpeningWith,
                    nextFocusRequesterHeight = focusThickness
                )
            }

            InputSection(title = stringResource(Res.string.plaster_section_thick), showDivider = false) {
                if (state.selectMortar != null) {
                    BaseSelectorCard(
                        icon = Icons.Default.Science,
                        title = state.selectMortar!!.name.asString(),
                        value = state.selectMortar!!.mixingRatio.asString(),
                        onClick = { state.showMixDialog = true },
                        showEditIcon = state.recipeOptions.size > 1,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                InputRow(horizontalArrangement = Arrangement.SpaceBetween) {
                    CmInput(
                        value = state.thickThickness,
                        onValueChange = { state.thickThickness = it },
                        label = stringResource(
                            Res.string.plaster_label_thickness,
                            stringResource(Res.string.unit_meters)
                        ),
                        errorText = state.thicknessError, // Conectamos el error
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusThickness,
                        onDone = { keyboardController?.hide() }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(Res.string.plaster_label_both_sides),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                stringResource(Res.string.plaster_label_x2_sup),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.bothSides,
                            onCheckedChange = { state.bothSides = it }
                        )
                    }
                }
            }

            ErrorMessageCard(state.errorMsg)

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (state.showMixDialog) {
        AppDialog(
            onDismissRequest = { state.showMixDialog = false },
            title = { Text(stringResource(Res.string.plaster_dialog_choose_mix)) },
            content = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(state.recipeOptions) { option ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                state.selectMortar = option.data
                                state.showMixDialog = false
                            }.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = (option.data == state.selectMortar), onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(option.name.asString(), style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${option.proportion.asString()}\n${option.technical}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            },
            actions = {
                TextButton(onClick = { state.showMixDialog = false }) { Text(stringResource(Res.string.button_cancel)) }
            }
        )
    }

    // --- MODAL DE RESULTADOS ---
    if (state.showResultSheet && state.result != null) {
        val shareText = rememberPlasterShareText(
            result = state.result!!,
            length = state.length.toSafeDoubleOrNull() ?: 0.0,
            height = state.height.toSafeDoubleOrNull() ?: 0.0,
            thicknessMeters = state.thickThickness.toSafeDoubleOrNull() ?: 0.0,
            bothSides = state.bothSides,
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
            PlasterResultContent(state.result!!)
        }
    }
}

/**
 * Composable que muestra el contenido del resultado.
 *
 * @param res Resultado del cálculo.
 */
@Composable
fun PlasterResultContent(res: PlasterResult) {
    val unitM2 = stringResource(Res.string.unit_square_meters)
    val unitKg = stringResource(Res.string.unit_kilograms)
    val unitM3 = stringResource(Res.string.unit_cubic_meters)
    val unitLt = stringResource(Res.string.unit_liters)

    ResultTitle(
        title = stringResource(
            Res.string.plaster_result_total_area,
            res.totalAreaM2.roundToDecimals(2),
            unitM2
        )
    )

    // Sección GRUESO
    ResultSection(
        stringResource(Res.string.plaster_result_thick_title),
        subTitle = stringResource(
            Res.string.label_result_waste_included,
            (res.thickPercentageWaste * 100).toInt()
        ),
    ) {
        ResultRow(
            label = stringResource(Res.string.plaster_result_cement),
            subLabel = stringResource(
                Res.string.label_result_subtitle_unit,
                res.thickCementKg.roundToDecimals(1),
                unitKg
            ),
            value = res.thickCementKg.toPresentationUnit(
                res.cementBagKg,
                Res.string.unit_bag,
                Res.string.unit_bags
            ),
        )

        ResultRow(
            label = stringResource(Res.string.plaster_result_lime),
            subLabel = stringResource(
                Res.string.label_result_subtitle_unit,
                res.thickLimeKg.roundToDecimals(1),
                unitKg
            ),
            value = res.thickLimeKg.toPresentationUnit(
                res.limeBagKg,
                Res.string.unit_bag,
                Res.string.unit_bags
            ),
        )

        ResultRow(
            label = stringResource(Res.string.plaster_result_sand),
            value = stringResource(
                Res.string.label_result_unit,
                res.thickSandKg.roundToDecimals(2),
                unitM3
            )
        )

        ResultRow(
            label = stringResource(Res.string.plaster_result_water),
            value = stringResource(
                Res.string.label_result_unit,
                res.thickWaterLiters.roundToDecimals(1),
                unitLt
            )
        )
    }

    // Sección FINO
    ResultSection(
        title = stringResource(Res.string.plaster_result_fine_title),
        subTitle = stringResource(Res.string.label_result_waste_included, (res.finePercentageWaste * 100).toInt()),
    ) {
        Text(stringResource(Res.string.plaster_result_choose_option), style = MaterialTheme.typography.labelLarge)

        // Opción A
        ResultRow(
            label = stringResource(Res.string.plaster_result_option_a),
            subLabel = stringResource(
                Res.string.label_result_subtitle_unit,
                res.finePremixKg.roundToDecimals(1),
                unitKg
            ),
            value = res.finePremixKg.toPresentationUnit(
                res.premixBagKg,
                Res.string.unit_bag,
                Res.string.unit_bags
            )
        )

        // Opción B
        ResultRow(
            label = stringResource(Res.string.plaster_result_option_b),
            subLabel = stringResource(
                Res.string.label_result_subtitle_unit,
                res.fineLimeKg.roundToDecimals(1),
                unitKg
            ),
            value = res.fineLimeKg.toPresentationUnit(
                res.limeBagKg,
                Res.string.unit_bag,
                Res.string.unit_bags
            )
        )

        ResultRow(
            label = stringResource(Res.string.plaster_result_fine_sand),
            value = stringResource(
                Res.string.label_result_unit,
                res.fineSandM3.roundToDecimals(2),
                stringResource(Res.string.unit_cubic_meters)
            )
        )
    }

    PriceResultSection(res.materialCost, res.laborCost)
}
