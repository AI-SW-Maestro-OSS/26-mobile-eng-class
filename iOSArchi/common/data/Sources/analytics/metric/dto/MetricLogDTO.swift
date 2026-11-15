import Foundation

/// `kotlinx.serialization.json.JsonElement` 에 대응하는 타입 안전 JSON 원시 표현.
public enum JsonElement: Codable, Equatable, Sendable {
    case null
    case bool(Bool)
    case int(Int64)
    case double(Double)
    case string(String)
    case array([JsonElement])
    case object([String: JsonElement])

    public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        if container.decodeNil() {
            self = .null
        } else if let b = try? container.decode(Bool.self) {
            self = .bool(b)
        } else if let i = try? container.decode(Int64.self) {
            self = .int(i)
        } else if let d = try? container.decode(Double.self) {
            self = .double(d)
        } else if let s = try? container.decode(String.self) {
            self = .string(s)
        } else if let arr = try? container.decode([JsonElement].self) {
            self = .array(arr)
        } else if let obj = try? container.decode([String: JsonElement].self) {
            self = .object(obj)
        } else {
            self = .null
        }
    }

    public func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        switch self {
        case .null:
            try container.encodeNil()
        case .bool(let b):
            try container.encode(b)
        case .int(let i):
            try container.encode(i)
        case .double(let d):
            try container.encode(d)
        case .string(let s):
            try container.encode(s)
        case .array(let arr):
            try container.encode(arr)
        case .object(let obj):
            try container.encode(obj)
        }
    }

    public static func fromAny(_ value: Any?) -> JsonElement {
        guard let value else { return .null }
        switch value {
        case let b as Bool:
            return .bool(b)
        case let i as Int:
            return .int(Int64(i))
        case let i64 as Int64:
            return .int(i64)
        case let d as Double:
            return .double(d)
        case let f as Float:
            return .double(Double(f))
        case let s as String:
            return .string(s)
        default:
            return .string(String(describing: value))
        }
    }
}

/// Apps Script `doPost(e)` 가 받는 요청 본문.
public struct MetricLogRequestDTO: Codable, Equatable, Sendable {
    public let token: String?
    public let event: String?
    public let params: [String: JsonElement]?

    public init(
        token: String? = nil,
        event: String? = nil,
        params: [String: JsonElement]? = nil
    ) {
        self.token = token
        self.event = event
        self.params = params
    }
}

/// Apps Script 응답. HTTP 200 이어도 `ok=false` 일 수 있다.
public struct MetricLogResponseDTO: Codable, Equatable, Sendable {
    public let ok: Bool?
    public let error: String?

    public init(ok: Bool? = nil, error: String? = nil) {
        self.ok = ok
        self.error = error
    }
}
