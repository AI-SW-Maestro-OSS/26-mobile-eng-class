package com.jongchan.androidarchi.common.domain.store

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.reflect.KClass

internal class StateStreamRegistry<T : Any> {
    private val lock = Any()
    private val streams = mutableMapOf<KClass<out T>, ResultStream<T>>()

    fun observe(type: KClass<out T>): SharedFlow<T> = streamFor(type).readOnly

    suspend fun emit(value: T) {
        streamFor(value::class).mutable.emit(value)
    }

    private fun streamFor(type: KClass<out T>): ResultStream<T> = synchronized(lock) {
        streams.getOrPut(type) { ResultStream() }
    }

    private class ResultStream<T> {
        val mutable = MutableSharedFlow<T>(
            replay = 1,
            extraBufferCapacity = 64,
            onBufferOverflow = BufferOverflow.SUSPEND,
        )
        val readOnly: SharedFlow<T> = mutable.asSharedFlow()
    }
}