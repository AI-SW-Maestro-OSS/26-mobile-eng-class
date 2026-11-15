import CommonDomain
import Foundation

public enum NetworkModule: DIModule {
    // API-CONFIG-INJECTION-POINT: base URL / API key 는 BuildConfig 로 주입된다.

    public static func register(in container: DIContainer) {
        container.register(ArchiJson.self, scope: .singleton) { _ in
            provideJson()
        }
        container.register(URLSession.self, scope: .singleton) { _ in
            provideURLSession()
        }
        container.register(RetrofitClient.self, scope: .singleton) { c in
            provideRetrofit(
                session: c.resolve(URLSession.self),
                json: c.resolve(ArchiJson.self)
            )
        }
    }

    public static func provideJson() -> ArchiJson {
        ArchiJson()
    }

    public static func provideURLSession() -> URLSession {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30
        config.timeoutIntervalForResource = 30
        return URLSession(configuration: config)
    }

    public static func provideRetrofit(session: URLSession, json: ArchiJson) -> RetrofitClient {
        // API-CONFIG-INJECTION-POINT: 인증 헤더 포맷은 대상 API 에 맞게 교체한다.
        // (기본값은 카카오 OpenAPI 의 "KakaoAK {key}" 포맷)
        let authInterceptor: RetrofitClient.RequestInterceptor = { request in
            request.setValue("KakaoAK \(BuildConfig.API_KEY)", forHTTPHeaderField: "Authorization")
        }
        return RetrofitClient(
            baseUrl: BuildConfig.API_BASE_URL,
            session: session,
            json: json,
            interceptors: [authInterceptor],
            enableLogging: BuildConfig.DEBUG
        )
    }
}
