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

package org.m415x.materialcalc.ui.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.about_version
import materialscalculator.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.domain.utils.PlatformInfo

import org.m415x.materialcalc.ui.common.AboutAppDialog
import org.m415x.materialcalc.ui.common.NumericInput

/**
 * Menú principal de configuración.
 *
 * @param onNavigate La función de navegación.
 */
@Composable
fun SettingsMainMenu(onNavigate: (SettingsSection) -> Unit) {
    // Estado para controlar la visibilidad del diálogo
    var showAboutDialog by remember { mutableStateOf(false) }

    val version = PlatformInfo.appVersion

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            SettingsCategoryTitle("General")
            SettingsMenuItem(
                title = "Apariencia",
                subtitle = "Tema oscuro, claro y alto contraste",
                icon = Icons.Default.Palette,
                onClick = { onNavigate(SettingsSection.APPEARANCE) }
            )
            SettingsMenuItem(
                title = "Parámetros Globales",
                subtitle = "Pesos de bolsas, capacidad de baldes y carretillas",
                icon = Icons.Default.Tune,
                onClick = { onNavigate(SettingsSection.GLOBAL_PARAMS) }
            )
        }

        item {
            SettingsCategoryTitle("Base de Datos")
            SettingsMenuItem(
                title = "Materiales y Medidas",
                subtitle = "Editar ladrillos, hierros y proporciones",
                icon = Icons.Default.Construction,
                onClick = { onNavigate(SettingsSection.MATERIALS_DB) }
            )
            SettingsMenuItem(
                title = "Precios",
                subtitle = "Configurar costos unitarios",
                icon = Icons.Default.AttachMoney,
                onClick = { onNavigate(SettingsSection.PRICES) }
            )
        }
        /*
        item {
            SettingsCategoryTitle("Datos")
            SettingsMenuItem(
                title = "Sincronización Nube",
                subtitle = "Guardar mis datos (Próximamente)",
                icon = Icons.Default.CloudUpload,
                onClick = { /* TODO */ }
            )
        }
        */
        item {
            SettingsCategoryTitle("Información")
            SettingsMenuItem(
                title = "Acerca de",
                subtitle = "Versión, desarrollador y contacto",
                icon = Icons.Default.Info, // Icono de información
                onClick = { showAboutDialog = true } // Activamos el diálogo
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${stringResource(Res.string.app_name)} ${stringResource(Res.string.about_version, version)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
    // 2. Renderizado del Diálogo
    if (showAboutDialog) {
        AboutAppDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
}

/**
 * Título de categoría de configuración.
 *
 * @param text El texto del título.
 */
@Composable
fun SettingsCategoryTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

/**
 * Entrada numérica para enteros.
 *
 * @param value El valor actual.
 * @param label El texto del label.
 * @param onSave La función de guardado.
 */
@Composable
fun EditIntegerSetting(
    value: Int,
    label: String,
    defaultValue: Int,
    suffix: String? = null,
    onSave: (Int) -> Unit,
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    onDone: (() -> Unit)? = null
) {
    var text by remember(value) { mutableStateOf(value.toString()) }

    // Detectamos si el valor actual difiere del default
    val isModified = value != defaultValue

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        // 1. Texto descriptivo a la izquierda
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )

        // 2. Botón de Reset (Solo visible si se modificó)
        // Usamos AnimatedVisibility para que aparezca/desaparezca suavemente
        AnimatedVisibility(visible = isModified) {
            IconButton(
                onClick = {
                    onSave(defaultValue) // Guardamos el default
                    text = defaultValue.toString() // Actualizamos el input visualmente
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh, // Ícono de flecha circular
                    contentDescription = "Restablecer",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        NumericInput(
            value = text,
            onValueChange = {
                text = it
                // Guardado automático si es válido
                it.toIntOrNull()?.let { num -> onSave(num) }
            },
            label = "", // Sin label flotante porque ya tenemos texto a la izquierda
            modifier = Modifier.width(100.dp),

            suffix = { if (suffix != null) Text(suffix) },
            focusRequester = focusRequester,
            nextFocusRequester = nextFocusRequester,
            onDone = onDone
        )
    }
}

/**
 * Entrada numérica para doubles.
 *
 * @param value El valor actual.
 * @param label El texto del label.
 * @param onSave La función de guardado.
 */
@Composable
fun EditDoubleSetting(
    value: Double,
    label: String,
    defaultValue: Double,
    suffix: String? = null,
    onSave: (Double) -> Unit,
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    onDone: (() -> Unit)? = null
) {
    var text by remember(value) { mutableStateOf(value.toString()) }

    // Detectamos si el valor actual difiere del default
    val isModified = value != defaultValue

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        // 1. Texto descriptivo a la izquierda
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )

        // 2. Botón de Reset (Solo visible si se modificó)
        // Usamos AnimatedVisibility para que aparezca/desaparezca suavemente
        AnimatedVisibility(visible = isModified) {
            IconButton(
                onClick = {
                    onSave(defaultValue) // Guardamos el default
                    text = defaultValue.toString() // Actualizamos el input visualmente
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh, // Ícono de flecha circular
                    contentDescription = "Restablecer",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Input numérico reutilizable (Ahora con Suffix!)
        NumericInput(
            value = text,
            onValueChange = {
                text = it
                it.toDoubleOrNull()?.let { num -> onSave(num) }
            },
            label = "", // Sin label flotante porque ya tenemos texto a la izquierda
            modifier = Modifier.width(100.dp),

            suffix = { if (suffix != null) Text(suffix) },
            focusRequester = focusRequester,
            nextFocusRequester = nextFocusRequester,
            onDone = onDone
        )
    }
}

// Componente pequeño para editar porcentajes en fila
@Composable
fun EditPercentSetting(
    label: String,
    value: Double,
    defaultValue: Double,
    onSave: (Double) -> Unit,
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    onDone: (() -> Unit)? = null
) {
    // Usamos el estado local para la edición fluida
    var text by remember(value) { mutableStateOf(value.toString()) }

    // Detectamos si el valor actual difiere del default
    val isModified = value != defaultValue

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        // 1. Texto descriptivo a la izquierda
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )

        // 2. Botón de Reset (Solo visible si se modificó)
        // Usamos AnimatedVisibility para que aparezca/desaparezca suavemente
        AnimatedVisibility(visible = isModified) {
            IconButton(
                onClick = {
                    onSave(defaultValue) // Guardamos el default
                    text = defaultValue.toString() // Actualizamos el input visualmente
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh, // Ícono de flecha circular
                    contentDescription = "Restablecer",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        NumericInput(
            value = text,
            onValueChange = { newText ->
                text = newText
                // Validamos y guardamos automáticamente
                newText.toDoubleOrNull()?.let { num -> onSave(num) }
            },
            label = "", // Sin label flotante porque ya tenemos texto a la izquierda
            modifier = Modifier.width(100.dp),

            suffix = { Text("%") },
            focusRequester = focusRequester,
            nextFocusRequester = nextFocusRequester,
            onDone = onDone
        )
    }
}