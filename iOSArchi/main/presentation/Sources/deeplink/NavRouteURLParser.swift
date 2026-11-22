import Foundation

public struct ParsedDeeplinkURL: Sendable, Equatable {
    public let path: String
    public let queryParams: [String: String]

    public init(path: String, queryParams: [String: String]) {
        self.path = path
        self.queryParams = queryParams
    }
}

public enum NavRouteURLParser {
    public static func parse(_ url: URL) -> ParsedDeeplinkURL? {
        guard let components = URLComponents(url: url, resolvingAgainstBaseURL: false) else {
            return nil
        }

        let rawPath: String
        if let host = components.host, host != "example.architecture", !host.contains(".") {
            rawPath = "/\(host)\(components.path)"
        } else {
            rawPath = components.path.isEmpty ? "/" : components.path
        }

        var queryParams: [String: String] = [:]
        for item in components.queryItems ?? [] {
            if let value = item.value {
                queryParams[item.name] = value
            }
        }

        return ParsedDeeplinkURL(path: rawPath, queryParams: queryParams)
    }

    public static func buildArgsJson(
        pathParams: [String: String],
        queryParams: [String: String]
    ) -> String {
        var merged: [String: Any] = [:]
        for (k, v) in queryParams {
            merged[k] = coerceScalar(v)
        }
        for (k, v) in pathParams {
            merged[k] = coerceScalar(v)
        }
        guard !merged.isEmpty,
              let data = try? JSONSerialization.data(withJSONObject: merged, options: [.sortedKeys]),
              let json = String(data: data, encoding: .utf8) else {
            return "{}"
        }
        return json
    }

    private static func coerceScalar(_ raw: String) -> Any {
        if raw.lowercased() == "true" { return true }
        if raw.lowercased() == "false" { return false }
        if let intVal = Int(raw) { return intVal }
        if let doubleVal = Double(raw), raw.contains(".") { return doubleVal }
        return raw
    }
}
