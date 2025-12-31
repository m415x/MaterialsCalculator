/*
 * materialCalc
 * Copyright (C) 2025 M415X
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
import androidx.compose.runtime.*
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
import org.m415x.materialcalc.domain.model.DosificacionHormigon
import org.m415x.materialcalc.domain.model.ResultadoHormigon
import org.m415x.materialcalc.domain.usecase.CalculateConcreteUseCase
import org.m415x.materialcalc.ui.common.*

/**
 * Pantalla principal de la calculadora de hormigón.
 *
 * @param settingsRepository El repositorio de configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcreteScreen(settingsRepository: SettingsRepository) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val appName = stringResource(Res.string.app_name)
    val calculateConcrete = remember { CalculateConcreteUseCase() }

    val weightBagCement by settingsRepository.bagCementKg.collectAsState(initial = 25)
    val weightBagLime by settingsRepository.bagLimeKg.collectAsState(initial = 25)
    val wasteConcretePct by settingsRepository.wasteConcretePct.collectAsState(5.0)

    // Usamos `null` como valor inicial para indicar que está cargando
    val defaultConcreteId by settingsRepository.defaultConcreteGenId.collectAsState(initial = null)

    var width by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var high by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }

    // Estado para el selector de hormigón
    var selectedRecipeId by remember { mutableStateOf("") }
    var selectedRecipe by remember { mutableStateOf<DosificacionHormigon?>(null) }

    var result by remember { mutableStateOf<ResultadoHormigon?>(null) }
    var showError by remember { mutableStateOf(false) } // Cambiado a Boolean
    var showResultSheet by remember { mutableStateOf(false) }

    val shareManager = remember { getShareManager() }

    val focusWidth = remember { FocusRequester() }
    val focusLength = remember { FocusRequester() }
    val focusHigh = remember { FocusRequester() }
    val focusQuantity = remember { FocusRequester() }
    val focusConcrete = remember { FocusRequester() }

    RequestFocusOnStart(focusWidth)

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    keyboardController?.hide()
                    val w = width.toSafeDoubleOrNull()
                    val l = length.toSafeDoubleOrNull()
                    val h = high.toSafeDoubleOrNull()
                    val q = quantity.toIntOrNull()

                    if (areValidDimensions(w, l, h, q) && selectedRecipe != null) {
                        result = calculateConcrete(
                            anchoMetros = w!!,
                            largoMetros = l!!,
                            espesorMetros = h!!,
                            quantityUnits = q!!,
                            receta = selectedRecipe!!,
                            pesoBolsaCementoKg = weightBagCement,
                            pesoBolsaCalKg = weightBagLime,
                            porcentajeDesperdicio = wasteConcretePct / 100.0
                        )
                        showError = false
                        showResultSheet = true
                    } else {
                        showError = true
                        result = null
                    }
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

            InputSection(title = stringResource(Res.string.concrete_section_dimensions)) {
                InputRow {
                    NumericInput(
                        value = width,
                        onValueChange = { width = it },
                        label = stringResource(
                            Res.string.concrete_label_width,
                            stringResource(Res.string.unit_meters)
                        ),
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusWidth,
                        nextFocusRequester = focusLength
                    )
                    NumericInput(
                        value = length,
                        onValueChange = { length = it },
                        label = stringResource(
                            Res.string.concrete_label_length,
                            stringResource(Res.string.unit_meters)
                        ),
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusLength,
                        nextFocusRequester = focusHigh
                    )
                }

                InputRow {
                    CmInput(
                        value = high,
                        onValueChange = { high = it },
                        label = stringResource(
                            Res.string.concrete_label_thickness,
                            stringResource(Res.string.unit_meters)
                        ),
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusHigh,
                        nextFocusRequester = focusQuantity
                    )
                    NumericInput(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = stringResource(
                            Res.string.concrete_label_quantity,
                        ),
                        suffix = { Text(stringResource(Res.string.unit_units)) },
                        modifier = Modifier.weight(1f),
                        onlyInteger = true,
                        focusRequester = focusQuantity,
                        nextFocusRequester = focusConcrete
                    )
                }
            }

            InputSection(title = stringResource(Res.string.concrete_section_resistance), showDivider = false) {
                ConcreteSelectorField(
                    selectedRecipeId = selectedRecipeId,
                    onRecipeSelected = { id, receta ->
                        selectedRecipeId = id
                        selectedRecipe = receta
                    },
                    settingsRepository = settingsRepository,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusConcrete),
                    defaultRecipeId = defaultConcreteId
                )
            }

            if (showError) {
                Text(
                    text = stringResource(Res.string.concrete_error_validation),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    if (showResultSheet && result != null) {
        // Generamos el texto para compartir usando el Composable
        val shareText = rememberConcreteShareText(
            result = result!!,
            width = width.toSafeDoubleOrNull() ?: 0.0,
            length = length.toSafeDoubleOrNull() ?: 0.0,
            high = high.toSafeDoubleOrNull() ?: 0.0,
            quantity = quantity.toIntOrNull() ?: 1,
            nameConcrete = selectedRecipe?.nombre ?: "N/A",
            proportionConcrete = selectedRecipe?.descripcionProporcion ?: "N/A",
            appName = appName
        )

        AppResultBottomSheet(
            onDismissRequest = { showResultSheet = false },
            onSave = { /* TODO */ },
            onEdit = { showResultSheet = false },
            onShare = { shareManager.shareText(shareText) }
        ) {
            ConcreteResultContent(result!!)
        }
    }
}

@Composable
fun ConcreteResultContent(res: ResultadoHormigon) {
    val unitM3 = stringResource(Res.string.unit_cubic_meters)
    val unitKg = stringResource(Res.string.unit_kilograms)
    val unitLt = stringResource(Res.string.unit_liters)

    Text(
        stringResource(
            Res.string.concrete_result_total_volume,
            res.volumenTotalM3.roundToDecimals(2),
            unitM3
        ),
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        stringResource(
            Res.string.concrete_result_waste_included,
            (res.porcentajeDesperdicioHormigon * 100).toInt()
        ),
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(16.dp))

    ResultRow(
        label = stringResource(Res.string.concrete_result_cement),
        value = res.cementoKg.toPresentationUnit(
            res.bolsaCementoKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        )
    )
    Text(
        stringResource(
            Res.string.concrete_result_weight_kg,
            res.cementoKg.roundToDecimals(1),
            unitKg
        ),
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    ResultRow(
        label = stringResource(Res.string.concrete_result_sand),
        value = stringResource(
            Res.string.concrete_result_volume_m3,
            res.arenaM3.roundToDecimals(2),
            unitM3
        )
    )

    ResultRow(
        label = stringResource(Res.string.concrete_result_gravel),
        value = stringResource(
            Res.string.concrete_result_volume_m3,
            res.piedraM3.roundToDecimals(2),
            unitM3
        )
    )

    ResultRow(
        label = stringResource(Res.string.concrete_result_water),
        value = stringResource(
            Res.string.concrete_result_volume_liters,
            res.aguaLitros.roundToDecimals(1),
            unitLt
        )
    )
}