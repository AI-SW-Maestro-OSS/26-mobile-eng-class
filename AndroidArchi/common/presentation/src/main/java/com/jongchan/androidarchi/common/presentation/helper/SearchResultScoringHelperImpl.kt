package com.jongchan.androidarchi.common.presentation.helper

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.jongchan.androidarchi.common.domain.helper.AIBasedFeatureHelper
import com.jongchan.androidarchi.common.domain.helper.SearchResultScoringHelper
import com.jongchan.androidarchi.common.entity.ai.AIServiceRequest
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
 * 범용 AI 브릿지([AIBasedFeatureHelper])의 임베딩 서비스를 활용하는 [SearchResultScoringHelper] 구현체.
 *
 * - 텍스트 임베딩 추출은 [AIBasedFeatureHelper.execute] (`AIServiceRequest.EmbedText` / `BatchEmbedText`) 브릿지에 위임한다.
 * - 유저의 Like / Dislike 평가 히스토리를 로컬(`SharedPreferences`)에 영속화하고,
 *   Rocchio 알고리즘($V_{user} = \alpha V_{like} - \beta V_{dislike}$)과 코사인 유사도로
 *   각 아이템의 취향 매칭 점수(0~100%)를 계산한다.
 */
class SearchResultScoringHelperImpl(
    private val context: Context?,
    private val aiBasedFeatureHelper: AIBasedFeatureHelper,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SearchResultScoringHelper {

    private val storageMutex = Mutex()

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
        aiBasedFeatureHelper.execute(AIServiceRequest.EmbedText(text = text))
            .getOrNull()
            ?.vector
            ?: FloatArray(DEFAULT_EMBEDDING_DIM)
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
        val targetVectorsByKey = aiBasedFeatureHelper
            .execute(AIServiceRequest.BatchEmbedText(texts = targetTexts))
            .getOrNull()
            ?.vectors
            .orEmpty()

        val dim = likedEmbeddings.firstOrNull()?.size
            ?: dislikedEmbeddings.firstOrNull()?.size
            ?: DEFAULT_EMBEDDING_DIM

        val meanLiked = averageAndNormalize(likedEmbeddings, dim)
        val meanDisliked = averageAndNormalize(dislikedEmbeddings, dim)

        // Rocchio Preference Vector: V_user = alpha * mean(V_like) - beta * mean(V_dislike)
        val userPreferenceVector = FloatArray(dim)
        for (i in 0 until dim) {
            userPreferenceVector[i] =
                ROCCHIO_ALPHA * meanLiked[i] - ROCCHIO_BETA * meanDisliked[i]
        }
        l2NormalizeInPlace(userPreferenceVector)

        targetTexts.mapValues { (key, semanticText) ->
            val targetVec = targetVectorsByKey[key] ?: embedText(semanticText)
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

    private fun averageAndNormalize(vectors: List<FloatArray>, dim: Int): FloatArray {
        val result = FloatArray(dim)
        if (vectors.isEmpty()) return result
        for (vec in vectors) {
            val len = min(vec.size, dim)
            for (i in 0 until len) {
                result[i] += vec[i]
            }
        }
        val invCount = 1f / vectors.size.toFloat()
        for (i in 0 until dim) {
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
        private const val PREFS_NAME = "androidarchi_ai_preference_prefs"
        private const val KEY_FEEDBACK_HISTORY = "key_ai_feedback_history"

        private const val DEFAULT_EMBEDDING_DIM = 228
        private const val ROCCHIO_ALPHA = 1.0f
        private const val ROCCHIO_BETA = 0.75f

        private const val MIN_SCORE_PERCENT = 1
        private const val MAX_SCORE_PERCENT = 99
        private const val MAX_FEEDBACK_HISTORY_SIZE = 100
        private const val EPSILON = 1e-9
    }
}
