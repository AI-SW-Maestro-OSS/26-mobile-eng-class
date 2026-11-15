import CommonDomain
import Foundation

/// TTI Logging(Google Apps Script Web App) 전용 네트워크 스택.
public enum TtiLoggingDataModule: DIModule {
    public static let NAMED_TTI_LOGGING = "ttiLogging"
    private static let PLACEHOLDER_BASE_URL = "https://script.google.com/"

    public static func register(in container: DIContainer) {
        container.register(TtiLoggingConfig.self, scope: .singleton) { _ in
            provideTtiLoggingConfig()
        }
        container.register(URLSession.self, named: NAMED_TTI_LOGGING, scope: .singleton) { _ in
            provideTtiLoggingURLSession()
        }
        container.register(RetrofitClient.self, named: NAMED_TTI_LOGGING, scope: .singleton) { c in
            provideTtiLoggingRetrofit(
                session: c.resolve(URLSession.self, named: NAMED_TTI_LOGGING),
                json: c.resolve(ArchiJson.self)
            )
        }
        container.register(TtiLoggingApiService.self, scope: .singleton) { c in
            provideTtiLoggingApiService(
                retrofit: c.resolve(RetrofitClient.self, named: NAMED_TTI_LOGGING)
            )
        }
        container.register(TtiLoggingDataSource.self, scope: .singleton) { c in
            provideTtiLoggingDataSource(apiService: c.resolve(TtiLoggingApiService.self))
        }
        container.register(TtiLoggingRepository.self, scope: .singleton) { c in
            provideTtiLoggingRepository(dataSource: c.resolve(TtiLoggingDataSource.self))
        }
    }

    public static func provideTtiLoggingConfig() -> TtiLoggingConfig {
        TtiLoggingConfig(
            isEnabled: !BuildConfig.TTI_LOG_URL.isEmpty && !BuildConfig.TTI_LOG_TOKEN.isEmpty
        )
    }

    public static func provideTtiLoggingRepository(dataSource: TtiLoggingDataSource) -> TtiLoggingRepository {
        TtiLoggingRepositoryImpl(
            dataSource: dataSource,
            token: BuildConfig.TTI_LOG_TOKEN
        )
    }

    public static func provideTtiLoggingDataSource(apiService: TtiLoggingApiService) -> TtiLoggingDataSource {
        TtiLoggingDataSource(
            apiService: apiService,
            endpointUrl: BuildConfig.TTI_LOG_URL
        )
    }

    public static func provideTtiLoggingApiService(retrofit: RetrofitClient) -> TtiLoggingApiService {
        TtiLoggingApiServiceImpl(retrofit: retrofit)
    }

    public static func provideTtiLoggingURLSession() -> URLSession {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 15
        config.timeoutIntervalForResource = 15
        return URLSession(configuration: config)
    }

    public static func provideTtiLoggingRetrofit(session: URLSession, json: ArchiJson) -> RetrofitClient {
        RetrofitClient(
            baseUrl: PLACEHOLDER_BASE_URL,
            session: session,
            json: json,
            interceptors: [],
            enableLogging: BuildConfig.DEBUG
        )
    }
}
