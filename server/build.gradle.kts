plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "shizuku.aurora.server"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
        targetSdk = 36
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    api(project(":core:domain"))
    api(libs.libsu.core)
    api(libs.kotlinx.coroutines.core)
}
