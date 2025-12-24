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
- **Mampostería:** Cálculo de ladrillos, mortero y revoques.
- **Armaduras Estructurales:** Cálculo de desarrollo de barras (hierro longitudinal y estribos) con soporte para formas complejas (ganchos, patas y remates).
- **Gestión de Materiales:** Posibilidad de añadir hierros y mezclas personalizadas que se guardan localmente.
- **Modo Oscuro/Claro:** Interfaz adaptativa basada en Material Design 3.

---

## Tecnologías

- **Lenguaje:** Kotlin 2.x
- **UI Framework:** Compose Multiplatform
- **Almacenamiento:** Multiplatform Settings (Observable)
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

## Licencia

Este proyecto es software libre: puedes redistribuirlo y/o modificarlo bajo los términos de la **GNU General Public License v3.0 (GPLv3)**. Consulta el archivo [LICENSE](./LICENSE) para más detalles.

_**Copyright (C) 2025 M415X**_

---

## Estado del Proyecto

Actualmente en fase **Beta** (`1.1.0-beta.2`). El motor de cálculo de armaduras está siendo integrado para soportar normativas de doblado de hierro internacionales.
