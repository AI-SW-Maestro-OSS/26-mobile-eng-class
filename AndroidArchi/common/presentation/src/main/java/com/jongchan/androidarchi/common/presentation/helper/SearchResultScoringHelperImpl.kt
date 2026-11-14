package com.jongchan.androidarchi.common.presentation.helper

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.util.LruCache
import androidx.core.content.edit
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import com.jongchan.androidarchi.common.domain.helper.SearchResultScoringHelper
import com.jongchan.androidarchi.common.entity.ai.MediaFeedbackType
import com.jongchan.androidarchi.common.entity.ai.MediaPreferenceFeedbackVO
import com.jongchan.androidarchi.common.entity.media.MediaType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Google MediaPipe Tasks (`TextEmbedder`) 를 활용한 [SearchResultScoringHelper] 구현체.
 *
 * - 온디바이스 `.tflite` 모델(`universal_sentence_encoder.tflite`)을 지연 초기화해 텍스트 임베딩을 추출한다.
 * - 한국어/다국어 키워드·카테고리 변별력을 높이기 위해 MediaPipe 신경망 임베딩 벡터(100차원)와
 *   다국어 단어/음절 N-gram 해싱 벡터(128차원)를 결합한 하이브리드 정규화 임베딩(228차원)을 생성한다.
 * - 유저의 Like / Dislike 평가 히스토리를 로컬(`SharedPreferences`)에 영속화하고,
 *   Rocchio 알고리즘($V_{user} = \alpha V_{like} - \beta V_{dislike}$)과 코사인 유사도로
 *   각 아이템의 취향 매칭 점수(0~100%)를 계산한다.
 */
class SearchResultScoringHelperImpl(
    private val context: Context?,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SearchResultScoringHelper {

    private val embedderMutex = Mutex()
    private val storageMutex = Mutex()

    private var textEmbedder: TextEmbedder? = null
    private var embedderInitialized: Boolean = false

    private val embeddingCache = object : LruCache<String, FloatArray>(MAX_EMBEDDING_CACHE_SIZE) {}

    private val sharedPreferences: SharedPreferences? by lazy {
        runCatching {
            context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }.getOrNull()
    }

    private val _feedbackState = MutableStateFlow<List<MediaPreferenceFeedbackVO>>(emptyList())

    init {
        _feedbackState.value = loadFeedbacksFromPrefs()
    }

    override fun observeFeedbacks(): Flow<List<MediaPreferenceFeedbackVO>> =
        _feedbackState.asStateFlow()

    override suspend fun recordFeedback(feedback: MediaPreferenceFeedbackVO) {
        if (feedback.urlKey.isBlank()) return
        storageMutex.withLock {
            withContext(ioDispatcher) {
                val current = _feedbackState.value
                val updated = buildList {
                    if (feedback.feedbackType != MediaFeedbackType.NONE) {
                        add(
                            feedback.copy(
                                updatedAt = if (feedback.updatedAt > 0L) {
                                    feedback.updatedAt
                                } else {
                                    System.currentTimeMillis()
                                },
                            )
                        )
                    }
                    current.forEach { item ->
                        if (item.urlKey != feedback.urlKey) {
                            add(item)
                        }
                    }
                }.take(MAX_FEEDBACK_HISTORY_SIZE)

                saveFeedbacksToPrefs(updated)
                _feedbackState.value = updated
            }
        }
    }

    override suspend fun clearFeedbacks() {
        storageMutex.withLock {
            withContext(ioDispatcher) {
                saveFeedbacksToPrefs(emptyList())
                _feedbackState.value = emptyList()
            }
        }
    }

    override suspend fun embedText(text: String): FloatArray = withContext(defaultDispatcher) {
        val normalized = text.trim().lowercase()
        if (normalized.isEmpty()) {
            return@withContext FloatArray(TOTAL_EMBEDDING_DIM)
        }

        synchronized(embeddingCache) {
            embeddingCache.get(normalized)?.let { return@withContext it }
        }

        val mediaPipeVec = extractMediaPipeEmbedding(normalized)
        val ngramVec = computeMultilingualNgramVector(normalized)
        val combined = combineAndNormalize(mediaPipeVec, ngramVec)

        synchronized(embeddingCache) {
            embeddingCache.put(normalized, combined)
        }
        combined
    }

    override fun cosineSimilarity(u: FloatArray, v: FloatArray): Double {
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

    override suspend fun calculatePreferenceScores(
        targetTexts: Map<String, String>,
        likedTexts: List<String>,
        dislikedTexts: List<String>,
    ): Map<String, Int> = withContext(defaultDispatcher) {
        if (targetTexts.isEmpty()) return@withContext emptyMap()
        val validLikes = likedTexts.filter { it.isNotBlank() }
        val validDislikes = dislikedTexts.filter { it.isNotBlank() }
        if (validLikes.isEmpty() && validDislikes.isEmpty()) {
            return@withContext emptyMap()
        }

        val likedEmbeddings = validLikes.map { embedText(it) }
        val dislikedEmbeddings = validDislikes.map { embedText(it) }

        val meanLiked = averageAndNormalize(likedEmbeddings)
        val meanDisliked = averageAndNormalize(dislikedEmbeddings)

        // Rocchio Preference Vector: V_user = alpha * mean(V_like) - beta * mean(V_dislike)
        val userPreferenceVector = FloatArray(TOTAL_EMBEDDING_DIM)
        for (i in 0 until TOTAL_EMBEDDING_DIM) {
            userPreferenceVector[i] =
                ROCCHIO_ALPHA * meanLiked[i] - ROCCHIO_BETA * meanDisliked[i]
        }
        l2NormalizeInPlace(userPreferenceVector)

        targetTexts.mapValues { (_, semanticText) ->
            val targetVec = embedText(semanticText)
            val rocchioSim = cosineSimilarity(targetVec, userPreferenceVector)

            val maxLikeSim = likedEmbeddings.maxOfOrNull { cosineSimilarity(targetVec, it) } ?: 0.0
            val maxDislikeSim =
                dislikedEmbeddings.maxOfOrNull { cosineSimilarity(targetVec, it) } ?: 0.0

            // Rocchio 방향 유사도와 개별 최고 유사/비유사 피드백 신호를 결합해 1~99% 매칭률로 변환
            val blendedSignal =
                (0.55 * rocchioSim) + (0.35 * maxLikeSim) - (0.35 * maxDislikeSim)
            val percentage = (50.0 + blendedSignal * 65.0).roundToInt()
            percentage.coerceIn(MIN_SCORE_PERCENT, MAX_SCORE_PERCENT)
        }
    }

    override suspend fun evaluatePreferenceScores(
        targetTexts: Map<String, String>,
    ): Map<String, Int> {
        val feedbacks = _feedbackState.value
        if (feedbacks.isEmpty() || targetTexts.isEmpty()) return emptyMap()

        val likedTexts = feedbacks
            .filter { it.feedbackType == MediaFeedbackType.LIKE }
            .map { it.semanticText }
        val dislikedTexts = feedbacks
            .filter { it.feedbackType == MediaFeedbackType.DISLIKE }
            .map { it.semanticText }

        return calculatePreferenceScores(
            targetTexts = targetTexts,
            likedTexts = likedTexts,
            dislikedTexts = dislikedTexts,
        )
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
                Log.w(TAG, "MediaPipe embed failed, using n-gram vector: ${e.message}")
                FloatArray(MEDIAPIPE_DIM)
            }
        }

    private fun getOrCreateTextEmbedder(): TextEmbedder? {
        if (embedderInitialized) return textEmbedder
        embedderInitialized = true
        val appContext = context ?: return null
        textEmbedder = runCatching {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_ASSET_PATH)
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

    /**
     * 한국어/다국어 어휘 및 문자 N-gram(2-gram, 3-gram) Feature Hashing 임베딩.
     * MediaPipe 영문 USE 모델이 놓칠 수 있는 한글 형태소·카테고리·도메인 특징을 보완한다.
     */
    private fun computeMultilingualNgramVector(text: String): FloatArray {
        val vec = FloatArray(NGRAM_DIM)
        val tokens = text
            .split(TOKEN_SPLIT_REGEX)
            .filter { it.isNotBlank() }

        for (token in tokens) {
            // 단어 토큰 해싱 (가중치 1.5)
            accumulateFeatureHash(vec, "tok:$token", weight = 1.5f)

            // 문자 2-gram / 3-gram 해싱 (한국어 복합명사·접두/접미 유사도 포착)
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

    private fun averageAndNormalize(vectors: List<FloatArray>): FloatArray {
        val result = FloatArray(TOTAL_EMBEDDING_DIM)
        if (vectors.isEmpty()) return result
        for (vec in vectors) {
            val len = min(vec.size, TOTAL_EMBEDDING_DIM)
            for (i in 0 until len) {
                result[i] += vec[i]
            }
        }
        val invCount = 1f / vectors.size.toFloat()
        for (i in 0 until TOTAL_EMBEDDING_DIM) {
            result[i] *= invCount
        }
        l2NormalizeInPlace(result)
        return result
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

    private fun loadFeedbacksFromPrefs(): List<MediaPreferenceFeedbackVO> {
        val raw = sharedPreferences?.getString(KEY_FEEDBACK_HISTORY, null)
            ?.takeIf { it.isNotBlank() }
            ?: return emptyList()

        return runCatching {
            val jsonArray = JSONArray(raw)
            buildList(jsonArray.length()) {
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.optJSONObject(i) ?: continue
                    val urlKey = obj.optString("urlKey", "")
                    if (urlKey.isBlank()) continue
                    val feedbackType = runCatching {
                        MediaFeedbackType.valueOf(obj.optString("feedbackType", "NONE"))
                    }.getOrDefault(MediaFeedbackType.NONE)
                    if (feedbackType == MediaFeedbackType.NONE) continue

                    add(
                        MediaPreferenceFeedbackVO(
                            urlKey = urlKey,
                            title = obj.optString("title", ""),
                            collection = obj.optString("collection", ""),
                            description = obj.optString("description", ""),
                            mediaType = MediaType.fromRawValue(obj.optInt("mediaType", 0)),
                            feedbackType = feedbackType,
                            updatedAt = obj.optLong("updatedAt", 0L),
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun saveFeedbacksToPrefs(items: List<MediaPreferenceFeedbackVO>) {
        val prefs = sharedPreferences ?: return
        runCatching {
            val jsonArray = JSONArray()
            items.forEach { item ->
                val obj = JSONObject().apply {
                    put("urlKey", item.urlKey)
                    put("title", item.title)
                    put("collection", item.collection)
                    put("description", item.description)
                    put("mediaType", item.mediaType.rawValue)
                    put("feedbackType", item.feedbackType.name)
                    put("updatedAt", item.updatedAt)
                }
                jsonArray.put(obj)
            }
            prefs.edit {
                putString(KEY_FEEDBACK_HISTORY, jsonArray.toString())
            }
        }
    }

    companion object {
        private const val TAG = "AIBasedFeatureHelper"
        private const val MODEL_ASSET_PATH = "universal_sentence_encoder.tflite"
        private const val PREFS_NAME = "androidarchi_ai_preference_prefs"
        private const val KEY_FEEDBACK_HISTORY = "key_ai_feedback_history"

        private const val MEDIAPIPE_DIM = 100
        private const val NGRAM_DIM = 128
        private const val TOTAL_EMBEDDING_DIM = MEDIAPIPE_DIM + NGRAM_DIM

        private const val MEDIAPIPE_WEIGHT = 0.6f
        private const val NGRAM_WEIGHT = 0.8f

        private const val ROCCHIO_ALPHA = 1.0f
        private const val ROCCHIO_BETA = 0.75f

        private const val MIN_SCORE_PERCENT = 1
        private const val MAX_SCORE_PERCENT = 99
        private const val MAX_EMBEDDING_CACHE_SIZE = 256
        private const val MAX_FEEDBACK_HISTORY_SIZE = 100
        private const val EPSILON = 1e-9

        private val TOKEN_SPLIT_REGEX = Regex("[^\\p{L}\\p{N}]+")
    }
}
