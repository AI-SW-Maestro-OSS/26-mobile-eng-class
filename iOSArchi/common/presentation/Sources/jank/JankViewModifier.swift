import SwiftUI

private struct JankReporterEnvironmentKey: EnvironmentKey {
    static let defaultValue: JankReporter? = nil
}

public extension EnvironmentValues {
    var jankReporter: JankReporter? {
        get { self[JankReporterEnvironmentKey.self] }
        set { self[JankReporterEnvironmentKey.self] = newValue }
    }
}

public struct JankPageEffect: ViewModifier {
    private let pageName: String
    @Environment(\.jankReporter) private var jankReporter

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
