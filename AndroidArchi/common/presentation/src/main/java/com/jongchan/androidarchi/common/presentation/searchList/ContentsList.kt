package com.jongchan.androidarchi.common.presentation.searchList

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.jongchan.androidarchi.common.entity.ai.MediaFeedbackType
import com.jongchan.androidarchi.common.presentation.R
import com.jongchan.androidarchi.common.presentation.component.ArchiText
import com.jongchan.androidarchi.common.presentation.imageUtil.rememberImageLoader
import com.jongchan.androidarchi.common.presentation.jank.JankScrollWatcher
import com.jongchan.androidarchi.common.presentation.ui.theme.DesignSystemThemeImpl
import kotlinx.collections.immutable.ImmutableList

private val THUMBNAIL_SIZE = 90.dp
private val ThumbnailShape = RoundedCornerShape(14.dp)

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
 * - 좌측 썸네일(90dp, R14) + 우측 텍스트(타이틀/주소/시간), 텍스트는 썸네일 기준 수직 중앙정렬
 * - 이미지 아이템만 타이틀 우측에 카테고리(collection) 노출, 동영상은 미노출
 * - 페이지가 바뀌는 경계에 페이지 번호 구분자, 마지막 페이지 끝에 "마지막" 표시
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
    // 기본 정렬에서는 페이지별로 그룹핑해 경계 구분자를 그리고, AI 취향순 정렬에서는 전체를 점수 순으로 그린다.
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

    // 마지막 콘텐츠 아이템이 화면 하단 근처에 들어오면 다음 페이지 로딩을 트리거한다.
    val onLoadMoreState by rememberUpdatedState(onLoadMore)
    val reachedLoadMoreThreshold by remember {
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

    LazyColumn(
        state = listState,
        // testTag 가 UiAutomator By.res() 로 보이도록 SemanticsTree 단위로 opt-in.
        modifier = modifier
            .fillMaxWidth()
            .semantics { testTagsAsResourceId = true },
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        if (isAiSortEnabled) {
            items(items = items, key = { it.contentsUrl }) { item ->
                ContentsListItem(
                    item = item,
                    onItemClick = onItemClick,
                    onFavoriteClick = onFavoriteClick,
                    onFeedbackClick = onFeedbackClick,
                )
            }
        } else {
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
                    item(key = "page_divider_$page") { PageNumberSeparator(page) }
                }
            }
        }
        item(key = "list_footer") {
            when {
                isLoadingMore -> LoadMoreIndicator()
                isLastPage -> ListEndMarker()
            }
        }
    }
}

@Composable
private fun ContentsListItem(
    item: MediaItemUiState,
    onItemClick: (MediaItemUiState) -> Unit,
    onFavoriteClick: (MediaItemUiState, Boolean) -> Unit,
    onFeedbackClick: ((MediaItemUiState, MediaFeedbackType) -> Unit)? = null,
) {
    val imageLoader = rememberImageLoader()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) }
            .testTag(SEARCH_ITEM_TEST_TAG)
            .padding(horizontal = 20.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(THUMBNAIL_SIZE)
                .border(
                    width = 1.dp,
                    color = DesignSystemThemeImpl.designSystemColor.borderDefaultLevel2,
                    shape = ThumbnailShape,
                )
                .clip(ThumbnailShape)
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                imageLoader = imageLoader,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(THUMBNAIL_SIZE)
                    .background(DesignSystemThemeImpl.designSystemColor.bgDefaultLevel0),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onFavoriteClick(item, item.isFavorite) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(item.favoriteIconRes),
                    contentDescription = item.title,
                    tint = if (item.isFavorite) DesignSystemThemeImpl.designSystemColor.contentFavorite else Color.Unspecified,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(item.mediaTypeIconRes),
                    contentDescription = item.mediaType.name,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                ArchiText(
                    text = item.title.ifEmpty { stringResource(R.string.media_default_title) },
                    style = DesignSystemThemeImpl.typeScale.textStrongM,
                    color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (item.collection.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    ArchiText(
                        text = item.collection,
                        style = DesignSystemThemeImpl.typeScale.textRegularM,
                        color = DesignSystemThemeImpl.designSystemColor.contentAccent,
                        maxLines = 1,
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            ArchiText(
                text = item.descriptionText,
                style = DesignSystemThemeImpl.typeScale.textRegularS,
                color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel3,
                maxLines = 2,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false),
                ) {
                    ArchiText(
                        text = item.dateTimeText,
                        style = DesignSystemThemeImpl.typeScale.textRegularXS,
                        color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel3,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    item.preferenceScore?.let { score ->
                        Spacer(modifier = Modifier.width(4.dp))
                        AIMatchScoreBadge(score = score)
                    }
                }

                if (onFeedbackClick != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        FeedbackChipButton(
                            label = stringResource(R.string.ai_like_label),
                            isSelected = item.feedbackType == MediaFeedbackType.LIKE,
                            isPositive = true,
                            onClick = { onFeedbackClick(item, MediaFeedbackType.LIKE) },
                        )
                        FeedbackChipButton(
                            label = stringResource(R.string.ai_dislike_label),
                            isSelected = item.feedbackType == MediaFeedbackType.DISLIKE,
                            isPositive = false,
                            onClick = { onFeedbackClick(item, MediaFeedbackType.DISLIKE) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AIMatchScoreBadge(score: Int) {
    val badgeShape = RoundedCornerShape(6.dp)
    val isHighMatch = score >= 60
    val borderColor = if (isHighMatch) {
        DesignSystemThemeImpl.designSystemColor.contentAccent
    } else {
        DesignSystemThemeImpl.designSystemColor.borderDefaultLevel1
    }
    val textColor = if (isHighMatch) {
        DesignSystemThemeImpl.designSystemColor.contentAccent
    } else {
        DesignSystemThemeImpl.designSystemColor.contentDefaultLevel2
    }

    Box(
        modifier = Modifier
            .clip(badgeShape)
            .background(DesignSystemThemeImpl.designSystemColor.bgDefaultLevel0)
            .border(width = 1.dp, color = borderColor, shape = badgeShape)
            .padding(horizontal = 5.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        ArchiText(
            text = stringResource(R.string.ai_match_badge, score),
            style = DesignSystemThemeImpl.typeScale.textRegularXS,
            color = textColor,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun FeedbackChipButton(
    label: String,
    isSelected: Boolean,
    isPositive: Boolean,
    onClick: () -> Unit,
) {
    val chipShape = RoundedCornerShape(8.dp)
    val activeColor = if (isPositive) {
        DesignSystemThemeImpl.designSystemColor.contentAccent
    } else {
        DesignSystemThemeImpl.designSystemColor.contentFavorite
    }
    val borderColor = if (isSelected) {
        activeColor
    } else {
        DesignSystemThemeImpl.designSystemColor.borderDefaultLevel1
    }
    val backgroundColor = if (isSelected) {
        DesignSystemThemeImpl.designSystemColor.bgDefaultLevel0
    } else {
        DesignSystemThemeImpl.designSystemColor.bgDefaultLevel1
    }
    val contentColor = if (isSelected) {
        activeColor
    } else {
        DesignSystemThemeImpl.designSystemColor.contentDefaultLevel2
    }

    Box(
        modifier = Modifier
            .clip(chipShape)
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = chipShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        ArchiText(
            text = label,
            style = DesignSystemThemeImpl.typeScale.textRegularXS,
            color = contentColor,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun PageNumberSeparator(page: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        ArchiText(
            text = page.toString(),
            style = DesignSystemThemeImpl.typeScale.textRegularM,
            color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel3,
        )
    }
    HorizontalDivider(thickness = 1.dp, color = DesignSystemThemeImpl.designSystemColor.borderDefaultLevel2)
}

@Composable
private fun ListEndMarker() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        ArchiText(
            text = stringResource(R.string.list_end_marker),
            style = DesignSystemThemeImpl.typeScale.textRegularM,
            color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel3,
        )
    }
}

@Composable
private fun LoadMoreIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
        )
    }
}

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
private fun ContentsGridItem(
    item: MediaItemUiState,
    onItemClick: (MediaItemUiState) -> Unit,
    onFavoriteClick: (MediaItemUiState, Boolean) -> Unit,
) {
    val imageLoader = rememberImageLoader()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) }
            .testTag(SEARCH_ITEM_TEST_TAG),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(
                    width = 1.dp,
                    color = DesignSystemThemeImpl.designSystemColor.borderDefaultLevel2,
                    shape = ThumbnailShape,
                )
                .clip(ThumbnailShape)
                .background(DesignSystemThemeImpl.designSystemColor.bgDefaultLevel0),
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                imageLoader = imageLoader,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(22.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onFavoriteClick(item, item.isFavorite) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(item.favoriteIconRes),
                    contentDescription = null,
                    tint = if (item.isFavorite) DesignSystemThemeImpl.designSystemColor.contentFavorite else Color.Unspecified,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(item.mediaTypeIconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            ArchiText(
                text = item.title.ifEmpty { stringResource(R.string.media_default_title) },
                style = DesignSystemThemeImpl.typeScale.textStrongM,
                color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel0,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
        }

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
