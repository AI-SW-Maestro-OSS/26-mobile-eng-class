package com.jongchan.androidarchi.common.domain.store

import javax.inject.Inject

class ActionStateMapper @Inject constructor() {
    fun map(action: GlobalAction): GlobalState = when (action) {
        is SampleAction -> SampleState(msg = action.msg)
    }
}