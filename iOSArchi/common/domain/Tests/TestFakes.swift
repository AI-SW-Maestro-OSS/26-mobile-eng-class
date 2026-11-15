import CommonDomain
import CommonEntity
import Foundation
import TTI

final class FakeMessageHelper: MessageHelper, @unchecked Sendable {
    var effect: AsyncStream<MessageEffect> {
        AsyncStream { $0.finish() }
    }

    private(set) var toasts: [String] = []
    private(set) var snackBars: [String] = []
    private(set) var dialogs: [String] = []

    func showToast(_ toastMsg: String) {
        toasts.append(toastMsg)
    }

    func showSnackBar(
        iconType: IconType,
        messageText: String,
        callToActionText: String?,
        onClickCTA: (@Sendable () -> Void)?
    ) {
        snackBars.append(messageText)
    }

    func showSnackBar(
        iconType: IconType,
        messageRes: StringResource,
        callToActionText: String?,
        onClickCTA: (@Sendable () -> Void)?
    ) {
        snackBars.append(messageRes.rawValue)
    }

    func showOneButtonDialog(
        titleText: String?,
        descText: String,
        cantIgnore: Bool,
        buttonText: String,
        onClickButton: (@Sendable () -> Void)?
    ) {
        dialogs.append(descText)
    }

    func showTwoButtonDialog(
        titleText: String?,
        descText: String,
        cantIgnore: Bool,
        leftButtonText: String,
        onClickLeftButton: (@Sendable () -> Void)?,
        rightButtonText: String,
        onClickRightButton: (@Sendable () -> Void)?
    ) {
        dialogs.append(descText)
    }
}

final class FakeNavigationHelper: NavigationHelper, @unchecked Sendable {
    var navigationFlow: AsyncStream<NavSignal> {
        AsyncStream { $0.finish() }
    }

    private(set) var navigatedRoutes: [NavRoute] = []
    private(set) var backCount = 0

    func navigateByRoute(_ route: NavRoute) {
        navigatedRoutes.append(route)
    }

    func navigateTo(_ page: any Page) {
        navigatedRoutes.append(page.toRoute())
    }

    func navigateDeepLink(_ route: NavRoute) {
        navigatedRoutes.append(route)
    }

    func navigateToBack() {
        backCount += 1
    }

    func navigateBackTo(_ page: any Page) {}
    func navigateBackToRoute(_ route: NavRoute) {}
    func navigateToInitial() {}
}

final class FakeResourceHelper: ResourceHelper, @unchecked Sendable {
    func getString(_ resource: StringResource) -> String {
        resource.rawValue
    }
}

final class FakeTTIHelper: TTIHelper, @unchecked Sendable {
    func startTTITracking(_ page: TTIPage) {}
    func startTTITimeline(_ category: TimelineCategory) {}
    func endTTITimeline(_ category: TimelineCategory) {}
    func endTTITracking() {}
    func shotTTILogging() {}
    func addTTIMetaData(_ metadata: TTIMetaData, value: Any?) {}
}

final class FakeFavoriteRepository: FavoriteRepository, @unchecked Sendable {
    var items: [FavoriteItemVO] = []
    var errorToThrow: Error?

    func getFavoriteItemsFlow() -> AsyncStream<[FavoriteItemVO]> {
        let snapshot = items
        return AsyncStream { continuation in
            continuation.yield(snapshot)
            continuation.finish()
        }
    }

    func createFavoriteItem(_ item: FavoriteItemVO) async throws {
        if let errorToThrow { throw errorToThrow }
        items.append(item)
    }

    func deleteFavoriteItem(_ url: String) async throws {
        if let errorToThrow { throw errorToThrow }
        items.removeAll { $0.urlKey == url }
    }
}
