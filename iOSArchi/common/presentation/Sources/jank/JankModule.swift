import CommonDomain
import Foundation

public enum JankModule: DIModule {
    public static func register(in container: DIContainer) {
        container.register(JankReport.self, scope: .singleton) { _ in
            #if DEBUG
            return DebugJankReport()
            #else
            return RemoteJankReport()
            #endif
        }
        container.register(JankReporter.self, scope: .singleton) { resolver in
            JankReporter(report: resolver.resolve(JankReport.self))
        }
    }
}
