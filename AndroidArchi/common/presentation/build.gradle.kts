import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.jongchan.androidarchi.common.presentation"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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
        compose = true
    }
    androidResources {
        noCompress += "tflite"
    }
}

val downloadMediaPipeModel = tasks.register("downloadMediaPipeModel") {
    val modelUrl =
        "https://storage.googleapis.com/mediapipe-models/text_embedder/universal_sentence_encoder/float32/1/universal_sentence_encoder.tflite"
    val outputFile = layout.projectDirectory.file("src/main/assets/universal_sentence_encoder.tflite")
    outputs.file(outputFile)
    onlyIf { !outputFile.asFile.exists() }
    doLast {
        val target = outputFile.asFile
        target.parentFile.mkdirs()
        runCatching {
            URI(modelUrl).toURL().openStream().use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }.onFailure { e ->
            logger.warn("Failed to download MediaPipe model asset (${e.message}). Hybrid fallback will be used.")
        }
    }
}

tasks.named("preBuild") {
    dependsOn(downloadMediaPipeModel)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }
}

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
    api(project(":common:domain"))
    api(project(":tti"))

    api(libs.androidx.core.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    api(libs.androidx.compose.material3)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    api(libs.androidx.hilt.navigation.compose)

    // Coroutines
    api(libs.kotlinx.coroutines.android)

    // Immutable collections — Compose 컴파일러가 ImmutableList/PersistentList 를 stable 로 인식하므로
    // UiState 의 List 필드를 안정적으로 노출시키기 위해 사용한다.
    api(libs.kotlinx.collections.immutable)

    // Lottie
    api(libs.lottie.compose)

    // Coil 3 (image / GIF / video frame)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.gif)
    implementation(libs.coil.video)

    // JankStats — frame-level performance tracking
    api(libs.androidx.metrics.performance)

    // On-Device AI — Google MediaPipe Tasks Text (TextEmbedder)
    implementation(libs.mediapipe.tasks.text)
}
