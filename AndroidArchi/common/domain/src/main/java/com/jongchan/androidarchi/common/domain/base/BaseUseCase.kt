package com.jongchan.androidarchi.common.domain.base

import com.jongchan.androidarchi.common.domain.error.HttpResponseException
import com.jongchan.androidarchi.common.domain.helper.MessageHelper
import com.jongchan.androidarchi.common.domain.helper.NavigationHelper
import com.jongchan.androidarchi.common.domain.helper.ResourceHelper
import com.jongchan.androidarchi.tti.TTIHelper

open class BaseUseCase(
    protected open val resourceHelper: ResourceHelper,
    protected open val messageHelper: MessageHelper,
    protected open val navigationHelper: NavigationHelper,
    protected open val ttiHelper: TTIHelper,
) {

    fun executeCommonErrorHanding(e: HttpResponseException) {
        when (e.rawCode) {
            401 -> {
                messageHelper.showOneButtonDialog(
                    cantIgnore = true,
                    descText = "Session expired. Please login again.",
                    buttonText = "Move to login",
                    onClickButton = {
                        // 목적지(로그인 화면)를 직접 지정하지 않는다.
                        // ex) navigationHelper.navigateTo(IntroPage)
                        // common:domain 이 feature 의 Page를 참조하면 순환 의존이 되므로
                        // "초기 화면으로 이동"이라는 시그널만 날리고 실제 이동은 NavHost가 결정
                        navigationHelper.navigateToInitial()
                    }
                )
            }

            404 -> {
                messageHelper.showOneButtonDialog(
                    cantIgnore = true,
                    descText = "This feature is not currently available in the app version.",
                    buttonText = "Move to back",
                    onClickButton = {
                        navigationHelper.navigateToBack()
                    }
                )
            }

            else -> {
                messageHelper.showOneButtonDialog(
                    titleText = "A temporary error occurred.",
                    descText = "error status : ${e.print()}",
                )
            }
        }
    }
}