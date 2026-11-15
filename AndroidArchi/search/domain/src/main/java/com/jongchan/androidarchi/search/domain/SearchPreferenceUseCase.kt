package com.jongchan.androidarchi.search.domain

import com.jongchan.androidarchi.common.domain.base.BaseUseCase
import com.jongchan.androidarchi.common.domain.helper.SearchResultScoringHelper
import com.jongchan.androidarchi.common.domain.helper.MessageHelper
import com.jongchan.androidarchi.common.domain.helper.NavigationHelper
import com.jongchan.androidarchi.common.domain.helper.ResourceHelper
import com.jongchan.androidarchi.common.entity.ai.MediaFeedbackType
import com.jongchan.androidarchi.common.entity.ai.MediaPreferenceFeedbackVO
import com.jongchan.androidarchi.common.entity.ai.toSemanticText
import com.jongchan.androidarchi.common.entity.media.MediaItemVO
import com.jongchan.androidarchi.tti.TTIHelper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 검색 결과 아이템에 대한 유저의 Like / Dislike 피드백 기록 및
 * 온디바이스 AI([SearchResultScoringHelper]) 기반 취향 매칭 점수 산출을 담당하는 UseCase.
 */
class SearchPreferenceUseCase @Inject constructor(
    private val searchResultScoringHelper: SearchResultScoringHelper,
    resourceHelper: ResourceHelper,
    messageHelper: MessageHelper,
    navigationHelper: NavigationHelper,
    ttiHelper: TTIHelper,
) : BaseUseCase(resourceHelper, messageHelper, navigationHelper, ttiHelper) {

    fun observeFeedbacks(): Flow<List<MediaPreferenceFeedbackVO>> =
        searchResultScoringHelper.observeFeedbacks()

    suspend fun recordFeedback(item: MediaItemVO, feedbackType: MediaFeedbackType) {
        val feedback = MediaPreferenceFeedbackVO(
            urlKey = item.urlKey,
            title = item.title,
            collection = item.collection,
            description = item.pageLinkUrl,
            mediaType = item.type,
            feedbackType = feedbackType,
        )
        searchResultScoringHelper.recordFeedback(feedback)
    }

    suspend fun clearFeedbacks() {
        searchResultScoringHelper.clearFeedbacks()
    }

    suspend fun evaluateScores(items: List<MediaItemVO>): Map<String, Int> {
        if (items.isEmpty()) return emptyMap()
        val targetTexts = items.associate { item -> item.urlKey to item.toSemanticText() }
        return searchResultScoringHelper.evaluatePreferenceScores(targetTexts)
    }
}
