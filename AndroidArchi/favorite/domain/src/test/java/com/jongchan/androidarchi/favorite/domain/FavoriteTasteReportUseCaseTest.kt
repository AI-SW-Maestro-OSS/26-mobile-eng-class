package com.jongchan.androidarchi.favorite.domain

import com.jongchan.androidarchi.common.domain.helper.AIBasedFeatureHelper
import com.jongchan.androidarchi.common.domain.helper.SearchResultScoringHelper
import com.jongchan.androidarchi.common.entity.ai.AIEngineType
import com.jongchan.androidarchi.common.entity.ai.AIServiceOutput
import com.jongchan.androidarchi.common.entity.ai.AIServiceRequest
import com.jongchan.androidarchi.common.entity.favorite.FavoriteItemVO
import com.jongchan.androidarchi.common.entity.media.MediaType
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FavoriteTasteReportUseCaseTest {

    private lateinit var searchResultScoringHelper: SearchResultScoringHelper
    private lateinit var aiBasedFeatureHelper: AIBasedFeatureHelper
    private lateinit var useCase: FavoriteTasteReportUseCase

    @Before
    fun setUp() {
        searchResultScoringHelper = mockk(relaxed = true)
        aiBasedFeatureHelper = mockk(relaxed = true)
        useCase = FavoriteTasteReportUseCase(
            searchResultScoringHelper = searchResultScoringHelper,
            aiBasedFeatureHelper = aiBasedFeatureHelper,
            resourceHelper = mockk(relaxed = true),
            messageHelper = mockk(relaxed = true),
            navigationHelper = mockk(relaxed = true),
            ttiHelper = mockk(relaxed = true),
        )
    }

    @Test
    fun `invoke - parses Firebase AI fallback response from AIBasedFeatureHelper bridge`() = runTest {
        val favorites = listOf(
            FavoriteItemVO(type = MediaType.IMAGE, title = "귀여운 고양이", urlKey = "u1"),
            FavoriteItemVO(type = MediaType.VIDEO, title = "고양이 브이로그", urlKey = "u2"),
        )
        coEvery {
            aiBasedFeatureHelper.execute(any<AIServiceRequest.GenerateText>())
        } returns Result.success(
            AIServiceOutput.GeneratedText(
                text = "SUMMARY: 귀여운 동물 사진과 힐링 브이로그 영상을 주로 수집하고 계시네요!\nTAGS: 고양이, 힐링브이로그, 반려동물, 일상짤",
                engineType = AIEngineType.CLOUD_FIREBASE_AI,
            )
        )

        val report = useCase(favorites, feedbacksOverride = emptyList())

        assertEquals(AIEngineType.CLOUD_FIREBASE_AI, report.engineType)
        assertEquals("귀여운 동물 사진과 힐링 브이로그 영상을 주로 수집하고 계시네요!", report.summary)
        assertEquals(listOf("고양이", "힐링브이로그", "반려동물", "일상짤"), report.tasteKeywords)
    }
}
