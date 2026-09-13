package com.jongchan.androidarchi.etc.presentation

import com.jongchan.androidarchi.common.domain.sdui.SDUIViewTypeVO
import com.jongchan.androidarchi.common.presentation.mvi.UiState

data class EtcUIState(
    val isLoading: Boolean,
    val sduiViewItems: List<SDUIViewTypeVO>?,
) : UiState {

    fun setSDUIViews(sduiViewItems: List<SDUIViewTypeVO>?): EtcUIState = copy(
        sduiViewItems = sduiViewItems,
    )

    companion object {
        val empty = EtcUIState(
            isLoading = true,
            sduiViewItems = emptyList(),
        )
    }
}
