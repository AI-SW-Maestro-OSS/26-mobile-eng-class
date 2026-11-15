package com.jongchan.androidarchi.common.presentation.searchList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jongchan.androidarchi.common.entity.ai.MediaFeedbackType
import com.jongchan.androidarchi.common.entity.media.MediaType
import com.jongchan.androidarchi.common.presentation.component.ArchiText
import com.jongchan.androidarchi.common.presentation.jank.JankScrollWatcher
import com.jongchan.androidarchi.common.presentation.ui.theme.DesignSystemTheme
import com.jongchan.androidarchi.common.presentation.ui.theme.DesignSystemThemeImpl
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private val THUMBNAIL_SIZE = 90.dp

// 마지막 콘텐츠 아이템으로부터 이만큼 앞 지점이 보이면 다음 페이지를 미리 불러온다.
private const val LOAD_MORE_PREFETCH_DISTANCE = 3

/**
 * Baseline Profile generator (그리고 향후 Compose UI / Macrobenchmark 테스트) 가
 * UiAutomator `By.res(packageName, "search_item")` 으로 리스트 아이템을 찾을 수 있도록
 * [Modifier.testTag] 를 안드로이드 view resource-id 로 노출시키는 용도이다.
 * 모든 셀에 동일하게 붙으므로 `findObjects(...).first()` 로 스크롤 위치와 무관하게
 * 화면에 보이는 셀을 선택할 수 있다.
 */
const val SEARCH_ITEM_TEST_TAG = "search_item"

/**
 * 검색 결과(이미지+동영상)를 세로 리스트로 보여준다. 디자인 스펙의 list 가이드를 따른다.
 * - 좌측 썸네일(90dp, R14) + 우측 텍스트(타이틀/주소/시간 + AI 매칭 점수 배지 및 취향 피드백 칩)
 * - 이미지 아이템만 타이틀 우측에 카테고리(collection) 노출, 동영상은 미노출
 * - 기본 정렬에서는 페이지 경계마다 페이지 번호 구분자를 노출하고, AI 추천순 정렬에서는 점수 순 단일 리스트로 노출
 * - 하단 근처까지 스크롤되면 [onLoadMore] 로 다음 페이지 로딩을 요청한다.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ContentsList(
    items: ImmutableList<MediaItemUiState>,
    onItemClick: (MediaItemUiState) -> Unit,
    onFavoriteClick: (MediaItemUiState, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isLoadingMore: Boolean = false,
    isLastPage: Boolean = true,
    isAiSortEnabled: Boolean = false,
    onLoadMore: () -> Unit = {},
    onFeedbackClick: ((MediaItemUiState, MediaFeedbackType) -> Unit)? = null,
) {
    val pageGroups = remember(items, isAiSortEnabled) {
        if (isAiSortEnabled) {
            emptyList()
        } else {
            items.groupBy { it.page }.entries.sortedBy { it.key }.map { it.key to it.value }
        }
    }
    val listState = rememberLazyListState()

    // 스크롤 구간 동안의 jank 를 별도 버킷으로 누적해 SCROLL_END 시점에 보고한다.
    JankScrollWatcher(listState)

    ObserveLoadMoreThreshold(
        listState = listState,
        isLoadingMore = isLoadingMore,
        isLastPage = isLastPage,
        onLoadMore = onLoadMore,
    )

    LazyColumn(
        state = listState,
        // testTag 가 UiAutomator By.res() 로 보이도록 SemanticsTree 단위로 opt-in.
        modifier = modifier
            .fillMaxWidth()
            .semantics { testTagsAsResourceId = true },
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        if (isAiSortEnabled) {
            aiSortedContentsItems(
                items = items,
                onItemClick = onItemClick,
                onFavoriteClick = onFavoriteClick,
                onFeedbackClick = onFeedbackClick,
            )
        } else {
            pagedContentsItems(
                pageGroups = pageGroups,
                onItemClick = onItemClick,
                onFavoriteClick = onFavoriteClick,
                onFeedbackClick = onFeedbackClick,
            )
        }
        item(key = "list_footer") {
            ContentsListFooter(
                isLoadingMore = isLoadingMore,
                isLastPage = isLastPage,
            )
        }
    }
}

/**
 * 리스트 하단 도달 여부를 감지하여 다음 페이지 프리페치([onLoadMore])를 트리거한다.
 */
@Composable
private fun ObserveLoadMoreThreshold(
    listState: LazyListState,
    isLoadingMore: Boolean,
    isLastPage: Boolean,
    onLoadMore: () -> Unit,
) {
    val onLoadMoreState by rememberUpdatedState(onLoadMore)
    val reachedLoadMoreThreshold by remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            totalItems > 0 && lastVisibleIndex >= totalItems - 1 - LOAD_MORE_PREFETCH_DISTANCE
        }
    }
    LaunchedEffect(reachedLoadMoreThreshold, isLoadingMore, isLastPage) {
        if (reachedLoadMoreThreshold && !isLoadingMore && !isLastPage) {
            onLoadMoreState()
        }
    }
}

private fun LazyListScope.aiSortedContentsItems(
    items: ImmutableList<MediaItemUiState>,
    onItemClick: (MediaItemUiState) -> Unit,
    onFavoriteClick: (MediaItemUiState, Boolean) -> Unit,
    onFeedbackClick: ((MediaItemUiState, MediaFeedbackType) -> Unit)?,
) {
    items(items = items, key = { it.contentsUrl }) { item ->
        ContentsListItem(
            item = item,
            onItemClick = onItemClick,
            onFavoriteClick = onFavoriteClick,
            onFeedbackClick = onFeedbackClick,
        )
    }
}

private fun LazyListScope.pagedContentsItems(
    pageGroups: List<Pair<Int, List<MediaItemUiState>>>,
    onItemClick: (MediaItemUiState) -> Unit,
    onFavoriteClick: (MediaItemUiState, Boolean) -> Unit,
    onFeedbackClick: ((MediaItemUiState, MediaFeedbackType) -> Unit)?,
) {
    pageGroups.forEachIndexed { groupIndex, (page, groupItems) ->
        items(items = groupItems, key = { it.contentsUrl }) { item ->
            ContentsListItem(
                item = item,
                onItemClick = onItemClick,
                onFavoriteClick = onFavoriteClick,
                onFeedbackClick = onFeedbackClick,
            )
        }
        if (groupIndex < pageGroups.lastIndex) {
            item(key = "page_divider_$page") {
                PageNumberSeparator(page = page)
            }
        }
    }
}

/**
 * 검색 결과 리스트의 개별 아이템 행(좌측 썸네일 + 우측 상세 정보 및 AI 피드백 영역).
 */
@Composable
internal fun ContentsListItem(
    item: MediaItemUiState,
    modifier: Modifier = Modifier,
    onItemClick: (MediaItemUiState) -> Unit,
    onFavoriteClick: (MediaItemUiState, Boolean) -> Unit,
    onFeedbackClick: ((MediaItemUiState, MediaFeedbackType) -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) }
            .testTag(SEARCH_ITEM_TEST_TAG)
            .padding(horizontal = 20.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MediaItemThumbnail(
            item = item,
            modifier = Modifier.size(THUMBNAIL_SIZE),
            favoriteButtonPadding = 6.dp,
            contentDescription = item.title,
            onFavoriteClick = onFavoriteClick,
        )

        Spacer(modifier = Modifier.width(10.dp))

        ContentsListItemBody(
            item = item,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onFeedbackClick = onFeedbackClick,
        )
    }
}

/**
 * 검색 결과 리스트 셀 우측 본문 영역(헤더 + 주소 설명 + 날짜/AI 점수/피드백 행).
 */
@Composable
private fun ContentsListItemBody(
    item: MediaItemUiState,
    modifier: Modifier = Modifier,
    onFeedbackClick: ((MediaItemUiState, MediaFeedbackType) -> Unit)? = null,
) {
    Column(modifier = modifier) {
        MediaItemHeaderRow(
            item = item,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(6.dp))
        ArchiText(
            text = item.descriptionText,
            style = DesignSystemThemeImpl.typeScale.textRegularS,
            color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel3,
            maxLines = 2,
        )
        Spacer(modifier = Modifier.height(6.dp))
        ContentsItemMetaAndAiRow(
            item = item,
            onFeedbackClick = onFeedbackClick,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ContentsListPreview() {
    DesignSystemTheme {
        ContentsList(
            items = persistentListOf(
                MediaItemUiState(
                    mediaType = MediaType.IMAGE,
                    title = "Daum카페",
                    descriptionText = "https://cafe.daum.net/seonglee/V5Z9/82656",
                    thumbnailUrl = "",
                    contentsUrl = "https://cafe.daum.net/seonglee/V5Z9/82656",
                    collection = "cafe",
                    dateTimeText = "2026년 11월 15일",
                    feedbackType = MediaFeedbackType.LIKE,
                    preferenceScore = 85,
                ),
            ),
            onItemClick = {},
            onFavoriteClick = { _, _ -> },
            onFeedbackClick = { _, _ -> },
        )
    }
}
