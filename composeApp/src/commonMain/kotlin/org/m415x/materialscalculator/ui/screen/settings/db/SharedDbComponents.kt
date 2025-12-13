package org.m415x.materialscalculator.ui.screen.settings.db

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// --- MODELO GENÉRICO PARA LA LISTA ---
// Usamos este modelo para que la LazyColumn sea igual para todos
data class MaterialUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val isCustom: Boolean,
    val originalData: Any? = null // Guardamos el objeto real (CustomBrick/CustomIron) aquí para editarlo
)

// --- ITEM DE LISTA UNIVERSAL ---
@Composable
fun UniversalMaterialItem(
    item: MaterialUiModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCustom) MaterialTheme.colorScheme.surfaceContainerLow
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono distintivo (C = Custom, F = Factory)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (item.isCustom) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (item.isCustom) "C" else "F",
                    fontWeight = FontWeight.Bold,
                    color = if (item.isCustom) MaterialTheme.colorScheme.onTertiaryContainer
                    else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(item.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            IconButton(onClick = onEdit) {
                // Si es custom editamos, si es fábrica copiamos
                val icon = if (item.isCustom) Icons.Default.Edit else Icons.Default.ContentCopy
                Icon(icon, "Editar", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Borrar/Ocultar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// --- DIALOGO DE BORRAR / OCULTAR GENÉRICO ---
@Composable
fun DeleteOrHideDialog(
    item: MaterialUiModel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isStatic = !item.isCustom
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Delete, null) },
        title = { Text(if (isStatic) "Ocultar Material" else "Eliminar Material") },
        text = {
            Text(
                if (isStatic) "Este es un material de fábrica. Se ocultará de la lista pero podrás restaurarlo luego."
                else "Se eliminará '${item.title}' permanentemente."
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(if (isStatic) "Ocultar" else "Eliminar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}