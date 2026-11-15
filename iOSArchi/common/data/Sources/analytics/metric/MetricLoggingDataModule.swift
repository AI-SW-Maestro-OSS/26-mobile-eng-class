import CommonDomain
import Foundation

/// Metric Logging(Google Apps Script Web App) 전용 네트워크 스택.
public enum MetricLoggingDataModule: DIModule {
    public static let NAMED_METRIC_LOGGING = "metricLogging"
    private static let PLACEHOLDER_BASE_URL = "https://script.google.com/"

    public static func register(in container: DIContainer) {
        container.register(MetricLoggingConfig.self, scope: .singleton) { _ in
            provideMetricLoggingConfig()
        }
        container.register(URLSession.self, named: NAMED_METRIC_LOGGING, scope: .singleton) { _ in
            provideMetricLoggingURLSession()
        }
        container.register(RetrofitClient.self, named: NAMED_METRIC_LOGGING, scope: .singleton) { c in
            provideMetricLoggingRetrofit(
                session: c.resolve(URLSession.self, named: NAMED_METRIC_LOGGING),
                json: c.resolve(ArchiJson.self)
            )
        }
        container.register(MetricLoggingApiService.self, scope: .singleton) { c in
            provideMetricLoggingApiService(
                retrofit: c.resolve(RetrofitClient.self, named: NAMED_METRIC_LOGGING)
            )
        }
        container.register(MetricLoggingDataSource.self, scope: .singleton) { c in
            provideMetricLoggingDataSource(apiService: c.resolve(MetricLoggingApiService.self))
        }
        container.register(MetricLoggingRepository.self, scope: .singleton) { c in
            provideMetricLoggingRepository(dataSource: c.resolve(MetricLoggingDataSource.self))
        }
    }

    public static func provideMetricLoggingConfig() -> MetricLoggingConfig {
        MetricLoggingConfig(
            isEnabled: !BuildConfig.METRIC_LOG_URL.isEmpty && !BuildConfig.METRIC_LOG_TOKEN.isEmpty
        )
    }

    public static func provideMetricLoggingRepository(dataSource: MetricLoggingDataSource) -> MetricLoggingRepository {
        MetricLoggingRepositoryImpl(
            dataSource: dataSource,
            token: BuildConfig.METRIC_LOG_TOKEN
        )
    }

    public static func provideMetricLoggingDataSource(apiService: MetricLoggingApiService) -> MetricLoggingDataSource {
        MetricLoggingDataSource(
            apiService: apiService,
            endpointUrl: BuildConfig.METRIC_LOG_URL
        )
    }

    public static func provideMetricLoggingApiService(retrofit: RetrofitClient) -> MetricLoggingApiService {
        MetricLoggingApiServiceImpl(retrofit: retrofit)
    }

    public static func provideMetricLoggingURLSession() -> URLSession {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 15
        config.timeoutIntervalForResource = 15
        return URLSession(configuration: config)
    }

    public static func provideMetricLoggingRetrofit(session: URLSession, json: ArchiJson) -> RetrofitClient {
        RetrofitClient(
            baseUrl: PLACEHOLDER_BASE_URL,
            session: session,
            json: json,
            interceptors: [],
            enableLogging: BuildConfig.DEBUG
        )
    }
}
