package com.jongchan.androidarchi.intro.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jongchan.androidarchi.common.domain.error.handlingErrorOnUseCase
import com.jongchan.androidarchi.common.domain.error.HttpResponseException
import com.jongchan.androidarchi.intro.domain.GetIntroUseCase
import com.jongchan.androidarchi.intro.domain.IntroErrorType
import com.jongchan.androidarchi.tti.TTIHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor(
    private val useCase: GetIntroUseCase,
) : ViewModel() {
    init {
        viewModelScope.launch {
            useCase().onFailure(::handlingIntroPageError)
        }
    }

    private fun handlingIntroPageError(throwable: Throwable) {
        val exception = throwable as? HttpResponseException ?: return
        exception.handlingErrorOnUseCase<IntroErrorType>()?.let { errorType ->
//            when (errorType) {
//                IntroErrorType.REQUIRED_FORCE_UPDATE -> {
//
//                }
//            }
        }
    }
}
