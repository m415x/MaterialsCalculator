package org.m415x.materialscalculator.ui.screen.settings

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

import org.m415x.materialscalculator.data.repository.SettingsRepository
import org.m415x.materialscalculator.ui.screen.settings.appearance.AppearanceSubScreen
import org.m415x.materialscalculator.ui.screen.settings.db.MaterialsDbScreen
import org.m415x.materialscalculator.ui.screen.settings.global.GlobalParamsSubScreen
import org.m415x.materialscalculator.ui.theme.ContrastMode
import org.m415x.materialscalculator.ui.theme.ThemeMode

/**
 * Pantalla principal de configuración.
 *
 * @param repository El repositorio de configuración.
 * @param currentTheme El tema actual.
 * @param currentContrast El contraste actual.
 * @param onThemeChange La función de cambio de tema.
 * @param onContrastChange La función de cambio de contraste.
 * @param currentSection La sección actual.
 * @param onSectionChange La función de cambio de sección.
 */
@Composable
fun SettingsScreen(
    repository: SettingsRepository, // Inyectamos el repo directo para leer/guardar
    // Estos params siguen viniendo de App.kt para el tema en tiempo real
    currentTheme: ThemeMode,
    currentContrast: ContrastMode,
    currentOutdoorMode: Boolean,
    currentSection: SettingsSection,
    onThemeChange: (ThemeMode) -> Unit,
    onContrastChange: (ContrastMode) -> Unit,
    onOutdoorModeChange: (Boolean) -> Unit,
    // Callback para informar a la App el cambio de título/estado
    onSectionChange: (SettingsSection) -> Unit
) {
    // Cada vez que cambia la sección, avisamos a App.kt
    LaunchedEffect(currentSection) {
        onSectionChange(currentSection)
    }

    AnimatedContent(
        targetState = currentSection,
        label = "SettingsAnimation",
        transitionSpec = {
            if (targetState == SettingsSection.MENU) {
                slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
            } else {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            }
        }
    ) { targetSection ->
        when (targetSection) {
            SettingsSection.MENU -> {
                SettingsMainMenu(onNavigate = onSectionChange)
            }

            SettingsSection.APPEARANCE -> {
                AppearanceSubScreen(
                    currentTheme,
                    currentContrast,
                    currentOutdoorMode,
                    onThemeChange,
                    onContrastChange,
                    onOutdoorModeChange
                )
            }

            SettingsSection.GLOBAL_PARAMS -> {
                GlobalParamsSubScreen(repository)
            }

            SettingsSection.MATERIALS_DB -> {
                MaterialsDbScreen(repository)
            }

            SettingsSection.PRICES -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Próximamente: Precios")
                }
            }
        }
    }
}

/**
 * Item de menú de configuración.
 *
 * @param title El título del item.
 * @param subtitle El subtítulo del item.
 * @param icon El icono del item.
 * @param onClick La función de click.
 */
@Composable
fun SettingsMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, null) },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
        modifier = Modifier.clickable { onClick() }
    )
    HorizontalDivider()
}

/**
 * Item de configuración.
 *
 * @param label Etiqueta del item.
 * @param value Valor del item.
 */
@Composable
fun SettingItem(label: String, value: String) {
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = { Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary) }
    )
}