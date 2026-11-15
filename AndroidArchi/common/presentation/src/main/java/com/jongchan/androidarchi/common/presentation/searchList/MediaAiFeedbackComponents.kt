package com.jongchan.androidarchi.common.presentation.searchList

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jongchan.androidarchi.common.entity.ai.MediaFeedbackType
import com.jongchan.androidarchi.common.entity.media.MediaType
import com.jongchan.androidarchi.common.presentation.R
import com.jongchan.androidarchi.common.presentation.component.ArchiText
import com.jongchan.androidarchi.common.presentation.ui.theme.DesignSystemTheme
import com.jongchan.androidarchi.common.presentation.ui.theme.DesignSystemThemeImpl

private const val HIGH_MATCH_SCORE_THRESHOLD = 60
private val ScoreBadgeShape = RoundedCornerShape(6.dp)
private val FeedbackChipShape = RoundedCornerShape(8.dp)

/**
 * 검색 결과 리스트 셀 하단 행:
 * - 좌측: 날짜 텍스트 + 온디바이스 AI 취향 매칭 점수 배지([AIMatchScoreBadge])
 * - 우측: 온디바이스 AI 취향 피드백(좋아요/싫어요) 칩 그룹([MediaFeedbackChipGroup])
 */
@Composable
internal fun ContentsItemMetaAndAiRow(
    item: MediaItemUiState,
    modifier: Modifier = Modifier,
    onFeedbackClick: ((MediaItemUiState, MediaFeedbackType) -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
            MediaFeedbackChipGroup(
                selectedFeedback = item.feedbackType,
                onFeedbackSelect = { feedbackType -> onFeedbackClick(item, feedbackType) },
            )
        }
    }
}

/**
 * 온디바이스 AI가 산출한 유저 취향 매칭 점수(0~100%)를 표시하는 배지.
 */
@Composable
internal fun AIMatchScoreBadge(
    score: Int,
    modifier: Modifier = Modifier,
) {
    val isHighMatch = score >= HIGH_MATCH_SCORE_THRESHOLD
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
        modifier = modifier
            .clip(ScoreBadgeShape)
            .background(DesignSystemThemeImpl.designSystemColor.bgDefaultLevel0)
            .border(width = 1.dp, color = borderColor, shape = ScoreBadgeShape)
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

/**
 * 온디바이스 AI 취향 학습을 위한 좋아요(👍) / 싫어요(👎) 피드백 칩 버튼 묶음.
 */
@Composable
internal fun MediaFeedbackChipGroup(
    selectedFeedback: MediaFeedbackType,
    modifier: Modifier = Modifier,
    onFeedbackSelect: (MediaFeedbackType) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FeedbackChipButton(
            label = stringResource(R.string.ai_like_label),
            isSelected = selectedFeedback == MediaFeedbackType.LIKE,
            isPositive = true,
            onClick = { onFeedbackSelect(MediaFeedbackType.LIKE) },
        )
        FeedbackChipButton(
            label = stringResource(R.string.ai_dislike_label),
            isSelected = selectedFeedback == MediaFeedbackType.DISLIKE,
            isPositive = false,
            onClick = { onFeedbackSelect(MediaFeedbackType.DISLIKE) },
        )
    }
}

/**
 * 개별 취향 피드백(좋아요/싫어요) 칩 버튼.
 */
@Composable
internal fun FeedbackChipButton(
    label: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    isPositive: Boolean,
    onClick: () -> Unit,
) {
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
        modifier = modifier
            .clip(FeedbackChipShape)
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = FeedbackChipShape)
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

@Preview(showBackground = true)
@Composable
private fun ContentsItemMetaAndAiRowPreview() {
    DesignSystemTheme {
        ContentsItemMetaAndAiRow(
            item = MediaItemUiState(
                mediaType = MediaType.IMAGE,
                title = "Daum카페",
                thumbnailUrl = "",
                contentsUrl = "https://cafe.daum.net/example",
                dateTimeText = "2026년 11월 15일",
                feedbackType = MediaFeedbackType.LIKE,
                preferenceScore = 85,
            ),
            onFeedbackClick = { _, _ -> },
        )
    }
}
