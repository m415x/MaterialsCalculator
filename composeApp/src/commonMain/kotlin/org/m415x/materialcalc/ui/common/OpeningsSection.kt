/*
 * materialCalc
 * Copyright (C) 2025 M415X
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

package org.m415x.materialcalc.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.m415x.materialcalc.domain.model.Abertura

/**
 * Sección reutilizable para gestionar aberturas (Agregar, Listar, Editar, Borrar).
 *
 * @param aberturas Lista mutable de aberturas (SnapshotStateList).
 */
@Composable
fun OpeningsSection(
    aberturas: MutableList<Abertura>, // Recibimos la lista para modificarla
    modifier: Modifier = Modifier,
    focusRequesterAncho: FocusRequester? = null, // Parámetro opcional para recibir el foco desde el padre
    nextFocusRequesterAlto: FocusRequester? = null // Para que al terminar la abertura, salte al siguiente campo del padre
) {
    // --- ESTADOS INTERNOS ---
    // Inputs temporales para agregar
    var anchoInput by remember { mutableStateOf("") }
    var altoInput by remember { mutableStateOf("") }

    // Control de diálogos
    var openingToEdit by remember { mutableStateOf<Abertura?>(null) }
    var indexToEdit by remember { mutableStateOf<Int?>(null) }
    var openingToDelete by remember { mutableStateOf<Abertura?>(null) }

    // Focos
    val focusAncho = focusRequesterAncho ?: remember { FocusRequester() }
    val focusAlto = remember { FocusRequester() }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {

        // 1. HEADER Y FORMULARIO DE AGREGAR (Inline)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Aberturas", style = MaterialTheme.typography.titleMedium)
                Text("Puertas y ventanas a descontar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumericInput(
                value = anchoInput,
                onValueChange = { anchoInput = it },
                label = "Ancho",
                suffix = { Text("m") },
                modifier = Modifier.weight(1f),
                focusRequester = focusAncho,
                nextFocusRequester = focusAlto
            )
            NumericInput(
                value = altoInput,
                onValueChange = { altoInput = it },
                label = "Alto",
                suffix = { Text("m") },
                modifier = Modifier.weight(1f),
                focusRequester = focusAlto,
                nextFocusRequester = nextFocusRequesterAlto,
                onDone = {} // Opcional: Podrías llamar a agregar aquí
            )
            // Botón Agregar (+)
            FilledIconButton(
                onClick = {
                    val w = anchoInput.toSafeDoubleOrNull()
                    val h = altoInput.toSafeDoubleOrNull()
                    if (areValidDimensions(w, h)) {
                        aberturas.add(Abertura(
                            anchoMetros = w!!,
                            altoMetros = h!!,
                            cantidad = 1,
                            nombre = "Abertura ${aberturas.size + 1}"
                        ))
                        // Limpiar y re-enfocar
                        anchoInput = ""
                        altoInput = ""
                        focusAncho.requestFocus()
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }

        // 2. LISTA DE ABERTURAS
        if (aberturas.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    aberturas.forEachIndexed { index, abertura ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    indexToEdit = index
                                    openingToEdit = abertura
                                }
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${abertura.cantidad} x ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = abertura.nombre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${abertura.anchoMetros}x${abertura.altoMetros} m",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = { openingToDelete = abertura },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, "Borrar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        if (index < aberturas.size - 1) HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            }
        } else {
            // Mensaje vacío opcional
            Text(
                "Sin aberturas cargadas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }

    // --- DIÁLOGOS INTERNOS ---

    // 1. Editar
    if (openingToEdit != null && indexToEdit != null) {
        EditAberturaDialog(
            abertura = openingToEdit!!,
            onDismiss = {
                openingToEdit = null
                indexToEdit = null
            },
            onConfirm = { nuevaAbertura ->
                if (indexToEdit in aberturas.indices) {
                    aberturas[indexToEdit!!] = nuevaAbertura
                }
                openingToEdit = null
                indexToEdit = null
            }
        )
    }

    // 2. Borrar
    if (openingToDelete != null) {
        AppConfirmDialog(
            title = "Borrar Abertura",
            text = "¿Quitar '${openingToDelete!!.nombre}'?",
            onConfirm = {
                aberturas.remove(openingToDelete)
                openingToDelete = null
            },
            onDismiss = { openingToDelete = null }
        )
    }
}