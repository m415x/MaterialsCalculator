package org.m415x.materialscalculator.ui.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Construction
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
import androidx.compose.ui.unit.dp

import org.m415x.materialscalculator.ui.common.NumericInput

/**
 * Menú principal de configuración.
 *
 * @param onNavigate La función de navegación.
 */
@Composable
fun SettingsMainMenu(onNavigate: (SettingsSection) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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