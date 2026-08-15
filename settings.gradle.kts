// Shizuku Aurora — 3A 超巨型拓展工程 · Gradle 模块注册
// 模块化单体：UI 应用 + 纯 JVM 领域层 + 数据层 + 设计系统 + 服务端控制器
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
        // RikkaApps 仓库：RikkaX 相关依赖
        maven("https://rikka.app/maven") { name = "Rikka" }
        // JitPack：libsu（root shell）由 topjohnwu 经 JitPack 发布
        maven("https://jitpack.io") { name = "JitPack" }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://rikka.app/maven") { name = "Rikka" }
        maven("https://jitpack.io") { name = "JitPack" }
    }
}

rootProject.name = "ShizukuAurora"

include(":app")
include(":core:designsystem")
include(":core:domain")
include(":core:data")
include(":server")
