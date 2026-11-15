package com.jongchan.androidarchi.search.domain

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
import com.jongchan.androidarchi.common.entity.media.MediaItemVO
import com.jongchan.androidarchi.tti.TTIHelper
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 현재 검색어 + 검색 결과 컨텍스트 + 유저의 Like / Dislike 피드백 이력을 바탕으로
 * [AIBasedFeatureHelper] 의 `GenerateText` 브릿지(1순위: AICore Gemini Nano -> 2순위: Firebase AI Logic)를 호출해
 * 연관 추천 검색어 칩([SearchQuerySuggestionVO])을 생성하는 UseCase.
 */
class SearchQuerySuggestionUseCase @Inject constructor(
    private val aiBasedFeatureHelper: AIBasedFeatureHelper,
    private val searchResultScoringHelper: SearchResultScoringHelper,
    resourceHelper: ResourceHelper,
    messageHelper: MessageHelper,
    navigationHelper: NavigationHelper,
    ttiHelper: TTIHelper,
) : BaseUseCase(resourceHelper, messageHelper, navigationHelper, ttiHelper) {

    suspend operator fun invoke(
        query: String,
        items: List<MediaItemVO>,
        feedbacksOverride: List<MediaPreferenceFeedbackVO>? = null,
    ): SearchQuerySuggestionVO {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return SearchQuerySuggestionVO.empty

        val feedbacks = feedbacksOverride ?: searchResultScoringHelper.observeFeedbacks().first()
        val likedTitles = feedbacks
            .filter { it.feedbackType == MediaFeedbackType.LIKE }
            .take(5)
            .map { "${it.title} (${it.collection})" }
        val dislikedTitles = feedbacks
            .filter { it.feedbackType == MediaFeedbackType.DISLIKE }
            .take(5)
            .map { "${it.title} (${it.collection})" }
        val sampleTitles = items
            .take(6)
            .map { "${it.title} (${it.collection})" }

        val prompt = buildString {
            appendLine("현재 검색어: \"$trimmedQuery\"")
            if (sampleTitles.isNotEmpty()) {
                appendLine("현재 검색 결과 주요 타이틀: ${sampleTitles.joinToString(", ")}")
            }
            if (likedTitles.isNotEmpty()) {
                appendLine("유저가 좋아요(👍) 한 콘텐츠: ${likedTitles.joinToString(", ")}")
            }
            if (dislikedTitles.isNotEmpty()) {
                appendLine("유저가 싫어요(👎) 한 콘텐츠: ${dislikedTitles.joinToString(", ")}")
            }
            appendLine()
            appendLine("위 검색 맥락과 유저 취향을 반영하여, 유저가 다음으로 검색해보면 좋을 한국어 확장 검색어 4개와 한 줄 추천 이유를 아래 형식으로만 답해줘.")
            appendLine("KEYWORDS: 키워드1, 키워드2, 키워드3, 키워드4")
            appendLine("REASON: (유저 취향을 반영한 한 줄 추천 코멘트, 45자 이내)")
        }

        val request = AIServiceRequest.GenerateText(
            prompt = prompt,
            systemInstruction = "당신은 모바일 미디어 검색 앱의 스마트 검색어 추천 어시스턴트입니다. 반드시 KEYWORDS와 REASON 두 줄 형식으로만 간결하게 답하세요.",
            temperature = 0.4f,
            maxOutputTokens = 160,
            policy = AIExecutionPolicy.PREFER_ON_DEVICE_FALLBACK_CLOUD,
        )

        val generated = aiBasedFeatureHelper.execute(request).getOrNull()
        if (generated != null && generated.text.isNotBlank()) {
            val parsed = parseSuggestionResponse(
                rawText = generated.text,
                engineType = generated.engineType,
                originalQuery = trimmedQuery,
            )
            if (parsed.suggestedKeywords.isNotEmpty()) {
                return parsed
            }
        }

        return buildLocalHeuristicSuggestion(
            query = trimmedQuery,
            items = items,
            feedbacks = feedbacks,
        )
    }

    private fun parseSuggestionResponse(
        rawText: String,
        engineType: AIEngineType,
        originalQuery: String,
    ): SearchQuerySuggestionVO {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val keywordsLine = lines.firstOrNull { it.startsWith("KEYWORDS:", ignoreCase = true) }
            ?.substringAfter(":")
            ?.trim()
            ?: lines.firstOrNull().orEmpty()
        val reasonLine = lines.firstOrNull { it.startsWith("REASON:", ignoreCase = true) }
            ?.substringAfter(":")
            ?.trim()
            ?: "'$originalQuery' 검색 결과와 선호도를 반영한 AI 추천 검색어입니다."

        val keywords = keywordsLine
            .split(",", "·", "|", "/")
            .map { it.trim().removePrefix("#").trim('"', '\'', '[', ']') }
            .filter { it.isNotBlank() && !it.equals(originalQuery, ignoreCase = true) }
            .distinct()
            .take(4)

        return SearchQuerySuggestionVO(
            comment = reasonLine,
            suggestedKeywords = keywords,
            engineType = engineType,
        )
    }

    private fun buildLocalHeuristicSuggestion(
        query: String,
        items: List<MediaItemVO>,
        feedbacks: List<MediaPreferenceFeedbackVO>,
    ): SearchQuerySuggestionVO {
        val likedKeywords = feedbacks
            .filter { it.feedbackType == MediaFeedbackType.LIKE }
            .flatMap { listOf(it.collection, it.title) }
            .flatMap { it.split(WORD_SPLIT_REGEX) }
            .map { it.trim() }
            .filter { it.length >= 2 && !it.equals(query, ignoreCase = true) }
            .distinct()
            .take(2)

        val itemCollections = items
            .map { it.collection.trim() }
            .filter { it.isNotBlank() && !it.equals(query, ignoreCase = true) }
            .distinct()
            .take(2)

        val candidates = buildList {
            likedKeywords.forEach { add("$query $it") }
            itemCollections.forEach { add("$query $it") }
            add("$query 고화질")
            add("$query 추천")
            add("$query 인기 클립")
            add("$query 배경화면")
        }
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.equals(query, ignoreCase = true) }
            .distinct()
            .take(4)

        val summary = if (likedKeywords.isNotEmpty()) {
            "좋아요 피드백(${likedKeywords.joinToString(", ")}) 취향을 반영해 확장한 추천 검색어예요."
        } else {
            "'$query' 검색 결과 카테고리를 바탕으로 확장한 추천 검색어예요."
        }

        return SearchQuerySuggestionVO(
            comment = summary,
            suggestedKeywords = candidates,
            engineType = AIEngineType.LOCAL_FALLBACK,
        )
    }

    private companion object {
        val WORD_SPLIT_REGEX = Regex("[^\\p{L}\\p{N}]+")
    }
}
