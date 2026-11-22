import CommonDomain
import Foundation
import TTI

public enum CommonPresentationModule: DIModule {
    public static func register(in container: DIContainer) {
        container.register(LocalizationHelper.self, scope: .singleton) { _ in
            LocalizationHelperImpl()
        }
        container.register(NavigationHelper.self, scope: .singleton) { _ in
            NavigationHelperImpl()
        }
        container.register(MessageHelper.self, scope: .singleton) { resolver in
            MessageHelperImpl(localizationHelper: resolver.resolve(LocalizationHelper.self))
        }
        container.register(LoggingHelper.self, scope: .singleton) { resolver in
            LoggingHelperImpl(
                repository: resolver.resolve(MetricLoggingRepository.self),
                config: resolver.resolve(MetricLoggingConfig.self),
                ioScope: resolver.resolve(AsyncTaskScope.self, named: ConcurrencyQualifiers.ioTaskScope)
            )
        }
        container.register(GlobalStore.self, scope: .singleton) { _ in
            DefaultGlobalStore()
        }
        container.register(TTIReporter.self, scope: .singleton) { resolver in
            SpreadsheetTTIReporter(
                repository: resolver.resolve(TtiLoggingRepository.self),
                config: resolver.resolve(TtiLoggingConfig.self),
                scope: resolver.resolve(AsyncTaskScope.self, named: ConcurrencyQualifiers.ioTaskScope)
            )
        }
        container.register(TTILogger.self, scope: .singleton) { _ in
            #if DEBUG
            return DebugTTILogger()
            #else
            return RemoteTTILogger()
            #endif
        }
        container.register(TTIHelper.self, scope: .scoped) { resolver in
            TTIHelperImpl(
                reporter: resolver.resolve(TTIReporter.self),
                logger: resolver.resolve(TTILogger.self),
                queue: resolver.resolve(DispatchQueue.self, named: ConcurrencyQualifiers.ttiQueue)
            )
        }
    }
}
