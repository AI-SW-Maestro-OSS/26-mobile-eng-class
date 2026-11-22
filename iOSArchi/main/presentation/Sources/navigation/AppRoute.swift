import CommonDomain
import Foundation
import SwiftUI

public struct AppRoute: @unchecked Sendable {
    public let path: String
    public let isBottomTab: Bool
    public let tabTitleKey: LocalizedKey?
    public let syntheticStack: @Sendable (GenericNavKey) -> [GenericNavKey]
    public let contentBuilder: @MainActor (GenericNavKey, DIContainer, @escaping (String) -> Void) -> AnyView

    public init(
        path: String,
        isBottomTab: Bool = false,
        tabTitleKey: LocalizedKey? = nil,
        syntheticStack: @escaping @Sendable (GenericNavKey) -> [GenericNavKey] = { _ in [] },
        contentBuilder: @escaping @MainActor (GenericNavKey, DIContainer, @escaping (String) -> Void) -> AnyView
    ) {
        self.path = path
        self.isBottomTab = isBottomTab
        self.tabTitleKey = tabTitleKey
        self.syntheticStack = syntheticStack
        self.contentBuilder = contentBuilder
    }
}
