# Material Calc

**Material Calc** es una herramienta profesional de cálculo de materiales de construcción desarrollada con **Kotlin Multiplatform** y **Compose Multiplatform**. Permite a profesionales y aficionados de la construcción estimar con precisión insumos para hormigón, mampostería y estructuras de hierro.

---

## Plataformas Soportadas

Gracias al poder de KMP, esta aplicación se ejecuta de forma nativa en:

- **Android:** Aplicación móvil nativa.
- **Desktop (JVM):** Versión de escritorio para Windows, macOS y Linux.
- **Web (Wasm & JS):** Ejecución directa en navegadores modernos mediante WebAssembly.

---

## Características Principales

- **Calculadora de Hormigón:** Dosificación de mezclas (H13, H17, H21) y recetas personalizadas.
- **Mampostería:** Cálculo de ladrillos, mortero y revoques con soporte para aparejos (Soga, Cabeza, Canto).
- **Armaduras Estructurales:** Cálculo de desarrollo de barras (hierro longitudinal y estribos) con soporte para formas complejas (ganchos, patas y remates).
- **Sistema de Precios Dinámico:** Integración de costos unitarios de materiales y mano de obra para presupuestos inmediatos.
- **Validación Normativa:** Avisos técnicos basados en reglamentos **CIRSOC 103 y 501** (especialmente críticos en zonas sísmicas).
- **Gestión de Materiales:** Base de datos local para añadir hierros, mallas SIMA y mezclas personalizadas.

---

## Tecnologías

- **Lenguaje:** Kotlin 2.x
- **UI Framework:** Compose Multiplatform
- **Formateo:** Soporte para texto enriquecido (AnnotatedString) y localización numérica regional.
- **Almacenamiento:** Multiplatform Settings (Observable) y Repositorios reactivos.
- **Calidad de Código:** Spotless para formateo y licencias.

---

## Cómo Ejecutar el Proyecto

Asegúrate de tener instalado el JDK 17 o superior.

### Escritorio (Desktop)

```bash
./gradlew :composeApp:run
```

### Web (WebAssembly)

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

### Android

Conecta un dispositivo o emulador y ejecuta:

```bash
./gradlew :composeApp:installDebug
```

---

## Estado del Proyecto

Actualmente en fase **Beta** (`1.4.0-beta.1`).

- [x] Motor de precios e integración de costos.
- [x] Lógica de validación estructural (CIRSOC).
- [ ] Implementación de persistencia de cálculos (ViewModels & Local DB).

---

## Licencia

Este proyecto es software libre: puedes redistribuirlo y/o modificarlo bajo los términos de la **GNU General Public License v3.0 (GPLv3)**. Consulta el archivo [LICENSE](./LICENSE) para más detalles.

_**Copyright (C) 2026 M415X**_
