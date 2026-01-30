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

package org.m415x.materialcalc.ui.screen.settings

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.domain.utils.PlatformInfo
import org.m415x.materialcalc.ui.common.dialogs.AboutAppDialog
import org.m415x.materialcalc.ui.common.inputs.CmInput
import org.m415x.materialcalc.ui.common.inputs.NumericInput
import org.m415x.materialcalc.ui.common.utils.toSafeDoubleOrNull

/**
 * Menú principal de configuración.
 *
 * @param onNavigate La función de navegación.
 */
@Composable
fun SettingsMainMenu(onNavigate: (SettingsSection) -> Unit) {
    // Estado para controlar la visibilidad del diálogo
    var showAboutDialog by remember { mutableStateOf(false) }

    val labelGeneral = stringResource(Res.string.settings_section_general)
    val h2Appearance = stringResource(Res.string.settings_item_appearance_title)
    val pAppearance = stringResource(Res.string.settings_item_appearance_desc)
    val h2GlobalParams = stringResource(Res.string.settings_item_params_title)
    val pGlobalParams = stringResource(Res.string.settings_item_params_desc)
    val labelDataBase = stringResource(Res.string.settings_section_database)
    val h2Materials = stringResource(Res.string.settings_item_materials_title)
    val pMaterials = stringResource(Res.string.settings_item_materials_desc)
    val h2Prices = stringResource(Res.string.settings_item_prices_title)
    val pPrices = stringResource(Res.string.settings_item_prices_desc)
    val labelInformation = stringResource(Res.string.settings_section_info)
    val h2About = stringResource(Res.string.settings_item_about_title)
    val pAbout = stringResource(Res.string.settings_item_about_desc)
    val appName = stringResource(Res.string.app_name)
    val version = stringResource(Res.string.about_version, PlatformInfo.appVersion)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            SettingsCategoryTitle(labelGeneral)
            SettingsMenuItem(
                title = h2Appearance,
                subtitle = pAppearance,
                icon = Icons.Default.Palette,
                onClick = { onNavigate(SettingsSection.APPEARANCE) }
            )
            SettingsMenuItem(
                title = h2GlobalParams,
                subtitle = pGlobalParams,
                icon = Icons.Default.Tune,
                onClick = { onNavigate(SettingsSection.GLOBAL_PARAMS) }
            )
        }

        item {
            SettingsCategoryTitle(labelDataBase)
            SettingsMenuItem(
                title = h2Materials,
                subtitle = pMaterials,
                icon = Icons.Default.Construction,
                onClick = { onNavigate(SettingsSection.MATERIALS_DB) }
            )
            SettingsMenuItem(
                title = h2Prices,
                subtitle = pPrices,
                icon = Icons.Default.AttachMoney,
                onClick = { onNavigate(SettingsSection.PRICES) }
            )
        }
        item {
            SettingsCategoryTitle(labelInformation)
            SettingsMenuItem(
                title = h2About,
                subtitle = pAbout,
                icon = Icons.Default.Info, // Icono de información
                onClick = { showAboutDialog = true } // Activamos el diálogo
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$appName $version",
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

@Composable
fun BaseEditSetting(
    label: String,
    currentText: String, // Texto actual en el input
    defaultText: String, // Valor por defecto convertido a texto
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    inputContent: @Composable RowScope.() -> Unit
) {
    // La lógica de modificación se centraliza aquí
    val isModified = (currentText != defaultText && currentText.isNotEmpty()) || currentText.isEmpty()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Etiqueta
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )

            // 2. Botón Reset
            AnimatedVisibility(
                visible = isModified,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                IconButton(onClick = onReset) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(Res.string.settings_params_reset_desc),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 3. El Input (se inyecta desde afuera)
        inputContent()
    }
}

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

    BaseEditSetting(
        label = label,
        currentText = text,
        defaultText = defaultValue.toString(),
        onReset = {
            onSave(defaultValue)
            text = defaultValue.toString()
        }
    ) {
        NumericInput(
            value = text,
            onValueChange = {
                text = it
                it.toIntOrNull()?.let { num -> onSave(num) }
            },
            label = "",
            modifier = Modifier.width(150.dp),
            suffix = { suffix?.let { Text(it, style = MaterialTheme.typography.labelSmall) } },
            focusRequester = focusRequester,
            nextFocusRequester = nextFocusRequester
        )
    }
}

@Composable
fun EditDoubleSetting(
    value: Double,
    label: String,
    defaultValue: Double,
    suffix: String? = null,
    onSave: (Double) -> Unit,
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    onDone: (() -> Unit)? = null,
    decimalDigits: Int = 2
) {
    var text by remember(value) { mutableStateOf(value.toString()) }

    BaseEditSetting(
        label = label,
        currentText = text,
        defaultText = defaultValue.toString(),
        onReset = {
            onSave(defaultValue)
            text = defaultValue.toString()
        }
    ) {
        CmInput(
            value = text,
            onValueChange = { newValue ->
                text = newValue
                onSave(newValue.toSafeDoubleOrNull() ?: 0.0)
            },
            label = "",
            modifier = Modifier.width(150.dp),
            suffix = { suffix?.let { Text(it, style = MaterialTheme.typography.labelSmall) } },
            focusRequester = focusRequester,
            nextFocusRequester = nextFocusRequester,
            onDone = onDone,
            decimalDigits = decimalDigits
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
    onDone: (() -> Unit)? = null,
    decimalDigits: Int = 1
) {
    // 1. Usamos el estado local para la edición fluida
    var text by remember(value) { mutableStateOf(value.toString()) }

    // 2. Usamos el componente Base inteligente
    BaseEditSetting(
        label = label,
        currentText = text,
        // Convertimos el default a String para la comparación visual
        defaultText = defaultValue.toString(),
        onReset = {
            onSave(defaultValue)
            text = defaultValue.toString()
        }
    ) {
        CmInput(
            value = text,
            onValueChange = { newValue ->
                text = newValue
                onSave(newValue.toSafeDoubleOrNull() ?: 0.0)
            },
            label = "",
            modifier = Modifier.width(150.dp),
            suffix = { Text(text = "%", style = MaterialTheme.typography.labelSmall) },
            focusRequester = focusRequester,
            nextFocusRequester = nextFocusRequester,
            onDone = onDone,
            decimalDigits = decimalDigits
        )
    }
}

@Composable
fun EditPriceSetting(
    label: String,
    value: Double,
    defaultValue: Double = 0.00,
    unit: String,
    onSave: (Double) -> Unit,
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    onDone: (() -> Unit)? = null
) {
    // Usamos una función de formateo que imite lo que hace el CmInput
    val formattedInitialValue = remember(value) {
        if (value == 0.0) "0.00"
        else {
            // Convertimos el Double (0.5) a Long (50) para evitar problemas de precisión
            val cents = (value * 100).toLong()
            val padded = cents.toString().padStart(3, '0')
            // Reconstruimos el String con el punto manual: "0.50"
            "${padded.dropLast(2)}.${padded.takeLast(2)}"
        }
    }

    var text by remember(value) { mutableStateOf(formattedInitialValue) }

    BaseEditSetting(
        label = label,
        currentText = text,
        defaultText = "0.00", // Forzamos el formato con dos decimales para el valor por defecto
        onReset = {
            onSave(defaultValue)
            text = "0.00" // Reseteamos a "0.00" explícitamente
        }
    ) {
        CmInput(
            value = text,
            onValueChange = { newValue ->
                text = newValue
                onSave(newValue.toSafeDoubleOrNull() ?: 0.0)
            },
            label = "",
            modifier = Modifier.width(180.dp),
            prefix = {
                Text(
                    "$",
                    modifier = Modifier.padding(end = 4.dp),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )
            },
            suffix = { Text("/$unit", style = MaterialTheme.typography.labelSmall) },
            focusRequester = focusRequester,
            nextFocusRequester = nextFocusRequester,
            onDone = onDone
        )
    }
}

/**
 * Contenedor acordeón estilizado como una Card.
 */
@Composable
fun SettingsAccordion(
    title: String,
    defaultExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(defaultExpanded) }

    // 1. Creamos el "peticionador" de vista
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    // 2. Efecto para scrollear cuando se expande
    LaunchedEffect(expanded) {
        if (expanded) {
            // Esperamos un poquito a que la animación de expansión empiece
            delay(100)
            // 3. Pedimos al padre scrollear hasta este componente
            bringIntoViewRequester.bringIntoView()
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            // 4. Vinculamos el modificador
            .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    content = content
                )
            }
        }
    }
}