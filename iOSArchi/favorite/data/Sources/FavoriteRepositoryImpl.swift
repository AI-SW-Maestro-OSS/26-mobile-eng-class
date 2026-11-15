import CommonDomain
import CommonEntity
import Foundation

public final class FavoriteRepositoryImpl: FavoriteRepository, @unchecked Sendable {
    private let dataSource: FavoriteDataSource

    public init(dataSource: FavoriteDataSource) {
        self.dataSource = dataSource
    }

    public func getFavoriteItemsFlow() -> AsyncStream<[FavoriteItemVO]> {
        dataSource.getFavoriteItemsFlow()
    }

    public func createFavoriteItem(_ item: FavoriteItemVO) async throws {
        dataSource.createFavoriteItem(item)
    }

    public func deleteFavoriteItem(_ url: String) async throws {
        dataSource.deleteFavoriteItem(url)
    }
}
