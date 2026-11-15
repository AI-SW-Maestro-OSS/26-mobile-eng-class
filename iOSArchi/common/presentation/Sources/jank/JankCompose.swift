import SwiftUI

private struct LocalJankReporterKey: EnvironmentKey {
    static let defaultValue: JankReporter? = nil
}

public extension EnvironmentValues {
    var localJankReporter: JankReporter? {
        get { self[LocalJankReporterKey.self] }
        set { self[LocalJankReporterKey.self] = newValue }
    }
}

public struct JankPageEffect: ViewModifier {
    private let pageName: String
    @Environment(\.localJankReporter) private var jankReporter

    public init(pageName: String) {
        self.pageName = pageName
    }

    public func body(content: Content) -> some View {
        content
            .onAppear {
                jankReporter?.startTracking(page: pageName)
            }
            .onDisappear {
                jankReporter?.flushAndStop(page: pageName)
            }
    }
}

public extension View {
    func jankPageEffect(_ pageName: String) -> some View {
        modifier(JankPageEffect(pageName: pageName))
    }
}
