import Foundation

public protocol GlobalStore: AnyObject, Sendable {
    func observeStates(_ resultStateType: GlobalState.Type) -> AsyncStream<GlobalState>
    func dispatch(_ action: any GlobalAction) async
}
