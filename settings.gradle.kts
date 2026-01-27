rootProject.name = "MaterialsCalculator"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        // Repositorio necesario para HumanReadable (si no está en Maven Central)
        // Según la documentación de la librería, suele estar en Maven Central, pero a veces requiere jitpack si es una versión muy nueva o snapshot.
        // Sin embargo, el error dice "Could not find", lo que sugiere que no está en los repositorios declarados o la versión es incorrecta.
        // Verificando la librería: nl.jacobras:HumanReadable suele estar en Maven Central.
        // Si falla, probamos añadir jitpack por si acaso, o verificamos la versión.
        // La versión 1.10.2 parece reciente.
        maven { url = uri("https://jitpack.io") }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":composeApp")