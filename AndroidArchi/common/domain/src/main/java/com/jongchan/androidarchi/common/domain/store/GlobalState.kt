package com.jongchan.androidarchi.common.domain.store

sealed class GlobalState

data class SampleState(val msg: String): GlobalState()
