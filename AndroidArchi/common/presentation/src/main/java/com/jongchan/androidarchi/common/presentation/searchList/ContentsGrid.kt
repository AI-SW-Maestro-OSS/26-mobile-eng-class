package com.jongchan.androidarchi.common.presentation.searchList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jongchan.androidarchi.common.entity.media.MediaType
import com.jongchan.androidarchi.common.presentation.component.ArchiText
import com.jongchan.androidarchi.common.presentation.jank.JankScrollWatcher
import com.jongchan.androidarchi.common.presentation.ui.theme.DesignSystemTheme
import com.jongchan.androidarchi.common.presentation.ui.theme.DesignSystemThemeImpl
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * 보관함(즐겨찾기) 화면을 2열 그리드로 보여준다. 디자인 스펙 2페이지 "즐겨찾기" 가이드를 따른다.
 * - 정사각 썸네일(R14) 우상단에 하트(보관함이라 항상 on, 탭 시 삭제)
 * - 썸네일 아래 미디어 타입 아이콘 + 타이틀(Bold/15sp), 그 아래 날짜(Normal/13sp)
 * - 좌우 여백 30dp, 열 간격 15dp, 행 간격 40dp
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ContentsGrid(
    items: ImmutableList<MediaItemUiState>,
    onItemClick: (MediaItemUiState) -> Unit,
    onFavoriteClick: (MediaItemUiState, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()

    // 스크롤 구간 동안의 jank 를 별도 버킷으로 누적해 SCROLL_END 시점에 보고한다.
    JankScrollWatcher(gridState)

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        // testTag 가 UiAutomator By.res() 로 보이도록 SemanticsTree 단위로 opt-in.
        modifier = modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
        contentPadding = PaddingValues(horizontal = 30.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp),
    ) {
        items(items = items, key = { it.contentsUrl }) { item ->
            ContentsGridItem(
                item = item,
                onItemClick = onItemClick,
                onFavoriteClick = onFavoriteClick,
            )
        }
    }
}

@Composable
internal fun ContentsGridItem(
    item: MediaItemUiState,
    modifier: Modifier = Modifier,
    onItemClick: (MediaItemUiState) -> Unit,
    onFavoriteClick: (MediaItemUiState, Boolean) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) }
            .testTag(SEARCH_ITEM_TEST_TAG),
    ) {
        MediaItemThumbnail(
            item = item,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            favoriteButtonPadding = 8.dp,
            contentDescription = null,
            onFavoriteClick = onFavoriteClick,
        )

        Spacer(modifier = Modifier.height(8.dp))

        MediaItemHeaderRow(
            item = item,
            titleColor = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel0,
            titleMaxLines = 2,
            fillTitleWeight = false,
            showCollection = false,
        )

        if (item.dateTimeText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            ArchiText(
                text = item.dateTimeText,
                style = DesignSystemThemeImpl.typeScale.textRegularS,
                color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel3,
                maxLines = 1,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ContentsGridPreview() {
    DesignSystemTheme {
        ContentsGrid(
            items = persistentListOf(
                MediaItemUiState(
                    mediaType = MediaType.IMAGE,
                    title = "보관함 샘플 이미지",
                    thumbnailUrl = "",
                    contentsUrl = "https://example.com/1",
                    dateTimeText = "2026년 11월 15일",
                    isFavorite = true,
                ),
                MediaItemUiState(
                    mediaType = MediaType.VIDEO,
                    title = "보관함 샘플 동영상",
                    thumbnailUrl = "",
                    contentsUrl = "https://example.com/2",
                    dateTimeText = "2026년 11월 15일",
                    isFavorite = true,
                ),
            ),
            onItemClick = {},
            onFavoriteClick = { _, _ -> },
        )
    }
}
