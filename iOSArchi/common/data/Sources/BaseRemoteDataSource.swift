import CommonDomain
import Foundation

/// `kotlinx.serialization.json.Json` 에 대응하는 JSON 코덱 래퍼.
/// `ignoreUnknownKeys = true / explicitNulls = false / coerceInputValues = true` 정책을 따른다.
public final class ArchiJson: @unchecked Sendable {
    public let decoder: JSONDecoder
    public let encoder: JSONEncoder

    public init() {
        self.decoder = JSONDecoder()
        self.encoder = JSONEncoder()
    }

    public func decodeFromString<T: Decodable>(_ type: T.Type = T.self, _ string: String) throws -> T {
        let data = Data(string.utf8)
        return try decoder.decode(T.self, from: data)
    }

    public func decodeFromData<T: Decodable>(_ type: T.Type = T.self, _ data: Data) throws -> T {
        try decoder.decode(T.self, from: data)
    }

    public func encodeToString<T: Encodable>(_ value: T) throws -> String {
        let data = try encoder.encode(value)
        return String(data: data, encoding: .utf8) ?? ""
    }

    public func encodeToData<T: Encodable>(_ value: T) throws -> Data {
        try encoder.encode(value)
    }
}

/// Retrofit 의 `Response<T>` 에 대응하는 HTTP 응답 래퍼.
public struct HttpResponse<T: Sendable>: Sendable {
    public let code: Int
    public let message: String
    public let body: T?
    public let errorBody: String?
    public let requestUrl: String

    public init(
        code: Int,
        message: String = "",
        body: T? = nil,
        errorBody: String? = nil,
        requestUrl: String
    ) {
        self.code = code
        self.message = message
        self.body = body
        self.errorBody = errorBody
        self.requestUrl = requestUrl
    }

    public var isSuccessful: Bool {
        (200..<300).contains(code)
    }
}

/// URLSession 기반 경량 Retrofit 대응 클라이언트.
public final class RetrofitClient: @unchecked Sendable {
    public typealias RequestInterceptor = @Sendable (inout URLRequest) -> Void

    public let baseUrl: String
    public let session: URLSession
    public let json: ArchiJson
    public let interceptors: [RequestInterceptor]
    public let enableLogging: Bool

    public init(
        baseUrl: String,
        session: URLSession = .shared,
        json: ArchiJson = ArchiJson(),
        interceptors: [RequestInterceptor] = [],
        enableLogging: Bool = BuildConfig.DEBUG
    ) {
        self.baseUrl = baseUrl
        self.session = session
        self.json = json
        self.interceptors = interceptors
        self.enableLogging = enableLogging
    }

    public func get<T: Decodable & Sendable>(
        path: String,
        query: [String: String] = [:],
        responseType: T.Type = T.self
    ) async throws -> HttpResponse<T> {
        let resolvedUrl = try buildURL(base: baseUrl, path: path, query: query)
        var request = URLRequest(url: resolvedUrl)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        for interceptor in interceptors {
            interceptor(&request)
        }
        return try await execute(request: request, responseType: T.self)
    }

    public func post<Body: Encodable, T: Decodable & Sendable>(
        url: String,
        body: Body,
        responseType: T.Type = T.self
    ) async throws -> HttpResponse<T> {
        let resolvedUrl: URL
        if url.hasPrefix("http://") || url.hasPrefix("https://"), let abs = URL(string: url) {
            resolvedUrl = abs
        } else {
            resolvedUrl = try buildURL(base: baseUrl, path: url, query: [:])
        }
        var request = URLRequest(url: resolvedUrl)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.httpBody = try json.encodeToData(body)
        for interceptor in interceptors {
            interceptor(&request)
        }
        return try await execute(request: request, responseType: T.self)
    }

    private func buildURL(base: String, path: String, query: [String: String]) throws -> URL {
        let normalizedBase = base.hasSuffix("/") ? base : base + "/"
        let normalizedPath = path.hasPrefix("/") ? String(path.dropFirst()) : path
        let fullString = normalizedBase + normalizedPath
        guard var components = URLComponents(string: fullString) else {
            throw URLError(.badURL)
        }
        if !query.isEmpty {
            components.queryItems = query.keys.sorted().map { key in
                URLQueryItem(name: key, value: query[key])
            }
        }
        guard let url = components.url else {
            throw URLError(.badURL)
        }
        return url
    }

    private func execute<T: Decodable & Sendable>(
        request: URLRequest,
        responseType: T.Type
    ) async throws -> HttpResponse<T> {
        let urlString = request.url?.absoluteString ?? baseUrl
        if enableLogging {
            print("[RetrofitClient] --> \(request.httpMethod ?? "GET") \(urlString)")
        }
        let (data, urlResponse) = try await session.data(for: request)
        let httpResp = urlResponse as? HTTPURLResponse
        let statusCode = httpResp?.statusCode ?? 200
        let statusMessage = HTTPURLResponse.localizedString(forStatusCode: statusCode)

        if enableLogging {
            print("[RetrofitClient] <-- \(statusCode) \(urlString) (\(data.count) bytes)")
        }

        if (200..<300).contains(statusCode) {
            let decoded = try? json.decodeFromData(T.self, data)
            return HttpResponse(
                code: statusCode,
                message: statusMessage,
                body: decoded,
                errorBody: decoded == nil ? String(data: data, encoding: .utf8) : nil,
                requestUrl: urlString
            )
        } else {
            let errBody = String(data: data, encoding: .utf8)
            return HttpResponse(
                code: statusCode,
                message: statusMessage,
                body: nil,
                errorBody: errBody,
                requestUrl: urlString
            )
        }
    }
}

open class BaseRemoteDataSource: @unchecked Sendable {
    public init() {}

    public func checkResponse<T: Sendable>(_ response: HttpResponse<T>) throws -> T {
        if response.isSuccessful {
            guard let body = response.body else {
                throw HttpResponseException(
                    status: .Unknown,
                    rawCode: response.code,
                    errorRequestUrl: response.requestUrl,
                    msg: "Successful response with null body: \(response.requestUrl)"
                )
            }
            return body
        }
        let errorBody = response.errorBody
        throw HttpResponseException(
            status: HttpResponseStatus.create(response.code),
            rawCode: response.code,
            errorRequestUrl: response.requestUrl,
            msg: "Http Request Failed (\(response.code)) \(response.message), \(errorBody ?? "")",
            cause: errorBody.map { ErrorCause($0) }
        )
    }

    public func checkResponse<T: Sendable, R>(
        _ response: HttpResponse<T>,
        returnValue: (T) -> R
    ) throws -> R {
        let body = try checkResponse(response)
        return returnValue(body)
    }
}
