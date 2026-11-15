package com.jongchan.androidarchi.search.domain

import com.jongchan.androidarchi.common.domain.helper.AIBasedFeatureHelper
import com.jongchan.androidarchi.common.domain.helper.SearchResultScoringHelper
import com.jongchan.androidarchi.common.entity.ai.AIEngineType
import com.jongchan.androidarchi.common.entity.ai.AIServiceOutput
import com.jongchan.androidarchi.common.entity.ai.AIServiceRequest
import com.jongchan.androidarchi.common.entity.media.MediaItemVO
import com.jongchan.androidarchi.common.entity.media.MediaType
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SearchQuerySuggestionUseCaseTest {

    private lateinit var aiBasedFeatureHelper: AIBasedFeatureHelper
    private lateinit var searchResultScoringHelper: SearchResultScoringHelper
    private lateinit var useCase: SearchQuerySuggestionUseCase

    @Before
    fun setUp() {
        aiBasedFeatureHelper = mockk(relaxed = true)
        searchResultScoringHelper = mockk(relaxed = true)
        useCase = SearchQuerySuggestionUseCase(
            aiBasedFeatureHelper = aiBasedFeatureHelper,
            searchResultScoringHelper = searchResultScoringHelper,
            resourceHelper = mockk(relaxed = true),
            messageHelper = mockk(relaxed = true),
            navigationHelper = mockk(relaxed = true),
            ttiHelper = mockk(relaxed = true),
        )
    }

    @Test
    fun `invoke - parses AICore response from AIBasedFeatureHelper bridge`() = runTest {
        val items = listOf(
            MediaItemVO(
                type = MediaType.IMAGE,
                title = "Cute Cat",
                urlKey = "https://example.com/1",
                collection = "animal",
            )
        )
        coEvery {
            aiBasedFeatureHelper.execute(any<AIServiceRequest.GenerateText>())
        } returns Result.success(
            AIServiceOutput.GeneratedText(
                text = "KEYWORDS: 아기 고양이, 고양이 브이로그, 랙돌 고양이, 고양이 움짤\nREASON: 좋아요한 동물 콘텐츠 취향을 반영한 추천 검색어입니다.",
                engineType = AIEngineType.ON_DEVICE_AICORE,
            )
        )

        val suggestion = useCase(
            query = "고양이",
            items = items,
            feedbacksOverride = emptyList(),
        )

        assertEquals(AIEngineType.ON_DEVICE_AICORE, suggestion.engineType)
        assertEquals(
            listOf("아기 고양이", "고양이 브이로그", "랙돌 고양이", "고양이 움짤"),
            suggestion.suggestedKeywords,
        )
        assertEquals("좋아요한 동물 콘텐츠 취향을 반영한 추천 검색어입니다.", suggestion.comment)
    }
}
