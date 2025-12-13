package org.m415x.materialscalculator.ui.screen.settings.appearance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import org.m415x.materialscalculator.ui.common.AppInput
import org.m415x.materialscalculator.ui.theme.ContrastMode
import org.m415x.materialscalculator.ui.theme.ThemeMode

/**
 * Pantalla de personalización visual.
 *
 * @param currentTheme El tema actual.
 * @param currentContrast El contraste actual.
 * @param onThemeChange La función de cambio de tema.
 * @param onContrastChange La función de cambio de contraste.
 */
@Composable
fun AppearanceSubScreen(
    currentTheme: ThemeMode,
    currentContrast: ContrastMode,
    currentOutdoorMode: Boolean,
    onThemeChange: (ThemeMode) -> Unit,
    onContrastChange: (ContrastMode) -> Unit,
    onOutdoorModeChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Personalización Visual", style = MaterialTheme.typography.titleMedium)

        Column {
            // Selector de Tema (Claro / Oscuro / Sistema)
            ThemeModeSelector(currentTheme, onThemeChange)

            Spacer(Modifier.height(16.dp))

            // Switch de Alto Contraste
            ContrastModeSwitch(currentContrast, onContrastChange)

            Spacer(Modifier.height(16.dp))

            // Switch Modo Exterior
            OutdoorModeSwitch(currentOutdoorMode, onOutdoorModeChange)
        }

        Text(
            text = "El modo de alto contraste ayuda a mejorar la visibilidad bajo la luz directa del sol.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

/**
 * Selector de tema.
 *
 * @param currentTheme Tema actual.
 * @param onThemeChange Acción al cambiar el tema.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeModeSelector(currentTheme: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text("Tema de la aplicación") },
        supportingContent = {
            Text("Define si usar modo claro, oscuro o del sistema.", style = MaterialTheme.typography.labelSmall)
        },
        trailingContent = {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.width(120.dp)
            ) {
                AppInput(
                    value = currentTheme.name,
                    onValueChange = { },
                    label = "",
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

/**
 * Switch de contraste.
 *
 * @param currentContrast Contraste actual.
 * @param onContrastChange Acción al cambiar el contraste.
 */
@Composable
fun ContrastModeSwitch(currentContrast: ContrastMode, onContrastChange: (ContrastMode) -> Unit) {
    val isHighContrast = currentContrast == ContrastMode.HighContrast

    ListItem(
        headlineContent = { Text("Modo Alto Contraste") },
        supportingContent = {
            Text(
                "Mejora la legibilidad en condiciones de mucha luz exterior.",
                style = MaterialTheme.typography.labelSmall
            )
        },
        leadingContent = { Icon(Icons.Default.Contrast, null) }, // Icono de Sol
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
fun OutdoorModeSwitch(isChecked: Boolean, onDataChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text("Modo Exterior") },
        supportingContent = {
            Text(
                "Fuerza el brillo al máximo para ver bajo el sol.",
                style = MaterialTheme.typography.labelSmall
            )
        },
        leadingContent = { Icon(Icons.Default.WbSunny, null) }, // Icono de Sol
        trailingContent = {
            Switch(
                checked = isChecked,
                onCheckedChange = onDataChange
            )
        }
    )
}