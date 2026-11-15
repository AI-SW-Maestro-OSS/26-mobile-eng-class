import Foundation

/// TTI 측정 결과를 외부 텔레메트리(예: Spreadsheet / Datadog RUM)로 흘려보내기 위한 추상화.
/// `TTI` 모듈을 플랫폼/SDK 비의존으로 유지하기 위한 Seam.
public protocol TTIReporter: Sendable {
    func startView(key: String, name: String, attributes: [String: Any])
    func stopView(key: String, attributes: [String: Any])
}

public extension TTIReporter {
    func startView(key: String, name: String) {
        startView(key: key, name: name, attributes: [:])
    }
    func stopView(key: String) {
        stopView(key: key, attributes: [:])
    }
}

public final class NoOpTTIReporter: TTIReporter {
    public static let shared = NoOpTTIReporter()
    public init() {}

    public func startView(key: String, name: String, attributes: [String: Any]) {}
    public func stopView(key: String, attributes: [String: Any]) {}
}
