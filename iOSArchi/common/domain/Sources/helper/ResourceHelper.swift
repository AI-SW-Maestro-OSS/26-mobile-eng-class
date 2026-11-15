import Foundation

/// 도메인 레이어에서 플랫폼 UI 의존 없이 다국어 문자열을 조회하기 위한 추상화.
public protocol ResourceHelper: Sendable {
    func getString(_ resource: StringResource) -> String
}
