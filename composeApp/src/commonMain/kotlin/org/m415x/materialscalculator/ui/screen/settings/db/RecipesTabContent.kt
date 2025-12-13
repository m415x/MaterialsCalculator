package org.m415x.materialscalculator.ui.screen.settings.db

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.launch

import org.m415x.materialscalculator.data.repository.SettingsRepository
import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.model.CustomRecipe
import org.m415x.materialscalculator.domain.model.DiametroHierro
import org.m415x.materialscalculator.domain.model.DosificacionHormigon
import org.m415x.materialscalculator.domain.model.TipoHormigon
import org.m415x.materialscalculator.ui.common.AppInput
import org.m415x.materialscalculator.ui.common.NumericInput
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
                    subtitle = r.dosificacionMezcla,
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
    var name by remember { mutableStateOf(recipeToEdit?.nombre ?: "") }
    var cemento by remember { mutableStateOf(recipeToEdit?.cementoKg?.toString() ?: "") }
    var arena by remember { mutableStateOf(recipeToEdit?.arenaM3?.toString() ?: "") }
    var piedra by remember { mutableStateOf(recipeToEdit?.piedraM3?.toString() ?: "") }
    var agua by remember { mutableStateOf(recipeToEdit?.relacionAgua?.toString() ?: "0.5") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(if (recipeToEdit?.id.isNullOrBlank()) "Nueva Mezcla" else "Editar Mezcla", style = MaterialTheme.typography.headlineSmall)

                AppInput(value = name, onValueChange = { name = it }, label = "Nombre")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericInput(cemento, { cemento = it }, "Cemento", suffix = { Text("kg") }, modifier = Modifier.weight(1f))
                    NumericInput(agua, { agua = it }, "Agua (A/C)", modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericInput(arena, { arena = it }, "Arena", suffix = { Text("m3") }, modifier = Modifier.weight(1f))
                    NumericInput(piedra, { piedra = it }, "Piedra", suffix = { Text("m3") }, modifier = Modifier.weight(1f))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(
                        enabled = name.isNotBlank() && cemento.isNotBlank(),
                        onClick = {
                            onSave(CustomRecipe(
                                id = recipeToEdit?.id?.ifBlank { Uuid.random().toString() } ?: Uuid.random().toString(),
                                nombre = name,
                                cementoKg = cemento.toSafeDoubleOrNull() ?: 0.0,
                                arenaM3 = arena.toSafeDoubleOrNull() ?: 0.0,
                                piedraM3 = piedra.toSafeDoubleOrNull() ?: 0.0,
                                calKg = 0.0, // Simplificación por ahora
                                relacionAgua = agua.toSafeDoubleOrNull() ?: 0.5,
                                tipo = if ((piedra.toSafeDoubleOrNull() ?: 0.0) > 0) "CONCRETE" else "MORTAR"
                            ))
                        }
                    ) { Text("Guardar") }
                }
            }
        }
    }
}