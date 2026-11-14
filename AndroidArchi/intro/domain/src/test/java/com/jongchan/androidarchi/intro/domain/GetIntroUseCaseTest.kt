package com.jongchan.androidarchi.intro.domain

import com.jongchan.androidarchi.common.domain.helper.MessageHelper
import com.jongchan.androidarchi.common.domain.helper.NavigationHelper
import com.jongchan.androidarchi.common.domain.error.HttpResponseException
import com.jongchan.androidarchi.common.domain.error.HttpResponseStatus
import com.jongchan.androidarchi.common.domain.remoteConfig.RemoteConfigKey
import com.jongchan.androidarchi.common.domain.remoteConfig.RemoteConfigRepository
import com.jongchan.androidarchi.search.domain.SearchPage
import com.jongchan.androidarchi.intro.entity.IntroVO
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetIntroUseCaseTest {

    private lateinit var messageHelper: MessageHelper
    private lateinit var navigationHelper: NavigationHelper
    private lateinit var remoteConfigRepository: RemoteConfigRepository
    private lateinit var useCase: GetIntroUseCase

    @Before
    fun setUp() {
        messageHelper = mockk(relaxed = true)
        navigationHelper = mockk(relaxed = true)
        // get() 은 제네릭 반환이라 relaxed 기본값이 캐스팅에 실패하므로 키별로 스텁한다.
        remoteConfigRepository = mockk {
            every { get(RemoteConfigKey.IsShowFeatureA) } returns false
            every { get(RemoteConfigKey.MarketingPromotionUrl) } returns ""
        }
        useCase = GetIntroUseCase(
            mockk(relaxed = true),
            messageHelper,
            navigationHelper,
            remoteConfigRepository,
            mockk(relaxed = true),
        )
    }

    @Test
    fun `invoke - on success - navigates to SearchPage and returns success with empty IntroVO`() = runTest {
        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(IntroVO.empty, result.getOrNull())
        verify(exactly = 1) { navigationHelper.navigateTo(SearchPage) }
    }

    @Test
    fun `invoke - on 401 Unauthorized - shows session expired dialog and returns failure`() = runTest {
        val exception = HttpResponseException(
            status = HttpResponseStatus.Unauthorized,
            rawCode = 401,
            errorRequestUrl = "/api/intro",
        )
        every { navigationHelper.navigateTo(SearchPage) } throws exception

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) {
            messageHelper.showOneButtonDialog(
                titleText = any(),
                descText = "Session expired. Please login again.",
                cantIgnore = true,
                buttonText = "Move to login",
                onClickButton = any(),
            )
        }
    }

    @Test
    fun `invoke - on 5xx server error - shows temporary error dialog and returns failure`() = runTest {
        val exception = HttpResponseException(
            status = HttpResponseStatus.InternalError,
            rawCode = 500,
            errorRequestUrl = "/api/intro",
        )
        every { navigationHelper.navigateTo(SearchPage) } throws exception

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) {
            messageHelper.showOneButtonDialog(
                titleText = "A temporary error occurred.",
                descText = any(),
                cantIgnore = any(),
                buttonText = any(),
                onClickButton = any(),
            )
        }
    }

    @Test
    fun `invoke - on 404 Not Found - shows feature unavailable dialog and navigates back on click`() = runTest {
        val exception = HttpResponseException(
            status = HttpResponseStatus.NotFound,
            rawCode = 404,
            errorRequestUrl = "/api/intro",
        )
        every { navigationHelper.navigateTo(SearchPage) } throws exception
        val onClickButton = slot<() -> Unit>()

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) {
            messageHelper.showOneButtonDialog(
                titleText = any(),
                descText = "This feature is not currently available in the app version.",
                cantIgnore = true,
                buttonText = "Move to back",
                onClickButton = capture(onClickButton),
            )
        }
        onClickButton.captured.invoke()
        verify(exactly = 1) { navigationHelper.navigateToBack() }
    }

    @Test
    fun `invoke - on non-common error with no matching error type - returns failure without dialog or navigation`() = runTest {
        val exception = HttpResponseException(
            status = HttpResponseStatus.Forbidden,
            rawCode = 403,
            errorRequestUrl = "/api/intro",
        )
        every { navigationHelper.navigateTo(SearchPage) } throws exception

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 0) {
            messageHelper.showOneButtonDialog(
                titleText = any(),
                descText = any(),
                cantIgnore = any(),
                buttonText = any(),
                onClickButton = any(),
            )
        }
        verify(exactly = 0) { navigationHelper.navigateToBack() }
    }

    @Test
    fun `invoke - on 400 with REQUIRED_FORCE_UPDATE - shows force update dialog and returns failure`() = runTest {
        val cause = Throwable("api.intro.requiredForceUpdate")
        val exception = HttpResponseException(
            status = HttpResponseStatus.BadRequest,
            rawCode = 400,
            errorRequestUrl = "/api/intro",
            cause = cause,
        )
        every { navigationHelper.navigateTo(SearchPage) } throws exception

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) {
            messageHelper.showOneButtonDialog(
                titleText = any(),
                descText = "You should update this App",
                cantIgnore = true,
                buttonText = "Move to Play Store",
                onClickButton = any(),
            )
        }
    }
}
