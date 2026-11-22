import CommonDomain
import Foundation

/// TTI Logging(Google Apps Script Web App) 전용 네트워크 스택.
public enum TtiLoggingDataModule: DIModule {
    public static let NAMED_TTI_LOGGING = "ttiLogging"
    private static let PLACEHOLDER_BASE_URL = "https://script.google.com/"

    public static func register(in container: DIContainer) {
        container.register(TtiLoggingConfig.self, scope: .singleton) { _ in
            makeTtiLoggingConfig()
        }
        container.register(URLSession.self, named: NAMED_TTI_LOGGING, scope: .singleton) { _ in
            makeTtiLoggingURLSession()
        }
        container.register(HTTPClient.self, named: NAMED_TTI_LOGGING, scope: .singleton) { c in
            makeTtiLoggingHTTPClient(
                session: c.resolve(URLSession.self, named: NAMED_TTI_LOGGING),
                json: c.resolve(ArchiJson.self)
            )
        }
        container.register(TtiLoggingApiService.self, scope: .singleton) { c in
            makeTtiLoggingApiService(
                httpClient: c.resolve(HTTPClient.self, named: NAMED_TTI_LOGGING)
            )
        }
        container.register(TtiLoggingDataSource.self, scope: .singleton) { c in
            makeTtiLoggingDataSource(apiService: c.resolve(TtiLoggingApiService.self))
        }
        container.register(TtiLoggingRepository.self, scope: .singleton) { c in
            makeTtiLoggingRepository(dataSource: c.resolve(TtiLoggingDataSource.self))
        }
    }

    public static func makeTtiLoggingConfig() -> TtiLoggingConfig {
        TtiLoggingConfig(
            isEnabled: !AppConfig.ttiLogURL.isEmpty && !AppConfig.ttiLogToken.isEmpty
        )
    }

    public static func makeTtiLoggingRepository(dataSource: TtiLoggingDataSource) -> TtiLoggingRepository {
        TtiLoggingRepositoryImpl(
            dataSource: dataSource,
            token: AppConfig.ttiLogToken
        )
    }

    public static func makeTtiLoggingDataSource(apiService: TtiLoggingApiService) -> TtiLoggingDataSource {
        TtiLoggingDataSource(
            apiService: apiService,
            endpointUrl: AppConfig.ttiLogURL
        )
    }

    public static func makeTtiLoggingApiService(httpClient: HTTPClient) -> TtiLoggingApiService {
        TtiLoggingApiServiceImpl(httpClient: httpClient)
    }

    public static func makeTtiLoggingURLSession() -> URLSession {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 15
        config.timeoutIntervalForResource = 15
        return URLSession(configuration: config)
    }

    public static func makeTtiLoggingHTTPClient(session: URLSession, json: ArchiJson) -> HTTPClient {
        HTTPClient(
            baseUrl: PLACEHOLDER_BASE_URL,
            session: session,
            json: json,
            interceptors: [],
            enableLogging: AppConfig.isDebug
        )
    }
}
