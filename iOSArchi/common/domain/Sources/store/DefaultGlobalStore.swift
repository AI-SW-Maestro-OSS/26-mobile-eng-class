import Foundation

public final class DefaultGlobalStore: GlobalStore, @unchecked Sendable {
    private let actionStateMapper: ActionStateMapper
    private let results = StateStreamRegistry<GlobalState>()

    public init(actionStateMapper: ActionStateMapper = ActionStateMapper()) {
        self.actionStateMapper = actionStateMapper
    }

    public func observeStates(_ resultStateType: GlobalState.Type) -> AsyncStream<GlobalState> {
        results.observe(resultStateType)
    }

    public func dispatch(_ action: any GlobalAction) async {
        guard !Task.isCancelled else { return }
        results.emit(actionStateMapper.map(action))
    }
}
