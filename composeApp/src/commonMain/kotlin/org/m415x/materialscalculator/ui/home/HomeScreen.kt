package org.m415x.materialscalculator.ui.home

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
import org.m415x.materialscalculator.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit // Callback para navegar
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculadora de Materiales") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                description = "Calcula losas, contrapisos y carpetas.",
                icon = Icons.Default.Menu, // Puedes buscar íconos más específicos luego
                onClick = { onNavigate(Screen.Hormigon) }
            )

            // 2. Botón Muros
            MenuCard(
                title = "Muros y Paredes",
                description = "Ladrillos y mortero para tus paredes.",
                icon = Icons.Default.Home,
                onClick = { onNavigate(Screen.Muro) }
            )

            // 3. Botón Estructuras
            MenuCard(
                title = "Vigas y Columnas",
                description = "Cálculo de hormigón y armadura (hierro).",
                icon = Icons.Default.Build,
                onClick = { onNavigate(Screen.Estructura) }
            )
        }
    }
}