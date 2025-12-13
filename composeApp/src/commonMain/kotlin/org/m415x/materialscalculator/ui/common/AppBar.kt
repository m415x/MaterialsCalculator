package org.m415x.materialscalculator.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.WbSunny     // Sol Relleno
import androidx.compose.material.icons.outlined.WbSunny   // Sol Contorno
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.m415x.materialscalculator.ui.navigation.BottomTab

/**
 * AppBar reutilizable para la aplicación.
 *
 * @param title Título de la AppBar.
 * @param showBackButton Indica si se muestra el botón de volver.
 * @param onBack Acción a realizar al presionar el botón de volver.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    showBackButton: Boolean,
    onBack: () -> Unit,
    isOutdoorMode: Boolean,
    onToggleOutdoorMode: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onToggleOutdoorMode) {
                // Alternamos ícono y color según el estado
                if (isOutdoorMode) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = "Desactivar Modo Exterior",
                        tint = Color(0xFFFFB300) // Amarillo Sol (Amber 600)
//                      tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.WbSunny,
                        contentDescription = "Activar Modo Exterior",
                        // Usamos el color por defecto del tema (blanco/negro)
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

/**
 * BottomBar reutilizable para la aplicación.
 *
 * @param currentTab Tab actual seleccionado.
 * @param onTabSelected Acción a realizar al seleccionar un tab.
 * @param modifier Modificador para personalizar el BottomBar.
 */
@Composable
fun AppBottomBar(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar (
        modifier = modifier,
    ){
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                icon = { Icon(tab.icon, contentDescription = tab.title) },
                label = { Text(tab.title) },
                selected = currentTab == tab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}