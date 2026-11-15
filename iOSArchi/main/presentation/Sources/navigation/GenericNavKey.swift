import Foundation

public struct GenericNavKey: Hashable, Codable, Sendable, Identifiable {
    public let id: UUID
    public let routePath: String
    public let argsJson: String

    public init(
        id: UUID = UUID(),
        routePath: String,
        argsJson: String = "{}"
    ) {
        self.id = id
        self.routePath = routePath
        self.argsJson = argsJson
    }

    public func decodeArgs<T: Decodable>(_ type: T.Type) -> T? {
        guard let data = argsJson.data(using: .utf8) else { return nil }
        return try? JSONDecoder().decode(type, from: data)
    }
}
