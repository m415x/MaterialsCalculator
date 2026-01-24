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

package org.m415x.materialcalc.ui.screen.settings.db

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.domain.model.TextSource
import org.m415x.materialcalc.domain.model.asString
import org.m415x.materialcalc.ui.common.dialogs.AppDialog
import org.m415x.materialcalc.ui.common.display.PrimaryBadge

// --- MODELO GENÉRICO PARA LA LISTA ---
// Usamos este modelo para que la LazyColumn sea igual para todos
data class MaterialUiModel(
    val id: String,
    val title: TextSource,
    val subtitle: TextSource,
    val isCustom: Boolean,
    val originalData: Any? = null, // Guardamos el objeto real (CustomBrick/CustomIron) aquí para editarlo
    val type: String? = null // Tipo de mezcla (CONCRETE, MORTAR, PLASTER)
)

// --- ITEM DE LISTA UNIVERSAL ---
@Composable
fun UniversalMaterialItem(
    item: MaterialUiModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Eliminamos el Card contenedor para que se integre mejor en el acordeón
    // o lo hacemos transparente/plano si queremos mantener el padding interno.
    // Para el diseño de acordeón, suele quedar mejor una fila limpia.
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.title.asString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (item.type != null) {
                    val badgeText = when (item.type) {
                        "CONCRETE" -> stringResource(Res.string.concrete_title)
                        "MORTAR" -> stringResource(Res.string.wall_label_mortar)
                        "PLASTER" -> stringResource(Res.string.plaster_title)
                        else -> item.type
                    }
                    PrimaryBadge(text = badgeText ?: "")
                }
            }
            Text(
                item.subtitle.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onEdit) {
            // Si es custom editamos, si es fábrica copiamos
            val icon = if (item.isCustom) Icons.Default.Edit else Icons.Default.ContentCopy
            Icon(icon, stringResource(Res.string.button_edit), tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                stringResource(Res.string.button_remove),
                tint = MaterialTheme.colorScheme.error
            )
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
    
    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isStatic) stringResource(Res.string.settings_db_hide_title) else stringResource(Res.string.settings_db_delete_title)) },
        content = {
            Text(
                if (isStatic) stringResource(Res.string.settings_db_hide_msg)
                else stringResource(Res.string.settings_db_delete_msg, item.title.asString())
            )
        },
        actions = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.button_cancel)) }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(if (isStatic) stringResource(Res.string.button_hide) else stringResource(Res.string.button_remove))
            }
        }
    )
}