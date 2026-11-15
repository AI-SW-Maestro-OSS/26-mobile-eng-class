import CommonEntity
import Foundation

public protocol FavoriteRepository: Sendable {
    func getFavoriteItemsFlow() -> AsyncStream<[FavoriteItemVO]>
    func createFavoriteItem(_ item: FavoriteItemVO) async throws
    func deleteFavoriteItem(_ url: String) async throws
}
