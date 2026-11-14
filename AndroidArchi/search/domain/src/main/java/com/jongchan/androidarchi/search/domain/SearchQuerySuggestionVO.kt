package com.jongchan.androidarchi.search.domain

import com.jongchan.androidarchi.common.entity.ai.AIEngineType

/**
 * 검색어 + 유저의 Like/Dislike 취향 히스토리를 바탕으로
 * 생성형 AI(AICore Gemini Nano / Firebase AI Logic)가 제안한 추천 검색어 및 한줄 코멘트 VO.
 */
data class SearchQuerySuggestionVO(
    val comment: String = "",
    val suggestedKeywords: List<String> = emptyList(),
    val engineType: AIEngineType = AIEngineType.LOCAL_FALLBACK,
) {
    val isEmpty: Boolean
        get() = comment.isBlank() && suggestedKeywords.isEmpty()

    companion object {
        val empty: SearchQuerySuggestionVO = SearchQuerySuggestionVO()
    }
}
