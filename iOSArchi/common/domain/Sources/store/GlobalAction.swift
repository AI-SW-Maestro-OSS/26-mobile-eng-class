import Foundation

public protocol GlobalAction: Sendable {}

public struct SampleAction: GlobalAction, Equatable {
    public let msg: String

    public init(msg: String) {
        self.msg = msg
    }
}
