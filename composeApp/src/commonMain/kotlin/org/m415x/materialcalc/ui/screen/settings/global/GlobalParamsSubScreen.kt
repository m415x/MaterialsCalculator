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

package org.m415x.materialcalc.ui.screen.settings.global

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.data.repository.SettingsRepository.Defaults
import org.m415x.materialcalc.domain.model.AppSettingsState
import org.m415x.materialcalc.ui.common.inputs.*
import org.m415x.materialcalc.ui.common.presenters.BrickPresenter
import org.m415x.materialcalc.ui.common.presenters.ConcretePresenter
import org.m415x.materialcalc.ui.common.presenters.MortarPresenter
import org.m415x.materialcalc.ui.screen.settings.EditDoubleSetting
import org.m415x.materialcalc.ui.screen.settings.EditIntegerSetting
import org.m415x.materialcalc.ui.screen.settings.EditPercentSetting
import org.m415x.materialcalc.ui.screen.settings.SettingsAccordion

/**
 * Pantalla de parámetros globales.
 *
 * @param repository El repositorio de configuración.
 * @param appSettings El estado global de la configuración.
 */
@Composable
fun GlobalParamsSubScreen(repository: SettingsRepository, appSettings: AppSettingsState) {
    val scope = rememberCoroutineScope()

    // Instanciamos los Presenters
    val brickPresenter = remember { BrickPresenter() }
    val concretePresenter = remember { ConcretePresenter() }
    val mortarPresenter = remember { MortarPresenter() }

    // --- DATOS PARA LAS LISTAS (Fusión) ---
    val customBricks = appSettings.customBricks
    val customRecipes = appSettings.customRecipes
    val hiddenBricks = appSettings.hiddenBrickIds
    val hiddenRecipes = appSettings.hiddenRecipeIds

    // Lectura de valores (con valores por defecto mientras carga)
    val cementWeight = appSettings.bagCementKg
    val limeWeight = appSettings.bagLimeKg
    val premixWeight = appSettings.bagPremixKg
    val bucketVol = appSettings.bucketVolL
    val barrowVol = appSettings.barrowVolL
    val mixerVol = appSettings.mixerVolL
    val fineThick = appSettings.fineThicknessMm
    val wConcrete = appSettings.wasteConcretePct
    val wMortar = appSettings.wasteMortarPct
    val wBrick = appSettings.wasteBrickPct
    val wIronMain = appSettings.wasteIronMainPct
    val wIronStirrup = appSettings.wasteIronStirrupPct
    val wPlaster = appSettings.wastePlasterPct

    // Defaults Seleccionados
    val defBrickId = appSettings.defaultBrickId
    val defConcGenId = appSettings.defaultConcreteGenId
    val defConcStrId = appSettings.defaultConcreteStrId
    val defPlasterId = appSettings.defaultPlasterId

    // --- PREPARACIÓN DE DATOS (State Hoisting) ---
    val brickOptions = remember(customBricks, hiddenBricks) {
        brickPresenter.getOptions(customBricks, hiddenBricks)
    }

    // Resolvemos strings para ConcretePresenter
    val resLabel = stringResource(Res.string.recipe_section_resistance)
    val resUnit = stringResource(Res.string.recipe_unit_kilogram_per_square_centimeters)
    val propLabel = stringResource(Res.string.concrete_result_proportion)
    val techLabel = stringResource(Res.string.concrete_result_technical, "")
    val unitKg = stringResource(Res.string.unit_kilograms)

    val concreteGenOptions = remember(customRecipes, hiddenRecipes) {
        concretePresenter.getOptions(
            customRecipes, hiddenRecipes,
            filterStructuralOnly = false,
            resLabel, resUnit, propLabel, techLabel, unitKg
        )
    }

    val concreteStrOptions = remember(customRecipes, hiddenRecipes) {
        concretePresenter.getOptions(
            customRecipes, hiddenRecipes,
            filterStructuralOnly = true,
            resLabel, resUnit, propLabel, techLabel, unitKg
        )
    }

    val labelCem = stringResource(Res.string.abbr_cement)
    val labelLime = stringResource(Res.string.abbr_lime)
    val labelSand = stringResource(Res.string.abbr_sand)
    val labelKg = stringResource(Res.string.unit_kilograms)
    val labelRatio = stringResource(Res.string.abbr_water_cement_ratio)
    val labelStdJaharro = stringResource(Res.string.plaster_type_std_jaharro)

    val plasterOptions = remember(customRecipes, hiddenRecipes) {
        mortarPresenter.getOptions(
            customRecipes,
            hiddenRecipes,
            filterType = "PLASTER",
            labelCem = labelCem,
            labelLime = labelLime,
            labelSand = labelSand,
            labelKg = labelKg,
            labelRatio = labelRatio,
            labelStdJaharro = labelStdJaharro
        )
    }

    // Estados para las selecciones (Objetos completos)
    var selectedBrick by remember { mutableStateOf<BrickOptionUi?>(null) }
    var selectedConcGen by remember { mutableStateOf<ConcreteOptionUi?>(null) }
    var selectedConcStr by remember { mutableStateOf<ConcreteOptionUi?>(null) }
    var selectedPlaster by remember { mutableStateOf<MortarOptionUi?>(null) }

    // Inicialización de selecciones
    LaunchedEffect(defBrickId, brickOptions) {
        if (selectedBrick == null) selectedBrick =
            brickOptions.find { it.id == defBrickId } ?: brickOptions.firstOrNull()
    }
    LaunchedEffect(defConcGenId, concreteGenOptions) {
        if (selectedConcGen == null) selectedConcGen =
            concreteGenOptions.find { it.id == defConcGenId } ?: concreteGenOptions.firstOrNull()
    }
    LaunchedEffect(defConcStrId, concreteStrOptions) {
        if (selectedConcStr == null) selectedConcStr =
            concreteStrOptions.find { it.id == defConcStrId } ?: concreteStrOptions.firstOrNull()
    }
    LaunchedEffect(defPlasterId, plasterOptions) {
        if (selectedPlaster == null) selectedPlaster =
            plasterOptions.find { it.id == defPlasterId } ?: plasterOptions.firstOrNull()
    }

    // Definimos los FocusRequesters necesarios
    val focusCement = remember { FocusRequester() }
    val focusLime = remember { FocusRequester() }
    val focusPremix = remember { FocusRequester() }
    val focusBucket = remember { FocusRequester() }
    val focusBarrow = remember { FocusRequester() }
    val focusMixer = remember { FocusRequester() }
    val focusFine = remember { FocusRequester() }
    val focusConcrete = remember { FocusRequester() }
    val focusMortar = remember { FocusRequester() }
    val focusBrick = remember { FocusRequester() }
    val focusPlaster = remember { FocusRequester() }
    val focusIron = remember { FocusRequester() }
    val focusStirrup = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SECCIÓN 1: MATERIALES PREDETERMINADOS ---
        SettingsAccordion(
            title = stringResource(Res.string.settings_params_default_title),
            defaultExpanded = true
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                BrickSelectorField(
                    options = brickOptions,
                    selectedOption = selectedBrick,
                    onOptionSelected = { opt ->
                        selectedBrick = opt
                        scope.launch { repository.saveDefaultBrick(opt.id) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                ConcreteSelectorField(
                    label = stringResource(Res.string.settings_params_concrete_gen_label),
                    options = concreteGenOptions,
                    selectedOption = selectedConcGen,
                    onOptionSelected = { opt ->
                        selectedConcGen = opt
                        scope.launch { repository.saveDefaultConcreteGen(opt.id) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                ConcreteSelectorField(
                    label = stringResource(Res.string.settings_params_concrete_str_label),
                    options = concreteStrOptions,
                    selectedOption = selectedConcStr,
                    onOptionSelected = { opt ->
                        selectedConcStr = opt
                        scope.launch { repository.saveDefaultConcreteStr(opt.id) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                MortarSelectorField(
                    label = stringResource(Res.string.settings_params_plaster_rough_label),
                    options = plasterOptions,
                    selectedOption = selectedPlaster,
                    onOptionSelected = { opt ->
                        selectedPlaster = opt
                        scope.launch { repository.saveDefaultPlasterRough(opt.id) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- SECCIÓN 2: PRESENTACIÓN (Pesos de bolsas) ---
        SettingsAccordion(title = stringResource(Res.string.settings_params_presentation_title)) {
            EditIntegerSetting(
                label = stringResource(Res.string.settings_params_cement_label),
                value = cementWeight,
                defaultValue = Defaults.DEFAULT_BAG_CEMENT,
                suffix = stringResource(Res.string.unit_kilograms),
                onSave = { scope.launch { repository.saveBagWeight("cement", it) } },
                focusRequester = focusCement,
                nextFocusRequester = focusLime
            )
            EditIntegerSetting(
                label = stringResource(Res.string.settings_params_lime_label),
                value = limeWeight,
                defaultValue = Defaults.DEFAULT_BAG_LIME,
                suffix = stringResource(Res.string.unit_kilograms),
                onSave = { scope.launch { repository.saveBagWeight("lime", it) } },
                focusRequester = focusLime,
                nextFocusRequester = focusPremix
            )
            EditIntegerSetting(
                label = stringResource(Res.string.settings_params_premix_label),
                value = premixWeight,
                defaultValue = Defaults.DEFAULT_BAG_PREMIX,
                suffix = stringResource(Res.string.unit_kilograms),
                onSave = { scope.launch { repository.saveBagWeight("premix", it) } },
                focusRequester = focusPremix,
                nextFocusRequester = focusBucket
            )
        }

        // --- SECCIÓN 3: EQUIVALENCIAS (Capacidades) ---
        SettingsAccordion(title = stringResource(Res.string.settings_params_equivalences_title)) {
            EditDoubleSetting(
                label = stringResource(Res.string.settings_params_bucket_label),
                value = bucketVol,
                defaultValue = Defaults.DEFAULT_BUCKET_VOL,
                suffix = stringResource(Res.string.unit_liters),
                onSave = { scope.launch { repository.saveVolumeCapacity("bucket", it) } },
                focusRequester = focusBucket,
                nextFocusRequester = focusBarrow
            )
            EditDoubleSetting(
                label = stringResource(Res.string.settings_params_barrow_label),
                value = barrowVol,
                defaultValue = Defaults.DEFAULT_BARROW_VOL,
                suffix = stringResource(Res.string.unit_liters),
                onSave = { scope.launch { repository.saveVolumeCapacity("barrow", it) } },
                focusRequester = focusBarrow,
                nextFocusRequester = focusMixer
            )
            EditDoubleSetting(
                label = stringResource(Res.string.settings_params_mixer_label),
                value = mixerVol,
                defaultValue = Defaults.DEFAULT_MIXER_VOL,
                suffix = stringResource(Res.string.unit_liters),
                onSave = { scope.launch { repository.saveVolumeCapacity("mixer", it) } },
                focusRequester = focusMixer,
                nextFocusRequester = focusFine
            )
        }

        // --- SECCIÓN 4: CONFIGURACIÓN TÉCNICA ---
        SettingsAccordion(title = stringResource(Res.string.settings_params_technical_title)) {
            EditDoubleSetting(
                label = stringResource(Res.string.settings_params_fine_thickness_label),
                value = fineThick,
                defaultValue = Defaults.DEFAULT_THICKNESS_FINE,
                suffix = stringResource(Res.string.unit_millimeters),
                onSave = { scope.launch { repository.saveFineThickness(it) } },
                focusRequester = focusFine,
                nextFocusRequester = focusConcrete
            )
        }

        // --- SECCIÓN 5: DESPERDICIOS ---
        SettingsAccordion(title = stringResource(Res.string.settings_params_waste_title)) {
            // El texto descriptivo lo ponemos dentro del acordeón al principio
            Text(
                text = stringResource(Res.string.settings_params_waste_desc),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            EditPercentSetting(
                label = stringResource(Res.string.settings_params_waste_concrete),
                value = wConcrete,
                defaultValue = Defaults.DEFAULT_WASTE_CONCRETE,
                onSave = { nuevoValor -> scope.launch { repository.saveWaste("concrete", nuevoValor) } },
                focusRequester = focusConcrete,
                nextFocusRequester = focusMortar
            )
            EditPercentSetting(
                label = stringResource(Res.string.settings_params_waste_mortar),
                value = wMortar,
                defaultValue = Defaults.DEFAULT_WASTE_MORTAR,
                onSave = { nuevoValor ->
                    scope.launch { repository.saveWaste("mortar", nuevoValor) }
                },
                focusRequester = focusMortar,
                nextFocusRequester = focusBrick
            )
            EditPercentSetting(
                label = stringResource(Res.string.settings_params_waste_bricks),
                value = wBrick,
                defaultValue = Defaults.DEFAULT_WASTE_BRICK,
                onSave = { nuevoValor ->
                    scope.launch { repository.saveWaste("bricks", nuevoValor) }
                },
                focusRequester = focusBrick,
                nextFocusRequester = focusPlaster
            )
            EditPercentSetting(
                label = stringResource(Res.string.settings_params_waste_plaster),
                value = wPlaster,
                defaultValue = Defaults.DEFAULT_WASTE_PLASTER,
                onSave = { nuevoValor ->
                    scope.launch { repository.saveWaste("plaster", nuevoValor) }
                },
                focusRequester = focusPlaster,
                nextFocusRequester = focusIron
            )
            EditPercentSetting(
                label = stringResource(Res.string.settings_params_waste_iron_main),
                value = wIronMain,
                defaultValue = Defaults.DEFAULT_WASTE_IRON_MAIN,
                onSave = { nuevoValor ->
                    scope.launch { repository.saveWaste("iron_main", nuevoValor) }
                },
                focusRequester = focusIron,
                nextFocusRequester = focusStirrup
            )
            EditPercentSetting(
                label = stringResource(Res.string.settings_params_waste_iron_stirrup),
                value = wIronStirrup,
                defaultValue = Defaults.DEFAULT_WASTE_IRON_STIRRUP,
                onSave = { nuevoValor ->
                    scope.launch { repository.saveWaste("iron_stirrup", nuevoValor) }
                },
                focusRequester = focusStirrup,
                onDone = {}
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
