import Foundation

/// TTI 로깅 활성 여부.
public struct TtiLoggingConfig: Equatable, Sendable {
    public let isEnabled: Bool

    public init(isEnabled: Bool = false) {
        self.isEnabled = isEnabled
    }
}
