package org.m415x.materialscalculator.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Build // Usaremos íconos genéricos por ahora
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.m415x.materialscalculator.ui.common.MenuCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onConcreteClick: () -> Unit,
    onWallClick: () -> Unit,
    onStructureClick: () -> Unit // Callback para navegar
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Scroll por si la pantalla es chica
    ) {
        Text(
            text = "¿Qué vas a construir hoy?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 1. Botón Hormigón
        MenuCard(
            title = "Hormigón / Concreto",
            description = "Losas, contrapisos y carpetas.",
            icon = Icons.Default.Menu, // Puedes buscar íconos más específicos luego
            onClick = onConcreteClick
        )

        // 2. Botón Muros
        MenuCard(
            title = "Muros y Paredes",
            description = "Ladrillos y mortero.",
            icon = Icons.Default.Home,
            onClick = onWallClick
        )

        // 3. Botón Estructuras
        MenuCard(
            title = "Armaduras",
            description = "Vigas y columnas. \nCálculo de hormigón.",
            icon = Icons.Default.Build,
            onClick = onStructureClick
        )
    }
}