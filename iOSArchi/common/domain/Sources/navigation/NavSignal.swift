import Foundation

/// 단일 네비게이션 스트림에 방출되는 신호.
public enum NavSignal: Equatable, Sendable {
    case goToDestPage(route: NavRoute)
    case deepLink(route: NavRoute)
    case back
    case backTo(route: NavRoute)
    case backToInitialPage
}
