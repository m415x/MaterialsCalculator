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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.data.repository.SettingsRepository.Defaults
import org.m415x.materialcalc.domain.model.AppSettingsState
import org.m415x.materialcalc.ui.common.BrickSelectorField
import org.m415x.materialcalc.ui.common.ConcreteSelectorField
import org.m415x.materialcalc.ui.common.MortarSelectorField
import org.m415x.materialcalc.ui.screen.settings.EditDoubleSetting
import org.m415x.materialcalc.ui.screen.settings.EditIntegerSetting
import org.m415x.materialcalc.ui.screen.settings.EditPercentSetting

/**
 * Pantalla de parámetros globales.
 *
 * @param repository El repositorio de configuración.
 * @param appSettings El estado global de la configuración.
 */
@Composable
fun GlobalParamsSubScreen(repository: SettingsRepository, appSettings: AppSettingsState) {
    val scope = rememberCoroutineScope()

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

    // Definimos los FocusRequesters necesarios
    val focusCemento = remember { FocusRequester() }
    val focusCal = remember { FocusRequester() }
    val focusPremezclado = remember { FocusRequester() }
    val focusBalde = remember { FocusRequester() }
    val focusCarretilla = remember { FocusRequester() }
    val focusMixer = remember { FocusRequester() }
    val focusFino = remember { FocusRequester() }
    val focusHormigon = remember { FocusRequester() }
    val focusMortero = remember { FocusRequester() }
    val focusLadrillo = remember { FocusRequester() }
    val focusRevoque = remember { FocusRequester() }
    val focusHierro = remember { FocusRequester() }
    val focusEstribo = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SECCIÓN 1: MATERIALES PREDETERMINADOS ---
        Text(
            "Valores Predeterminados",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Selector Ladrillo
                BrickSelectorField(
                    selectedBrickId = defBrickId,
                    onBrickSelected = { opt -> scope.launch { repository.saveDefaultBrick(opt.id) } },
                    customBricks = customBricks,
                    hiddenIds = hiddenBricks,
                    modifier = Modifier.fillMaxWidth()
                )

                ConcreteSelectorField(
                    selectedRecipeId = defConcGenId,
                    onRecipeSelected = { id, _ -> scope.launch { repository.saveDefaultConcreteGen(id) } },
                    customRecipes = customRecipes,
                    hiddenIds = hiddenRecipes,
                    modifier = Modifier.fillMaxWidth(),
                    label = "Hormigón General (Pisos/Losas)"
                )

                ConcreteSelectorField(
                    selectedRecipeId = defConcStrId,
                    onRecipeSelected = { id, _ -> scope.launch { repository.saveDefaultConcreteStr(id) } },
                    customRecipes = customRecipes,
                    hiddenIds = hiddenRecipes,
                    modifier = Modifier.fillMaxWidth(),
                    filterStructuralOnly = true,
                    label = "Hormigón Estructural (Vigas/Columnas)"
                )

                MortarSelectorField(
                    selectedRecipeId = defPlasterId,
                    onRecipeSelected = { id, _ -> scope.launch { repository.saveDefaultPlasterRough(id) } },
                    customRecipes = customRecipes,
                    hiddenIds = hiddenRecipes,
                    modifier = Modifier.fillMaxWidth(),
                    filterType = "PLASTER",
                    label = "Mezcla Revoque Grueso"
                )
// TODO
//                AppDropdown(
//                    label = "Hierro Principal",
//                    selectedText = ,
//                    options = ,
//                    onSelect = { opt -> scope.launch {  } }
//                ) { option -> }
//                AppDropdown(
//                    label = "Hierro Estribos",
//                    selectedText = ,
//                    options = ,
//                    onSelect = { opt -> scope.launch {  } }
//                ) { option -> }
//                AppDropdown(
//                    label = "Hierro Malla",
//                    selectedText = ,
//                    options = ,
//                    onSelect = { opt -> scope.launch {  } }
//                ) { option -> }
            }
        }

        Text("Presentación de Materiales", style = MaterialTheme.typography.titleMedium)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                EditIntegerSetting(
                    label = "Cemento",
                    value = cementWeight,
                    defaultValue = Defaults.DEFAULT_BAG_CEMENT,
                    suffix = "kg",
                    onSave = { scope.launch { repository.saveBagWeight("cement", it) } },
                    focusRequester = focusCemento,
                    nextFocusRequester = focusCal
                )

                EditIntegerSetting(
                    label = "Cal",
                    value = limeWeight,
                    defaultValue = Defaults.DEFAULT_BAG_LIME,
                    suffix = "kg",
                    onSave = { scope.launch { repository.saveBagWeight("lime", it) } },
                    focusRequester = focusCal,
                    nextFocusRequester = focusPremezclado
                )

                EditIntegerSetting(
                    label = "Premezclado Fino",
                    value = premixWeight,
                    defaultValue = Defaults.DEFAULT_BAG_PREMIX,
                    suffix = "kg",
                    onSave = { scope.launch { repository.saveBagWeight("premix", it) } },
                    focusRequester = focusPremezclado,
                    nextFocusRequester = focusBalde
                )
            }
        }

        Text("Equivalencias de Obra", style = MaterialTheme.typography.titleMedium)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                EditDoubleSetting(
                    label = "Balde",
                    value = bucketVol,
                    defaultValue = Defaults.DEFAULT_BUCKET_VOL,
                    suffix = "Lt",
                    onSave = { scope.launch { repository.saveVolumeCapacity("bucket", it) } },
                    focusRequester = focusBalde,
                    nextFocusRequester = focusCarretilla
                )
                EditDoubleSetting(
                    label = "Carretilla",
                    value = barrowVol,
                    defaultValue = Defaults.DEFAULT_BARROW_VOL,
                    suffix = "Lt",
                    onSave = { scope.launch { repository.saveVolumeCapacity("barrow", it) } },
                    focusRequester = focusCarretilla,
                    nextFocusRequester = focusMixer
                )
                EditDoubleSetting(
                    label = "Hormigonera",
                    value = mixerVol,
                    defaultValue = Defaults.DEFAULT_MIXER_VOL,
                    suffix = "Lt",
                    onSave = { scope.launch { repository.saveVolumeCapacity("mixer", it) } },
                    focusRequester = focusMixer,
                    nextFocusRequester = focusFino
                )
            }
        }

        Text("Configuración Técnica", style = MaterialTheme.typography.titleMedium)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                EditDoubleSetting(
                    label = "Espesor Revoque Fino",
                    value = fineThick,
                    defaultValue = Defaults.DEFAULT_THICKNESS_FINE,
                    suffix = "mm",
                    onSave = { scope.launch { repository.saveFineThickness(it) } },
                    focusRequester = focusFino,
                    nextFocusRequester = focusHormigon
                )
// TODO
//                EditDoubleSetting(
//                    label = "Separación máxima entre columnas",
//                    value = ,
//                    defaultValue = ,
//                    suffix = ,
//                    onSave = { scope.launch {  } },
//                    focusRequester = ,
//                    nextFocusRequester =
//                )
            }
        }

//        HorizontalDivider()

        Text("Desperdicios / Márgenes (%)", style = MaterialTheme.typography.titleMedium)
        Text(
            "Porcentaje extra que se sumará al cálculo para cubrir roturas y pérdidas.",
            style = MaterialTheme.typography.labelSmall
        )

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                EditPercentSetting(
                    label = "Hormigón",
                    value = wConcrete,
                    defaultValue = Defaults.DEFAULT_WASTE_CONCRETE,
                    onSave = { nuevoValor ->
                        scope.launch { repository.saveWaste("concrete", nuevoValor) }
                    },
                    focusRequester = focusHormigon,
                    nextFocusRequester = focusMortero
                )
                EditPercentSetting(
                    label = "Mortero",
                    value = wMortar,
                    defaultValue = Defaults.DEFAULT_WASTE_MORTAR,
                    onSave = { nuevoValor ->
                        scope.launch { repository.saveWaste("mortar", nuevoValor) }
                    },
                    focusRequester = focusMortero,
                    nextFocusRequester = focusLadrillo
                )
                EditPercentSetting(
                    label = "Ladrillos",
                    value = wBrick,
                    defaultValue = Defaults.DEFAULT_WASTE_BRICK,
                    onSave = { nuevoValor ->
                        scope.launch { repository.saveWaste("bricks", nuevoValor) }
                    },
                    focusRequester = focusLadrillo,
                    nextFocusRequester = focusRevoque
                )
                EditPercentSetting(
                    label = "Revoques",
                    value = wPlaster,
                    defaultValue = Defaults.DEFAULT_WASTE_PLASTER,
                    onSave = { nuevoValor ->
                        scope.launch { repository.saveWaste("plaster", nuevoValor) }
                    },
                    focusRequester = focusRevoque,
                    nextFocusRequester = focusHierro
                )
                EditPercentSetting(
                    label = "Hierro Principal",
                    value = wIronMain,
                    defaultValue = Defaults.DEFAULT_WASTE_IRON_MAIN,
                    onSave = { nuevoValor ->
                        scope.launch { repository.saveWaste("iron_main", nuevoValor) }
                    },
                    focusRequester = focusHierro,
                    nextFocusRequester = focusEstribo
                )
                EditPercentSetting(
                    label = "Estribos",
                    value = wIronStirrup,
                    defaultValue = Defaults.DEFAULT_WASTE_IRON_STIRRUP,
                    onSave = { nuevoValor ->
                        scope.launch { repository.saveWaste("iron_stirrup", nuevoValor) }
                    },
                    focusRequester = focusEstribo,
                    onDone = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
