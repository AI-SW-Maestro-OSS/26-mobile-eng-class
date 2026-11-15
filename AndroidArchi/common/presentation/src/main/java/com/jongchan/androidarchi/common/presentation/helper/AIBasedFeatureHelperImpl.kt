package com.jongchan.androidarchi.common.presentation.helper

import android.content.Context
import android.os.Build
import android.util.Log
import android.util.LruCache
import com.google.ai.edge.aicore.GenerationConfig as AiCoreGenerationConfig
import com.google.ai.edge.aicore.GenerativeModel as AiCoreGenerativeModel
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import com.jongchan.androidarchi.common.domain.helper.AIBasedFeatureHelper
import com.jongchan.androidarchi.common.entity.ai.AIEngineType
import com.jongchan.androidarchi.common.entity.ai.AIExecutionPolicy
import com.jongchan.androidarchi.common.entity.ai.AIServiceOutput
import com.jongchan.androidarchi.common.entity.ai.AIServiceRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.math.sqrt

/**
 * [AIServiceRequest] 의 목적(인자 타입 및 정책)에 따라 하위 AI 서비스
 * (MediaPipe TextEmbedder, AICore Gemini Nano, Firebase AI Logic)를 선택·실행하는 브릿지 구현체.
 *
 * - 특정 피처나 비즈니스 도메인 상태를 보유하지 않으며 순수 AI 추론 브릿지 역할만 수행한다.
 * - [AIServiceRequest.EmbedText] / [AIServiceRequest.BatchEmbedText] → MediaPipe Tasks `TextEmbedder` 실행
 * - [AIServiceRequest.CalculateVectorSimilarity] → 벡터 간 코사인 유사도 계산
 * - [AIServiceRequest.GenerateText] → [AIExecutionPolicy] 에 따라
 *   1차: 온디바이스 AICore (Gemini Nano) 실행 → 미지원/실패 시 2차: Firebase AI Logic (`gemini-2.5-flash`) 폴백 실행
 */
class AIBasedFeatureHelperImpl(
    private val context: Context?,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AIBasedFeatureHelper {

    private val embedderMutex = Mutex()
    private val aiCoreMutex = Mutex()

    private var textEmbedder: TextEmbedder? = null
    private var embedderInitialized: Boolean = false

    private val embeddingCache = object : LruCache<String, FloatArray>(MAX_EMBEDDING_CACHE_SIZE) {}

    @Suppress("UNCHECKED_CAST")
    override suspend fun <R : AIServiceOutput> execute(
        request: AIServiceRequest<R>,
    ): Result<R> = runCatching {
        when (request) {
            is AIServiceRequest.EmbedText -> runEmbedTextService(request) as R
            is AIServiceRequest.BatchEmbedText -> runBatchEmbedTextService(request) as R
            is AIServiceRequest.CalculateVectorSimilarity -> runVectorSimilarityService(request) as R
            is AIServiceRequest.GenerateText -> runGenerateTextService(request) as R
        }
    }

    private suspend fun runEmbedTextService(
        request: AIServiceRequest.EmbedText,
    ): AIServiceOutput.TextEmbedding = withContext(defaultDispatcher) {
        val (vector, engineType) = embedSingleTextInternal(request.text)
        AIServiceOutput.TextEmbedding(
            vector = vector,
            engineType = engineType,
        )
    }

    private suspend fun runBatchEmbedTextService(
        request: AIServiceRequest.BatchEmbedText,
    ): AIServiceOutput.BatchTextEmbedding = withContext(defaultDispatcher) {
        if (request.texts.isEmpty()) {
            return@withContext AIServiceOutput.BatchTextEmbedding(
                vectors = emptyMap(),
                engineType = AIEngineType.ON_DEVICE_MEDIAPIPE,
            )
        }
        var resolvedEngine = AIEngineType.ON_DEVICE_MEDIAPIPE
        val vectors = request.texts.mapValues { (_, text) ->
            val (vec, engine) = embedSingleTextInternal(text)
            resolvedEngine = engine
            vec
        }
        AIServiceOutput.BatchTextEmbedding(
            vectors = vectors,
            engineType = resolvedEngine,
        )
    }

    private fun runVectorSimilarityService(
        request: AIServiceRequest.CalculateVectorSimilarity,
    ): AIServiceOutput.VectorSimilarity {
        val similarity = computeCosineSimilarity(request.firstVector, request.secondVector)
        return AIServiceOutput.VectorSimilarity(similarity = similarity)
    }

    private suspend fun runGenerateTextService(
        request: AIServiceRequest.GenerateText,
    ): AIServiceOutput.GeneratedText = withContext(ioDispatcher) {
        when (request.policy) {
            AIExecutionPolicy.ON_DEVICE_ONLY -> {
                val text = generateWithOnDeviceAICore(request)
                AIServiceOutput.GeneratedText(
                    text = text,
                    engineType = AIEngineType.ON_DEVICE_AICORE,
                )
            }

            AIExecutionPolicy.CLOUD_ONLY -> {
                val text = generateWithFirebaseAILogic(request)
                AIServiceOutput.GeneratedText(
                    text = text,
                    engineType = AIEngineType.CLOUD_FIREBASE_AI,
                )
            }

            AIExecutionPolicy.PREFER_ON_DEVICE_FALLBACK_CLOUD -> {
                val onDeviceResult = runCatching { generateWithOnDeviceAICore(request) }
                if (onDeviceResult.isSuccess) {
                    val generated = onDeviceResult.getOrNull().orEmpty().trim()
                    if (generated.isNotEmpty()) {
                        return@withContext AIServiceOutput.GeneratedText(
                            text = generated,
                            engineType = AIEngineType.ON_DEVICE_AICORE,
                        )
                    }
                } else {
                    Log.d(
                        TAG,
                        "AICore (Gemini Nano) unavailable on this device (${onDeviceResult.exceptionOrNull()?.message}). Falling back to Firebase AI Logic.",
                    )
                }

                val cloudText = generateWithFirebaseAILogic(request).trim()
                if (cloudText.isEmpty()) {
                    throw IllegalStateException("Empty response from Firebase AI Logic")
                }
                AIServiceOutput.GeneratedText(
                    text = cloudText,
                    engineType = AIEngineType.CLOUD_FIREBASE_AI,
                )
            }
        }
    }

    private suspend fun generateWithOnDeviceAICore(
        request: AIServiceRequest.GenerateText,
    ): String = aiCoreMutex.withLock {
        val appContext = context ?: throw IllegalStateException("Context is null for AICore")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            throw UnsupportedOperationException("AICore requires Android API 31+")
        }

        val config = AiCoreGenerationConfig.Builder().apply {
            this.context = appContext
            this.temperature = request.temperature
            this.maxOutputTokens = request.maxOutputTokens
        }.build()

        val model = AiCoreGenerativeModel(config)
        try {
            model.prepareInferenceEngine()
            val composedPrompt = buildFullPrompt(request.systemInstruction, request.prompt)
            val response = model.generateContent(composedPrompt)
            val output = response.text?.trim().orEmpty()
            if (output.isEmpty()) {
                throw IllegalStateException("AICore returned empty text")
            }
            output
        } finally {
            runCatching { model.close() }
        }
    }

    private suspend fun generateWithFirebaseAILogic(
        request: AIServiceRequest.GenerateText,
    ): String {
        val config = generationConfig {
            temperature = request.temperature
            maxOutputTokens = request.maxOutputTokens
        }
        val sysInstruction = request.systemInstruction
            .takeIf { it.isNotBlank() }
            ?.let { instruction -> content { text(instruction) } }

        val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = FIREBASE_GEMINI_MODEL_NAME,
                generationConfig = config,
                systemInstruction = sysInstruction,
            )

        val response = generativeModel.generateContent(request.prompt)
        return response.text?.trim().orEmpty()
    }

    private fun buildFullPrompt(systemInstruction: String, prompt: String): String =
        if (systemInstruction.isBlank()) {
            prompt
        } else {
            "$systemInstruction\n\n$prompt"
        }

    private suspend fun embedSingleTextInternal(
        text: String,
    ): Pair<FloatArray, AIEngineType> {
        val normalized = text.trim().lowercase()
        if (normalized.isEmpty()) {
            return FloatArray(TOTAL_EMBEDDING_DIM) to AIEngineType.ON_DEVICE_MEDIAPIPE
        }

        synchronized(embeddingCache) {
            embeddingCache.get(normalized)?.let { cached ->
                return cached to AIEngineType.ON_DEVICE_MEDIAPIPE
            }
        }

        val mediaPipeVec = extractMediaPipeEmbedding(normalized)
        val hasMediaPipeSignal = mediaPipeVec.any { it != 0f }
        val ngramVec = computeMultilingualNgramVector(normalized)
        val combined = combineAndNormalize(mediaPipeVec, ngramVec)

        synchronized(embeddingCache) {
            embeddingCache.put(normalized, combined)
        }
        val engine = if (hasMediaPipeSignal) {
            AIEngineType.ON_DEVICE_MEDIAPIPE
        } else {
            AIEngineType.LOCAL_FALLBACK
        }
        return combined to engine
    }

    private suspend fun extractMediaPipeEmbedding(text: String): FloatArray =
        embedderMutex.withLock {
            val embedder = getOrCreateTextEmbedder() ?: return@withLock FloatArray(MEDIAPIPE_DIM)
            runCatching {
                val result = embedder.embed(text)
                val firstEmbedding = result.embeddingResult().embeddings().firstOrNull()
                val raw = firstEmbedding?.floatEmbedding()
                if (raw != null && raw.isNotEmpty()) {
                    val resized = FloatArray(MEDIAPIPE_DIM)
                    val copyLen = min(raw.size, MEDIAPIPE_DIM)
                    System.arraycopy(raw, 0, resized, 0, copyLen)
                    l2NormalizeInPlace(resized)
                    resized
                } else {
                    FloatArray(MEDIAPIPE_DIM)
                }
            }.getOrElse { e ->
                Log.w(TAG, "MediaPipe embed fallback (${e.message})")
                FloatArray(MEDIAPIPE_DIM)
            }
        }

    private fun getOrCreateTextEmbedder(): TextEmbedder? {
        if (embedderInitialized) return textEmbedder
        embedderInitialized = true
        val appContext = context ?: return null
        textEmbedder = runCatching {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MEDIAPIPE_MODEL_ASSET_PATH)
                .build()
            val options = TextEmbedder.TextEmbedderOptions.builder()
                .setBaseOptions(baseOptions)
                .setL2Normalize(true)
                .build()
            TextEmbedder.createFromOptions(appContext, options)
        }.onFailure { e ->
            Log.w(TAG, "MediaPipe TextEmbedder init skipped/fallback (${e.message})")
        }.getOrNull()
        return textEmbedder
    }

    private fun computeMultilingualNgramVector(text: String): FloatArray {
        val vec = FloatArray(NGRAM_DIM)
        val tokens = text
            .split(TOKEN_SPLIT_REGEX)
            .filter { it.isNotBlank() }

        for (token in tokens) {
            accumulateFeatureHash(vec, "tok:$token", weight = 1.5f)
            if (token.length >= 2) {
                for (i in 0..token.length - 2) {
                    accumulateFeatureHash(vec, "c2:${token.substring(i, i + 2)}", weight = 1.0f)
                }
            }
            if (token.length >= 3) {
                for (i in 0..token.length - 3) {
                    accumulateFeatureHash(vec, "c3:${token.substring(i, i + 3)}", weight = 0.8f)
                }
            }
        }
        l2NormalizeInPlace(vec)
        return vec
    }

    private fun accumulateFeatureHash(vec: FloatArray, feature: String, weight: Float) {
        val hash = feature.hashCode()
        val index = (hash and Int.MAX_VALUE) % NGRAM_DIM
        val sign = if ((hash ushr 31) == 0) 1f else -1f
        vec[index] += sign * weight
    }

    private fun combineAndNormalize(mediaPipeVec: FloatArray, ngramVec: FloatArray): FloatArray {
        val result = FloatArray(TOTAL_EMBEDDING_DIM)
        val hasMediaPipeSignal = mediaPipeVec.any { it != 0f }
        val mpWeight = if (hasMediaPipeSignal) MEDIAPIPE_WEIGHT else 0f
        val ngWeight = NGRAM_WEIGHT

        for (i in 0 until MEDIAPIPE_DIM) {
            result[i] = mediaPipeVec[i] * mpWeight
        }
        for (i in 0 until NGRAM_DIM) {
            result[MEDIAPIPE_DIM + i] = ngramVec[i] * ngWeight
        }
        l2NormalizeInPlace(result)
        return result
    }

    private fun computeCosineSimilarity(u: FloatArray, v: FloatArray): Double {
        if (u.isEmpty() || v.isEmpty() || u.size != v.size) return 0.0
        var dot = 0.0
        var normU = 0.0
        var normV = 0.0
        for (i in u.indices) {
            val ui = u[i].toDouble()
            val vi = v[i].toDouble()
            dot += ui * vi
            normU += ui * ui
            normV += vi * vi
        }
        val denom = sqrt(normU) * sqrt(normV)
        return if (denom > EPSILON) (dot / denom).coerceIn(-1.0, 1.0) else 0.0
    }

    private fun l2NormalizeInPlace(vector: FloatArray) {
        var sumSq = 0.0
        for (v in vector) {
            sumSq += v.toDouble() * v.toDouble()
        }
        val norm = sqrt(sumSq).toFloat()
        if (norm > EPSILON) {
            for (i in vector.indices) {
                vector[i] /= norm
            }
        }
    }

    companion object {
        private const val TAG = "AIBasedFeatureHelper"
        private const val MEDIAPIPE_MODEL_ASSET_PATH = "universal_sentence_encoder.tflite"
        private const val FIREBASE_GEMINI_MODEL_NAME = "gemini-2.5-flash"

        private const val MEDIAPIPE_DIM = 100
        private const val NGRAM_DIM = 128
        private const val TOTAL_EMBEDDING_DIM = MEDIAPIPE_DIM + NGRAM_DIM

        private const val MEDIAPIPE_WEIGHT = 0.6f
        private const val NGRAM_WEIGHT = 0.8f

        private const val MAX_EMBEDDING_CACHE_SIZE = 256
        private const val EPSILON = 1e-9

        private val TOKEN_SPLIT_REGEX = Regex("[^\\p{L}\\p{N}]+")
    }
}
