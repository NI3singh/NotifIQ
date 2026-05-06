pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "NotifIQ"

include(":app")
include(":core:core-model")
include(":core:core-common")
include(":core:core-database")
include(":core:core-datastore")
include(":core:core-designsystem")
include(":classification")
include(":capture")
include(":feature:feature-onboarding")
include(":feature:feature-inbox")
include(":feature:feature-detail")
include(":feature:feature-home")
include(":feature:feature-priority")
include(":feature:feature-analytics")
include(":feature:feature-rules")
include(":feature:feature-settings")
include(":feature:feature-summary")
include(":worker")