package com.jongchan.androidarchi.search.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jongchan.androidarchi.common.presentation.R
import com.jongchan.androidarchi.common.presentation.component.ArchiText
import com.jongchan.androidarchi.common.presentation.sdui.SDUIRootView
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
                        onLoadMore = { onIntent(SearchIntent.LoadNextPage) },
                        onItemClick = { onIntent(SearchIntent.OpenFullScreen(it.contentsUrl)) },
                        onFavoriteClick = { item, isFavorite ->
                            onIntent(
                                if (isFavorite) SearchIntent.DeleteFavorite(item.contentsUrl)
                                else SearchIntent.AddFavorite(item.contentsUrl)
                            )
                        },
                    )
                }
            }
        }
    }
}
