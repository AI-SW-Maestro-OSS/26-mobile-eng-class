package com.jongchan.androidarchi.favorite.domain

import com.jongchan.androidarchi.common.domain.base.BaseUseCase
import com.jongchan.androidarchi.common.domain.helper.AIBasedFeatureHelper
import com.jongchan.androidarchi.common.domain.helper.MessageHelper
import com.jongchan.androidarchi.common.domain.helper.NavigationHelper
import com.jongchan.androidarchi.common.domain.helper.ResourceHelper
import com.jongchan.androidarchi.common.domain.helper.SearchResultScoringHelper
import com.jongchan.androidarchi.common.entity.ai.AIEngineType
import com.jongchan.androidarchi.common.entity.ai.AIExecutionPolicy
import com.jongchan.androidarchi.common.entity.ai.AIServiceRequest
import com.jongchan.androidarchi.common.entity.ai.MediaFeedbackType
import com.jongchan.androidarchi.common.entity.ai.MediaPreferenceFeedbackVO
import com.jongchan.androidarchi.common.entity.favorite.FavoriteItemVO
import com.jongchan.androidarchi.common.entity.media.MediaType
import com.jongchan.androidarchi.tti.TTIHelper
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 즐겨찾기(보관함) 아이템들과 유저의 Like/Dislike 피드백 이력을 바탕으로
 * [AIBasedFeatureHelper] 의 `GenerateText` 브릿지(1순위: AICore Gemini Nano -> 2순위: Firebase AI Logic)를 호출해
 * 보관함 취향 분석 리포트([FavoriteTasteReportVO])를 생성하는 UseCase.
 */
class FavoriteTasteReportUseCase @Inject constructor(
    private val searchResultScoringHelper: SearchResultScoringHelper,
    private val aiBasedFeatureHelper: AIBasedFeatureHelper,
    resourceHelper: ResourceHelper,
    messageHelper: MessageHelper,
    navigationHelper: NavigationHelper,
    ttiHelper: TTIHelper,
) : BaseUseCase(resourceHelper, messageHelper, navigationHelper, ttiHelper) {

    suspend operator fun invoke(
        favoriteItems: List<FavoriteItemVO>,
        feedbacksOverride: List<MediaPreferenceFeedbackVO>? = null,
    ): FavoriteTasteReportVO {
        if (favoriteItems.isEmpty()) return FavoriteTasteReportVO.empty

        val feedbacks = feedbacksOverride ?: searchResultScoringHelper.observeFeedbacks().first()
        val favoriteSummary = favoriteItems
            .take(8)
            .map { "${it.title} (${it.type.name})" }
        val likedSummary = feedbacks
            .filter { it.feedbackType == MediaFeedbackType.LIKE }
            .take(5)
            .map { it.title }
            .filter { it.isNotBlank() }

        val prompt = buildString {
            appendLine("유저가 보관함에 저장한 미디어 목록(총 ${favoriteItems.size}건): ${favoriteSummary.joinToString(", ")}")
            if (likedSummary.isNotEmpty()) {
                appendLine("유저가 추가로 좋아요(👍) 평가한 콘텐츠: ${likedSummary.joinToString(", ")}")
            }
            appendLine()
            appendLine("위 컬렉션을 분석해 다음 두 줄 형식만 정확히 출력해줘:")
            appendLine("SUMMARY: (유저의 미디어 수집 취향을 요약하는 한국어 1~2문장, 60자 이내)")
            appendLine("TAGS: (핵심 취향 키워드 4개, 쉼표로만 구분, 해시태그 기호 제외)")
        }

        val request = AIServiceRequest.GenerateText(
            prompt = prompt,
            systemInstruction = "당신은 유저의 미디어 보관함 취향을 분석하는 AI 큐레이터입니다. SUMMARY와 TAGS 두 줄 형식으로만 응답하세요.",
            temperature = 0.4f,
            maxOutputTokens = 200,
            policy = AIExecutionPolicy.PREFER_ON_DEVICE_FALLBACK_CLOUD,
        )

        val generated = aiBasedFeatureHelper.execute(request).getOrNull()
        if (generated != null && generated.text.isNotBlank()) {
            val parsed = parseTasteReport(generated.text, generated.engineType)
            if (!parsed.isEmpty) return parsed
        }

        return buildFallbackTasteReport(favoriteItems, feedbacks)
    }

    private fun parseTasteReport(
        rawText: String,
        engineType: AIEngineType,
    ): FavoriteTasteReportVO {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val summaryLine = lines.firstOrNull { it.startsWith("SUMMARY:", ignoreCase = true) }
            ?.substringAfter(":")
            ?.trim()
            ?: lines.firstOrNull { !it.startsWith("TAGS:", ignoreCase = true) }.orEmpty()

        val tagsLine = lines.firstOrNull { it.startsWith("TAGS:", ignoreCase = true) }
            ?.substringAfter(":")
            ?.trim()
            .orEmpty()

        val tags = tagsLine
            .split(",", "·", "|", "/")
            .map { it.trim().removePrefix("#").trim('"', '\'', '[', ']') }
            .filter { it.isNotBlank() }
            .distinct()
            .take(4)

        return FavoriteTasteReportVO(
            summary = summaryLine,
            tasteKeywords = tags,
            engineType = engineType,
        )
    }

    private fun buildFallbackTasteReport(
        favoriteItems: List<FavoriteItemVO>,
        feedbacks: List<MediaPreferenceFeedbackVO>,
    ): FavoriteTasteReportVO {
        val imageCount = favoriteItems.count { it.type == MediaType.IMAGE }
        val videoCount = favoriteItems.count { it.type == MediaType.VIDEO }
        val dominantMediaLabel = when {
            imageCount > videoCount * 2 -> "감각적인 이미지 위주"
            videoCount > imageCount -> "생동감 있는 동영상 위주"
            else -> "이미지와 동영상의 균형 잡힌"
        }

        val frequentWords = (
            favoriteItems.map { it.title } +
                feedbacks.filter { it.feedbackType == MediaFeedbackType.LIKE }.map { it.title }
            )
            .flatMap { it.split(WORD_REGEX) }
            .map { it.trim() }
            .filter { it.length >= 2 }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(4)

        val tags = (frequentWords + listOf("취향컬렉션", if (videoCount > imageCount) "영상클립" else "고화질미디어"))
            .distinct()
            .take(4)

        val topKeyword = frequentWords.firstOrNull() ?: "다양한 주제"
        val summary = "총 ${favoriteItems.size}개의 보관함 아이템 분석 결과, '$topKeyword' 중심의 $dominantMediaLabel 컬렉션을 선호하시네요!"

        return FavoriteTasteReportVO(
            summary = summary,
            tasteKeywords = tags,
            engineType = AIEngineType.LOCAL_FALLBACK,
        )
    }

    private companion object {
        val WORD_REGEX = Regex("[^\\p{L}\\p{N}]+")
    }
}
