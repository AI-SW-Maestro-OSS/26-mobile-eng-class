import Foundation

/// 앱 전역에서 IO / Main / Default / TTI 작업에 사용하는 큐 및 비동기 태스크 스코프 식별자.
public enum ConcurrencyQualifiers {
    public static let ioQueue = "ioQueue"
    public static let mainQueue = "mainQueue"
    public static let defaultQueue = "defaultQueue"
    public static let ttiQueue = "ttiQueue"
    public static let ioTaskScope = "ioTaskScope"
    public static let mainTaskScope = "mainTaskScope"
}

/// 앱 전역 비동기 태스크 스코프 추상화.
public final class AsyncTaskScope: @unchecked Sendable {
    public let name: String
    public let queue: DispatchQueue

    public init(name: String, queue: DispatchQueue) {
        self.name = name
        self.queue = queue
    }

    @discardableResult
    public func run(_ operation: @escaping @Sendable () async -> Void) -> Task<Void, Never> {
        Task {
            await operation()
        }
    }
}
