package com.jongchan.androidarchi.search.presentation

import com.jongchan.androidarchi.common.domain.sdui.SDUIViewTypeVO
import com.jongchan.androidarchi.common.entity.ai.MediaFeedbackType
import com.jongchan.androidarchi.common.entity.media.MediaItemVO
import com.jongchan.androidarchi.common.entity.media.MediaSearchResultVO
import com.jongchan.androidarchi.common.presentation.mvi.UiState
import com.jongchan.androidarchi.common.presentation.searchList.MediaItemUiState
import com.jongchan.androidarchi.common.presentation.searchList.formatKakaoDateTime
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet

data class SearchUIState(
    val sduiViewItems: ImmutableList<SDUIViewTypeVO>?,
    val query: String,
    val isLoading: Boolean,
    val isLoadingMore: Boolean,
    val isLastPage: Boolean,
    val hasSearched: Boolean,
    val currentPage: Int,
    val searchItemList: ImmutableList<MediaItemUiState>,
    val favoriteUrls: ImmutableSet<String>,
    val rawMediaItems: ImmutableList<MediaItemVO>,
    val feedbackMap: ImmutableMap<String, MediaFeedbackType> = persistentMapOf(),
    val preferenceScores: ImmutableMap<String, Int> = persistentMapOf(),
    val likedCount: Int = 0,
    val dislikedCount: Int = 0,
    val isAiSortEnabled: Boolean = false,
) : UiState {

    val hasFeedbackHistory: Boolean
        get() = likedCount > 0 || dislikedCount > 0

    fun setSDUIViews(sduiViewItems: List<SDUIViewTypeVO>?): SearchUIState = copy(
        sduiViewItems = sduiViewItems?.toImmutableList(),
    )

    fun rawItemByUrl(url: String): MediaItemVO? = rawMediaItems.firstOrNull { it.urlKey == url }

    /**
     * 다음 페이지 결과를 기존 리스트 뒤에 이어 붙인다.
     * 이미 노출 중인 url 은 LazyColumn key 충돌을 막기 위해 제외한다.
     * 응답의 [MediaSearchResultVO.isEnd] 가 true 거나 빈 페이지면 마지막 페이지로 표시해 추가 로드를 멈춘다.
     */
    fun appendPage(
        result: MediaSearchResultVO,
        page: Int,
        newPreferenceScores: ImmutableMap<String, Int>,
    ): SearchUIState {
        val existingUrls = rawMediaItems.mapTo(HashSet(rawMediaItems.size)) { it.urlKey }
        val newItems = result.items.filterNot { it.urlKey in existingUrls }
        val updatedRawItems = (rawMediaItems + newItems).toImmutableList()
        val mergedScores = (preferenceScores + newPreferenceScores).toImmutableMap()

        return copy(
            isLoadingMore = false,
            isLastPage = result.isEnd || result.items.isEmpty(),
            currentPage = page,
            rawMediaItems = updatedRawItems,
            preferenceScores = mergedScores,
            searchItemList = buildDisplayList(
                rawItems = updatedRawItems,
                favoriteUrls = favoriteUrls,
                feedbackMap = feedbackMap,
                preferenceScores = mergedScores,
                isAiSortEnabled = isAiSortEnabled,
            ),
        )
    }

    /** 즐겨찾기 url 집합 변경을 노출 중인 아이템들에 접어 반영한다(상태가 분기를 끝내고 Compose 는 반영만). */
    fun withFavorites(urls: ImmutableSet<String>): SearchUIState = copy(
        favoriteUrls = urls,
        searchItemList = searchItemList
            .map { it.copy(isFavorite = it.contentsUrl in urls) }
            .toImmutableList(),
    )

    /**
     * 유저의 Like/Dislike 피드백 및 온디바이스 AI 매칭 점수 변경을 반영한다.
     */
    fun withPreferences(
        newFeedbackMap: ImmutableMap<String, MediaFeedbackType>,
        newPreferenceScores: ImmutableMap<String, Int>,
        newLikedCount: Int,
        newDislikedCount: Int,
    ): SearchUIState {
        val hasFeedback = newLikedCount > 0 || newDislikedCount > 0
        val effectiveAiSort = if (!hasFeedback) false else isAiSortEnabled
        return copy(
            feedbackMap = newFeedbackMap,
            preferenceScores = newPreferenceScores,
            likedCount = newLikedCount,
            dislikedCount = newDislikedCount,
            isAiSortEnabled = effectiveAiSort,
            searchItemList = buildDisplayList(
                rawItems = rawMediaItems,
                favoriteUrls = favoriteUrls,
                feedbackMap = newFeedbackMap,
                preferenceScores = newPreferenceScores,
                isAiSortEnabled = effectiveAiSort,
            ),
        )
    }

    /**
     * AI 취향순 정렬 ON / OFF 토글을 반영해 리스트를 재구성한다.
     */
    fun toggleAiSort(): SearchUIState {
        val nextSortState = !isAiSortEnabled
        return copy(
            isAiSortEnabled = nextSortState,
            searchItemList = buildDisplayList(
                rawItems = rawMediaItems,
                favoriteUrls = favoriteUrls,
                feedbackMap = feedbackMap,
                preferenceScores = preferenceScores,
                isAiSortEnabled = nextSortState,
            ),
        )
    }

    companion object {
        const val FIRST_PAGE = 1

        val empty = SearchUIState(
            sduiViewItems = persistentListOf(),
            query = "",
            isLoading = false,
            isLoadingMore = false,
            isLastPage = false,
            hasSearched = false,
            currentPage = FIRST_PAGE,
            searchItemList = persistentListOf(),
            favoriteUrls = persistentSetOf(),
            rawMediaItems = persistentListOf(),
            feedbackMap = persistentMapOf(),
            preferenceScores = persistentMapOf(),
            likedCount = 0,
            dislikedCount = 0,
            isAiSortEnabled = false,
        )

        fun fromSearchResult(
            previousState: SearchUIState,
            result: MediaSearchResultVO,
            preferenceScores: ImmutableMap<String, Int>,
        ): SearchUIState {
            val rawItems = result.items.toImmutableList()
            return previousState.copy(
                isLoading = false,
                isLoadingMore = false,
                isLastPage = result.isEnd || result.items.isEmpty(),
                hasSearched = true,
                currentPage = FIRST_PAGE,
                rawMediaItems = rawItems,
                preferenceScores = preferenceScores,
                searchItemList = buildDisplayList(
                    rawItems = rawItems,
                    favoriteUrls = previousState.favoriteUrls,
                    feedbackMap = previousState.feedbackMap,
                    preferenceScores = preferenceScores,
                    isAiSortEnabled = previousState.isAiSortEnabled,
                ),
            )
        }

        private fun buildDisplayList(
            rawItems: List<MediaItemVO>,
            favoriteUrls: Set<String>,
            feedbackMap: Map<String, MediaFeedbackType>,
            preferenceScores: Map<String, Int>,
            isAiSortEnabled: Boolean,
        ): ImmutableList<MediaItemUiState> {
            val mapped = rawItems.map { item ->
                item.toUiState(
                    favoriteUrls = favoriteUrls,
                    feedbackType = feedbackMap[item.urlKey] ?: MediaFeedbackType.NONE,
                    preferenceScore = preferenceScores[item.urlKey],
                )
            }
            if (!isAiSortEnabled || preferenceScores.isEmpty()) {
                return mapped.toImmutableList()
            }
            return mapped
                .sortedWith(
                    compareByDescending<MediaItemUiState> { item ->
                        val baseScore = item.preferenceScore ?: 50
                        val feedbackBonus = when (item.feedbackType) {
                            MediaFeedbackType.LIKE -> 20
                            MediaFeedbackType.DISLIKE -> -30
                            MediaFeedbackType.NONE -> 0
                        }
                        baseScore + feedbackBonus
                    }
                )
                .toImmutableList()
        }
    }
}

private fun MediaItemVO.toUiState(
    favoriteUrls: Set<String>,
    feedbackType: MediaFeedbackType,
    preferenceScore: Int?,
): MediaItemUiState = MediaItemUiState(
    mediaType = type,
    title = title,
    thumbnailUrl = thumbnailImageUrl,
    contentsUrl = urlKey,
    descriptionText = pageLinkUrl,
    collection = collection,
    dateTimeText = formatKakaoDateTime(dateTime),
    page = page,
    isFavorite = urlKey in favoriteUrls,
    feedbackType = feedbackType,
    preferenceScore = preferenceScore,
)

internal fun Set<String>.asImmutable(): ImmutableSet<String> = toImmutableSet()
