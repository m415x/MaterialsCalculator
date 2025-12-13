package org.m415x.materialscalculator.ui.screen.settings.global

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import org.m415x.materialscalculator.data.repository.SettingsRepository
import org.m415x.materialscalculator.data.repository.SettingsRepository.Defaults
import org.m415x.materialscalculator.ui.screen.settings.EditDoubleSetting
import org.m415x.materialscalculator.ui.screen.settings.EditIntegerSetting
import org.m415x.materialscalculator.ui.screen.settings.EditPercentSetting

/**
 * Pantalla de parámetros globales.
 *
 * @param repository El repositorio de configuración.
 */
@Composable
fun GlobalParamsSubScreen(repository: SettingsRepository) {
    val scope = rememberCoroutineScope()

    // Lectura de valores (con valores por defecto mientras carga)
    val cementWeight by repository.bagCementKg.collectAsState(Defaults.DEFAULT_BAG_CEMENT)
    val limeWeight by repository.bagLimeKg.collectAsState(Defaults.DEFAULT_BAG_LIME)
    val premixWeight by repository.bagPremixKg.collectAsState(Defaults.DEFAULT_BAG_PREMIX)
    val bucketVol by repository.bucketCapacityLiters.collectAsState(Defaults.DEFAULT_BUCKET_VOL)
    val barrowVol by repository.barrowCapacityLiters.collectAsState(Defaults.DEFAULT_BARROW_VOL)
    val fineThick by repository.fineThicknessMm.collectAsState(Defaults.DEFAULT_THICKNESS_FINE)
    val wConcrete by repository.wasteConcretePct.collectAsState(Defaults.DEFAULT_WASTE_CONCRETE)
    val wMortar by repository.wasteMortarPct.collectAsState(Defaults.DEFAULT_WASTE_MORTAR)
    val wBrick by repository.wasteBricksPct.collectAsState(Defaults.DEFAULT_WASTE_BRICK)
    val wIronMain by repository.wasteIronMainPct.collectAsState(Defaults.DEFAULT_WASTE_IRON_MAIN)
    val wIronStirrup by repository.wasteIronStirrupPct.collectAsState(Defaults.DEFAULT_WASTE_IRON_STIRRUP)
    val wPlaster by repository.wastePlasterPct.collectAsState(Defaults.DEFAULT_WASTE_PLASTER)

    // Definimos los FocusRequesters necesarios
    val focusCemento = remember { FocusRequester() }
    val focusCal = remember { FocusRequester() }
    val focusPremezclado = remember { FocusRequester() }
    val focusBalde = remember { FocusRequester() }
    val focusCarretilla = remember { FocusRequester() }
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
        Text("Presentación de Materiales", style = MaterialTheme.typography.titleMedium)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(modifier = Modifier.padding(8.dp)) {
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

//        HorizontalDivider()

        Text("Equivalencias de Obra", style = MaterialTheme.typography.titleMedium)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(modifier = Modifier.padding(8.dp)) {
                EditDoubleSetting(
                    label = "Capacidad del Balde",
                    value = bucketVol,
                    defaultValue = Defaults.DEFAULT_BUCKET_VOL,
                    suffix = "Lt",
                    onSave = { scope.launch { repository.saveVolumeCapacity("bucket", it) } },
                    focusRequester = focusBalde,
                    nextFocusRequester = focusCarretilla
                )
                EditDoubleSetting(
                    label = "Capacidad de Carretilla",
                    value = barrowVol,
                    defaultValue = Defaults.DEFAULT_BARROW_VOL,
                    suffix = "Lt",
                    onSave = { scope.launch { repository.saveVolumeCapacity("barrow", it) } },
                    focusRequester = focusCarretilla,
                    nextFocusRequester = focusFino
                )

            }
        }
//        HorizontalDivider()

        Text("Configuración Técnica", style = MaterialTheme.typography.titleMedium)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(modifier = Modifier.padding(8.dp)) {
                EditDoubleSetting(
                    label = "Espesor Revoque Fino",
                    value = fineThick,
                    defaultValue = Defaults.DEFAULT_THICKNESS_FINE,
                    suffix = "mm",
                    onSave = { scope.launch { repository.saveFineThickness(it) } },
                    focusRequester = focusFino,
                    nextFocusRequester = focusHormigon
                )
            }
        }

//        HorizontalDivider()

        Text("Desperdicios / Márgenes (%)", style = MaterialTheme.typography.titleMedium)
        Text(
            "Porcentaje extra que se sumará al cálculo para cubrir roturas y pérdidas.",
            style = MaterialTheme.typography.labelSmall
        )

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(modifier = Modifier.padding(8.dp)) {
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