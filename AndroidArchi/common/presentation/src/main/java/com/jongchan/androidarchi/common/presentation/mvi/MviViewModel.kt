package com.jongchan.androidarchi.common.presentation.mvi

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jongchan.androidarchi.common.domain.store.GlobalState
import com.jongchan.androidarchi.common.domain.store.GlobalStore
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.reflect.KClass

/**
 * 사용자 입력(클릭, 스크롤 등)을 표현하는 마커 인터페이스.
 * `View → ViewModel` 방향으로만 흐른다.
 */
interface MviIntent

/**
 * 화면에 노출되는 불변 상태의 마커 인터페이스.
 * `ViewModel → View` 방향으로만 흐른다.
 */
@Stable
interface UiState

/**
 * Reducer 에 입력되는 내부 이벤트 마커 인터페이스.
 * (예: 데이터 로딩 완료, 즐겨찾기 셋 변경 등) — 외부에서 직접 dispatch 하지 않는다.
 */
interface ReducerEvent

/**
 * MVI 베이스 ViewModel.
 *
 * - [uiState] 단일 [StateFlow] 만 View 에 노출한다.
 * - 외부 진입점은 [onIntent] 하나로 통일한다.
 * - 모든 상태 변이는 [reduce] 한 곳을 거쳐 [dispatch] 로만 일어난다.
 *   (collectors / coroutines 곳곳에서 `uiState.update {}` 를 직접 호출하지 않는다)
 */
abstract class MviViewModel<I : MviIntent, S : UiState, E : ReducerEvent>(
    initialState: S,
    protected val globalStore: GlobalStore,
    observedStateTypes: Set<KClass<out GlobalState>> = emptySet()
) : ViewModel() {

    val uiState: StateFlow<S>
        field = MutableStateFlow<S>(initialState)

    protected val currentState: S
        get() = uiState.value

    init {
        observedStateTypes.forEach { state ->
            viewModelScope.launch(Dispatchers.Main, start = CoroutineStart.UNDISPATCHED) {
                globalStore.observeStates(state)
                    .onSubscription { yield() }
                    .distinctUntilChanged()
                    .collect(::observeGlobalState)
            }
        }
    }

    /**
     * Main 쓰레드에서 호출하며 같은 타입 내 발행 순서를 유지한다.
     * 필요한 결과를 when으로 처리한다.
     * 화면 상태 변경은 [dispatch]를 거치고, 비동기 작업은 viewModelScope에서 실행한다.
     */
    protected open fun observeGlobalState(state: GlobalState) = Unit

    abstract fun onIntent(intent: I)

    protected abstract fun reduce(state: S, event: E): S

    protected fun dispatch(event: E) {
        uiState.update { current -> reduce(current, event) }
    }
}
