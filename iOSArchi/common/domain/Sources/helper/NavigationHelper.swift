import Foundation

/// 단일 네비게이션 플로우. 전진/후진 모두 `NavSignal` 한 가지 형식으로 emit 된다.
public protocol NavigationHelper: AnyObject, Sendable {
    var navigationFlow: AsyncStream<NavSignal> { get }
    func navigateByRoute(_ route: NavRoute)
    func navigateTo(_ page: any Page)
    func navigateDeepLink(_ route: NavRoute)
    func navigateToBack()
    func navigateBackTo(_ page: any Page)
    func navigateBackToRoute(_ route: NavRoute)
    func navigateToInitial()
}
