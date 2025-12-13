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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

import org.m415x.materialscalculator.data.repository.SettingsRepository
import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.model.CustomIron
import org.m415x.materialscalculator.domain.model.DiametroHierro
import org.m415x.materialscalculator.ui.common.AppInput
import org.m415x.materialscalculator.ui.common.NumericInput
import org.m415x.materialscalculator.ui.common.toSafeDoubleOrNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun IronsTabContent(repository: SettingsRepository) {
    val scope = rememberCoroutineScope()
    val customIrons by repository.customIrons.collectAsState(initial = emptyList())
    val hiddenIds by repository.hiddenIronIds.collectAsState(initial = emptySet())
    val staticRepo = remember { StaticMaterialRepository() }

    // 1. Fusionar en MaterialUiModel (SOLUCION DEL ERROR)
    val uiList = remember(customIrons, hiddenIds) {
        val list = mutableListOf<MaterialUiModel>()

        // Custom
        list.addAll(customIrons.map {
            MaterialUiModel(
                id = it.id,
                title = it.nombre,
                subtitle = "Ø ${it.diametroMm}mm | ${it.pesoPorMetro} kg/m",
                isCustom = true,
                originalData = it // Guardamos el CustomIron aquí
            )
        })

        // Static
        DiametroHierro.entries.forEach { type ->
            if (type.name !in hiddenIds) {
                val peso = staticRepo.getPesoHierroPorMetro(type)
                list.add(MaterialUiModel(
                    id = type.name,
                    title = "Hierro Ø ${type.mm} mm",
                    subtitle = "$peso kg/m (Estándar)",
                    isCustom = false,
                    // Creamos copia custom temporal
                    originalData = CustomIron(
                        id = "",
                        nombre = "Hierro Ø ${type.mm} mm",
                        diametroMm = type.mm,
                        pesoPorMetro = peso
                    )
                ))
            }
        }
        list.sortedBy { it.title }
    }

    var showEditor by remember { mutableStateOf(false) }
    var ironToEdit by remember { mutableStateOf<CustomIron?>(null) }

    var itemToDelete by remember { mutableStateOf<MaterialUiModel?>(null) }
    var showRestore by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    ironToEdit = null
                    showEditor = true
                },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nuevo") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // Botón Restaurar
            if (hiddenIds.isNotEmpty()) {
                TextButton(
                    onClick = { showRestore = true },
                    modifier = Modifier.padding(vertical = 4.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Restore, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Restaurar materiales de fábrica(${hiddenIds.size})")
                }
            } else {
                Text(
                    "Gestiona los hierros disponibles en la calculadora.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (uiList.isEmpty()) {
                Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No hay hierros disponibles.")
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
                                val original = item.originalData as? CustomIron
                                // Si es de fábrica, forzamos ID nuevo
                                ironToEdit = original?.copy(id = if (item.isCustom) original.id else "")
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
        IronEditorDialog(
            ironToEdit = ironToEdit,
            onDismiss = { showEditor = false },
            onSave = {
                scope.launch { repository.saveCustomIron(it); showEditor = false }
            }
        )
    }

    if (itemToDelete != null) {
        DeleteOrHideDialog(
            item = itemToDelete!!,
            onDismiss = { itemToDelete = null },
            onConfirm = {
                scope.launch {
                    if (itemToDelete!!.isCustom) repository.deleteCustomIron(itemToDelete!!.id)
                    else repository.hideStaticIron(itemToDelete!!.id)
                    itemToDelete = null
                }
            }
        )
    }

    // --- DIALOGO RESTAURAR (Muestra lista de ocultos) ---
    if (showRestore) {
        RestoreIronsDialog(
            hiddenIds = hiddenIds,
            onRestore = { id -> scope.launch { repository.restoreStaticIron(id) } },
            onDismiss = { showRestore = false }
        )
    }
}

@Composable
fun RestoreIronsDialog(
    hiddenIds: Set<String>,
    onRestore: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restaurar Hierros") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp) // Limitar altura
            ) {
                items(hiddenIds.toList()) { id ->
                    // Buscamos el nombre legible usando el Enum
                    val nombre = try { DiametroHierro.valueOf(id).nombre } catch (e: Exception) { id }

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
fun IronEditorDialog(
    ironToEdit: CustomIron?,
    onDismiss: () -> Unit,
    onSave: (CustomIron) -> Unit
) {
    // Inicializamos valores (Convertimos Metros a String MM para inputs)
    var name by remember { mutableStateOf(ironToEdit?.nombre ?: "") }

    var diamMm by remember {
        mutableStateOf(ironToEdit?.diametroMm?.let { if (it == 0) "" else it.toString() } ?: "")
    }

    var pesoMetro by remember { mutableStateOf((ironToEdit?.pesoPorMetro ?: 0.0).toString()) }

    val isFormValid = name.isNotBlank() && diamMm.isNotBlank() && pesoMetro.isNotBlank()

    // Definimos los FocusRequesters necesarios
    val focusNombreHierro = remember { FocusRequester() }
    val focusDiametroHierro = remember { FocusRequester() }
    val focusPesoHierro = remember { FocusRequester() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    if (ironToEdit == null) "Nuevo Hierro" else "Editar Hierro",
                    style = MaterialTheme.typography.headlineSmall
                )

                AppInput(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre del hierro",
                    focusRequester = focusNombreHierro,
                    nextFocusRequester = focusDiametroHierro
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericInput(
                        value = diamMm,
                        onValueChange = { diamMm = it },
                        label = "Diámetro",
                        suffix = { Text("mm") },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusDiametroHierro,
                        nextFocusRequester = focusPesoHierro
                    )
                    NumericInput(
                        value = pesoMetro,
                        onValueChange = { pesoMetro = it },
                        label = "Peso/Metro",
                        suffix = { Text("kg/m") },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusPesoHierro,
                        onDone = {}
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        enabled = isFormValid,
                        onClick = {
                            val diam = diamMm.toIntOrNull() ?: 0
                            val peso = pesoMetro.toSafeDoubleOrNull() ?: 0.0

                            val newIron = CustomIron(
                                // Si ironToEdit es null (nuevo), generamos un UUID nativo y lo pasamos a String
                                id = ironToEdit?.id ?: Uuid.random().toString(),
                                nombre = name,
                                diametroMm = diam,
                                pesoPorMetro = peso
                            )
                            onSave(newIron)
                        }
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}