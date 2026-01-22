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
import org.m415x.materialcalc.domain.model.AppSettingsState
import org.m415x.materialcalc.domain.model.PlasterResult
import org.m415x.materialcalc.domain.model.asString
import org.m415x.materialcalc.domain.usecase.CalculatePlasterUseCase
import org.m415x.materialcalc.ui.common.dialogs.AppDialog
import org.m415x.materialcalc.ui.common.display.AppResultBottomSheet
import org.m415x.materialcalc.ui.common.display.ErrorMessage
import org.m415x.materialcalc.ui.common.display.PriceResultSection
import org.m415x.materialcalc.ui.common.display.ResultRow
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
fun PlasterScreen(appSettings: AppSettingsState) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // Nombre de la app
    val appName = stringResource(Res.string.app_name)

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
    RequestFocusOnStart(focusLength)

    Scaffold(
        // El FAB vive aquí, donde tiene acceso a las variables 'largo' y 'alto'
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
                .verticalScroll(rememberScrollState()), // Permite scrollear si el teclado tapa
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            InputSection(title = stringResource(Res.string.plaster_section_dimensions)) {
                InputRow {
                    NumericInput(
                        value = state.length,
                        onValueChange = { state.length = it },
                        label = stringResource(Res.string.plaster_label_length, stringResource(Res.string.unit_meters)),
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusLength,
                        nextFocusRequester = focusHeight
                    )
                    NumericInput(
                        value = state.height,
                        onValueChange = { state.height = it },
                        label = stringResource(Res.string.plaster_label_height, stringResource(Res.string.unit_meters)),
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
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.5f
                            )
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Science,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    state.selectMortar!!.name.asString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    state.selectMortar!!.mixingRatio.asString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (state.recipeOptions.size > 1) {
                                TextButton(onClick = { state.showMixDialog = true }) {
                                    Text(stringResource(Res.string.button_change))
                                }
                            }
                        }
                    }
                }

                InputRow(horizontalArrangement = Arrangement.SpaceBetween) {
                    CmInput(
                        value = state.thickThickness,
                        onValueChange = { state.thickThickness = it },
                        label = stringResource(
                            Res.string.plaster_label_thickness,
                            stringResource(Res.string.unit_meters)
                        ),
                        placeholder = "0.02",
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

            ErrorMessage(state.errorMsg)

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
            appName = appName
        )

        AppResultBottomSheet(
            onDismissRequest = { state.showResultSheet = false },
            onSave = { /* ... */ },
            onEdit = { state.showResultSheet = false },
            onShare = { shareManager.shareText(shareText) }
        ) {
            PlasterResultContent(state.result!!, appSettings)
        }
    }
}

/**
 * Composable que muestra el contenido del resultado.
 *
 * @param res Resultado del cálculo.
 */
@Composable
fun PlasterResultContent(res: PlasterResult, appSettings: AppSettingsState) {
    Text(
        stringResource(Res.string.plaster_result_total_area, res.totalAreaM2.roundToDecimals(2)),
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Sección GRUESO
    Text(
        stringResource(Res.string.plaster_result_thick_title),
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        stringResource(Res.string.plaster_result_waste_included, (res.thickPercentageWaste * 100).toInt()),
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    ResultRow(
        stringResource(Res.string.plaster_result_cement),
        res.thickCementKg.toPresentationUnit(
            res.cementBagKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        ),
    )

    ResultRow(
        stringResource(Res.string.plaster_result_lime),
        res.thickLimeKg.toPresentationUnit(
            res.limeBagKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        ),
    )

    ResultRow(
        stringResource(Res.string.plaster_result_sand),
        stringResource(
            Res.string.concrete_result_volume_m3,
            res.thickSandKg.roundToDecimals(2),
            stringResource(Res.string.unit_cubic_meters)
        )
    )

    ResultRow(
        stringResource(Res.string.plaster_result_water),
        stringResource(
            Res.string.concrete_result_volume_liters,
            res.thickWaterLiters.roundToDecimals(1),
            stringResource(Res.string.unit_liters)
        )
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Sección FINO
    Text(
        stringResource(Res.string.plaster_result_fine_title),
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        stringResource(Res.string.plaster_result_waste_included, (res.finePercentageWaste * 100).toInt()),
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(stringResource(Res.string.plaster_result_choose_option), style = MaterialTheme.typography.labelLarge)

    Spacer(modifier = Modifier.height(8.dp))

    // Opción A
    ResultRow(
        stringResource(Res.string.plaster_result_option_a),
        res.finePremixKg.toPresentationUnit(
            res.premixBagKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        )
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Opción B
    ResultRow(
        stringResource(Res.string.plaster_result_option_b),
        res.fineLimeKg.toPresentationUnit(
            res.limeBagKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        )
    )

    ResultRow(
        stringResource(Res.string.plaster_result_fine_sand),
        stringResource(
            Res.string.concrete_result_volume_m3,
            res.fineSandM3.roundToDecimals(2),
            stringResource(Res.string.unit_cubic_meters)
        )
    )

    // --- CÁLCULO DE PRECIOS ---
    val prices = appSettings.priceSettings
    var materialCost = 0.0
    
    // Cemento
    prices.materialPrices.find { it.name.contains("Cemento", ignoreCase = true) }?.let {
        if (it.unit.contains("bolsa", ignoreCase = true)) {
            materialCost += it.price * res.cementBagKg
        } else if (it.unit.contains("kg", ignoreCase = true)) {
            materialCost += it.price * res.thickCementKg
        }
    }
    
    // Cal
    if (res.thickLimeKg > 0 || res.fineLimeKg > 0) {
        prices.materialPrices.find { it.name.contains("Cal", ignoreCase = true) }?.let {
             if (it.unit.contains("bolsa", ignoreCase = true)) {
                materialCost += it.price * res.limeBagKg
            } else if (it.unit.contains("kg", ignoreCase = true)) {
                materialCost += it.price * (res.thickLimeKg + res.fineLimeKg)
            }
        }
    }
    
    // Arena
    prices.materialPrices.find { it.name.contains("Arena", ignoreCase = true) }?.let {
        if (it.unit.contains("m3", ignoreCase = true)) {
            materialCost += it.price * (res.thickSandKg + res.fineSandM3)
        }
    }
    
    // Premezcla (Opción A) - Sumamos si hay precio, asumiendo que se elige A
    prices.materialPrices.find { it.name.contains("Premezcla", ignoreCase = true) || it.name.contains("Fino", ignoreCase = true) }?.let {
        if (it.unit.contains("bolsa", ignoreCase = true)) {
            materialCost += it.price * res.premixBagKg
        }
    }

    // Mano de Obra (Revoque)
    var laborCost = 0.0
    prices.laborPrices.find { it.name.contains("Revoque", ignoreCase = true) || it.name.contains("Jaharro", ignoreCase = true) }?.let {
        if (it.unit.contains("m2", ignoreCase = true)) {
            laborCost += it.price * res.totalAreaM2
        }
    }

    PriceResultSection(materialCost, laborCost)
}
