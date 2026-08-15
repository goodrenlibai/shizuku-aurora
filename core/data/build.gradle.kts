plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "shizuku.aurora.data"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
        targetSdk = 36
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    api(project(":core:domain"))
    api(project(":server"))

    // 官方 Shizuku 客户端 API（复用官方 Binder 核心，而非重造）
    api(libs.shizuku.api)
    // ShizukuProvider：提供 Manifest 中声明的 rikka.shizuku.ShizukuProvider 组件
    api(libs.shizuku.provider)
    // libsu：用于 root 模式启动 server
    api(libs.libsu.core)

    api(libs.kotlinx.coroutines.android)
    api(libs.androidx.core.ktx)

    // Room
    api(libs.room.runtime)
    api(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    api(libs.datastore.preferences)

    // Hilt
    api(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // 安全：DataStore 加密 + 生物识别
    api(libs.androidx.security.crypto)
    api(libs.androidx.biometric)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
