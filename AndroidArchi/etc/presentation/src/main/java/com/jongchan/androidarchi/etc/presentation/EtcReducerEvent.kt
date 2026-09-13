package com.jongchan.androidarchi.etc.presentation

import com.jongchan.androidarchi.common.presentation.mvi.ReducerEvent

sealed interface EtcReducerEvent : ReducerEvent {
    data object LoadStarted : EtcReducerEvent
    data object Loaded : EtcReducerEvent
}
