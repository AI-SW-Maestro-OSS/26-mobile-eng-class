import Foundation

/// 단일 네비게이션 스트림. 전진/후진 모두 `NavSignal` 한 가지 형식으로 방출된다.
public protocol NavigationHelper: AnyObject, Sendable {
    var navigationStream: AsyncStream<NavSignal> { get }
    func navigateByRoute(_ route: NavRoute)
    func navigateTo(_ page: any Page)
    func navigateDeepLink(_ route: NavRoute)
    func navigateToBack()
    func navigateBackTo(_ page: any Page)
    func navigateBackToRoute(_ route: NavRoute)
    func navigateToInitial()
}
