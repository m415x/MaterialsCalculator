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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.domain.common.toPresentationUnit
import org.m415x.materialcalc.domain.model.*
import org.m415x.materialcalc.domain.usecase.CalculateWallUseCase
import org.m415x.materialcalc.domain.utils.estimateProportionTxt
import org.m415x.materialcalc.ui.common.dialogs.AppDialog
import org.m415x.materialcalc.ui.common.display.*
import org.m415x.materialcalc.ui.common.inputs.BaseSelectorCard
import org.m415x.materialcalc.ui.common.inputs.BrickSelectorField
import org.m415x.materialcalc.ui.common.inputs.LayoutSelectorCard
import org.m415x.materialcalc.ui.common.inputs.NumericInput
import org.m415x.materialcalc.ui.common.layout.InputRow
import org.m415x.materialcalc.ui.common.layout.InputSection
import org.m415x.materialcalc.ui.common.layout.OpeningsSection
import org.m415x.materialcalc.ui.common.presenters.BrickPresenter
import org.m415x.materialcalc.ui.common.presenters.MortarPresenter
import org.m415x.materialcalc.ui.common.utils.*

/**
 * Pantalla principal de la calculadora de muros.
 *
 * @param appSettings El estado global de la configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallScreen(appSettings: AppSettingsState, repository: SettingsRepository) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val appName = stringResource(Res.string.app_name)

    // Observamos los precios en tiempo real
    val materialPrices by repository.materialPrices.collectAsState(initial = emptyList())
    val laborPrices by repository.laborPrices.collectAsState(initial = emptyList())

    // Creamos un objeto PriceSettings actualizado
    val currentPriceSettings = remember(materialPrices, laborPrices) {
        PriceSettings(materialPrices, laborPrices)
    }

    val calculateWall = remember { CalculateWallUseCase() }

    // Instanciamos los Presenters
    val brickPresenter = remember { BrickPresenter() }
    val mortarPresenter = remember { MortarPresenter() }

    // --- PREPARACIÓN DE DATOS (State Hoisting) ---
    val brickOptions = remember(appSettings.customBricks, appSettings.hiddenBrickIds) {
        brickPresenter.getOptions(appSettings.customBricks, appSettings.hiddenBrickIds)
    }

    // --- B. LISTA DE MEZCLAS (Usando MortarPresenter) ---
    // Aquí usamos "MORTAR" para obtener mezclas de asiento
    val labelCem = stringResource(Res.string.abbr_cement)
    val labelLime = stringResource(Res.string.abbr_lime)
    val labelSand = stringResource(Res.string.abbr_sand)
    val labelKg = stringResource(Res.string.unit_kilograms)
    val labelRatio = stringResource(Res.string.abbr_water_cement_ratio)

    val mortarOptions = remember(appSettings.customRecipes, appSettings.hiddenRecipeIds) {
        mortarPresenter.getOptions(
            appSettings.customRecipes,
            appSettings.hiddenRecipeIds,
            filterType = "MORTAR",
            labelCem = labelCem,
            labelLime = labelLime,
            labelSand = labelSand,
            labelKg = labelKg,
            labelRatio = labelRatio
        )
    }

    // --- INICIALIZACIÓN DEL STATE HOLDER ---
    val state = rememberWallScreenState(
        appSettings = appSettings,
        calculateWall = calculateWall,
        brickOptions = brickOptions,
        mortarOptions = mortarOptions
    )

    val shareManager = remember { getShareManager() }

    val focusLength = remember { FocusRequester() }
    val focusHeight = remember { FocusRequester() }
    val focusOpeningWidth = remember { FocusRequester() }
    val focusBrickType = remember { FocusRequester() }

    RequestFocusOnStart(focusLength, enabled = appSettings.requestFocusOnStart)

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
            verticalArrangement = Arrangement.spacedBy(24.dp) // Aumentamos espaciado entre secciones
        ) {
            InputSection(title = stringResource(Res.string.label_dimensions)) {
                InputRow {
                    NumericInput(
                        value = state.wallLength,
                        onValueChange = { state.wallLength = it },
                        label = stringResource(
                            Res.string.label_length,
                            stringResource(Res.string.unit_meters)
                        ),
                        errorText = state.lengthError, // Conectamos el error
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusLength,
                        nextFocusRequester = focusHeight
                    )
                    NumericInput(
                        value = state.wallHeight,
                        onValueChange = { state.wallHeight = it },
                        label = stringResource(
                            Res.string.label_height,
                            stringResource(Res.string.unit_meters)
                        ),
                        errorText = state.heightError, // Conectamos el error
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusHeight,
                        nextFocusRequester = focusOpeningWidth
                    )
                }
            }

            InputSection(
                title = stringResource(Res.string.openings_title),
                attenuatedTitle = stringResource(Res.string.openings_attenuated_title)
            ) {
                OpeningsSection(
                    openings = state.openings,
                    onAddOpening = { state.addOpening(it) },
                    onRemoveOpening = { state.removeOpening(it) },
                    onEditOpening = { index, newOpening -> state.updateOpening(index, newOpening) },
                    focusRequesterWidth = focusOpeningWidth,
                    nextFocusRequesterHeight = focusBrickType
                )
            }

            InputSection(
                title = stringResource(Res.string.wall_section_brick_mortar),
                showDivider = false
            ) {
                BrickSelectorField(
                    options = state.brickOptions,
                    selectedOption = state.selectedBrickOption,
                    onOptionSelected = { state.onBrickSelected(it) },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusBrickType)
                )

                LayoutSelectorCard(
                    selectedLayout = state.selectedLayout,
                    availableLayouts = state.availableLayouts,
                    thicknessText = if (state.estimatedWallThicknessCm > 0) {
                        stringResource(
                            Res.string.label_estimated_thickness,
                            state.estimatedWallThicknessCm.roundToDecimals(1)
                        )
                    } else {
                        null
                    },
                    onLayoutClick = { state.showLayoutDialog = true }
                )

                // Advertencia Sismorresistente
                if (state.isSismoResistenteWarning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    WarningMessageCard(TextSource.Resource(Res.string.wall_warning_cirsoc_103_sismo))
                }

                if (state.selectedMix != null) {
                    BaseSelectorCard(
                        icon = Icons.Default.Science,
                        title = stringResource(Res.string.wall_label_seat_mortar),
                        value = state.selectedMix!!.estimateProportionTxt().asString(),
                        onClick = { state.showWasteDialog = true },
                        showEditIcon = state.mortarOptions.size > 1,
                        useButton = false,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            ErrorMessageCard(state.errorMsg)

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Diálogo de selección de mezcla
    if (state.showWasteDialog) {
        AppDialog(
            onDismissRequest = { state.showWasteDialog = false },
            title = { Text(stringResource(Res.string.wall_dialog_choose_mix)) },
            content = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(state.mortarOptions) { option ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                state.onMixSelected(option.data)
                            }.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Comparamos por nombre o contenido porque selectedMix es MortarDosing y opcion.data también
                            RadioButton(
                                selected = (option.data.name.asString() == state.selectedMix?.name?.asString()),
                                onClick = null
                            )
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
                TextButton(onClick = {
                    state.showWasteDialog = false
                }) { Text(stringResource(Res.string.button_cancel)) }
            }
        )
    }

    // Diálogo de selección de Layout
    if (state.showLayoutDialog) {
        AppDialog(
            onDismissRequest = { state.showLayoutDialog = false },
            title = { Text(stringResource(Res.string.label_layout_type)) },
            content = {
                Column {
                    state.availableLayouts.forEach { layout ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { state.onLayoutSelected(layout) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (layout == state.selectedLayout),
                                onClick = null
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(layout.resName),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(layout.description),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            },
            actions = {
                TextButton(onClick = { state.showLayoutDialog = false }) {
                    Text(stringResource(Res.string.button_cancel))
                }
            }
        )
    }

    if (state.showResultSheet && state.result != null) {
        val brickDimensions =
            "${((state.selectedBrickOption?.props?.width ?: 0.0) * 100).roundToDecimals(1)}x${((state.selectedBrickOption?.props?.height ?: 0.0) * 100).roundToDecimals(1)}x${((state.selectedBrickOption?.props?.length ?: 0.0) * 100).roundToDecimals(1)}"
        val shareText = rememberWallShareText(
            result = state.result!!,
            length = state.wallLength.toSafeDoubleOrNull() ?: 0.0,
            height = state.wallHeight.toSafeDoubleOrNull() ?: 0.0,
            brickType = state.selectedBrickOption?.label?.asString() ?: "N/A",
            brickDetail = "(${brickDimensions} ${stringResource(Res.string.unit_centimeters)})",
            openings = state.openings.toList(),
            mixDetail = state.selectedMix?.mixingRatio?.asString() ?: "N/A",
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
            WallResultContent(state.result!!)
        }
    }
}

@Composable
fun WallResultContent(res: WallResult) {
    val unitM2 = stringResource(Res.string.unit_square_meters)
    val unitU = stringResource(Res.string.unit_units)
    val unitKg = stringResource(Res.string.unit_kilograms)
    val unitM3 = stringResource(Res.string.unit_cubic_meters)
    val unitLt = stringResource(Res.string.unit_liters)

    ResultSection(
        title = stringResource(
            Res.string.wall_result_net_area,
            res.netAreaM2.roundToDecimals(2),
            unitM2
        )
    ) {
        ResultRow(
            label = stringResource(Res.string.wall_result_bricks),
            value = stringResource(
                Res.string.label_result_unit,
                res.quantityBricks,
                unitU
            )
        )
        Text(
            stringResource(Res.string.label_result_waste_included, (res.percentageBrickWaste * 100).toInt()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        ResultTitle(
            title = stringResource(Res.string.wall_result_mortar, res.mortarM3.roundToDecimals(2)),
            subTitle = stringResource(
                Res.string.label_result_waste_included,
                (res.percentageMortarWaste * 100).toInt()
            ),
            spacer = Modifier.height(8.dp)
        )

        ResultRow(
            label = stringResource(Res.string.wall_result_cement),
            subLabel = stringResource(
                Res.string.label_result_subtitle_unit,
                res.cementKg.roundToDecimals(1),
                unitKg
            ),
            value = res.cementKg.toPresentationUnit(
                res.cementBagKg,
                Res.string.unit_bag,
                Res.string.unit_bags
            )
        )
        
        if (res.limeKg > 0) {
            ResultRow(
                label = stringResource(Res.string.wall_result_lime),
                subLabel = stringResource(
                    Res.string.label_result_subtitle_unit,
                    res.limeKg.roundToDecimals(1),
                    unitKg
                ),
                value = res.limeKg.toPresentationUnit(
                    res.limeBagKg,
                    Res.string.unit_bag,
                    Res.string.unit_bags
                )
            )
        }

        ResultRow(
            label = stringResource(Res.string.wall_result_sand),
            value = stringResource(
                Res.string.label_result_unit,
                res.sandM3.roundToDecimals(2),
                unitM3
            )
        )

        ResultRow(
            label = stringResource(Res.string.wall_result_water),
            value = stringResource(
                Res.string.label_result_unit,
                res.waterLiters.roundToDecimals(1),
                unitLt
            )
        )
    }

    PriceResultSection(
        materialCost = res.materialCost,
        laborCost = res.laborCost,
        materialDetails = res.costBreakdown
    )
}
