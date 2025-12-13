package org.m415x.materialscalculator.domain.usecase

import org.m415x.materialscalculator.domain.common.calculateWetMaterials
import org.m415x.materialscalculator.domain.model.*
import org.m415x.materialscalculator.ui.common.roundToDecimals
import kotlin.math.ceil

/**
 * Calcula los materiales para un volumen de hormigón.
 *
 * @param repository Repositorio de materiales.
 */
class CalculateWallUseCase {

    /**
     * Calcula los materiales para un volumen de hormigón.
     *
     * @param largoMuroMetros Largo del muro en metros.
     * @param altoMuroMetros Alto del muro en metros.
     * @param props Tipo de ladrillo.
     * @param aberturas Lista de aberturas en el muro.
     * @param bolsaCementoKg Peso de la bolsa de cemento en kg.
     * @param bolsaCalKg Peso de la bolsa de cal en kg.
     * @return Resultado del cálculo.
     */
    operator fun invoke(
        largoMuroMetros: Double,
        altoMuroMetros: Double,
        props: PropiedadesLadrillo,
        dosis: DosificacionMortero,
        aberturas: List<Abertura>,
        bolsaCementoKg: Int,
        bolsaCalKg: Int,
        desperdicioLadrillos: Double,
        desperdicioMortero: Double
    ): ResultadoMuro {

        // ============================================================
        // 1. GEOMETRÍA (ÁREA NETA)
        // ============================================================
        val areaMuro = largoMuroMetros * altoMuroMetros

        val areaAberturas = aberturas.sumOf { it.anchoMetros * it.altoMetros * it.cantidad }

        if (areaAberturas > areaMuro) {
            throw IllegalArgumentException(
                "El área de aberturas (${areaAberturas.roundToDecimals(2)} m²) supera al área del muro."
            )
        } else if (areaAberturas == areaMuro) {
            throw IllegalArgumentException(
                "El área de aberturas (${areaAberturas.roundToDecimals(2)} m²) es igual a área del muro."
            )
        }

        val areaNeta = (areaMuro - areaAberturas).coerceAtLeast(0.0)

        // ============================================================
        // 2. CÁLCULO DE LADRILLOS (Unidades Físicas)
        // ============================================================
        // Fórmula: 1 / ((Largo + Junta) * (Alto + Junta))
        val supLadrilloConJunta = (props.largoUnidad + props.espesorJunta) * (props.altoUnidad + props.espesorJunta)
        val ladrillosPorM2 = 1.0 / supLadrilloConJunta

        // Cantidad Teórica
        val totalLadrillosTeorico = areaNeta * ladrillosPorM2

        // Cantidad Real (con desperdicio)
        val cantidadRealLadrillos = ceil(totalLadrillosTeorico * (1 + desperdicioLadrillos)).toInt()

        // ============================================================
        // 3. CÁLCULO DE MORTERO (Mezcla Húmeda)
        // ============================================================

        // A. Volumen Geométrico del Muro (Área * Espesor)
        val volumenParedM3 = areaNeta * props.anchoMuro

        // B. Volumen ocupado por Ladrillos (Sin desperdicio, ocupación física real)
        val volumenLadrillosSolidos = totalLadrillosTeorico * (props.largoUnidad * props.altoUnidad * props.anchoMuro)

        // C. Volumen Geométrico de la Mezcla (Diferencia)
        val volumenMorteroGeo = (volumenParedM3 - volumenLadrillosSolidos).coerceAtLeast(0.0)

        // D. ¡MOTOR DE CÁLCULO! (Aplica desperdicio 15% y calcula materiales)
        val matsMortero = calculateWetMaterials(
            volumenM3 = volumenMorteroGeo,
            receta = dosis,
            desperdicio = desperdicioMortero,
            pesoBolsaCemento = bolsaCementoKg,
            pesoBolsaCal = bolsaCalKg
        )

        // ============================================================
        // 4. RESULTADO FINAL
        // ============================================================
        return ResultadoMuro(
            areaNetaM2 = areaNeta,

            // Ladrillos
            cantidadLadrillos = cantidadRealLadrillos,
            porcentajeDesperdicioLadrillos = desperdicioLadrillos,

            // Mortero (Viene del motor)
            morteroM3 = volumenMorteroGeo * (1 + desperdicioMortero),
            cementoKg = matsMortero.cementoKg,
            calKg = matsMortero.calKg,
            arenaTotalM3 = matsMortero.arenaM3,
            aguaLitros = matsMortero.aguaLitros,
            porcentajeDesperdicioMortero = desperdicioMortero,

            // Configuración
            proporcionMezcla = dosis.dosificacionMezcla,
            bolsaCementoKg = bolsaCementoKg,
            bolsaCalKg = bolsaCalKg
        )
    }
}