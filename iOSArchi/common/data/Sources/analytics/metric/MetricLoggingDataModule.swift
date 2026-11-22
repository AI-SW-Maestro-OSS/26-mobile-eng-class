import CommonDomain
import Foundation

/// Metric Logging(Google Apps Script Web App) 전용 네트워크 스택.
public enum MetricLoggingDataModule: DIModule {
    public static let NAMED_METRIC_LOGGING = "metricLogging"
    private static let PLACEHOLDER_BASE_URL = "https://script.google.com/"

    public static func register(in container: DIContainer) {
        container.register(MetricLoggingConfig.self, scope: .singleton) { _ in
            makeMetricLoggingConfig()
        }
        container.register(URLSession.self, named: NAMED_METRIC_LOGGING, scope: .singleton) { _ in
            makeMetricLoggingURLSession()
        }
        container.register(HTTPClient.self, named: NAMED_METRIC_LOGGING, scope: .singleton) { c in
            makeMetricLoggingHTTPClient(
                session: c.resolve(URLSession.self, named: NAMED_METRIC_LOGGING),
                json: c.resolve(ArchiJson.self)
            )
        }
        container.register(MetricLoggingApiService.self, scope: .singleton) { c in
            makeMetricLoggingApiService(
                httpClient: c.resolve(HTTPClient.self, named: NAMED_METRIC_LOGGING)
            )
        }
        container.register(MetricLoggingDataSource.self, scope: .singleton) { c in
            makeMetricLoggingDataSource(apiService: c.resolve(MetricLoggingApiService.self))
        }
        container.register(MetricLoggingRepository.self, scope: .singleton) { c in
            makeMetricLoggingRepository(dataSource: c.resolve(MetricLoggingDataSource.self))
        }
    }

    public static func makeMetricLoggingConfig() -> MetricLoggingConfig {
        MetricLoggingConfig(
            isEnabled: !AppConfig.metricLogURL.isEmpty && !AppConfig.metricLogToken.isEmpty
        )
    }

    public static func makeMetricLoggingRepository(dataSource: MetricLoggingDataSource) -> MetricLoggingRepository {
        MetricLoggingRepositoryImpl(
            dataSource: dataSource,
            token: AppConfig.metricLogToken
        )
    }

    public static func makeMetricLoggingDataSource(apiService: MetricLoggingApiService) -> MetricLoggingDataSource {
        MetricLoggingDataSource(
            apiService: apiService,
            endpointUrl: AppConfig.metricLogURL
        )
    }

    public static func makeMetricLoggingApiService(httpClient: HTTPClient) -> MetricLoggingApiService {
        MetricLoggingApiServiceImpl(httpClient: httpClient)
    }

    public static func makeMetricLoggingURLSession() -> URLSession {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 15
        config.timeoutIntervalForResource = 15
        return URLSession(configuration: config)
    }

    public static func makeMetricLoggingHTTPClient(session: URLSession, json: ArchiJson) -> HTTPClient {
        HTTPClient(
            baseUrl: PLACEHOLDER_BASE_URL,
            session: session,
            json: json,
            interceptors: [],
            enableLogging: AppConfig.isDebug
        )
    }
}
