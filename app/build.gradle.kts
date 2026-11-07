import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

// 版本号默认值；CI 可通过 -PversionName=... / -PversionCode=... 覆盖
val appVersionName = (project.findProperty("versionName") as String?) ?: "1.1"
val appVersionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull() ?: 2

android {
    namespace = "com.github.hwhaocool.pointbunny"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.github.hwhaocool.pointbunny"
        minSdk = 26          // 最低支持 Android 8.0，可直接使用 java.time API
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersionName

        // 「关于」页展示的构建时间（可用 -PbuildTime=... 覆盖，便于复现构建）
        val buildTime = (project.findProperty("buildTime") as String?)
            ?: SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        buildConfigField("String", "BUILD_TIME", "\"$buildTime\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// APK 产物命名：PointBunny-<版本号>-<构建类型>.apk
android.applicationVariants.all {
    outputs.all {
        (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
            "PointBunny-${versionName}-${buildType.name}.apk"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")

    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Room 本地数据库（数据全部存本地）
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
