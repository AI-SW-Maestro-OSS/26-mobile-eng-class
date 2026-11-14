package com.jongchan.androidarchi.search.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jongchan.androidarchi.common.presentation.R
import com.jongchan.androidarchi.common.presentation.component.ArchiText
import com.jongchan.androidarchi.common.presentation.searchList.ContentsList
import com.jongchan.androidarchi.common.presentation.searchList.MediaSearchBar
import com.jongchan.androidarchi.common.presentation.ui.theme.DesignSystemThemeImpl

@Composable
fun SearchPage(viewModel: SearchViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchPageContent(uiState = uiState, onIntent = viewModel::onIntent)
}

@Composable
private fun SearchPageContent(
    uiState: SearchUIState,
    onIntent: (SearchIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignSystemThemeImpl.designSystemColor.bgDefaultLevel1)
    ) {
        MediaSearchBar(
            query = uiState.query,
            onQueryChange = { onIntent(SearchIntent.QueryChanged(it)) },
            onSearch = { onIntent(SearchIntent.Search(it)) },
            onClear = { onIntent(SearchIntent.Clear) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
        )

        if (uiState.hasSearched && uiState.searchItemList.isNotEmpty()) {
            AIPreferenceControlBar(
                hasFeedbackHistory = uiState.hasFeedbackHistory,
                likedCount = uiState.likedCount,
                dislikedCount = uiState.dislikedCount,
                isAiSortEnabled = uiState.isAiSortEnabled,
                onToggleAiSort = { onIntent(SearchIntent.ToggleAiSort) },
                onClearFeedbacks = { onIntent(SearchIntent.ClearFeedbacks) },
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = DesignSystemThemeImpl.designSystemColor.borderDefaultLevel2,
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading && uiState.searchItemList.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                !uiState.hasSearched -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_search_24),
                            contentDescription = null,
                            modifier = Modifier
                                .width(120.dp)
                                .align(Alignment.CenterHorizontally),
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        ArchiText(
                            text = stringResource(R.string.search_prompt),
                            style = DesignSystemThemeImpl.typeScale.textRegularL,
                            color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel3,
                            maxLines = Int.MAX_VALUE,
                        )
                    }
                }

                uiState.searchItemList.isEmpty() -> {
                    ArchiText(
                        text = stringResource(R.string.media_empty),
                        style = DesignSystemThemeImpl.typeScale.textRegularL,
                        color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel3,
                        maxLines = Int.MAX_VALUE,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    ContentsList(
                        items = uiState.searchItemList,
                        isLoadingMore = uiState.isLoadingMore,
                        isLastPage = uiState.isLastPage,
                        isAiSortEnabled = uiState.isAiSortEnabled,
                        onLoadMore = { onIntent(SearchIntent.LoadNextPage) },
                        onItemClick = { onIntent(SearchIntent.OpenFullScreen(it.contentsUrl)) },
                        onFavoriteClick = { item, isFavorite ->
                            onIntent(
                                if (isFavorite) SearchIntent.DeleteFavorite(item.contentsUrl)
                                else SearchIntent.AddFavorite(item.contentsUrl)
                            )
                        },
                        onFeedbackClick = { item, targetType ->
                            onIntent(SearchIntent.ToggleFeedback(item.contentsUrl, targetType))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AIPreferenceControlBar(
    hasFeedbackHistory: Boolean,
    likedCount: Int,
    dislikedCount: Int,
    isAiSortEnabled: Boolean,
    onToggleAiSort: () -> Unit,
    onClearFeedbacks: () -> Unit,
) {
    val toggleShape = RoundedCornerShape(10.dp)
    val resetShape = RoundedCornerShape(8.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DesignSystemThemeImpl.designSystemColor.bgDefaultLevel0)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArchiText(
                text = if (hasFeedbackHistory) {
                    stringResource(R.string.ai_preference_learned, likedCount, dislikedCount)
                } else {
                    stringResource(R.string.ai_preference_guide)
                },
                style = DesignSystemThemeImpl.typeScale.textRegularS,
                color = if (hasFeedbackHistory) {
                    DesignSystemThemeImpl.designSystemColor.contentDefaultLevel1
                } else {
                    DesignSystemThemeImpl.designSystemColor.contentDefaultLevel3
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )

            if (hasFeedbackHistory) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(resetShape)
                        .background(DesignSystemThemeImpl.designSystemColor.bgDefaultLevel1)
                        .border(
                            width = 1.dp,
                            color = DesignSystemThemeImpl.designSystemColor.borderDefaultLevel1,
                            shape = resetShape,
                        )
                        .clickable(onClick = onClearFeedbacks)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    ArchiText(
                        text = stringResource(R.string.ai_preference_reset),
                        style = DesignSystemThemeImpl.typeScale.textRegularXS,
                        color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel2,
                        maxLines = 1,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        val sortBackgroundColor = if (isAiSortEnabled) {
            DesignSystemThemeImpl.designSystemColor.contentAccent
        } else {
            DesignSystemThemeImpl.designSystemColor.bgDefaultLevel1
        }
        val sortBorderColor = if (isAiSortEnabled) {
            DesignSystemThemeImpl.designSystemColor.contentAccent
        } else {
            DesignSystemThemeImpl.designSystemColor.borderDefaultLevel1
        }
        val sortTextColor = if (isAiSortEnabled) {
            DesignSystemThemeImpl.designSystemColor.bgDefaultLevel1
        } else {
            DesignSystemThemeImpl.designSystemColor.contentDefaultLevel1
        }

        Box(
            modifier = Modifier
                .clip(toggleShape)
                .background(sortBackgroundColor)
                .border(width = 1.dp, color = sortBorderColor, shape = toggleShape)
                .clickable(onClick = onToggleAiSort)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            ArchiText(
                text = if (isAiSortEnabled) {
                    stringResource(R.string.ai_sort_toggle_on)
                } else {
                    stringResource(R.string.ai_sort_toggle_off)
                },
                style = DesignSystemThemeImpl.typeScale.textStrongM,
                color = sortTextColor,
                maxLines = 1,
            )
        }
    }
}
