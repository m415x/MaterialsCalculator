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

package org.m415x.materialcalc.ui.screen.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.vectorResource

import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.* // importar iconos
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.ui.common.display.MenuCard

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
fun CalculatorMenuScreen(
    onConcreteClick: () -> Unit,
    onWallClick: () -> Unit,
    onStructureClick: () -> Unit,
    onPlasterClick: () -> Unit
) {
    val subtitle = stringResource(Res.string.home_subtitle)
    val concrete = stringResource(Res.string.home_category_concrete)
    val concreteDesc = stringResource(Res.string.home_category_concrete_desc)
    val wall = stringResource(Res.string.home_category_wall)
    val wallDesc = stringResource(Res.string.home_category_wall_desc)
    val structure = stringResource(Res.string.home_category_structure)
    val structureDesc = stringResource(Res.string.home_category_structure_desc)
    val plaster = stringResource(Res.string.home_category_plaster)
    val plasterDesc = stringResource(Res.string.home_category_plaster_desc)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Scroll por si la pantalla es chica
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        // 1. Botón Hormigón
        MenuCard(
            title = concrete,
            description = concreteDesc,
            icon = vectorResource(Res.drawable.ic_concrete),
            onClick = onConcreteClick
        )

        // 2. Botón Muros
        MenuCard(
            title = wall,
            description = wallDesc,
            icon = vectorResource(Res.drawable.ic_wall),
            onClick = onWallClick
        )

        // 3. Botón Estructuras
        MenuCard(
            title = structure,
            description = structureDesc,
            icon = vectorResource(Res.drawable.ic_structure),
            onClick = onStructureClick
        )

        // 4. Botón Revoques (enlucidos)
        MenuCard(
            title = plaster,
            description = plasterDesc,
            icon = vectorResource(Res.drawable.ic_plaster),
            onClick = onPlasterClick
        )
    }
}