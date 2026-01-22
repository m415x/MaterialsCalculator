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

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.common.toPresentationUnit
import org.m415x.materialcalc.domain.model.*
import org.m415x.materialcalc.domain.usecase.CalculateStructureUseCase
import org.m415x.materialcalc.ui.common.display.*
import org.m415x.materialcalc.ui.common.inputs.*
import org.m415x.materialcalc.ui.common.layout.InputColumn
import org.m415x.materialcalc.ui.common.layout.InputRow
import org.m415x.materialcalc.ui.common.layout.InputSection
import org.m415x.materialcalc.ui.common.presenters.ConcretePresenter
import org.m415x.materialcalc.ui.common.presenters.IronPresenter
import org.m415x.materialcalc.ui.common.utils.*
import kotlin.math.ceil

/**
 * Pantalla principal de la calculadora de estructuras.
 *
 * @param appSettings El estado global de la configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructureScreen(appSettings: AppSettingsState) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val appName = stringResource(Res.string.app_name)
    val repository = remember { StaticMaterialRepository() }
    val calculateStructure = remember { CalculateStructureUseCase(repository) }

    // Instanciamos los Presenters
    val concretePresenter = remember { ConcretePresenter() }
    val ironPresenter = remember { IronPresenter() }

    // --- PREPARACIÓN DE DATOS DE HORMIGÓN ---
    val resLabel = stringResource(Res.string.recipe_section_resistance)
    val resUnit = stringResource(Res.string.recipe_unit_kilogram_per_square_centimeters)
    val propLabel = stringResource(Res.string.concrete_result_proportion)
    val techLabel = stringResource(Res.string.concrete_result_technical, "")
    val unitKg = stringResource(Res.string.unit_kilograms)

    val concreteOptions = remember(appSettings.customRecipes, appSettings.hiddenRecipeIds) {
        concretePresenter.getOptions(
            customRecipes = appSettings.customRecipes,
            hiddenIds = appSettings.hiddenRecipeIds,
            filterStructuralOnly = true,
            resLabel = resLabel,
            resUnit = resUnit,
            propLabel = propLabel,
            techLabel = techLabel,
            unitKg = unitKg
        )
    }

    // --- PREPARACIÓN DE DATOS DE HIERRO ---
    val ironOptions = remember(appSettings.customIrons, appSettings.hiddenIronIds) {
        ironPresenter.getOptions(
            customIrons = appSettings.customIrons,
            hiddenIds = appSettings.hiddenIronIds
        )
    }

    // --- INICIALIZACIÓN DEL STATE HOLDER ---
    val state = rememberStructureScreenState(
        appSettings = appSettings,
        calculateStructure = calculateStructure,
        concreteOptions = concreteOptions,
        ironOptions = ironOptions
    )

    val shareManager = remember { getShareManager() }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    keyboardController?.hide()
                    state.calculate()
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

            InputSection(title = stringResource(Res.string.structure_section_type)) {
                InputRow {
                    StructureType.entries.forEach { type ->
                        FilterChip(
                            selected = state.selectedStructureType == type,
                            onClick = { state.onStructureTypeChange(type) },
                            label = { Text(stringResource(type.labelRes)) },
                        )
                    }
                }
            }

            when (state.selectedStructureType) {
                StructureType.SLAB -> {
                    SlabInputs(state = state)
                }

                StructureType.BEAM, StructureType.COLUMN -> {
                    BeamColumnInputs(state = state)
                }
            }

            ErrorMessage(state.errorMsg)

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (state.showResultSheet) {
        if (state.selectedStructureType == StructureType.SLAB && state.slabResult != null) {
            val slabShareText = rememberSlabShareText(
                result = state.slabResult!!,
                width = state.slabWidth.toSafeDoubleOrNull() ?: 0.0,
                length = state.slabLength.toSafeDoubleOrNull() ?: 0.0,
                thickness = state.slabThickness.toSafeDoubleOrNull() ?: 0.0,
                concreteType = try {
                    ConcreteType.valueOf(state.selectedConcreteOption?.id ?: "")
                } catch (e: Exception) {
                    ConcreteType.H21
                },
                appName = appName
            )

            AppResultBottomSheet(
                onDismissRequest = { state.showResultSheet = false },
                onSave = { /* ... */ },
                onEdit = { state.showResultSheet = false },
                onShare = { shareManager.shareText(slabShareText) }
            ) {
                SlabResultContent(state.slabResult!!, appSettings)
            }
        } else if (state.result != null) {
            val shareText = rememberStructureShareText(
                result = state.result!!,
                length = state.length.toSafeDoubleOrNull() ?: 0.0,
                sideA = state.sideA.toSafeDoubleOrNull() ?: 0.0,
                sideB = state.sideB.toSafeDoubleOrNull() ?: 0.0,
                isCircular = state.isCircular,
                concreteType = try {
                    ConcreteType.valueOf(state.selectedConcreteOption?.id ?: "")
                } catch (e: Exception) {
                    ConcreteType.H21
                },
                stirrupSpacingCm = state.stirrupSpacingM.toSafeDoubleOrNull()?.times(100) ?: 20.0,
                appName = appName
            )

            AppResultBottomSheet(
                onDismissRequest = { state.showResultSheet = false },
                onSave = { /* ... */ },
                onEdit = { state.showResultSheet = false },
                onShare = { shareManager.shareText(shareText) }
            ) {
                StructureResultContent(state.result!!, appSettings)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeamColumnInputs(state: StructureScreenState) {
    val structureName =
        if (state.selectedStructureType == StructureType.BEAM) stringResource(Res.string.structure_type_beam) else stringResource(
            Res.string.structure_type_column
        )

    val focusSideA = remember { FocusRequester() }
    val focusSideB = remember { FocusRequester() }
    val focusLength = remember { FocusRequester() }
    val focusQuantityRods = remember { FocusRequester() }
    val focusStirrupSpacing = remember { FocusRequester() }

    RequestFocusOnStart(focusSideA)

    if (state.selectedStructureType == StructureType.COLUMN) {
        InputSection(title = stringResource(Res.string.structure_section_column_shape)) {
            InputRow {
                RadioButtonRow(
                    selected = !state.isCircular,
                    text = stringResource(Res.string.structure_label_rectangular),
                    onClick = { state.isCircular = false })
                Spacer(modifier = Modifier.width(16.dp))
                RadioButtonRow(
                    selected = state.isCircular,
                    text = stringResource(Res.string.structure_label_circular),
                    onClick = { state.isCircular = true })
            }
        }
    }

    InputSection(title = stringResource(Res.string.structure_section_dimensions, structureName)) {
        InputRow {
            CmInput(
                value = state.sideA,
                onValueChange = { state.sideA = it },
                label = if (state.isCircular) stringResource(
                    Res.string.structure_label_diameter,
                    stringResource(Res.string.unit_meters)
                ) else stringResource(Res.string.structure_label_side_a, stringResource(Res.string.unit_meters)),
                suffix = { Text(stringResource(Res.string.unit_meters)) },
                modifier = Modifier.weight(1f),
                focusRequester = focusSideA,
                nextFocusRequester = if (!state.isCircular) focusSideB else focusLength
            )
            if (!state.isCircular) {
                CmInput(
                    value = state.sideB,
                    onValueChange = { state.sideB = it },
                    label = stringResource(Res.string.structure_label_side_b, stringResource(Res.string.unit_meters)),
                    suffix = { Text(stringResource(Res.string.unit_meters)) },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusSideB,
                    nextFocusRequester = focusLength
                )
            }
        }
        NumericInput(
            value = state.length,
            onValueChange = { state.length = it },
            label = stringResource(Res.string.structure_label_total_length, stringResource(Res.string.unit_meters)),
            suffix = { Text(stringResource(Res.string.unit_meters)) },
            modifier = Modifier.fillMaxWidth(),
            focusRequester = focusLength,
            nextFocusRequester = focusQuantityRods
        )
    }

    InputSection(title = stringResource(Res.string.structure_section_armature)) {
        InputRow {
            IronSelectorField(
                options = state.ironOptions,
                selectedOption = state.selectedMainIron,
                onOptionSelected = { state.onMainIronChange(it) },
                modifier = Modifier.weight(0.5f),
                label = stringResource(Res.string.structure_label_main_iron),
                customLabel = Res.string.label_custom_c
            )
            NumericInput(
                value = state.quantityIronRods,
                onValueChange = { state.quantityIronRods = it },
                label = stringResource(Res.string.structure_label_rods_quantity),
                suffix = { Text(stringResource(Res.string.unit_units)) },
                modifier = Modifier.weight(0.5f),
                focusRequester = focusQuantityRods,
                nextFocusRequester = focusStirrupSpacing
            )
        }

        InputRow {
            IronSelectorField(
                options = state.ironOptions,
                selectedOption = state.selectedStirrup,
                onOptionSelected = { state.selectedStirrup = it },
                modifier = Modifier.weight(0.5f),
                label = stringResource(Res.string.structure_label_stirrups),
                customLabel = Res.string.label_custom_c
            )
            CmInput(
                value = state.stirrupSpacingM,
                onValueChange = { state.stirrupSpacingM = it },
                label = stringResource(
                    Res.string.structure_label_stirrup_spacing,
                    stringResource(Res.string.unit_meters)
                ),
                suffix = { Text(stringResource(Res.string.unit_meters)) },
                modifier = Modifier.weight(0.5f),
                focusRequester = focusStirrupSpacing,
                nextFocusRequester = null
            )
        }
    }

    InputSection(title = stringResource(Res.string.structure_section_terminations)) {
        InputColumn(title = TextSource.Resource(Res.string.structure_label_start_end)) {
            // --- Extremo Inicial ---
            InputRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RebarTerminationType.entries.forEach { type ->
                    FilterChip(
                        selected = (type == state.selectedStartTermination),
                        onClick = { state.onStartTerminationChange(type) },
                        label = { Text(stringResource(type.displayNameRes)) },
                        leadingIcon = {
                            RebarShapeIcon(
                                type = type,
                                color = if (type == state.selectedStartTermination) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }
            if (state.selectedStartTermination != RebarTerminationType.STRAIGHT) {
                CmInput(
                    value = state.startHookLength,
                    onValueChange = { state.startHookLength = it },
                    label = stringResource(
                        Res.string.structure_label_hook_start,
                        stringResource(Res.string.unit_meters)
                    ),
                    suffix = { Text(stringResource(Res.string.unit_meters)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        InputColumn(title = TextSource.Resource(Res.string.structure_label_end_end)) {
            // --- Extremo Final ---
            InputRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RebarTerminationType.entries.forEach { type ->
                    FilterChip(
                        selected = (type == state.selectedEndTermination),
                        onClick = { state.onEndTerminationChange(type) },
                        label = { Text(stringResource(type.displayNameRes)) },
                        leadingIcon = {
                            RebarShapeIcon(
                                type = type,
                                color = if (type == state.selectedEndTermination) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }
            if (state.selectedEndTermination != RebarTerminationType.STRAIGHT) {
                CmInput(
                    value = state.endHookLength,
                    onValueChange = { state.endHookLength = it },
                    label = stringResource(Res.string.structure_label_hook_end, stringResource(Res.string.unit_meters)),
                    suffix = { Text(stringResource(Res.string.unit_meters)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    InputSection(title = stringResource(Res.string.structure_section_concrete), showDivider = false) {
        ConcreteSelectorField(
            options = state.concreteOptions,
            selectedOption = state.selectedConcreteOption,
            onOptionSelected = { state.selectedConcreteOption = it },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SlabInputs(state: StructureScreenState) {
    val focusWidth = remember { FocusRequester() }
    val focusLength = remember { FocusRequester() }
    val focusThickness = remember { FocusRequester() }
    val focusSepX = remember { FocusRequester() }
    val focusSepY = remember { FocusRequester() }

    RequestFocusOnStart(focusWidth)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InputSection(title = stringResource(Res.string.structure_section_slab_dimensions)) {
            InputRow {
                NumericInput(
                    value = state.slabWidth,
                    onValueChange = { state.slabWidth = it },
                    label = stringResource(Res.string.structure_label_width_x),
                    suffix = { Text(stringResource(Res.string.unit_meters)) },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusWidth,
                    nextFocusRequester = focusLength
                )
                NumericInput(
                    value = state.slabLength,
                    onValueChange = { state.slabLength = it },
                    label = stringResource(Res.string.structure_label_length_y),
                    suffix = { Text(stringResource(Res.string.unit_meters)) },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusLength,
                    nextFocusRequester = focusThickness
                )
            }
            CmInput(
                value = state.slabThickness,
                onValueChange = { state.slabThickness = it },
                label = stringResource(Res.string.label_thickness, stringResource(Res.string.unit_meters)),
                suffix = { Text(stringResource(Res.string.unit_meters)) },
                modifier = Modifier.fillMaxWidth(),
                focusRequester = focusThickness,
                nextFocusRequester = if (state.isManualRebar) focusSepX else null
            )
        }

        InputSection(title = stringResource(Res.string.structure_section_armature_config)) {
            MeshSelectorField(
                selectedMeshId = state.selectedMeshId,
                onMeshSelected = { state.selectedMeshId = it },
                isManualRebar = state.isManualRebar,
                onModeToggle = { state.isManualRebar = it }
            )
            if (state.isManualRebar) {
                // Configuración Eje X
                InputRow(title = TextSource.Resource(Res.string.structure_label_armature_x)) {
                    IronSelectorField(
                        options = state.ironOptions,
                        selectedOption = state.selectedPhiX,
                        onOptionSelected = { state.onPhiXChange(it) },
                        modifier = Modifier.weight(0.5f),
                        label = stringResource(Res.string.structure_label_iron_x),
                        customLabel = Res.string.label_custom_c
                    )
                    CmInput(
                        value = state.separationX,
                        onValueChange = { state.separationX = it },
                        label = stringResource(
                            Res.string.structure_label_sep_x,
                            stringResource(Res.string.unit_meters)
                        ),
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(0.5f),
                        focusRequester = focusSepX,
                        nextFocusRequester = focusSepY
                    )
                }

                // Configuración Eje Y
                InputRow(title = TextSource.Resource(Res.string.structure_label_armature_y)) {
                    IronSelectorField(
                        options = state.ironOptions,
                        selectedOption = state.selectedPhiY,
                        onOptionSelected = { state.onPhiYChange(it) },
                        modifier = Modifier.weight(0.5f),
                        label = stringResource(Res.string.structure_label_iron_y),
                        customLabel = Res.string.label_custom_c
                    )
                    CmInput(
                        value = state.separationY,
                        onValueChange = { state.separationY = it },
                        label = stringResource(
                            Res.string.structure_label_sep_y,
                            stringResource(Res.string.unit_meters)
                        ),
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(0.5f),
                        focusRequester = focusSepY,
                        nextFocusRequester = null
                    )
                }
            }
        }

        if (state.isManualRebar) {
            InputSection(
                title = stringResource(Res.string.structure_section_terminations),
                attenuatedTitle = stringResource(Res.string.structure_label_terminations_both)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RebarTerminationType.entries.forEach { type ->
                            FilterChip(
                                selected = (type == state.selectedSlabTermination),
                                onClick = { state.onSlabTerminationChange(type) },
                                label = { Text(stringResource(type.displayNameRes)) },
                                leadingIcon = {
                                    RebarShapeIcon(
                                        type = type,
                                        color = if (type == state.selectedSlabTermination) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )
                        }
                    }
                    if (state.selectedSlabTermination != RebarTerminationType.STRAIGHT) {
                        CmInput(
                            value = state.slabHookLength,
                            onValueChange = { state.slabHookLength = it },
                            label = stringResource(
                                Res.string.structure_label_hook,
                                stringResource(Res.string.unit_meters)
                            ),
                            suffix = { Text(stringResource(Res.string.unit_meters)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        InputSection(title = stringResource(Res.string.structure_section_concrete), showDivider = false) {
            ConcreteSelectorField(
                options = state.concreteOptions,
                selectedOption = state.selectedConcreteOption,
                onOptionSelected = { state.selectedConcreteOption = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        val sepX = state.separationX.toSafeDoubleOrNull() ?: 0.0
        val sepY = state.separationY.toSafeDoubleOrNull() ?: 0.0
        if (state.isManualRebar && (sepX > 0.3 || sepY > 0.3)) {
            WarningMessage(TextSource.Resource(Res.string.structure_warning_cirsoc))
        }
    }
}

/**
 * Componente local para los Radio Buttons.
 *
 * @param selected Indica si el Radio Button está seleccionado.
 * @param text Texto del Radio Button.
 * @param onClick Acción al hacer clic en el Radio Button.
 */
@Composable
fun RadioButtonRow(selected: Boolean, text: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.selectable(selected = selected, onClick = onClick)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

/**
 * Componente que muestra el contenido del resultado.
 *
 * @param res Resultado del cálculo.
 */
@Composable
fun StructureResultContent(res: StructureResult, appSettings: AppSettingsState) {
    // Sección Hormigón
    Text(
        stringResource(Res.string.structure_result_concrete, res.volumeConcreteM3.roundToDecimals(2)),
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        stringResource(Res.string.structure_result_waste_included, (res.percentageConcreteWaste * 100).toInt()),
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    ResultRow(
        label = stringResource(Res.string.structure_result_cement),
        value = res.cementKg.toPresentationUnit(
            res.cementBagKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        )
    )

    ResultRow(
        label = stringResource(Res.string.structure_result_sand),
        value = stringResource(
            Res.string.concrete_result_volume_m3,
            res.sandM3.roundToDecimals(2),
            stringResource(Res.string.unit_cubic_meters)
        )
    )

    ResultRow(
        label = stringResource(Res.string.structure_result_gravel),
        value = stringResource(
            Res.string.concrete_result_volume_m3,
            res.gravelM3.roundToDecimals(2),
            stringResource(Res.string.unit_cubic_meters)
        )
    )

    ResultRow(
        label = stringResource(Res.string.structure_result_water),
        value = stringResource(
            Res.string.concrete_result_volume_liters,
            res.waterLiters.roundToDecimals(1),
            stringResource(Res.string.unit_liters)
        )
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Sección Hierro
    Text(
        stringResource(Res.string.structure_result_iron_steel, (res.mainIronKg + res.stirrupIronKg).roundToDecimals(1)),
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        stringResource(
            Res.string.structure_result_waste_included,
            ((res.percentageMainIronWaste + res.percentageStirrupIronWaste) * 50).toInt()
        ),
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    ResultRow(
        label = stringResource(Res.string.structure_result_main, res.mainDiameterMm),
        value = stringResource(
            Res.string.concrete_result_volume_m3,
            res.mainIronMeters.roundToDecimals(1),
            stringResource(Res.string.unit_meters)
        )
    )
    Text(
        stringResource(Res.string.structure_result_weight_kg, res.mainIronKg.roundToDecimals(1)),
        style = MaterialTheme.typography.bodySmall
    )

    ResultRow(
        label = stringResource(Res.string.structure_result_stirrups, res.stirrupDiameterMm),
        value = stringResource(
            Res.string.concrete_result_volume_m3,
            res.stirrupIronMeters.roundToDecimals(1),
            stringResource(Res.string.unit_meters)
        )
    )
    Text(
        stringResource(Res.string.structure_result_weight_kg, res.stirrupIronKg.roundToDecimals(1)),
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Tarjeta anidada para el consejo (Tip)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.structure_result_tip_intro) + "\n" +
                        stringResource(
                            Res.string.structure_result_tip_main,
                            res.mainIronAmount,
                            if (res.mainIronAmount != 1) "s" else "",
                            res.mainDiameterMm
                        ) + "\n" +
                        stringResource(
                            Res.string.structure_result_tip_stirrup,
                            res.stirrupIronAmount,
                            if (res.stirrupIronAmount != 1) "s" else "",
                            res.stirrupDiameterMm
                        ),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }

    // --- CÁLCULO DE PRECIOS ---
    val prices = appSettings.priceSettings
    var materialCost = 0.0

    // Cemento
    prices.materialPrices.find { it.name.contains("Cemento", ignoreCase = true) }?.let {
        if (it.unit.contains("bolsa", ignoreCase = true)) {
            materialCost += it.price * res.cementBagKg
        } else if (it.unit.contains("kg", ignoreCase = true)) {
            materialCost += it.price * res.cementKg
        }
    }

    // Arena
    prices.materialPrices.find { it.name.contains("Arena", ignoreCase = true) }?.let {
        if (it.unit.contains("m3", ignoreCase = true)) {
            materialCost += it.price * res.sandM3
        }
    }

    // Piedra
    prices.materialPrices.find {
        it.name.contains("Piedra", ignoreCase = true) || it.name.contains(
            "Canto",
            ignoreCase = true
        )
    }?.let {
        if (it.unit.contains("m3", ignoreCase = true)) {
            materialCost += it.price * res.gravelM3
        }
    }

    // Hierro Principal
    prices.materialPrices.find {
        it.name.contains(
            "Hierro",
            ignoreCase = true
        ) && it.name.contains(res.mainDiameterMm.toString())
    }?.let {
        if (it.unit.contains("barra", ignoreCase = true) || it.unit.contains("varilla", ignoreCase = true)) {
            // Asumimos barra de 12m
            materialCost += it.price * (res.mainIronMeters / 12.0)
        } else if (it.unit.contains("kg", ignoreCase = true)) {
            materialCost += it.price * res.mainIronKg
        }
    }

    // Hierro Estribos
    prices.materialPrices.find {
        it.name.contains(
            "Hierro",
            ignoreCase = true
        ) && it.name.contains(res.stirrupDiameterMm.toString())
    }?.let {
        if (it.unit.contains("barra", ignoreCase = true) || it.unit.contains("varilla", ignoreCase = true)) {
            materialCost += it.price * (res.stirrupIronMeters / 12.0)
        } else if (it.unit.contains("kg", ignoreCase = true)) {
            materialCost += it.price * res.stirrupIronKg
        }
    }

    // Mano de Obra (Viga/Columna)
    var laborCost = 0.0
    prices.laborPrices.find {
        it.name.contains("Viga", ignoreCase = true) || it.name.contains(
            "Columna",
            ignoreCase = true
        )
    }?.let {
        if (it.unit.contains("ml", ignoreCase = true) || it.unit.contains("m", ignoreCase = true)) {
            // Asumimos que el largo es lo que se cobra
            // No tenemos el largo directo en StructureResult, pero podemos estimarlo del volumen o pasarlo
            // Para simplificar, usamos volumen si es m3, o nada si es ml porque falta el dato
            if (it.unit.contains("m3", ignoreCase = true)) {
                laborCost += it.price * res.volumeConcreteM3
            }
        }
    }

    PriceResultSection(materialCost, laborCost)
}

@Composable
fun SlabResultContent(res: SlabResult, appSettings: AppSettingsState) {
    // Sección Hormigón
    Text(
        stringResource(Res.string.structure_result_concrete, res.volumeConcreteM3.roundToDecimals(2)),
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        stringResource(Res.string.structure_result_waste_included, (res.percentageConcreteWaste * 100).toInt()),
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    ResultRow(
        label = stringResource(Res.string.structure_result_cement),
        value = res.cementKg.toPresentationUnit(
            res.cementBagKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        )
    )

    ResultRow(
        label = stringResource(Res.string.structure_result_sand),
        value = stringResource(
            Res.string.concrete_result_volume_m3,
            res.sandM3.roundToDecimals(2),
            stringResource(Res.string.unit_cubic_meters)
        )
    )

    ResultRow(
        label = stringResource(Res.string.structure_result_gravel),
        value = stringResource(
            Res.string.concrete_result_volume_m3,
            res.gravelM3.roundToDecimals(2),
            stringResource(Res.string.unit_cubic_meters)
        )
    )

    ResultRow(
        label = stringResource(Res.string.structure_result_water),
        value = stringResource(
            Res.string.concrete_result_volume_liters,
            res.waterLiters.roundToDecimals(1),
            stringResource(Res.string.unit_liters)
        )
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Sección Hierro
    if (res.suggestedMesh != null) {
        Text(
            stringResource(Res.string.structure_result_mesh),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        ResultRow(
            label = stringResource(Res.string.structure_result_mesh_type),
            value = res.suggestedMesh
        )
        if (res.meshPanelsNeeded != null) {
            ResultRow(
                label = stringResource(Res.string.structure_result_mesh_panels),
                value = "${res.meshPanelsNeeded} u"
            )
        }
    } else {
        Text(
            stringResource(Res.string.structure_result_iron_steel, res.totalWeightKg.roundToDecimals(1)),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            stringResource(Res.string.structure_result_waste_included, (res.percentageIronWaste * 100).toInt()),
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (res.diameterX != res.diameterY) {
            ResultRow(
                label = stringResource(Res.string.structure_result_iron_x, res.diameterX),
                value = stringResource(
                    Res.string.concrete_result_volume_m3,
                    (res.lengthX * res.countX * (1 + res.percentageIronWaste)).roundToDecimals(1),
                    stringResource(Res.string.unit_meters)
                )
            )
            Text(
                stringResource(
                    Res.string.structure_result_weight_rods,
                    res.weightX.roundToDecimals(1),
                    ceil((res.lengthX * res.countX * (1 + res.percentageIronWaste)) / 12).toInt()
                ),
                style = MaterialTheme.typography.bodySmall
            )

            ResultRow(
                label = stringResource(Res.string.structure_result_iron_y, res.diameterY),
                value = stringResource(
                    Res.string.concrete_result_volume_m3,
                    (res.lengthY * res.countY * (1 + res.percentageIronWaste)).roundToDecimals(1),
                    stringResource(Res.string.unit_meters)
                )
            )
            Text(
                stringResource(
                    Res.string.structure_result_weight_rods,
                    res.weightY.roundToDecimals(1),
                    ceil((res.lengthY * res.countY * (1 + res.percentageIronWaste)) / 12).toInt()
                ),
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            ResultRow(
                label = stringResource(Res.string.structure_result_iron_generic, res.diameterX),
                value = stringResource(
                    Res.string.concrete_result_volume_m3,
                    ((res.lengthX * res.countX + res.lengthY * res.countY) * (1 + res.percentageIronWaste)).roundToDecimals(
                        1
                    ),
                    stringResource(Res.string.unit_meters)
                )
            )
            Text(
                stringResource(
                    Res.string.structure_result_weight_rods,
                    (res.weightX + res.weightY).roundToDecimals(1),
                    ceil(((res.lengthX * res.countX + res.lengthY * res.countY) * (1 + res.percentageIronWaste)) / 12).toInt()
                ),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Row {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(Res.string.structure_result_detail_title),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        Res.string.structure_result_detail_x,
                        res.countX,
                        res.lengthX.roundToDecimals(2)
                    ) + "\n" +
                            stringResource(
                                Res.string.structure_result_detail_y,
                                res.countY,
                                res.lengthY.roundToDecimals(2)
                            ),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }

    // --- CÁLCULO DE PRECIOS ---
    val prices = appSettings.priceSettings
    var materialCost = 0.0

    // Cemento
    prices.materialPrices.find { it.name.contains("Cemento", ignoreCase = true) }?.let {
        if (it.unit.contains("bolsa", ignoreCase = true)) {
            materialCost += it.price * res.cementBagKg
        } else if (it.unit.contains("kg", ignoreCase = true)) {
            materialCost += it.price * res.cementKg
        }
    }

    // Arena
    prices.materialPrices.find { it.name.contains("Arena", ignoreCase = true) }?.let {
        if (it.unit.contains("m3", ignoreCase = true)) {
            materialCost += it.price * res.sandM3
        }
    }

    // Piedra
    prices.materialPrices.find {
        it.name.contains("Piedra", ignoreCase = true) || it.name.contains(
            "Canto",
            ignoreCase = true
        )
    }?.let {
        if (it.unit.contains("m3", ignoreCase = true)) {
            materialCost += it.price * res.gravelM3
        }
    }

    // Malla
    if (res.suggestedMesh != null) {
        prices.materialPrices.find {
            it.name.contains("Malla", ignoreCase = true) || it.name.contains(
                "Sima",
                ignoreCase = true
            )
        }?.let {
            if (it.unit.contains("u", ignoreCase = true) || it.unit.contains("panel", ignoreCase = true)) {
                materialCost += it.price * (res.meshPanelsNeeded ?: 0)
            }
        }
    } else {
        // Hierro
        // Simplificación: Buscamos "Hierro" y usamos el precio por kg si existe, o por barra estimando
        prices.materialPrices.find { it.name.contains("Hierro", ignoreCase = true) }?.let {
            if (it.unit.contains("kg", ignoreCase = true)) {
                materialCost += it.price * res.totalWeightKg
            }
        }
    }

    // Mano de Obra (Losa)
    var laborCost = 0.0
    prices.laborPrices.find { it.name.contains("Losa", ignoreCase = true) }?.let {
        if (it.unit.contains("m2", ignoreCase = true)) {
            // Estimamos área: volumen / espesor promedio (o usamos inputs si los tuviéramos aquí)
            // Como no tenemos el área directa en SlabResult, usamos volumen / 0.1 (espesor aprox) o lo omitimos
            // Mejor: Si es m3, usamos volumen
            if (it.unit.contains("m3", ignoreCase = true)) {
                laborCost += it.price * res.volumeConcreteM3
            }
        }
    }

    PriceResultSection(materialCost, laborCost)
}
