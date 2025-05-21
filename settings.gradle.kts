pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Inventory"
include(":app")

// Enable Kotlin daemon with stable settings
gradle.projectsLoaded {
    System.setProperty("kotlin.daemon.jvm.options", "-Xmx2048m")
}
