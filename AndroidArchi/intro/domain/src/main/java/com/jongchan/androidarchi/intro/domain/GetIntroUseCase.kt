package com.jongchan.androidarchi.intro.domain

import com.jongchan.androidarchi.common.domain.base.BaseUseCase
import com.jongchan.androidarchi.common.domain.error.HttpResponseException
import com.jongchan.androidarchi.common.domain.error.handlingErrorOnUseCase
import com.jongchan.androidarchi.common.domain.error.isCommonErrorHandling
import com.jongchan.androidarchi.common.domain.helper.MessageHelper
import com.jongchan.androidarchi.common.domain.helper.NavigationHelper
import com.jongchan.androidarchi.common.domain.helper.ResourceHelper
import com.jongchan.androidarchi.common.domain.remoteConfig.RemoteConfigKey
import com.jongchan.androidarchi.common.domain.remoteConfig.RemoteConfigRepository
import com.jongchan.androidarchi.intro.entity.IntroVO
import com.jongchan.androidarchi.search.domain.SearchPage
import com.jongchan.androidarchi.tti.TTIHelper
import javax.inject.Inject

class GetIntroUseCase @Inject constructor(
    resourceHelper: ResourceHelper,
    messageHelper: MessageHelper,
    navigationHelper: NavigationHelper,
    val remoteConfigRepository: RemoteConfigRepository,
    ttiHelper: TTIHelper,
) : BaseUseCase(resourceHelper, messageHelper, navigationHelper, ttiHelper) {

    operator fun invoke(): Result<IntroVO> {
        return try {
//            val result = introRepository.getIntro()
            val isShowFeatureA = remoteConfigRepository.get(RemoteConfigKey.IsShowFeatureA)
            val marketingUrl = remoteConfigRepository.get(RemoteConfigKey.MarketingPromotionUrl)

            navigationHelper.navigateTo(SearchPage)
            Result.success(IntroVO.empty)
        } catch (e: HttpResponseException) {
            handleIntroError(e)
            Result.failure(e)
        }
    }

    private fun handleIntroError(e: HttpResponseException) {
        if (e.isCommonErrorHandling()) {
            executeCommonErrorHanding(e)
            return
        }
        val errorType = e.handlingErrorOnUseCase<IntroErrorType>() ?: return
        when (errorType) {
            IntroErrorType.REQUIRED_FORCE_UPDATE -> {
                messageHelper.showOneButtonDialog(
                    cantIgnore = true,
                    descText = "You should update this App",
                    buttonText = "Move to Play Store",
                    onClickButton = {
                        navigationHelper.navigateToBack()
                    },
                )
            }
        }
    }
}
