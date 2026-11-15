import Foundation

/// 앱 전역에서 IO / Main / Default / TTI 작업에 사용하는 디스패처 및 스코프 식별자.
public enum CoroutineQualifiers {
    public static let IoDispatcher = "IoDispatcher"
    public static let MainDispatcher = "MainDispatcher"
    public static let DefaultDispatcher = "DefaultDispatcher"
    public static let TtiDispatcher = "TtiDispatcher"
    public static let IoScope = "IoScope"
    public static let MainScope = "MainScope"
}

/// 앱 전역 비동기 스코프 추상화 (Android 의 `CoroutineScope` 대응).
public final class ArchiCoroutineScope: @unchecked Sendable {
    public let name: String
    public let queue: DispatchQueue

    public init(name: String, queue: DispatchQueue) {
        self.name = name
        self.queue = queue
    }

    @discardableResult
    public func launch(_ operation: @escaping @Sendable () async -> Void) -> Task<Void, Never> {
        Task {
            await operation()
        }
    }
}
