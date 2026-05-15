plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.notifiq.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.notifiq.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // All modules
    implementation(project(":core:core-model"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-database"))
    implementation(project(":core:core-datastore"))
    implementation(project(":core:core-designsystem"))
    implementation(project(":classification"))
    implementation(project(":capture"))
    implementation(project(":worker"))
    implementation(project(":feature:feature-onboarding"))
    implementation(project(":feature:feature-inbox"))
    implementation(project(":feature:feature-home"))
    implementation(project(":feature:feature-detail"))
    implementation(project(":feature:feature-priority"))
    implementation(project(":feature:feature-analytics"))
    implementation(project(":feature:feature-rules"))
    implementation(project(":feature:feature-settings"))
    implementation(project(":feature:feature-summary"))
    ksp(libs.androidx.hilt.compiler)
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.animation)
    implementation("androidx.appcompat:appcompat:1.7.0")


    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // Activity
    implementation(libs.androidx.activity.compose)

    // Core
    implementation(libs.androidx.core.ktx)

    // Room
    implementation(libs.room.runtime)

    // Material (for XML themes)
    implementation("com.google.android.material:material:1.12.0")

    // WorkManager
    implementation(libs.workmanager.ktx)
    implementation(libs.hilt.work)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}