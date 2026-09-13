package com.jongchan.androidarchi.etc.presentation

import com.jongchan.androidarchi.common.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EtcViewModel @Inject constructor() : MviViewModel<EtcIntent, EtcUIState, EtcReducerEvent>(EtcUIState.empty) {

    init {
        onIntent(EtcIntent.Load)
    }

    override fun onIntent(intent: EtcIntent) {
        when (intent) {
            EtcIntent.Load -> load()
        }
    }

    // 상태 변이는 이 한 곳(reduce)에서만 일어난다 — (state, event) 의 순수 함수.
    override fun reduce(state: EtcUIState, event: EtcReducerEvent): EtcUIState =
        when (event) {
            is EtcReducerEvent.LoadStarted -> state.copy(isLoading = true)
            is EtcReducerEvent.Loaded -> state.copy(isLoading = false)
        }

    private fun load() {
        dispatch(EtcReducerEvent.LoadStarted)
        // TODO: 실제 데이터 소스(UseCase)가 정해지면 여기서 호출하고 결과를 ReducerEvent 로 dispatch 한다.
//        dispatch(EtcReducerEvent.Loaded)
    }
}
