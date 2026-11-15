import Foundation

public struct Timeline: Equatable, Sendable {
    public var startTime: Int64
    public var endTime: Int64

    public init(startTime: Int64 = 0, endTime: Int64 = 0) {
        self.startTime = startTime
        self.endTime = endTime
    }

    public func isCompleted() -> Bool {
        startTime != 0 && endTime != 0
    }
}
