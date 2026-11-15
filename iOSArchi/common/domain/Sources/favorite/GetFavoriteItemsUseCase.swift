import CommonEntity
import Foundation
import TTI

public final class GetFavoriteItemsUseCase: BaseUseCase, @unchecked Sendable {
    private let favoriteRepository: FavoriteRepository

    public init(
        favoriteRepository: FavoriteRepository,
        resourceHelper: ResourceHelper,
        messageHelper: MessageHelper,
        navigationHelper: NavigationHelper,
        ttiHelper: TTIHelper
    ) {
        self.favoriteRepository = favoriteRepository
        super.init(
            resourceHelper: resourceHelper,
            messageHelper: messageHelper,
            navigationHelper: navigationHelper,
            ttiHelper: ttiHelper
        )
    }

    public func callAsFunction() -> AsyncStream<[FavoriteItemVO]> {
        favoriteRepository.getFavoriteItemsFlow()
    }

    public func invoke() -> AsyncStream<[FavoriteItemVO]> {
        callAsFunction()
    }
}
