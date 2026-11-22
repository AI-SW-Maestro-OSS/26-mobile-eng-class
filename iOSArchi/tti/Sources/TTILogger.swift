import Foundation

/// 디버깅용 로그 싱크. 빌드 타입에 따라 `DebugTTILogger` / `RemoteTTILogger` 중 하나가 주입된다.
public protocol TTILogger: Sendable {
    func debug(category: String, message: String)
}

/// Debug 빌드용 sink. print 로 출력한다.
public final class DebugTTILogger: TTILogger {
    public init() {}

    public func debug(category: String, message: String) {
        print("[\(category)] \(message)")
    }
}

/// Release 빌드용 sink.
/// 추후 사내 모니터링 SDK / Datadog Logs / Sentry 등 외부 로깅 시스템 연동 지점.
public final class RemoteTTILogger: TTILogger {
    public init() {}

    public func debug(category: String, message: String) {
        // TODO: 사내 모니터링 SDK / Datadog Logs / Sentry 등 외부 로깅 연동
    }
}
