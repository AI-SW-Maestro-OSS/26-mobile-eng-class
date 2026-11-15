import CommonEntity
import Foundation

public final class FavoriteDataSource: @unchecked Sendable {
    private let storage: FavoriteKVStorage

    public init(storage: FavoriteKVStorage) {
        self.storage = storage
    }

    public func getFavoriteItemsFlow() -> AsyncStream<[FavoriteItemVO]> {
        let dtoStream = storage.observeItems()
        return AsyncStream { continuation in
            let task = Task {
                for await dtos in dtoStream {
                    if Task.isCancelled { break }
                    continuation.yield(dtos.map { $0.toVO() })
                }
                continuation.finish()
            }
            continuation.onTermination = { _ in
                task.cancel()
            }
        }
    }

    public func createFavoriteItem(_ item: FavoriteItemVO) {
        storage.upsert(FavoriteItemDto(from: item))
    }

    public func deleteFavoriteItem(_ url: String) {
        storage.remove(urlKey: url)
    }
}
