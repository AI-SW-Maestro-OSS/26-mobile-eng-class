package com.jongchan.androidarchi.common.domain.store

sealed interface GlobalAction

data class SampleAction(val msg: String) : GlobalAction