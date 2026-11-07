pluginManagement {
    // CI 环境使用官方仓库，避免第三方镜像不可达/同步延迟导致依赖解析失败
    val useMirrors = System.getenv("CI")?.toBoolean() != true
    repositories {
        if (useMirrors) {
            // 阿里云镜像（加速，失效时自动回退到下面的官方源）
            maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
            maven { url = uri("https://maven.aliyun.com/repository/google") }
            maven { url = uri("https://maven.aliyun.com/repository/public") }
        }
        // KSP 等插件发布在 Maven Central，优先于 Google，避免 Google Maven 超时阻塞解析
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    // 同上：CI 环境跳过阿里云镜像
    val useMirrors = System.getenv("CI")?.toBoolean() != true
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        if (useMirrors) {
            maven { url = uri("https://maven.aliyun.com/repository/google") }
            maven { url = uri("https://maven.aliyun.com/repository/public") }
        }
        google()
        mavenCentral()
    }
}
rootProject.name = "PointBunny"
include(":app")
