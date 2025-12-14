package org.m415x.materialscalculator.ui.screen.settings.db

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.launch

import org.m415x.materialscalculator.data.repository.SettingsRepository
import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.model.CustomRecipe
import org.m415x.materialscalculator.domain.model.TipoHormigon
import org.m415x.materialscalculator.ui.common.AppInput
import org.m415x.materialscalculator.ui.common.NumericInput
import org.m415x.materialscalculator.ui.common.roundToDecimals
import org.m415x.materialscalculator.ui.common.toSafeDoubleOrNull

@Composable
fun RecipesTabContent(repository: SettingsRepository) {
    val scope = rememberCoroutineScope()
    val customRecipes by repository.customRecipes.collectAsState(initial = emptyList())
    val hiddenIds by repository.hiddenRecipeIds.collectAsState(initial = emptySet())
    val staticRepo = remember { StaticMaterialRepository() }

    // 1. Fusionar Listas en MaterialUiModel
    val uiList = remember(customRecipes, hiddenIds) {
        val list = mutableListOf<MaterialUiModel>()

        // Custom
        list.addAll(customRecipes.map {
            MaterialUiModel(
                id = it.id,
                title = it.nombre,
                subtitle = "${it.cementoKg}kg Cem | A/C: ${it.relacionAgua}",
                isCustom = true,
                originalData = it
            )
        })

        // Static (Hormigones)
        TipoHormigon.entries.forEach { type ->
            if (type.name !in hiddenIds) {
                val r = staticRepo.getDosificacionHormigon(type)!!
                list.add(MaterialUiModel(
                    id = type.name,
                    title = type.name, // Ej: H21
                    subtitle = r.proporcionMezcla,
                    isCustom = false,
                    // Creamos un CustomRecipe temporal para facilitar la copia en el editor
                    originalData = CustomRecipe(
                        id = "",
                        nombre = type.name,
                        cementoKg = r.cementoKg,
                        arenaM3 = r.arenaM3,
                        piedraM3 = r.piedraM3,
                        calKg = 0.0,
                        relacionAgua = r.relacionAgua,
                        tipo = "CONCRETE"
                    )
                ))
            }
        }
        // TODO: Aquí podrías agregar también los morteros estáticos si los tuvieras en un Enum

        list.sortedBy { it.title }
    }

    var showEditor by remember { mutableStateOf(false) }
    var recipeToEdit by remember { mutableStateOf<CustomRecipe?>(null) }

    var itemToDelete by remember { mutableStateOf<MaterialUiModel?>(null) }
    var showRestore by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    recipeToEdit = null
                    showEditor = true
                },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nueva") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // Botón de Restaurar (Solo si hay ocultos)
            if (hiddenIds.isNotEmpty()) {
                TextButton(
                    onClick = { showRestore = true },
                    modifier = Modifier.padding(vertical = 4.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Restore, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Restaurar materiales de fábrica (${hiddenIds.size})")
                }
            } else {
                Text(
                    "Gestiona las mezclas disponibles en la calculadora.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Lista Principal
            if (uiList.isEmpty()) {
                Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No hay materiales disponibles.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiList) { item ->
                        UniversalMaterialItem(
                            item = item,
                            onEdit = {
                                // Recuperamos el objeto original (Custom o convertido de Static)
                                val original = item.originalData as? CustomRecipe
                                // Si es de fábrica, forzamos ID nuevo
                                recipeToEdit = original?.copy(id = if (item.isCustom) original.id else "")
                                showEditor = true
                            },
                            onDelete = { itemToDelete = item }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Diálogos
    if (showEditor) {
        RecipeEditorDialog(
            recipeToEdit = recipeToEdit,
            onDismiss = { showEditor = false },
            onSave = {
                scope.launch { repository.saveCustomRecipe(it); showEditor = false }
            }
        )
    }

    if (itemToDelete != null) {
        DeleteOrHideDialog(
            item = itemToDelete!!,
            onDismiss = { itemToDelete = null },
            onConfirm = {
                scope.launch {
                    if (itemToDelete!!.isCustom) repository.deleteCustomRecipe(itemToDelete!!.id)
                    else repository.hideStaticRecipe(itemToDelete!!.id)
                    itemToDelete = null
                }
            }
        )
    }

    // --- DIALOGO RESTAURAR (Muestra lista de ocultos) ---
    if (showRestore) {
        RestoreRecipesDialog(
            hiddenIds = hiddenIds,
            onRestore = { id -> scope.launch { repository.restoreStaticRecipe(id) } },
            onDismiss = { showRestore = false }
        )
    }
}

@Composable
fun RestoreRecipesDialog(
    hiddenIds: Set<String>,
    onRestore: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restaurar Mezcla") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp) // Limitar altura
            ) {
                items(hiddenIds.toList()) { id ->
                    // Buscamos el nombre legible usando el Enum
                    val nombre = try {
                        TipoHormigon.valueOf(id).name } catch (e: Exception) { id }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRestore(id) } // Al clickear se restaura
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(nombre, style = MaterialTheme.typography.bodyLarge)
                        Icon(Icons.Default.Restore, "Restaurar", tint = MaterialTheme.colorScheme.primary)
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun RecipeEditorDialog(
    recipeToEdit: CustomRecipe?,
    onDismiss: () -> Unit,
    onSave: (CustomRecipe) -> Unit
) {
    // ESTADOS GENERALES
    var name by remember { mutableStateOf(recipeToEdit?.nombre ?: "") }
    var selectedType by remember { mutableStateOf(recipeToEdit?.tipo ?: "CONCRETE") }
    var usos by remember { mutableStateOf(recipeToEdit?.usos ?: "") }
    var isEstructural by remember { mutableStateOf(recipeToEdit?.isEstructural ?: false) }

    // MODO DE ENTRADA: TRUE = Por Partes (1:3:3), FALSE = Manual (300kg...)
    var isProportionMode by remember { mutableStateOf(false) }

    // Definimos los FocusRequesters necesarios
    val focusNombreMezcla = remember { FocusRequester() }
    val focusCementoKg = remember { FocusRequester() }
    val focusCalKg = remember { FocusRequester() }
    val focusArenaKg = remember { FocusRequester() }
    val focusPiedraKg = remember { FocusRequester() }
    val focusRelacioAgua = remember { FocusRequester() }
    val focusCementoParte = remember { FocusRequester() }
    val focusCalParte = remember { FocusRequester() }
    val focusArenaParte = remember { FocusRequester() }
    val focusPiedraParte = remember { FocusRequester() }
    val focusUsos = remember { FocusRequester() }

    // ESTADOS MANUALES (Kg/m3) - Lo que se guarda finalmente
    var cemento by remember { mutableStateOf(recipeToEdit?.cementoKg?.toString() ?: "") }
    var cal by remember { mutableStateOf(recipeToEdit?.calKg?.toString() ?: "") }
    var arena by remember { mutableStateOf(recipeToEdit?.arenaM3?.toString() ?: "") }
    var piedra by remember { mutableStateOf(recipeToEdit?.piedraM3?.toString() ?: "") }
    var agua by remember { mutableStateOf(recipeToEdit?.relacionAgua?.toString() ?: "0.5") }

    // ESTADOS POR PARTES (1, 3, 3...)
    var pCemento by remember { mutableStateOf("1") }
    var pCal by remember { mutableStateOf("0") }
    var pArena by remember { mutableStateOf("3") }
    var pPiedra by remember { mutableStateOf("3") }
    var pAgua by remember { mutableStateOf("0.5") } // Media parte de agua

    // LÓGICA DE CONVERSIÓN
    fun calcularYActualizarManual() {
        val res = CalculadoraMezcla.calcularPorPartes(
            partesCemento = pCemento.toSafeDoubleOrNull() ?: 0.0,
            partesCal = pCal.toSafeDoubleOrNull() ?: 0.0,
            partesArena = pArena.toSafeDoubleOrNull() ?: 0.0,
            partesPiedra = if (selectedType == "CONCRETE") pPiedra.toSafeDoubleOrNull() ?: 0.0 else 0.0,
            partesAgua = pAgua.toSafeDoubleOrNull() ?: 0.0
        )
        // Actualizamos los campos manuales (que son los que se guardan)
        cemento = res.cementoKg.toInt().toString() // Redondeamos cemento a entero
        cal = res.calKg.toInt().toString()
        arena = res.arenaM3.roundToDecimals(3).replace(',', '.')
        piedra = res.piedraM3.roundToDecimals(3).replace(',', '.')
        // agua = ... (La relación A/C se calcula sola o se deja)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(if (recipeToEdit?.id.isNullOrBlank()) "Nueva Mezcla" else "Editar Mezcla", style = MaterialTheme.typography.headlineSmall)

                Text("Tipo de Mezcla", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // 1. Agregamos el estado de scroll
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == RecipeType.CONCRETE,
                        onClick = { selectedType = RecipeType.CONCRETE },
                        label = { Text("Hormigón") }
                    )
                    FilterChip(
                        selected = selectedType == RecipeType.MORTAR,
                        onClick = { selectedType = RecipeType.MORTAR },
                        label = { Text("Mortero") }
                    )
                    FilterChip(
                        selected = selectedType == RecipeType.PLASTER,
                        onClick = { selectedType = RecipeType.PLASTER },
                        label = { Text("Revoque") }
                    )
                }

                AppInput(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre",
                    focusRequester = focusNombreMezcla,
                    nextFocusRequester = if (!isProportionMode) focusCementoKg else focusCementoParte,
                )

                // --- CHECKBOX ESTRUCTURAL (Solo Hormigón) ---
                if (selectedType == RecipeType.CONCRETE) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isEstructural = !isEstructural }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isEstructural,
                            onCheckedChange = { isEstructural = it }
                        )
                        Text(
                            text = "Es Apto Estructura (Portante)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                HorizontalDivider()

                // SWITCH DE MODO
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Modo de Ingreso", style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = isProportionMode,
                            onCheckedChange = { isProportionMode = it }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isProportionMode) "Proporción (Baldes)" else "Técnico (kg/m³)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (isProportionMode) {
                    // --- MODO PROPORCIÓN (1 : 3 : 3) ---
                    Text(
                        "Ingrese partes (ej: baldes, paladas)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumericInput(
                            pCemento,
                            { pCemento = it },
                            "Cemento",
                            modifier = Modifier.weight(1f),
                            focusRequester = focusCementoParte,
                            nextFocusRequester = if (selectedType != "CONCRETE") focusCalParte else focusArenaParte,
                        )
                        if (selectedType != "CONCRETE") {
                            NumericInput(
                                pCal,
                                { pCal = it },
                                "Cal",
                                modifier = Modifier.weight(1f),
                                focusRequester = focusCalParte,
                                nextFocusRequester = focusArenaParte
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumericInput(
                            pArena,
                            { pArena = it },
                            "Arena",
                            modifier = Modifier.weight(1f),
                            focusRequester = focusArenaParte,
                            nextFocusRequester = if (selectedType == "CONCRETE") focusPiedraParte else null,
                        )
                        if (selectedType == "CONCRETE") {
                            NumericInput(
                                pPiedra,
                                { pPiedra = it },
                                "Piedra",
                                modifier = Modifier.weight(1f),
                                focusRequester = focusPiedraParte,
                                onDone = {}
                            )
                        }
                    }

                    // Botón de cálculo mágico
                    Button(
                        onClick = {
                            calcularYActualizarManual()
                            isProportionMode = false // Volvemos al modo manual para ver el resultado
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.Science, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Calcular y Aplicar")
                    }

                } else {

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumericInput(
                            cemento,
                            { cemento = it },
                            "Cemento",
                            suffix = { Text("kg") },
                            modifier = Modifier.weight(1f),
                            focusRequester = focusCementoKg,
                            nextFocusRequester = if (selectedType != RecipeType.CONCRETE) focusCalKg else focusPiedraKg,
                        )
                        // La cal solo suele usarse en Morteros y Revoques
                        if (selectedType != RecipeType.CONCRETE) {
                            NumericInput(
                                cal,
                                { cal = it },
                                "Cal",
                                suffix = { Text("kg") },
                                modifier = Modifier.weight(1f),
                                focusRequester = focusCalKg,
                                nextFocusRequester = focusArenaKg
                            )
                        } else {
                            NumericInput(
                                piedra,
                                { piedra = it },
                                "Piedra",
                                suffix = { Text("m³") },
                                modifier = Modifier.weight(1f),
                                focusRequester = focusPiedraKg,
                                nextFocusRequester = focusArenaKg
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumericInput(
                            arena,
                            { arena = it },
                            "Arena",
                            suffix = { Text("m³") },
                            modifier = Modifier.weight(1f),
                            focusRequester = focusArenaKg,
                            nextFocusRequester = if (selectedType == RecipeType.CONCRETE) focusRelacioAgua else focusUsos
                        )

                        // LA PIEDRA SOLO EN HORMIGÓN
                        if (selectedType == RecipeType.CONCRETE) {
                            NumericInput(
                                agua,
                                { agua = it },
                                "Agua (A/C)",
                                modifier = Modifier.weight(1f),
                                focusRequester = focusRelacioAgua,
                                nextFocusRequester = focusUsos

                            )
                        } else {
                            // Relleno visual para mantener alineación o dejamos vacío
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppInput(
                            value = usos,
                            onValueChange = { usos = it },
                            label = "Usos / Descripción",
                            placeholder = "Ej: Para vigas, columnas y losas",
                            focusRequester = focusUsos,
                            onDone = {}
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Cancelar") }
                        Button(
                            enabled = name.isNotBlank() && cemento.isNotBlank(),
                            onClick = {

                                // Verificamos si es nulo O ESTÁ VACÍO.
                                val finalId = if (recipeToEdit?.id.isNullOrBlank()) {
                                    Uuid.random().toString() // Generar ID nuevo si es copia o nuevo
                                } else {
                                    recipeToEdit.id // Mantener ID si es edición de uno existente
                                }

                                onSave(
                                    CustomRecipe(
                                        id = finalId,
                                        nombre = name,
                                        cementoKg = cemento.toSafeDoubleOrNull() ?: 0.0,
                                        calKg = cal.toSafeDoubleOrNull() ?: 0.0,
                                        arenaM3 = arena.toSafeDoubleOrNull() ?: 0.0,
                                        piedraM3 = if (selectedType == RecipeType.CONCRETE) piedra.toSafeDoubleOrNull()
                                            ?: 0.0 else 0.0,
                                        relacionAgua = agua.toSafeDoubleOrNull() ?: 0.5,
                                        tipo = selectedType,
                                        usos = usos,
                                        isEstructural = if (selectedType == RecipeType.CONCRETE) isEstructural else false
                                    )
                                )
                            }
                        ) { Text("Guardar") }
                    }
                }
            }
        }
    }
}

object RecipeType {
    const val CONCRETE = "CONCRETE"
    const val MORTAR = "MORTAR"
    const val PLASTER = "PLASTER" // Revoque
}

// Helper object para calcular proporciones
object CalculadoraMezcla {
    // Densidades aproximadas (kg/m3)
    const val DENSIDAD_CEMENTO = 1400.0

    // Coeficientes de Aporte (Volumen Real / Volumen Aparente)
    // Fuente: Chandias / Manuales de Construcción
    const val COEF_CEMENTO = 0.47
    const val COEF_CAL = 0.37 // Polvo
    const val COEF_ARENA = 0.63
    const val COEF_PIEDRA = 0.51
    const val COEF_AGUA = 1.0

    data class ResultadoProporcion(
        val cementoKg: Double,
        val calKg: Double,
        val arenaM3: Double,
        val piedraM3: Double,
        val aguaLitros: Double
    )

    fun calcularPorPartes(
        partesCemento: Double,
        partesCal: Double,
        partesArena: Double,
        partesPiedra: Double,
        partesAgua: Double // Generalmente es un porcentaje del cemento, pero si lo ponen por partes...
    ): ResultadoProporcion {
        // 1. Calcular el Volumen Real que genera esa suma de partes (ej: 1 balde + 3 baldes...)
        val volumenReal = (partesCemento * COEF_CEMENTO) +
                (partesCal * COEF_CAL) +
                (partesArena * COEF_ARENA) +
                (partesPiedra * COEF_PIEDRA) +
                (partesAgua * COEF_AGUA)

        if (volumenReal == 0.0) return ResultadoProporcion(0.0, 0.0, 0.0, 0.0, 0.0)

        // 2. Factor de Multiplicación para llegar a 1 m3 (1000 litros)
        // Cuántas veces entra esa "mezclita" en 1 metro cúbico real
        val factor = 1000.0 / volumenReal // litros

        // 3. Calcular cantidades por m3
        // Cemento: (Partes * Factor) nos da LITROS aparentes de cemento. Multiplicamos por densidad para KG.
        val cementoKg = (partesCemento * factor) * (DENSIDAD_CEMENTO / 1000.0)

        // Cal: Similar, asumimos densidad aprox 600kg/m3 si quisiéramos kg, pero simplifiquemos
        // Si la cal viene en bolsa de 25kg, y densidad ~500-600.
        val calKg = (partesCal * factor) * 0.6 // Aprox 600kg/m3 densidad aparente

        // Arena y Piedra: Queremos m3 aparentes (volumen de compra)
        val arenaM3 = (partesArena * factor) / 1000.0
        val piedraM3 = (partesPiedra * factor) / 1000.0

        // Agua: Relación A/C estimada
        val aguaLitros = (partesAgua * factor)

        return ResultadoProporcion(cementoKg, calKg, arenaM3, piedraM3, aguaLitros / cementoKg)
    }
}