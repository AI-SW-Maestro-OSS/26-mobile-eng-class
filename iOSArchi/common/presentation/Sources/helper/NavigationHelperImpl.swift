import CommonDomain
import Foundation
import SwiftUI

public final class NavigationHelperImpl: NavigationHelper, @unchecked Sendable {
    private let lock = NSLock()
    private var continuations: [UUID: AsyncStream<NavSignal>.Continuation] = [:]

    public init() {}

    public var navigationStream: AsyncStream<NavSignal> {
        AsyncStream { continuation in
            let id = UUID()
            self.lock.lock()
            self.continuations[id] = continuation
            self.lock.unlock()
            continuation.onTermination = { [weak self] _ in
                guard let self else { return }
                self.lock.lock()
                self.continuations.removeValue(forKey: id)
                self.lock.unlock()
            }
        }
    }

    private func emit(_ signal: NavSignal) {
        lock.lock()
        let active = Array(continuations.values)
        lock.unlock()
        for cont in active {
            cont.yield(signal)
        }
    }

    public func navigateByRoute(_ route: NavRoute) {
        emit(.goToDestPage(route: route))
    }

    public func navigateTo(_ page: any Page) {
        emit(.goToDestPage(route: page.toRoute()))
    }

    public func navigateDeepLink(_ route: NavRoute) {
        emit(.deepLink(route: route))
    }

    public func navigateToBack() {
        emit(.back)
    }

    public func navigateBackTo(_ page: any Page) {
        emit(.backTo(route: page.toRoute()))
    }

    public func navigateBackToRoute(_ route: NavRoute) {
        emit(.backTo(route: route))
    }

    public func navigateToInitial() {
        emit(.backToInitialPage)
    }
}

private struct NavigationHelperEnvironmentKey: EnvironmentKey {
    static let defaultValue: NavigationHelper = NavigationHelperImpl()
}

public extension EnvironmentValues {
    var navigationHelper: NavigationHelper {
        get { self[NavigationHelperEnvironmentKey.self] }
        set { self[NavigationHelperEnvironmentKey.self] = newValue }
    }
}
