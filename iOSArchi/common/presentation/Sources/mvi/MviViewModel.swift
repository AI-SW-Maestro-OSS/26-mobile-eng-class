import Combine
import CommonDomain
import Foundation

public protocol MviIntent {}
public protocol UiState {}
public protocol ReducerEvent {}

public enum NoOpIntent: MviIntent {
    case none
}

public enum NoOpReducerEvent: ReducerEvent {
    case none
}

public struct NoOpUiState: UiState, Equatable {
    public init() {}
}

@MainActor
open class MviViewModel<I: MviIntent, S: UiState, E: ReducerEvent>: ObservableObject {
    @Published public private(set) var uiState: S

    public var currentState: S {
        uiState
    }

    public var backgroundTasks: [Task<Void, Never>] = []

    public init(
        initialState: S,
        globalStore: GlobalStore? = nil,
        observedStateTypes: [GlobalState.Type] = []
    ) {
        self.uiState = initialState
        if let globalStore, !observedStateTypes.isEmpty {
            subscribeGlobalStates(globalStore: globalStore, types: observedStateTypes)
        }
    }

    deinit {
        for task in backgroundTasks {
            task.cancel()
        }
    }

    open func onIntent(_ intent: I) {}

    open func reduce(state: S, event: E) -> S {
        state
    }

    public func dispatch(_ event: E) {
        uiState = reduce(state: uiState, event: event)
    }

    open func observeGlobalState(_ state: GlobalState) {}

    private func subscribeGlobalStates(globalStore: GlobalStore, types: [GlobalState.Type]) {
        for type in types {
            let task = Task { @MainActor [weak self] in
                for await state in globalStore.observeStates(type) {
                    guard let self, !Task.isCancelled else { break }
                    self.observeGlobalState(state)
                }
            }
            backgroundTasks.append(task)
        }
    }
}
