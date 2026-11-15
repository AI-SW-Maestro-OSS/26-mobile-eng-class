import Foundation

open class GlobalState: @unchecked Sendable, Equatable {
    public init() {}

    public static func == (lhs: GlobalState, rhs: GlobalState) -> Bool {
        lhs.isEqual(to: rhs)
    }

    open func isEqual(to other: GlobalState) -> Bool {
        ObjectIdentifier(self) == ObjectIdentifier(other)
    }
}

public final class SampleState: GlobalState, @unchecked Sendable {
    public let msg: String

    public init(msg: String) {
        self.msg = msg
        super.init()
    }

    override public func isEqual(to other: GlobalState) -> Bool {
        guard let otherSample = other as? SampleState else { return false }
        return self.msg == otherSample.msg
    }
}
