package org.m415x.materialscalculator.ui.screen.settings.db

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

import org.m415x.materialscalculator.data.repository.SettingsRepository

// Enum para las pestañas
enum class MaterialTab(val title: String, val icon: ImageVector) {
    BRICKS("Ladrillos", Icons.Default.GridView),
    IRONS("Hierros", Icons.Default.Menu), // O Icons.Default.LinearScale
    RECIPES("Mezclas", Icons.Default.Science)
}

@Composable
fun MaterialsDbScreen(repository: SettingsRepository) {
    var currentTab by remember { mutableStateOf(MaterialTab.BRICKS) }

    Scaffold(
        topBar = {
            // Barra de Pestañas
            PrimaryTabRow(selectedTabIndex = currentTab.ordinal) {
                MaterialTab.entries.forEach { tab ->
                    Tab(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        text = { Text(tab.title) },
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