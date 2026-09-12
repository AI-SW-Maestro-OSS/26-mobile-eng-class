import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.jongchan.androidarchi.common.data"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        // API-CONFIG-INJECTION-POINT: API 키/베이스 URL 은 local.properties 에서 주입한다.
        //   API_KEY=...        (QA/Prod 빌드는 CI 단계에서 주입)
        //   API_BASE_URL=...
        // 기본값은 레퍼런스 feature(search)가 사용하는 카카오 검색 OpenAPI 데모 설정.
        val localProps = Properties().apply {
            val f = rootProject.file("local.properties")
            if (f.exists()) f.inputStream().use { load(it) }
        }
        val apiKey = localProps.getProperty("API_KEY") ?: "177352d0cea0c5e2b07da2d5a930be97"
        val apiBaseUrl = localProps.getProperty("API_BASE_URL") ?: "https://dapi.kakao.com/"
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")

        // METRIC-LOG-INJECTION-POINT: Metric Logging(Google Apps Script Web App) 엔드포인트/토큰.
        //   METRIC_LOG_URL=https://script.google.com/macros/s/.../exec
        //   METRIC_LOG_TOKEN=<Apps Script 스크립트 속성 TOKEN 과 동일한 값>
        // 비어 있으면 LoggingHelperImpl 이 전송을 건너뛴다(로컬 빌드는 깨지지 않음).
        val metricLogUrl = localProps.getProperty("METRIC_LOG_URL") ?: ""
        val metricLogToken = localProps.getProperty("METRIC_LOG_TOKEN") ?: ""
        buildConfigField("String", "METRIC_LOG_URL", "\"$metricLogUrl\"")
        buildConfigField("String", "METRIC_LOG_TOKEN", "\"$metricLogToken\"")
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
    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }
}

dependencies {
    implementation(project(":common:domain"))
    implementation(project(":common:entity"))

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Firebase Remote Config — RemoteConfigRepositoryV2Impl (common/data/remoteConfig)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
