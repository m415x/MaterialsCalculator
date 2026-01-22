package org.m415x.materialcalc.domain.service

import org.m415x.materialcalc.domain.model.LaborPrice
import org.m415x.materialcalc.domain.model.MaterialPrice

class CostCalculator(
    private val materialPrices: List<MaterialPrice>,
    private val laborPrices: List<LaborPrice>
) {
    /**
     * Calcula el costo de un material buscando por nombre aproximado o ID.
     * En una app real usaríamos IDs estrictos, pero aquí mantenemos la flexibilidad
     * de buscar por nombre ("Cemento", "Arena") como fallback.
     */
    fun getMaterialCost(nameOrId: String, quantity: Double): Double {
        if (quantity <= 0) return 0.0
        
        // 1. Buscar por ID exacto (si tuviéramos)
        var item = materialPrices.find { it.id == nameOrId }
        
        // 2. Si no, buscar por coincidencia de nombre (flexible)
        if (item == null) {
            item = materialPrices.find { it.name.contains(nameOrId, ignoreCase = true) }
        }

        return if (item != null) {
            // Aquí asumimos que la unidad del precio coincide con la unidad de la cantidad
            // O hacemos una conversión básica si es necesario (ej: bolsa vs kg)
            // Por simplicidad en este paso, asumimos que el precio viene normalizado
            // o que el usuario cargó el precio por la unidad que usamos (ej: precio por bolsa)
            quantity * item.price
        } else {
            0.0
        }
    }
    
    /**
     * Calcula costo de material sabiendo que el precio puede estar en bolsas o kg.
     */
    fun getBagMaterialCost(nameKeyword: String, bags: Double, kgTotal: Double): Double {
        val item = materialPrices.find { it.name.contains(nameKeyword, ignoreCase = true) } ?: return 0.0
        
        return if (item.unit.contains("bolsa", ignoreCase = true)) {
            bags * item.price
        } else if (item.unit.contains("kg", ignoreCase = true)) {
            kgTotal * item.price
        } else {
            // Fallback: asumimos precio por unidad principal (bolsa)
            bags * item.price
        }
    }

    fun getLaborCost(nameKeyword: String, quantity: Double): Double {
        val item = laborPrices.find { it.name.contains(nameKeyword, ignoreCase = true) }
        return if (item != null) {
            quantity * item.price
        } else {
            0.0
        }
    }
}