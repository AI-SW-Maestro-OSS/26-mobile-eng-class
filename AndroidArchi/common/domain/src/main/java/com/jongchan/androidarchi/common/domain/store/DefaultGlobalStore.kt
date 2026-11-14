package com.jongchan.androidarchi.common.domain.store

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.SharedFlow
import kotlin.reflect.KClass

class DefaultGlobalStore(
    private val actionStateMapper: ActionStateMapper,
) : GlobalStore {

    private val results = StateStreamRegistry<GlobalState>()

    override fun observeStates(resultStateType: KClass<out GlobalState>): SharedFlow<GlobalState> =
        results.observe(resultStateType)


    override suspend fun dispatch(action: GlobalAction) {
        currentCoroutineContext().ensureActive()
        results.emit(actionStateMapper.map(action))
    }
}