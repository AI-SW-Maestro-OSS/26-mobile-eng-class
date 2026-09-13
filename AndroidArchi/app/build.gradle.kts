import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.baselineprofile)
}

// FIREBASE-CONFIG-INJECTION-POINT: Firebase(Remote Config) 는 app/google-services.json 이 있을 때만 활성화된다.
// 파일이 없으면 google-services 플러그인을 건너뛰어 빌드는 통과하고, RemoteConfigRepositoryV2 는
// 항상 RemoteConfigKey.defaultValue 를 돌려준다. Firebase 콘솔에서 받은 json 을 app/ 에 넣으면 그대로 동작한다.
if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
} else {
    logger.warn("[AndroidArchi] app/google-services.json 이 없어 google-services 플러그인을 적용하지 않습니다. Remote Config 는 기본값으로 동작합니다.")
}

android {
    namespace = "com.jongchan.androidarchi"
    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt())
    }

    defaultConfig {
        applicationId = "com.jongchan.androidarchi"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        // Macrobenchmark / Baseline Profile 수집 시 사용되는 빌드 타입.
        // release 와 동일한 최적화 상태를 가지면서, ART 가 메서드 trace 를 dump 할 수 있도록
        // profileable 로 표시한다. androidx.baselineprofile 플러그인이 자동으로 생성/사용한다.
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
            isProfileable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }
}

// Compose Compiler reports/metrics — enable with `-Pcomposecompiler.reports=true`.
composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_stability.conf"))
    if (providers.gradleProperty("composecompiler.reports").orNull == "true") {
        val outDir = rootProject.layout.buildDirectory.dir(
            "compose_reports/${project.path.replace(":", "_").trim('_')}"
        )
        reportsDestination.set(outDir)
        metricsDestination.set(outDir)
    }
}

dependencies {
    implementation(project(":common:presentation"))
    implementation(project(":common:domain"))
    implementation(project(":common:data"))
    implementation(project(":common:entity"))

    implementation(project(":main:presentation"))
    implementation(project(":main:domain"))
    implementation(project(":main:data"))
    implementation(project(":main:entity"))

    implementation(project(":intro:presentation"))
    implementation(project(":intro:domain"))
    implementation(project(":intro:data"))
    implementation(project(":intro:entity"))

    implementation(project(":search:presentation"))
    implementation(project(":search:domain"))
    implementation(project(":search:data"))
    implementation(project(":search:entity"))

    implementation(project(":favorite:presentation"))
    implementation(project(":favorite:domain"))
    implementation(project(":favorite:data"))
    implementation(project(":favorite:entity"))

    implementation(project(":fullScreenMedia:presentation"))
    implementation(project(":fullScreenMedia:domain"))
    implementation(project(":fullScreenMedia:data"))
    implementation(project(":fullScreenMedia:entity"))

    implementation(project(":etc:presentation"))
    implementation(project(":etc:domain"))
    implementation(project(":etc:data"))
    implementation(project(":etc:entity"))

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Baseline Profile: 설치 시점에 dump 된 프로필을 ART 에 등록해 주는 런타임 라이브러리.
    // minSdk 24 ~ 27 백포트를 위해 필수.
    implementation(libs.androidx.profileinstaller)

    // 빌드 시 :baselineprofile 모듈이 만들어 둔 baseline-prof.txt 를 가져다
    // 자동으로 src/main/baseline-prof.txt 위치로 머지/패키징 한다.
    "baselineProfile"(project(":baselineprofile"))
}
