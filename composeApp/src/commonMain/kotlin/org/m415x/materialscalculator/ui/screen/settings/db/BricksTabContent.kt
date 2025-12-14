package org.m415x.materialscalculator.ui.screen.settings.db

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import org.m415x.materialscalculator.domain.model.CustomBrick
import org.m415x.materialscalculator.domain.model.TipoLadrillo
import org.m415x.materialscalculator.ui.common.AppInput
import org.m415x.materialscalculator.ui.common.NumericInput
import org.m415x.materialscalculator.ui.common.toSafeDoubleOrNull

// Definimos el modelo UI aquí para uso local
data class BrickUiModel(
    val id: String,
    val nombre: String,
    val ancho: Double,
    val alto: Double,
    val largo: Double,
    val junta: Double,
    val isCustom: Boolean
)

@Composable
fun BricksTabContent(repository: SettingsRepository) {
    val scope = rememberCoroutineScope()

    // 1. Datos: Observamos Custom + Hidden Ids
    val customBricks by repository.customBricks.collectAsState(initial = emptyList())
    val hiddenIds by repository.hiddenBrickIds.collectAsState(initial = emptySet())

    // Repositorio estático para obtener los defaults
    val staticRepo = remember { StaticMaterialRepository() }

    // 2. Lógica de Fusión (Merge)
    // Creamos la lista unificada para la UI
    val uiList = remember(customBricks, hiddenIds) {
        val list = mutableListOf<MaterialUiModel>()

        // A. Agregamos los CUSTOM
        list.addAll(customBricks.map {
            // Convertimos CustomBrick a MaterialUiModel
            val w = (it.ancho * 100).toInt()
            val h = (it.alto * 100).toInt()
            val l = (it.largo * 100).toInt()

            MaterialUiModel(
                id = it.id,
                title = it.nombre,
                subtitle = "${w}x${h}x${l} cm",
                isCustom = true,
                originalData = it // Guardamos el objeto real
            )
        })

        // B. Agregamos los ESTÁTICOS (Si no están ocultos)
        TipoLadrillo.entries.forEach { type ->
            if (type.name !in hiddenIds) { // Usamos type.name como ID único
                val props = staticRepo.getPropiedadesLadrillo(type)!!
                val w = (props.anchoMuro * 100).toInt()
                val h = (props.altoUnidad * 100).toInt()
                val l = (props.largoUnidad * 100).toInt()

                list.add(MaterialUiModel(
                    id = type.name,
                    title = type.nombre,
                    subtitle = "${w}x${h}x${l} cm",
                    isCustom = false,
                    // Creamos un CustomBrick temporal para facilitar la copia en el editor
                    originalData = CustomBrick(
                        id = "", // ID vacío para que al guardar se genere uno nuevo
                        nombre = type.nombre,
                        ancho = props.anchoMuro,
                        alto = props.altoUnidad,
                        largo = props.largoUnidad,
                        junta = props.espesorJunta,
                        isPortante = type.isPortante,
                        descripcion = type.descripcion
                    )
                ))
            }
        }
        // Ordenamos alfabéticamente para que se mezclen bien
        list.sortedBy { it.title }
    }

    // 3. Estados de Diálogos
    var showEditor by remember { mutableStateOf(false) }
    var brickToEdit by remember { mutableStateOf<CustomBrick?>(null) }

    var itemToDelete by remember { mutableStateOf<MaterialUiModel?>(null) } // Usamos el modelo UI
    var showRestore by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    brickToEdit = null // Modo Crear
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
                    "Gestiona los ladrillos disponibles en la calculadora.",
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
                                val original = item.originalData as? CustomBrick
                                // Si es de fábrica, forzamos ID nuevo
                                brickToEdit = original?.copy(id = if (item.isCustom) original.id else "")
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

    // --- DIALOGO EDITOR ---
    if (showEditor) {
        // Asegúrate de que BrickEditorDialog acepte CustomBrick?
        BrickEditorDialog(
            brickToEdit = brickToEdit,
            onDismiss = { showEditor = false },
            onSave = {
                scope.launch { repository.saveCustomBrick(it); showEditor = false }
            }
        )
    }

    // --- DIALOGO BORRAR / OCULTAR ---
    if (itemToDelete != null) {
        DeleteOrHideDialog(
            item = itemToDelete!!,
            onDismiss = { itemToDelete = null },
            onConfirm = {
                scope.launch {
                    if (itemToDelete!!.isCustom) repository.deleteCustomBrick(itemToDelete!!.id)
                    else repository.hideStaticBrick(itemToDelete!!.id)
                    itemToDelete = null
                }
            }
        )
    }

    // --- DIALOGO RESTAURAR (Muestra lista de ocultos) ---
    if (showRestore) {
        RestoreBricksDialog(
            hiddenIds = hiddenIds,
            onRestore = { id -> scope.launch { repository.restoreStaticBrick(id) } },
            onDismiss = { showRestore = false }
        )
    }
}

@Composable
fun RestoreBricksDialog(
    hiddenIds: Set<String>,
    onRestore: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restaurar Ladrillos") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp) // Limitar altura
            ) {
                items(hiddenIds.toList()) { id ->
                    // Buscamos el nombre legible usando el Enum
                    val nombre = try { TipoLadrillo.valueOf(id).nombre } catch (e: Exception) { id }

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

// --- FORMULARIO DE EDICIÓN ---
@OptIn(ExperimentalUuidApi::class)
@Composable
fun BrickEditorDialog(
    brickToEdit: CustomBrick?,
    onDismiss: () -> Unit,
    onSave: (CustomBrick) -> Unit
) {
    // Inicializamos valores (Convertimos Metros a String CM para inputs)
    var name by remember { mutableStateOf(brickToEdit?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(brickToEdit?.descripcion ?: "") }
    var isPortante by remember { mutableStateOf(brickToEdit?.isPortante ?: false) }

    // Función auxiliar para formatear "0.18" -> "18"
    fun mToCmStr(m: Double): String {
        if (m == 0.0) return ""
        val cm = m * 100
        // Quitamos decimales si es entero (18.0 -> 18)
        return if (cm % 1 == 0.0) cm.toInt().toString() else cm.toString()
    }

    var anchoCm by remember { mutableStateOf(mToCmStr(brickToEdit?.ancho ?: 0.0)) }
    var altoCm by remember { mutableStateOf(mToCmStr(brickToEdit?.alto ?: 0.0)) }
    var largoCm by remember { mutableStateOf(mToCmStr(brickToEdit?.largo ?: 0.0)) }
    var juntaCm by remember { mutableStateOf(mToCmStr(brickToEdit?.junta ?: 0.015)) }

    val isFormValid = name.isNotBlank() && anchoCm.isNotBlank() && altoCm.isNotBlank() && largoCm.isNotBlank()

    // Definimos los FocusRequesters necesarios
    val focusNombreLadrillo = remember { FocusRequester() }
    val focusIsPortante = remember { FocusRequester() }
    val focusAnchoLadrillo = remember { FocusRequester() }
    val focusAltoLadrillo = remember { FocusRequester() }
    val focusLargoLadrillo = remember { FocusRequester() }
    val focusJuntaLadrillo = remember { FocusRequester() }
    val focusDescripcionLadrillo = remember { FocusRequester() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    if (brickToEdit == null) "Nuevo Ladrillo" else "Editar Ladrillo",
                    style = MaterialTheme.typography.headlineSmall
                )

                AppInput(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre del ladrillo",
                    focusRequester = focusNombreLadrillo,
                    nextFocusRequester = focusAnchoLadrillo
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { isPortante = !isPortante }
                ) {
                    Checkbox(
                        checked = isPortante,
                        onCheckedChange = { isPortante = it },
                        Modifier.focusRequester(focusIsPortante)
                    )
                    Text("Es Portante", style = MaterialTheme.typography.labelMedium)
                    Text(" (Estructural)", style = MaterialTheme.typography.labelSmall)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericInput(
                        value = anchoCm,
                        onValueChange = { anchoCm = it },
                        label = "Ancho",
                        suffix = { Text("cm") },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusAnchoLadrillo,
                        nextFocusRequester = focusAltoLadrillo
                    )
                    NumericInput(
                        value = altoCm,
                        onValueChange = { altoCm = it },
                        label = "Alto",
                        suffix = { Text("cm") },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusAltoLadrillo,
                        nextFocusRequester = focusLargoLadrillo
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericInput(
                        value = largoCm,
                        onValueChange = { largoCm = it },
                        label = "Largo",
                        suffix = { Text("cm") },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusLargoLadrillo,
                        nextFocusRequester = focusJuntaLadrillo
                    )
                    NumericInput(
                        value = juntaCm,
                        onValueChange = { juntaCm = it },
                        label = "Junta",
                        suffix = { Text("cm") },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusJuntaLadrillo,
                        nextFocusRequester = focusDescripcionLadrillo
                    )
                }

                AppInput(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = "Descripción / Uso",
                    placeholder = "Ej: Para muros de carga",
                    focusRequester = focusDescripcionLadrillo,
                    onDone = {}
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        enabled = isFormValid,
                        onClick = {
                            // Convertir Inputs CM -> Metros Storage
                            val w = (anchoCm.toSafeDoubleOrNull() ?: 0.0) / 100.0
                            val h = (altoCm.toSafeDoubleOrNull() ?: 0.0) / 100.0
                            val l = (largoCm.toSafeDoubleOrNull() ?: 0.0) / 100.0
                            val j = (juntaCm.toSafeDoubleOrNull() ?: 0.0) / 100.0

                            // Verificamos si es nulo O ESTÁ VACÍO.
                            val finalId = if (brickToEdit?.id.isNullOrBlank()) {
                                Uuid.random().toString() // Generar ID nuevo si es copia o nuevo
                            } else {
                                brickToEdit.id // Mantener ID si es edición de uno existente
                            }

                            val newBrick = CustomBrick(
                                id = finalId,
                                nombre = name,
                                ancho = w,
                                alto = h,
                                largo = l,
                                junta = j,
                                isPortante = isPortante,
                                descripcion = descripcion
                            )
                            onSave(newBrick)
                        }
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}