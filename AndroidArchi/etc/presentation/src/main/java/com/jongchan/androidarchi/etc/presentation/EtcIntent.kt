package com.jongchan.androidarchi.etc.presentation

import com.jongchan.androidarchi.common.presentation.mvi.MviIntent

sealed interface EtcIntent : MviIntent {
    data object Load : EtcIntent
    // TODO: add screen actions (e.g. data class ClickItem(val id: Long) : EtcIntent)
}
