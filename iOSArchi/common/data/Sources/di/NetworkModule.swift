import CommonDomain
import Foundation

public enum NetworkModule: DIModule {
    // API-CONFIG-INJECTION-POINT: base URL / API key 는 AppConfig 로 주입된다.

    public static func register(in container: DIContainer) {
        container.register(ArchiJson.self, scope: .singleton) { _ in
            makeJson()
        }
        container.register(URLSession.self, scope: .singleton) { _ in
            makeURLSession()
        }
        container.register(HTTPClient.self, scope: .singleton) { c in
            makeHTTPClient(
                session: c.resolve(URLSession.self),
                json: c.resolve(ArchiJson.self)
            )
        }
    }

    public static func makeJson() -> ArchiJson {
        ArchiJson()
    }

    public static func makeURLSession() -> URLSession {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30
        config.timeoutIntervalForResource = 30
        return URLSession(configuration: config)
    }

    public static func makeHTTPClient(session: URLSession, json: ArchiJson) -> HTTPClient {
        // API-CONFIG-INJECTION-POINT: 인증 헤더 포맷은 대상 API 에 맞게 교체한다.
        // (기본값은 카카오 OpenAPI 의 "KakaoAK {key}" 포맷)
        let authInterceptor: HTTPClient.RequestInterceptor = { request in
            request.setValue("KakaoAK \(AppConfig.apiKey)", forHTTPHeaderField: "Authorization")
        }
        return HTTPClient(
            baseUrl: AppConfig.apiBaseURL,
            session: session,
            json: json,
            interceptors: [authInterceptor],
            enableLogging: AppConfig.isDebug
        )
    }
}
