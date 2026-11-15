import CommonData
import Foundation

public final class FavoriteKVStorage: @unchecked Sendable {
    private static let storageKey = "com.example.architecture.favorite_items"

    private let userDefaults: UserDefaults
    private let json: ArchiJson
    private let lock = NSLock()
    private var items: [FavoriteItemDto]
    private var continuations: [UUID: AsyncStream<[FavoriteItemDto]>.Continuation] = [:]

    public init(
        userDefaults: UserDefaults = .standard,
        json: ArchiJson = ArchiJson()
    ) {
        self.userDefaults = userDefaults
        self.json = json
        self.items = Self.loadFromDefaults(userDefaults, json: json)
    }

    public func observeItems() -> AsyncStream<[FavoriteItemDto]> {
        AsyncStream { continuation in
            let id = UUID()
            self.lock.lock()
            let initial = self.items
            self.continuations[id] = continuation
            self.lock.unlock()

            continuation.yield(initial)

            continuation.onTermination = { [weak self] _ in
                guard let self else { return }
                self.lock.lock()
                self.continuations.removeValue(forKey: id)
                self.lock.unlock()
            }
        }
    }

    public func upsert(_ item: FavoriteItemDto) {
        lock.lock()
        if let index = items.firstIndex(where: { $0.urlKey == item.urlKey }) {
            items[index] = item
        } else {
            items.insert(item, at: 0)
        }
        let snapshot = items
        let active = Array(continuations.values)
        saveToDefaults(snapshot)
        lock.unlock()

        for cont in active {
            cont.yield(snapshot)
        }
    }

    public func remove(urlKey: String) {
        lock.lock()
        items.removeAll { $0.urlKey == urlKey }
        let snapshot = items
        let active = Array(continuations.values)
        saveToDefaults(snapshot)
        lock.unlock()

        for cont in active {
            cont.yield(snapshot)
        }
    }

    private func saveToDefaults(_ list: [FavoriteItemDto]) {
        if let data = try? json.encodeToData(list) {
            userDefaults.set(data, forKey: Self.storageKey)
        }
    }

    private static func loadFromDefaults(_ defaults: UserDefaults, json: ArchiJson) -> [FavoriteItemDto] {
        guard let data = defaults.data(forKey: storageKey),
              let decoded = try? json.decodeFromData([FavoriteItemDto].self, data) else {
            return []
        }
        return decoded
    }
}
