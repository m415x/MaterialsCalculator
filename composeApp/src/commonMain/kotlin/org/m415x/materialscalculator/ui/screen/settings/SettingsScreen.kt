package org.m415x.materialscalculator.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import org.m415x.materialscalculator.ui.common.AppInput

import org.m415x.materialscalculator.ui.theme.*

@Composable
fun SettingsScreen(
    currentTheme: ThemeMode,
    currentContrast: ContrastMode,
    onThemeChange: (ThemeMode) -> Unit,
    onContrastChange: (ContrastMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Apariencia", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // 1. SWITCH DE MODO CLARO/OSCURO (Dropdown para System/Light/Dark)
        ThemeModeSelector(currentTheme, onThemeChange)

        // 2. SWITCH DE CONTRASTE (Switch para Standard/HighContrast)
        ContrastModeSwitch(currentContrast, onContrastChange)

/*
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        Text("Configuración General", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Ejemplo de lo que pediste
        SettingItem("Peso Bolsa Cemento", "25 kg")
        SettingItem("Peso Bolsa Cal", "25 kg")
        SettingItem("Volumen Balde", "10 Litros")
        SettingItem("Volumen Carretilla", "70 Litros")

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text("Editor de Materiales", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Edit, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Editar Tipos de Ladrillos")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Edit, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Editar Dosificaciones Hormigón")
        }

 */
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeModeSelector(currentTheme: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text("Tema de la aplicación") },
        supportingContent = { Text("Define si usar modo claro, oscuro o del sistema.") },
        trailingContent = {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.width(120.dp)
            ) {
                AppInput(
                    value = currentTheme.name,
                    onValueChange = { },
                    label = "Theme",
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },

                    // Colores del menu
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),

                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ThemeMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.name) },
                            onClick = {
                                onThemeChange(mode) // Llama al callback de App.kt
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun ContrastModeSwitch(currentContrast: ContrastMode, onContrastChange: (ContrastMode) -> Unit) {
    val isHighContrast = currentContrast == ContrastMode.HighContrast

    ListItem(
        headlineContent = { Text("Modo Alto Contraste") },
        supportingContent = { Text("Mejora la legibilidad en condiciones de mucha luz exterior.") },
        trailingContent = {
            Switch(
                checked = isHighContrast,
                onCheckedChange = { isChecked ->
                    onContrastChange(if (isChecked) ContrastMode.HighContrast else ContrastMode.Standard)
                }
            )
        }
    )
}

@Composable
fun SettingItem(label: String, value: String) {
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = { Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary) }
    )
}
