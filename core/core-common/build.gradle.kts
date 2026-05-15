plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.notifiq.core.common"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


}

dependencies {
    implementation(project(":core:core-model"))
    implementation(libs.core.ktx)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
}