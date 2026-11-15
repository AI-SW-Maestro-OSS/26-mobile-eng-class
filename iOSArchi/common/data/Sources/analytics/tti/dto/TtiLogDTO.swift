import Foundation

public struct TtiLogRequestDTO: Codable, Equatable, Sendable {
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

public struct TtiLogResponseDTO: Codable, Equatable, Sendable {
    public let ok: Bool?
    public let error: String?

    public init(ok: Bool? = nil, error: String? = nil) {
        self.ok = ok
        self.error = error
    }
}
