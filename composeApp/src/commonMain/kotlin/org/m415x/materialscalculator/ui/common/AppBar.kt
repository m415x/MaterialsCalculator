package org.m415x.materialscalculator.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    onBack: () -> Unit
) {
    TopAppBar(
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