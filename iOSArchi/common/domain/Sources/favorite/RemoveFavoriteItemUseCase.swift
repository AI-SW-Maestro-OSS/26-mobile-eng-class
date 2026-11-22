import Foundation
import TTI

public final class RemoveFavoriteItemUseCase: BaseUseCase, @unchecked Sendable {
    private let favoriteRepository: FavoriteRepository

    public init(
        favoriteRepository: FavoriteRepository,
        localizationHelper: LocalizationHelper,
        messageHelper: MessageHelper,
        navigationHelper: NavigationHelper,
        ttiHelper: TTIHelper
    ) {
        self.favoriteRepository = favoriteRepository
        super.init(
            localizationHelper: localizationHelper,
            messageHelper: messageHelper,
            navigationHelper: navigationHelper,
            ttiHelper: ttiHelper
        )
    }

    @discardableResult
    public func callAsFunction(_ url: String) async -> Result<Void, Error> {
        do {
            try await favoriteRepository.deleteFavoriteItem(url)
            return .success(())
        } catch {
            messageHelper.showBanner(
                iconType: .error,
                messageText: localizationHelper.string(for: .favoriteRemoveFailed),
                callToActionText: nil,
                onTapAction: nil
            )
            return .failure(error)
        }
    }

    @discardableResult
    public func invoke(_ url: String) async -> Result<Void, Error> {
        await callAsFunction(url)
    }
}
