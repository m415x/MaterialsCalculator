package org.m415x.materialscalculator.ui.screen.settings.global

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import org.m415x.materialscalculator.data.repository.SettingsRepository
import org.m415x.materialscalculator.data.repository.SettingsRepository.Defaults
import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.model.TipoHormigon
import org.m415x.materialscalculator.domain.model.TipoLadrillo
import org.m415x.materialscalculator.ui.common.AppDropdown
import org.m415x.materialscalculator.ui.screen.settings.EditDoubleSetting
import org.m415x.materialscalculator.ui.screen.settings.EditIntegerSetting
import org.m415x.materialscalculator.ui.screen.settings.EditPercentSetting

// --- MODELOS VISUALES ---
private data class BrickDisplayOption(
    val id: String,
    val name: String,
    val details: String,     // Ej: "18x18x33 cm"
    val isPortante: Boolean,
    val isCustom: Boolean
) {
    // Sobrescribimos toString para que AppDropdown muestre el nombre simple cuando está cerrado
    override fun toString(): String = name
}

private data class ConcreteDisplayOption(
    val id: String,
    val name: String,
    val proportions: String, // Ej: "1:3:3" o "300kg Cem..."
    val isCustom: Boolean
) {
    override fun toString(): String = name
}

/**
 * Pantalla de parámetros globales.
 *
 * @param repository El repositorio de configuración.
 */
@Composable
fun GlobalParamsSubScreen(repository: SettingsRepository) {
    val scope = rememberCoroutineScope()
    val staticRepo = remember { StaticMaterialRepository() }

    // --- DATOS PARA LAS LISTAS (Fusión) ---
    val customBricks by repository.customBricks.collectAsState(initial = emptyList())
    val customRecipes by repository.customRecipes.collectAsState(initial = emptyList())
    val hiddenBricks by repository.hiddenBrickIds.collectAsState(initial = emptySet())
    val hiddenRecipes by repository.hiddenRecipeIds.collectAsState(initial = emptySet())

    // --- LADRILLOS ---
    val brickOptions = remember(customBricks, hiddenBricks) {
        val list = mutableListOf<BrickDisplayOption>()

        // A. Fábrica
        TipoLadrillo.entries.filter { it.name !in hiddenBricks }.forEach { t ->
            val p = staticRepo.getPropiedadesLadrillo(t)!!
            val medidas =
                "${(p.anchoMuro * 100).toInt()}x${(p.altoUnidad * 100).toInt()}x${(p.largoUnidad * 100).toInt()} cm"
            list.add(BrickDisplayOption(t.name, t.nombre, medidas, t.isPortante, false))
        }

        // B. Custom
        customBricks.forEach { c ->
            val medidas = "${(c.ancho * 100).toInt()}x${(c.alto * 100).toInt()}x${(c.largo * 100).toInt()} cm"
            // Agregamos "(C)" al nombre para distinguir copias si tienen el mismo nombre
            list.add(BrickDisplayOption(c.id, "${c.nombre} (C)", medidas, c.isPortante, true))
        }
        list.sortedBy { it.name }
    }

    // --- HORMIGONES ---
    val concreteOptions = remember(customRecipes, hiddenRecipes) {
        val list = mutableListOf<ConcreteDisplayOption>()

        // A. Fábrica
        TipoHormigon.entries.filter { it.name !in hiddenRecipes }.forEach { t ->
            val d = staticRepo.getDosificacionHormigon(t)!!
            // Usamos la descripción oficial (Ej: 1:3:3)
            list.add(ConcreteDisplayOption(t.name, t.name, d.proporcionMezcla, false))
        }

        // B. Custom (Solo Hormigones)
        customRecipes.filter { it.tipo == "CONCRETE" }.forEach { c ->
            // Para custom, armamos un resumen de la receta
            val desc = "${c.cementoKg.toInt()}kg Cem | A/C: ${c.relacionAgua}"
            list.add(ConcreteDisplayOption(c.id, "${c.nombre} (C)", desc, true))
        }
        list.sortedBy { it.name }
    }

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

    // Defaults Seleccionados
    val defBrickId by repository.defaultBrickId.collectAsState("")
    val defConcGenId by repository.defaultConcreteGenId.collectAsState("")
    val defConcStrId by repository.defaultConcreteStrId.collectAsState("")

    // Helpers para encontrar el objeto seleccionado completo
    val selectedBrick = brickOptions.find { it.id == defBrickId }
    val selectedConcGen = concreteOptions.find { it.id == defConcGenId }
    val selectedConcStr = concreteOptions.find { it.id == defConcStrId }

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
        // --- SECCIÓN 1: MATERIALES PREDETERMINADOS ---
        Text(
            "Valores Predeterminados",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Selector Ladrillo
                AppDropdown(
                    label = "Ladrillo para Muros",
                    selectedText = selectedBrick?.name ?: "Seleccionar...",
                    options = brickOptions,
                    onSelect = { opt -> scope.launch { repository.saveDefaultBrick(opt.id) } }
                ) { option ->
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(option.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            if (option.isPortante) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Text(
                                        "PORTANTE",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                        Text(
                            option.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Selector Hormigón General
                AppDropdown(
                    label = "Hormigón (Pisos/Losa)",
                    selectedText = selectedConcGen?.name ?: "Seleccionar...",
                    options = concreteOptions,
                    onSelect = { opt -> scope.launch { repository.saveDefaultConcreteGen(opt.id) } }
                ) { option ->
                    ConcreteItemRow(option)
                }

                // Selector Hormigón Estructura
                AppDropdown(
                    label = "Hormigón (Vigas/Col)",
                    selectedText = selectedConcStr?.name ?: "Seleccionar...",
                    options = concreteOptions,
                    onSelect = { opt -> scope.launch { repository.saveDefaultConcreteStr(opt.id) } }
                ) { option ->
                    ConcreteItemRow(option)
                }
            }
        }

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

        Text("Equivalencias de Obra", style = MaterialTheme.typography.titleMedium)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(modifier = Modifier.padding(8.dp)) {
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
                    nextFocusRequester = focusFino
                )

            }
        }

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

@Composable
private fun ConcreteItemRow(option: ConcreteDisplayOption) {
    Column {
        Text(option.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                // Puedes usar un icono de mezcla o puntos
                imageVector = androidx.compose.material.icons.Icons.Default.Science,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.width(4.dp))
            Text(
                option.proportions,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}