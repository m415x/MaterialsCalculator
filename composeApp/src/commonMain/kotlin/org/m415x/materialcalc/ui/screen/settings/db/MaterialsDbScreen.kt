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

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.settings_db_tab_bricks
import materialscalculator.composeapp.generated.resources.settings_db_tab_irons
import materialscalculator.composeapp.generated.resources.settings_db_tab_recipes
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.domain.model.AppSettingsState

// Enum para las pestañas
enum class MaterialTab(val titleRes: StringResource, val icon: ImageVector) {
    BRICKS(Res.string.settings_db_tab_bricks, Icons.Default.Dashboard),
    IRONS(Res.string.settings_db_tab_irons, Icons.Default.Grid3x3),
    RECIPES(Res.string.settings_db_tab_recipes, Icons.Default.Science)
}

@Composable
fun MaterialsDbScreen(repository: SettingsRepository, appSettings: AppSettingsState) {
    var currentTab by remember { mutableStateOf(MaterialTab.BRICKS) }

    Scaffold(
        topBar = {
            // Barra de Pestañas
            PrimaryTabRow(selectedTabIndex = currentTab.ordinal) {
                MaterialTab.entries.forEach { tab ->
                    Tab(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        text = { Text(stringResource(tab.titleRes)) },
                        icon = { Icon(tab.icon, null) }
                    )
                }
            }
        }
        // El FAB se maneja dentro de cada contenido si la acción es distinta,
        // o aquí si es genérica (pero el "OnClick" cambia).
        // Para simplificar, lo pasamos a los hijos.
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            // Contenido cambiante con animación
            AnimatedContent(targetState = currentTab) { tab ->
                when (tab) {
                    MaterialTab.BRICKS -> BricksTabContent(repository)
                    MaterialTab.IRONS -> IronsTabContent(repository)
                    MaterialTab.RECIPES -> RecipesTabContent(repository)
                }
            }
        }
    }
}