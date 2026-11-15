import CommonEntity
import Foundation
import TTI

public final class RegisterFavoriteItemUseCase: BaseUseCase, @unchecked Sendable {
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

    @discardableResult
    public func callAsFunction(_ favoriteItem: FavoriteItemVO) async -> Result<Void, Error> {
        do {
            try await favoriteRepository.createFavoriteItem(favoriteItem)
            return .success(())
        } catch {
            messageHelper.showSnackBar(
                iconType: .ERROR,
                messageText: resourceHelper.getString(.FAVORITE_REGISTER_FAILED),
                callToActionText: nil,
                onClickCTA: nil
            )
            return .failure(error)
        }
    }

    @discardableResult
    public func invoke(_ favoriteItem: FavoriteItemVO) async -> Result<Void, Error> {
        await callAsFunction(favoriteItem)
    }
}
