package com.jongchan.androidarchi.common.domain.store

import kotlinx.coroutines.flow.SharedFlow
import kotlin.reflect.KClass

interface GlobalStore {
    fun observeStates(resultStateType: KClass<out GlobalState>): SharedFlow<GlobalState>

    suspend fun dispatch(action: GlobalAction)
}