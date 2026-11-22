import CommonDomain
import CommonPresentation
import Foundation
import SwiftUI

public struct MainView: View {
    private let rootContainer: DIContainer
    private let initialDeeplink: URL?

    public init(
        rootContainer: DIContainer = .shared,
        initialDeeplink: URL? = nil
    ) {
        self.rootContainer = rootContainer
        self.initialDeeplink = initialDeeplink
    }

    public var body: some View {
        DesignSystemTheme {
            RootView(
                rootContainer: rootContainer,
                initialDeeplink: initialDeeplink
            )
        }
    }
}
