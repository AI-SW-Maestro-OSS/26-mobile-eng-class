import CommonDomain
import CommonPresentation
import Foundation
import SwiftUI

public struct AppNavHost: View {
    private let currentKey: GenericNavKey
    private let rootContainer: DIContainer
    private let onOpenDeeplink: (String) -> Void

    public init(
        currentKey: GenericNavKey,
        rootContainer: DIContainer,
        onOpenDeeplink: @escaping (String) -> Void
    ) {
        self.currentKey = currentKey
        self.rootContainer = rootContainer
        self.onOpenDeeplink = onOpenDeeplink
    }

    public var body: some View {
        let scopeContainer = ViewModelStoreCache.shared.scope(for: currentKey.id, parent: rootContainer)
        let navHelper = scopeContainer.resolve(NavigationHelper.self)
        let msgHelper = scopeContainer.resolve(MessageHelper.self)

        Group {
            if let route = AppRouteRegistry.route(for: currentKey.routePath) {
                route.contentBuilder(currentKey, scopeContainer, onOpenDeeplink)
            } else {
                ArchiText(
                    text: "Unknown Route: \(currentKey.routePath)",
                    style: DesignSystemThemeImpl.typeScale.textStrongM,
                    color: DesignSystemThemeImpl.designSystemColor.accentRed
                )
            }
        }
        .id(currentKey.id)
        .environment(\.localNavigationHelper, navHelper)
        .environment(\.localMessageHelper, msgHelper)
    }
}
