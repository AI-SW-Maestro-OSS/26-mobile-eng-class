import Foundation

public final class ActionStateMapper: Sendable {
    public init() {}

    public func map(_ action: any GlobalAction) -> GlobalState {
        switch action {
        case let sample as SampleAction:
            return SampleState(msg: sample.msg)
        default:
            return GlobalState()
        }
    }
}
