package org.m415x.materialscalculator.data.repository

import org.m415x.materialscalculator.domain.repository.MaterialRepository
import org.m415x.materialscalculator.domain.model.*

class StaticMaterialRepository : MaterialRepository {

    /**
     * Base de datos interna de dosificaciones de hormigones.
     * Aquí es donde "guardamos" las tablas que buscamos.
     *
     * @property hormigonDB Base de datos de dosificaciones de hormigones.
     */
    private val hormigonDB = mapOf(

        // H8: Hormigón de limpieza (pobre). Poco cemento.
        // Usos: Contrapisos, carpetas, nivelación.
        TipoHormigon.H8 to DosificacionHormigon(
            dosificacionMezcla = "1:3:5 (Cem:Arena:Piedra)",
            cementoKg = 180.0,
            arenaM3 = 0.55,
            piedraM3 = 0.9,
            relacionAgua = 0.6
        ),

        // H13: Hormigón simple (no estructural o estructuras muy livianas).
        TipoHormigon.H13 to DosificacionHormigon(
            dosificacionMezcla = "1:3:4 (Cem:Arena:Piedra)",
            cementoKg = 260.0,
            arenaM3 = 0.63,
            piedraM3 = 0.84,
            relacionAgua = 0.5
        ),

        // H17: Hormigón estándar para viviendas pequeñas (bases, encadenados).
        TipoHormigon.H17 to DosificacionHormigon(
            dosificacionMezcla = "1:3:3 (Cem:Arena:Piedra)",
            cementoKg = 300.0,
            arenaM3 = 0.67,
            piedraM3 = 0.67,
            relacionAgua = 0.5
        ),

        // H21: Hormigón ESTRUCTURAL estándar (Losas, vigas, columnas).
        // Es el más utilizado en construcción tradicional.
        TipoHormigon.H21 to DosificacionHormigon(
            dosificacionMezcla = "1:2:3 (Cem:Arena:Piedra)",
            cementoKg = 350.0,
            arenaM3 = 0.55,
            piedraM3 = 0.75,
            relacionAgua = 0.45
        ),

        // H25: Hormigón de alta resistencia.
        // Usos: Columnas muy cargadas, estructuras importantes.
        TipoHormigon.H25 to DosificacionHormigon(
            dosificacionMezcla = "1:2:2 (Cem:Arena:Piedra)",
            cementoKg = 380.0,
            arenaM3 = 0.5,
            piedraM3 = 0.75,
            relacionAgua = 0.42
        ),

        // H30: Hormigón de muy alta resistencia.
        // NOTA: Difícil de lograr a mano sin aditivos fluidificantes por la poca agua.
        TipoHormigon.H30 to DosificacionHormigon(
            dosificacionMezcla = "1:1:2 (Cem:Arena:Piedra)",
            cementoKg = 430.0,
            arenaM3 = 0.45,
            piedraM3 = 0.70,
            relacionAgua = 0.40
        )
    )

    /**
     * Mapeo de dimensiones y juntas sugeridas.
     * Se asume colocación estándar (no panderete/canto, salvo especificación).
     *
     * @property ladrilloDB Mapeo de dimensiones y juntas sugeridas.
     */
    private val ladrilloDB = mapOf(
        // // --- Ladrillos Macizos ---
        // Junta más gruesa (1.5 cm a 2.0 cm)
        TipoLadrillo.COMUN to PropiedadesLadrillo(
            anchoMuro = 0.12,
            altoUnidad = 0.05,
            largoUnidad = 0.25,
            espesorJunta = 0.015
        ),
        TipoLadrillo.LADRILLON to PropiedadesLadrillo(
            anchoMuro = 0.18,
            altoUnidad = 0.05,
            largoUnidad = 0.25,
            espesorJunta = 0.015
        ),

        // --- Cerámicos HUECOS (No Portantes) ---
        // Altura 18 cm. Junta suele ser 1.5 cm aprox.
        TipoLadrillo.HUECO_8 to PropiedadesLadrillo(
            anchoMuro = 0.08,
            altoUnidad = 0.18,
            largoUnidad = 0.33,
            espesorJunta = 0.015
        ),
        TipoLadrillo.HUECO_12 to PropiedadesLadrillo(
            anchoMuro = 0.12,
            altoUnidad = 0.18,
            largoUnidad = 0.33,
            espesorJunta = 0.015
        ),
        TipoLadrillo.HUECO_18 to PropiedadesLadrillo(
            anchoMuro = 0.18,
            altoUnidad = 0.18,
            largoUnidad = 0.33,
            espesorJunta = 0.015
        ),

        // --- Cerámicos PORTANTES ---
        // Altura 19 cm. Se usan con menos mezcla (1 cm) para modulación perfecta de 20cm.
        TipoLadrillo.PORTANTE_12 to PropiedadesLadrillo(
            anchoMuro = 0.12,
            altoUnidad = 0.19,
            largoUnidad = 0.33,
            espesorJunta = 0.012
        ),
        TipoLadrillo.PORTANTE_18 to PropiedadesLadrillo(
            anchoMuro = 0.18,
            altoUnidad = 0.19,
            largoUnidad = 0.33,
            espesorJunta = 0.012
        ),

        // --- Bloques de Hormigón ---
        // Medidas estándar Argentina: Largo 39, Alto 19.
        // Bloques de Hormigón: Junta fina (1.0 cm)
        TipoLadrillo.BLOQUE_10 to PropiedadesLadrillo(
            anchoMuro = 0.1,
            altoUnidad = 0.19,
            largoUnidad = 0.39,
            espesorJunta = 0.01
        ),
        TipoLadrillo.BLOQUE_13 to PropiedadesLadrillo(
            anchoMuro = 0.13,
            altoUnidad = 0.19,
            largoUnidad = 0.39,
            espesorJunta = 0.01
        ),
        TipoLadrillo.BLOQUE_15 to PropiedadesLadrillo(
            anchoMuro = 0.15,
            altoUnidad = 0.19,
            largoUnidad = 0.39,
            espesorJunta = 0.01
        ),
        TipoLadrillo.BLOQUE_20 to PropiedadesLadrillo(
            anchoMuro = 0.2,
            altoUnidad = 0.19,
            largoUnidad = 0.39,
            espesorJunta = 0.01
        )
    )

    /**
     * Peso en Kg por metro lineal según tabla estándar.
     *
     * @property pesosHierro Peso en Kg por metro lineal según tabla estándar.
     */
    private val pesosHierro = mapOf(
        DiametroHierro.HIERRO_6 to 0.222,
        DiametroHierro.HIERRO_8 to 0.395,
        DiametroHierro.HIERRO_10 to 0.617,
        DiametroHierro.HIERRO_12 to 0.888,
        DiametroHierro.HIERRO_16 to 1.580
    )

    /**
     * Base de datos de mezclas (Valores estándar por m3 de mortero).
     *
     * @property mezclaCalReforzada Base de datos de mezclas (Valores estándar por m3 de mortero).
     */
    private val mezclaCalReforzada = DosificacionMortero(
        dosificacionMezcla = "1/4:1:3 (Cem:Cal:Arena)",
        cementoKg = 210.0,
        calKg = 150.0,
        arenaM3 = 1.05,
        relacionAgua = 0.6
    )

    /**
     * Dosificación para 1 m3 de Revoque Grueso (1/4 Cemento : 1 Cal : 3 Arena).
     *
     * @property recetaGrueso Dosificación para 1 m3 de Revoque Grueso (1/4 Cemento : 1 Cal : 3 Arena).
     */
    private val mezclaCementoArena = DosificacionMortero(
        dosificacionMezcla = "1:3 (Cem:Arena)",
        cementoKg = 350.0,
        calKg = 0.0,
        arenaM3 = 1.1,
        relacionAgua = 0.5
    )

    /**
     * Dosificación para 1 m3 de Revoque Grueso (1/4 Cemento : 1 Cal : 3 Arena).
     *
     * @property recetaGrueso Dosificación para 1 m3 de Revoque Grueso (1/4 Cemento : 1 Cal : 3 Arena).
     */
    private val recetaGrueso = DosificacionMortero(
        dosificacionMezcla = "1/4:1:3 (Cem:Cal:Arena)",
        cementoKg = 75.0,  // Aprox 3 bolsas por m3 (es una mezcla "bastarda", lleva menos cemento que un concreto)
        calKg = 160.0,     // Mucha cal para plasticidad
        arenaM3 = 1.1,      // Arena común
        relacionAgua = 0.6
    )

    /**
     * Dosificación para 1 m3 de Revoque Fino Tradicional (1/8 Cemento : 1 Aérea : 2 Arena Fina).
     *
     * @property recetaFino Dosificación para 1 m3 de Revoque Fino Tradicional (1/8 Cemento : 1 Aérea : 2 Arena Fina).
     */
    private val recetaFino = DosificacionMortero(
        dosificacionMezcla = "1/8:1:2 (Cem:Cal:Arena)",
        cementoKg = 30.0,  // Muy poco, solo para ligar
        calKg = 250.0,     // Pura cal aérea
        arenaM3 = 1.0,      // Arena fina (voladora)
        relacionAgua = 0.5
    )

    /**
     * Dosificación para 1 m3 de hormigón.
     *
     * @property getDosificacionHormigon Dosificación para 1 m3 de hormigón.
     */
    override fun getDosificacionHormigon(tipo: TipoHormigon) = hormigonDB[tipo]

    /**
     * Propiedades de un ladrillo.
     *
     * @property getPropiedadesLadrillo Propiedades de un ladrillo.
     */
    override fun getPropiedadesLadrillo(tipo: TipoLadrillo) = ladrilloDB[tipo]

    /**
     * Dosificación para 1 m3 de mortero.
     *
     * @property getDosificacionMortero Dosificación para 1 m3 de mortero.
     */
    override fun getDosificacionMortero(tipo: TipoLadrillo): DosificacionMortero {
        return when (tipo) {
            TipoLadrillo.BLOQUE_10,
            TipoLadrillo.BLOQUE_13,
            TipoLadrillo.BLOQUE_15,
            TipoLadrillo.BLOQUE_20 -> mezclaCementoArena
            else -> mezclaCalReforzada
        }
    }

    /**
     * Peso en Kg por metro lineal según tabla estándar.
     *
     * @property getPesoHierroPorMetro Peso en Kg por metro lineal según tabla estándar.
     */
    override fun getPesoHierroPorMetro(diametro: DiametroHierro): Double {
        return pesosHierro[diametro] ?: 0.0
    }

    fun getRecetaGrueso() = recetaGrueso
    fun getRecetaFino() = recetaFino
}