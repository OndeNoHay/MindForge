pluginManagement {
    includeBuild("build-logic")
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

rootProject.name = "MindForge"

// App module
include(":app")

// Core modules
include(":core:core-common")
include(":core:core-ui")
include(":core:core-domain")
include(":core:core-data")

// Feature modules
include(":features:feature-home")
include(":features:feature-profile")
include(":features:feature-progress")
include(":features:feature-settings")

// Game modules
include(":games:game-core")
include(":games:game-memory-matrix")
include(":games:game-task-prioritizer")
include(":games:game-name-face")
include(":games:game-meeting-recall")
include(":games:game-concept-linker")
include(":games:game-spaced-review")

// Navigation module
include(":navigation")
