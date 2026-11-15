import CommonDomain
import CommonPresentation
import Foundation
import SwiftUI

public struct MainActivityView: View {
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
            RootComposable(
                rootContainer: rootContainer,
                initialDeeplink: initialDeeplink
            )
        }
    }
}
