pluginManagement {
    includeBuild("build-logic")
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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "flux"

include(":app")
include(":core:model")
include(":core:network")
include(":core:database")
include(":core:common")
include(":core:player")
include(":data:addon")
include(":data:debrid")
include(":domain")
include(":feature:home")
include(":feature:detail")
include(":feature:player")
include(":feature:search")
include(":feature:settings")
include(":feature:addons")
