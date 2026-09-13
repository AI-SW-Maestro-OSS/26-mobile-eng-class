pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AndroidArchi"
include(":app")

include(":common:presentation")
include(":common:domain")
include(":common:data")
include(":common:entity")

include(":main:presentation")
include(":main:domain")
include(":main:data")
include(":main:entity")

include(":intro:presentation")
include(":intro:domain")
include(":intro:data")
include(":intro:entity")

include(":search:presentation")
include(":search:domain")
include(":search:data")
include(":search:entity")

include(":favorite:presentation")
include(":favorite:domain")
include(":favorite:data")
include(":favorite:entity")

include(":fullScreenMedia:presentation")
include(":fullScreenMedia:domain")
include(":fullScreenMedia:data")
include(":fullScreenMedia:entity")

include(":etc:presentation")
include(":etc:domain")
include(":etc:data")
include(":etc:entity")

include(":tti")

include(":baselineprofile")
