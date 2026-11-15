package com.jongchan.androidarchi.search.presentation

import com.jongchan.androidarchi.common.domain.sdui.SDUIViewTypeVO
import com.jongchan.androidarchi.common.entity.ai.AIEngineType
import com.jongchan.androidarchi.common.entity.ai.MediaFeedbackType
import com.jongchan.androidarchi.common.entity.media.MediaItemVO
import com.jongchan.androidarchi.common.entity.media.MediaSearchResultVO
import com.jongchan.androidarchi.common.presentation.mvi.UiState
import com.jongchan.androidarchi.common.presentation.searchList.MediaItemUiState
import com.jongchan.androidarchi.common.presentation.searchList.formatKakaoDateTime
import com.jongchan.androidarchi.search.domain.SearchQuerySuggestionVO
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
    val isGeneratingSuggestion: Boolean = false,
    val querySuggestionSummary: String = "",
    val suggestedKeywords: ImmutableList<String> = persistentListOf(),
    val suggestionEngineType: AIEngineType = AIEngineType.LOCAL_FALLBACK,
) : UiState {

    val hasFeedbackHistory: Boolean
        get() = likedCount > 0 || dislikedCount > 0

    fun setSDUIViews(sduiViewItems: List<SDUIViewTypeVO>?): SearchUIState = copy(
        sduiViewItems = sduiViewItems?.toImmutableList(),
    )

    fun rawItemByUrl(url: String): MediaItemVO? = rawMediaItems.firstOrNull { it.urlKey == url }

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

    fun withFavorites(urls: ImmutableSet<String>): SearchUIState = copy(
        favoriteUrls = urls,
        searchItemList = searchItemList
            .map { it.copy(isFavorite = it.contentsUrl in urls) }
            .toImmutableList(),
    )

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

    fun withQuerySuggestion(suggestion: SearchQuerySuggestionVO): SearchUIState = copy(
        isGeneratingSuggestion = false,
        querySuggestionSummary = suggestion.comment,
        suggestedKeywords = suggestion.suggestedKeywords.toImmutableList(),
        suggestionEngineType = suggestion.engineType,
    )

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
            isGeneratingSuggestion = false,
            querySuggestionSummary = "",
            suggestedKeywords = persistentListOf(),
            suggestionEngineType = AIEngineType.LOCAL_FALLBACK,
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
