plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.notifiq.core.common"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    sourceSets.named("main") {
        kotlin.directories += "src/main/java"
    }

    sourceSets.named("test") {
        kotlin.directories += "src/test/java"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {
    implementation(project(":core:core-model"))
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
}