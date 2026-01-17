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

package org.m415x.materialcalc.domain.model

/**
 * Representa el resultado del cálculo de una losa.
 *
 * @property totalWeightKg Peso total para el presupuesto
 * @property totalMeters Metros lineales totales
 * @property countX Cantidad de varillas en dirección X
 * @property countY Cantidad de varillas en dirección Y
 * @property lengthX Largo de cada varilla X (con ganchos)
 * @property lengthY Largo de cada varilla Y (con ganchos)
 * @property diameterX Diámetro de cada varilla X
 * @property diameterY Diámetro de cada varilla Y
 * @property weightX Peso de varillas en dirección X
 * @property weightY Peso de varillas en dirección Y
 * @property commercialBars12m Cantidad de barras de 12m a comprar
 * @property wasteAmountKg Cuánto del peso es desperdicio
 * @property suggestedMesh Esquema recomendado
 * @property meshPanelsNeeded Cantidad de paneles de 2x5m necesarios
 * @property percentageIronWaste Porcentaje de hormigón desperdiciado
 * @property volumeConcreteM3 Volumen de hormigón en m³
 * @property cementKg Cantidad de cemento en kg
 * @property sandM3 Cantidad de arena en m³
 * @property gravelM3 Cantidad de piedra en m³
 * @property waterLiters Cantidad de agua en litros
 * @property cementBagKg Peso de la bolsa de cemento
 * @property percentageConcreteWaste Porcentaje de hormigón desperdiciado
 */
data class SlabResult(
    val totalWeightKg: Double,
    val totalMeters: Double,
    val countX: Int,
    val countY: Int,
    val lengthX: Double,
    val lengthY: Double,
    val diameterX: Double,
    val diameterY: Double,
    val weightX: Double,
    val weightY: Double,
    val commercialBars12m: Int,
    val wasteAmountKg: Double,
    val suggestedMesh: String? = null,
    val meshPanelsNeeded: Int? = null,
    val percentageIronWaste: Double,
    // Materiales de Hormigón
    val volumeConcreteM3: Double,
    val cementKg: Double,
    val sandM3: Double,
    val gravelM3: Double,
    val waterLiters: Double,
    val cementBagKg: Int,
    val percentageConcreteWaste: Double
)