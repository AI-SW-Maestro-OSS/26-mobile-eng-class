package com.jongchan.androidarchi.favorite.domain

import com.jongchan.androidarchi.common.entity.ai.AIEngineType

/**
 * 보관함(즐겨찾기) 아이템 목록과 유저의 Like/Dislike 피드백 이력을 바탕으로
 * 생성형 AI(AICore Gemini Nano / Firebase AI Logic)가 분석한 취향 리포트 VO.
 */
data class FavoriteTasteReportVO(
    val summary: String = "",
    val tasteKeywords: List<String> = emptyList(),
    val engineType: AIEngineType = AIEngineType.LOCAL_FALLBACK,
) {
    val isEmpty: Boolean
        get() = summary.isBlank() && tasteKeywords.isEmpty()

    companion object {
        val empty: FavoriteTasteReportVO = FavoriteTasteReportVO()
    }
}
