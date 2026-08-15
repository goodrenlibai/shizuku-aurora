// Shizuku Aurora — 根构建脚本：仅声明插件版本，SDK 参数由各 Android 模块自行声明
// （避免在根脚本用易碎泛型反射配置 android 扩展，提升跨 AGP 版本的构建稳健性）
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
