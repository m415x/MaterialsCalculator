package org.m415x.materialscalculator.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.vectorResource

import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.* // importar iconos
import org.m415x.materialscalculator.ui.common.MenuCard

/**
 * Pantalla principal de la calculadora.
 * 
 * @param onConcreteClick Acción al hacer clic en el botón de hormigón.
 * @param onWallClick Acción al hacer clic en el botón de muros.
 * @param onStructureClick Acción al hacer clic en el botón de estructuras.
 * @param onPlasterClick Acción al hacer clic en el botón de revoques.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onConcreteClick: () -> Unit,
    onWallClick: () -> Unit,
    onStructureClick: () -> Unit,
    onPlasterClick: () -> Unit
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
            icon = vectorResource(Res.drawable.ic_concrete),
            onClick = onConcreteClick
        )

        // 2. Botón Muros
        MenuCard(
            title = "Muros y Paredes",
            description = "Ladrillos y mortero.",
            icon = vectorResource(Res.drawable.ic_wall),
            onClick = onWallClick
        )

        // 3. Botón Estructuras
        MenuCard(
            title = "Armaduras",
            description = "Vigas y columnas.\nCálculo de hormigón.",
            icon = vectorResource(Res.drawable.ic_structure),
            onClick = onStructureClick
        )

        // 4. Botón Revoques (enlucidos)
        MenuCard(
            title = "Revoques / Enlucidos",
            description = "Calcula grueso y fino para tus paredes.",
            icon = vectorResource(Res.drawable.ic_plaster),
            onClick = onPlasterClick
        )
    }
}