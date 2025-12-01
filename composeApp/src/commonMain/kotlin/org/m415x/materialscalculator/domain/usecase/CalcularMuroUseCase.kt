package org.m415x.materialscalculator.domain.usecase

import org.m415x.materialscalculator.domain.repository.MaterialRepository
import org.m415x.materialscalculator.domain.model.*
import kotlin.math.ceil

/**
 * Calcula los materiales para un volumen de hormigón.
 */
class CalcularMuroUseCase(private val repository: MaterialRepository) {

    // Función 'invoke' permite llamar a la clase como si fuera una función
    operator fun invoke(
        largoMuroMetros: Double,
        altoMuroMetros: Double,
        tipo: TipoLadrillo,
        aberturas: List<Abertura>,
        porcentajeDesperdicio: Double = 0.05 // 5% de desperdicio por roturas (Estándar)
    ): ResultadoMuro {

        // 1. Obtener datos del Repo
        val props = repository.getPropiedadesLadrillo(tipo)
            ?: throw IllegalArgumentException("Tipo no soportado")

        // Obtenemos la dosificación adecuada para este ladrillo (con o sin cal)
        val dosificacion = repository.getDosificacionMortero(tipo)
            ?: throw IllegalArgumentException("Dosificación no encontrada")

        // 2. Calcular Áreas
        val areaPared = largoMuroMetros * altoMuroMetros
        val areaAberturas = aberturas.sumOf { it.anchoMetros * it.altoMetros }
        val areaNeta = (areaPared - areaAberturas).coerceAtLeast(0.0)

        // 3. Calcular Ladrillos por m2
        // Fórmula: 1 / ((Largo + Junta) * (Alto + Junta))
        val superficieLadrilloConJunta = (props.largoUnidad + props.espesorJunta) * (props.altoUnidad + props.espesorJunta)
        val ladrillosPorM2 = 1.0 / superficieLadrilloConJunta

        var totalLadrillos = areaNeta * ladrillosPorM2
        // Sumamos desperdicio
        totalLadrillos += totalLadrillos * porcentajeDesperdicio

        // 4. Calcular Mortero (Mezcla)
        // Método geométrico: Volumen Pared - Volumen Ladrillos (sin junta)
        // Volumen de 1 m2 de pared = 1 * 1 * espesor
        val volumenParedM3 = areaNeta * props.anchoMuro

        // Volumen que ocupan SOLO los ladrillos (sin mezcla) en ese espacio
        // Cantidad Real (sin desperdicio) * VolUnitario
        val cantidadRealLadrillos = areaNeta * ladrillosPorM2
        val volumenLadrillosM3 = cantidadRealLadrillos * (props.largoUnidad * props.altoUnidad * props.anchoMuro)

        var volumenMortero = volumenParedM3 - volumenLadrillosM3
        // Sumamos un desperdicio mayor al mortero (se cae, se seca, etc -> 10% a 20%)
        volumenMortero += volumenMortero * 0.15 // 15% desperdicio mezcla

        // 5. Calcular Materiales finos
        // Cemento
        val cementoTotalKg = volumenMortero * dosificacion.cementoKg
        // Asumimos bolsa de 25kg (puedes parametrizarlo igual que en hormigón)
        val cementoBolsas = ceil(cementoTotalKg / 25.0).toInt()

        // Cal (Si la dosificación tiene calKg > 0)
        val calTotalKg = volumenMortero * dosificacion.calKg
        // Asumimos bolsa de 25kg para la cal hidratada estándar
        val calBolsas = ceil(calTotalKg / 25.0).toInt()

        // Arena
        val arenaTotal = volumenMortero * dosificacion.arenaM3

        // Agua
        val aguaTotalLitros = cementoTotalKg * dosificacion.relacionAguaCemento

        return ResultadoMuro(
            areaNetaM2 = areaNeta,
            cantidadLadrillos = ceil(totalLadrillos).toInt(),
            morteroM3 = volumenMortero,
            cementoBolsas = cementoBolsas,
            calBolsas = calBolsas,
            arenaTotalM3 = arenaTotal,
            aguaLitros = aguaTotalLitros
        )
    }
}