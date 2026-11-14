package com.jongchan.androidarchi.common.domain.helper

import com.jongchan.androidarchi.common.entity.ai.MediaPreferenceFeedbackVO
import kotlinx.coroutines.flow.Flow

/**
 * 온디바이스 AI(Google MediaPipe Tasks TextEmbedder)를 활용한 텍스트 임베딩 추론 및
 * 유저 Like / Dislike 피드백 기반 취향 매칭 점수 계산을 제공하는 도메인 헬퍼 인터페이스.
 *
 * - 도메인/프리젠테이션 레이어는 MediaPipe SDK 에 직접 의존하지 않고 이 인터페이스를 DI 로 주입받아 사용한다.
 * - 구현체([com.jongchan.androidarchi.common.presentation.helper.AIBasedFeatureHelperImpl])는
 *   MediaPipe `TextEmbedder` 추론과 Rocchio 취향 벡터($V_{user} = \alpha V_{like} - \beta V_{dislike}$) 계산,
 *   그리고 유저 피드백 히스토리 관리를 담당한다.
 */
interface SearchResultScoringHelper {
    /**
     * 유저가 남긴 Like / Dislike 피드백 목록의 실시간 스트림을 반환한다.
     */
    fun observeFeedbacks(): Flow<List<MediaPreferenceFeedbackVO>>

    /**
     * 특정 미디어 아이템에 대한 유저 피드백(Like / Dislike / None)을 기록한다.
     * [MediaPreferenceFeedbackVO.feedbackType] 이 `NONE` 이면 기존 피드백을 제거한다.
     */
    suspend fun recordFeedback(feedback: MediaPreferenceFeedbackVO)

    /**
     * 누적된 유저 Like / Dislike 피드백 히스토리를 모두 초기화한다.
     */
    suspend fun clearFeedbacks()

    /**
     * 주어진 텍스트를 정규화된 온디바이스 AI 임베딩 벡터로 변환한다.
     */
    suspend fun embedText(text: String): FloatArray

    /**
     * 두 임베딩 벡터 간의 코사인 유사도(-1.0 ~ 1.0)를 계산한다.
     */
    fun cosineSimilarity(u: FloatArray, v: FloatArray): Double

    /**
     * 명시적으로 전달된 Like / Dislike 텍스트 목록으로부터 Rocchio 유저 취향 벡터를 구성해
     * 각 대상 텍스트(`key -> semanticText`)의 취향 매칭 점수(0 ~ 100%)를 계산한다.
     */
    suspend fun calculatePreferenceScores(
        targetTexts: Map<String, String>,
        likedTexts: List<String>,
        dislikedTexts: List<String>,
    ): Map<String, Int>

    /**
     * 현재 저장된 유저의 Like / Dislike 피드백 히스토리를 바탕으로
     * 각 대상 텍스트(`key -> semanticText`)의 취향 매칭 점수(0 ~ 100%)를 계산한다.
     */
    suspend fun evaluatePreferenceScores(
        targetTexts: Map<String, String>,
    ): Map<String, Int>
}
