package com.jongchan.androidarchi.search.domain

import com.jongchan.androidarchi.common.domain.helper.SearchResultScoringHelper
import com.jongchan.androidarchi.common.entity.ai.MediaFeedbackType
import com.jongchan.androidarchi.common.entity.ai.MediaPreferenceFeedbackVO
import com.jongchan.androidarchi.common.entity.ai.toSemanticText
import com.jongchan.androidarchi.common.entity.media.MediaItemVO
import com.jongchan.androidarchi.common.entity.media.MediaType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SearchPreferenceUseCaseTest {

    private lateinit var searchResultScoringHelper: SearchResultScoringHelper
    private lateinit var useCase: SearchPreferenceUseCase

    @Before
    fun setUp() {
        searchResultScoringHelper = mockk(relaxed = true)
        useCase = SearchPreferenceUseCase(
            searchResultScoringHelper = searchResultScoringHelper,
            resourceHelper = mockk(relaxed = true),
            messageHelper = mockk(relaxed = true),
            navigationHelper = mockk(relaxed = true),
            ttiHelper = mockk(relaxed = true),
        )
    }

    @Test
    fun `observeFeedbacks - delegates to AIBasedFeatureHelper`() = runTest {
        val expected = listOf(
            MediaPreferenceFeedbackVO(
                urlKey = "https://example.com/1",
                title = "Cute Cat",
                feedbackType = MediaFeedbackType.LIKE,
            )
        )
        every { searchResultScoringHelper.observeFeedbacks() } returns flowOf(expected)

        val actual = useCase.observeFeedbacks().first()

        assertEquals(expected, actual)
    }

    @Test
    fun `recordFeedback - maps MediaItemVO to MediaPreferenceFeedbackVO and delegates`() = runTest {
        val item = MediaItemVO(
            type = MediaType.IMAGE,
            title = "Ryan Kakao",
            urlKey = "https://example.com/ryan.png",
            collection = "blog",
            pageLinkUrl = "https://blog.kakao.com/1",
        )

        useCase.recordFeedback(item, MediaFeedbackType.LIKE)

        coVerify(exactly = 1) {
            searchResultScoringHelper.recordFeedback(
                MediaPreferenceFeedbackVO(
                    urlKey = "https://example.com/ryan.png",
                    title = "Ryan Kakao",
                    collection = "blog",
                    description = "https://blog.kakao.com/1",
                    mediaType = MediaType.IMAGE,
                    feedbackType = MediaFeedbackType.LIKE,
                )
            )
        }
    }

    @Test
    fun `evaluateScores - converts items to semantic text map and returns scores`() = runTest {
        val item1 = MediaItemVO(
            type = MediaType.IMAGE,
            title = "Cat Photo",
            urlKey = "u1",
            collection = "blog",
        )
        val item2 = MediaItemVO(
            type = MediaType.VIDEO,
            title = "Dog Video",
            urlKey = "u2",
            collection = "video",
        )
        val expectedScores = mapOf("u1" to 92, "u2" to 24)
        coEvery {
            searchResultScoringHelper.evaluatePreferenceScores(
                mapOf(
                    "u1" to item1.toSemanticText(),
                    "u2" to item2.toSemanticText(),
                )
            )
        } returns expectedScores

        val result = useCase.evaluateScores(listOf(item1, item2))

        assertEquals(expectedScores, result)
    }
}
