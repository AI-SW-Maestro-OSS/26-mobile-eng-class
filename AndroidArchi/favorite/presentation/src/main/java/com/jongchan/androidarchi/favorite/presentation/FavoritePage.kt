package com.jongchan.androidarchi.favorite.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jongchan.androidarchi.common.entity.ai.AIEngineType
import com.jongchan.androidarchi.common.presentation.R
import com.jongchan.androidarchi.common.presentation.component.ArchiText
import com.jongchan.androidarchi.common.presentation.searchList.ContentsGrid
import com.jongchan.androidarchi.common.presentation.ui.theme.DesignSystemThemeImpl
import kotlinx.collections.immutable.ImmutableList

@Composable
fun FavoritePage(viewModel: FavoriteViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FavoritePageContent(uiState = uiState, onIntent = viewModel::onIntent)
}

@Composable
private fun FavoritePageContent(
    uiState: FavoriteUIState,
    onIntent: (FavoriteIntent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignSystemThemeImpl.designSystemColor.bgDefaultLevel1)
    ) {
        when {
            uiState.isLoading && uiState.favoriteItemList.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.favoriteItemList.isEmpty() -> {
                ArchiText(
                    text = stringResource(R.string.favorite_empty),
                    style = DesignSystemThemeImpl.typeScale.textRegularL,
                    color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel3,
                    maxLines = Int.MAX_VALUE,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    AITasteReportCard(
                        isAnalyzing = uiState.isAnalyzingTaste,
                        summary = uiState.tasteSummary,
                        keywords = uiState.tasteKeywords,
                        engineType = uiState.tasteEngineType,
                        onRefresh = { onIntent(FavoriteIntent.RefreshTasteReport) },
                        modifier = Modifier.padding(
                            start = 30.dp,
                            end = 30.dp,
                            top = 16.dp,
                            bottom = 4.dp,
                        ),
                    )
                    ContentsGrid(
                        items = uiState.favoriteItemList,
                        onItemClick = { onIntent(FavoriteIntent.OpenFullScreen(it.contentsUrl)) },
                        onFavoriteClick = { item, _ ->
                            onIntent(FavoriteIntent.DeleteFavorite(item.contentsUrl))
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun AITasteReportCard(
    isAnalyzing: Boolean,
    summary: String,
    keywords: ImmutableList<String>,
    engineType: AIEngineType,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(14.dp)
    val badgeShape = RoundedCornerShape(6.dp)
    val tagShape = RoundedCornerShape(12.dp)

    val engineLabel = when (engineType) {
        AIEngineType.ON_DEVICE_AICORE -> stringResource(R.string.ai_engine_aicore)
        AIEngineType.CLOUD_FIREBASE_AI -> stringResource(R.string.ai_engine_firebase)
        AIEngineType.ON_DEVICE_MEDIAPIPE -> stringResource(R.string.ai_engine_mediapipe)
        AIEngineType.LOCAL_FALLBACK -> stringResource(R.string.ai_engine_local_fallback)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(DesignSystemThemeImpl.designSystemColor.bgDefaultLevel0)
            .border(
                width = 1.dp,
                color = DesignSystemThemeImpl.designSystemColor.borderDefaultLevel1,
                shape = cardShape,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ArchiText(
                text = stringResource(R.string.ai_favorite_report_title),
                style = DesignSystemThemeImpl.typeScale.textStrongM,
                color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel0,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(badgeShape)
                        .background(DesignSystemThemeImpl.designSystemColor.bgDefaultLevel1)
                        .border(
                            width = 1.dp,
                            color = DesignSystemThemeImpl.designSystemColor.borderDefaultLevel1,
                            shape = badgeShape,
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    ArchiText(
                        text = engineLabel,
                        style = DesignSystemThemeImpl.typeScale.textRegularXS,
                        color = DesignSystemThemeImpl.designSystemColor.contentAccent,
                        maxLines = 1,
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(badgeShape)
                        .background(DesignSystemThemeImpl.designSystemColor.bgDefaultLevel1)
                        .border(
                            width = 1.dp,
                            color = DesignSystemThemeImpl.designSystemColor.borderDefaultLevel1,
                            shape = badgeShape,
                        )
                        .clickable(onClick = onRefresh)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    ArchiText(
                        text = stringResource(R.string.ai_favorite_report_refresh),
                        style = DesignSystemThemeImpl.typeScale.textRegularXS,
                        color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel2,
                        maxLines = 1,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isAnalyzing && summary.isBlank()) {
            ArchiText(
                text = stringResource(R.string.ai_favorite_report_loading),
                style = DesignSystemThemeImpl.typeScale.textRegularS,
                color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel3,
                maxLines = 2,
            )
        } else {
            if (summary.isNotBlank()) {
                ArchiText(
                    text = summary,
                    style = DesignSystemThemeImpl.typeScale.textRegularS,
                    color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel1,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (keywords.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    keywords.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(tagShape)
                                .background(DesignSystemThemeImpl.designSystemColor.bgDefaultLevel1)
                                .border(
                                    width = 1.dp,
                                    color = DesignSystemThemeImpl.designSystemColor.contentAccent,
                                    shape = tagShape,
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            ArchiText(
                                text = stringResource(R.string.ai_keyword_chip_format, tag),
                                style = DesignSystemThemeImpl.typeScale.textRegularXS,
                                color = DesignSystemThemeImpl.designSystemColor.contentAccent,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}
