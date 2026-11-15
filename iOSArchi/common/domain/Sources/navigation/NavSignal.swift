import Foundation

/// 단일 네비게이션 플로우에 흘려보내는 신호.
public enum NavSignal: Equatable, Sendable {
    case GoToDestPage(route: NavRoute)
    case DeepLink(route: NavRoute)
    case Back
    case BackTo(route: NavRoute)
    case BackToInitialPage
}
