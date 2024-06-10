pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io") // Add JitPack repository
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io") // Add JitPack repository
    }
}

rootProject.name = "SyncPlus"
include(":app")



