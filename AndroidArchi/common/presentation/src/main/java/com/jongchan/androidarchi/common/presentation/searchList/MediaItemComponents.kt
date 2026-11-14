package com.jongchan.androidarchi.common.presentation.searchList

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.jongchan.androidarchi.common.entity.media.MediaType
import com.jongchan.androidarchi.common.presentation.R
import com.jongchan.androidarchi.common.presentation.component.ArchiText
import com.jongchan.androidarchi.common.presentation.imageUtil.rememberImageLoader
import com.jongchan.androidarchi.common.presentation.ui.theme.DesignSystemTheme
import com.jongchan.androidarchi.common.presentation.ui.theme.DesignSystemThemeImpl

internal val ThumbnailShape = RoundedCornerShape(14.dp)

/**
 * 리스트/그리드 셀에서 공통으로 사용하는 미디어 썸네일 + 우상단 즐겨찾기(하트) 토글 오버레이.
 */
@Composable
internal fun MediaItemThumbnail(
    item: MediaItemUiState,
    modifier: Modifier = Modifier,
    favoriteButtonPadding: Dp = 6.dp,
    contentDescription: String? = item.title,
    onFavoriteClick: (MediaItemUiState, Boolean) -> Unit,
) {
    val imageLoader = rememberImageLoader()

    Box(
        modifier = modifier
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
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(favoriteButtonPadding)
                .size(22.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onFavoriteClick(item, item.isFavorite) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(item.favoriteIconRes),
                contentDescription = contentDescription,
                tint = if (item.isFavorite) {
                    DesignSystemThemeImpl.designSystemColor.contentFavorite
                } else {
                    Color.Unspecified
                },
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * 미디어 타입 아이콘 + 타이틀 + (선택) 이미지 카테고리(collection)를 표시하는 상단 헤더 행.
 */
@Composable
internal fun MediaItemHeaderRow(
    item: MediaItemUiState,
    modifier: Modifier = Modifier,
    titleColor: Color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel1,
    titleMaxLines: Int = 1,
    fillTitleWeight: Boolean = true,
    showCollection: Boolean = true,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
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
            color = titleColor,
            maxLines = titleMaxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = fillTitleWeight),
        )
        if (showCollection && item.collection.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            ArchiText(
                text = item.collection,
                style = DesignSystemThemeImpl.typeScale.textRegularM,
                color = DesignSystemThemeImpl.designSystemColor.contentAccent,
                maxLines = 1,
            )
        }
    }
}

/**
 * 검색 결과 페이지 경계에 표시되는 페이지 번호 구분자.
 */
@Composable
internal fun PageNumberSeparator(
    page: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
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
    HorizontalDivider(
        thickness = 1.dp,
        color = DesignSystemThemeImpl.designSystemColor.borderDefaultLevel2,
    )
}

/**
 * 리스트 하단 상태(추가 페이지 로딩 인디케이터 또는 마지막 페이지 마커)를 렌더링한다.
 */
@Composable
internal fun ContentsListFooter(
    isLoadingMore: Boolean,
    isLastPage: Boolean,
    modifier: Modifier = Modifier,
) {
    when {
        isLoadingMore -> LoadMoreIndicator(modifier = modifier)
        isLastPage -> ListEndMarker(modifier = modifier)
    }
}

@Composable
internal fun ListEndMarker(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
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
internal fun LoadMoreIndicator(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
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

@Preview(showBackground = true)
@Composable
private fun MediaItemHeaderRowPreview() {
    DesignSystemTheme {
        MediaItemHeaderRow(
            item = MediaItemUiState(
                mediaType = MediaType.IMAGE,
                title = "Daum카페",
                thumbnailUrl = "",
                contentsUrl = "https://cafe.daum.net/example",
                collection = "cafe",
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
