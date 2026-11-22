import CommonEntity
import Foundation
import TTI

public final class RegisterFavoriteItemUseCase: BaseUseCase, @unchecked Sendable {
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
    public func callAsFunction(_ favoriteItem: FavoriteItemVO) async -> Result<Void, Error> {
        do {
            try await favoriteRepository.createFavoriteItem(favoriteItem)
            return .success(())
        } catch {
            messageHelper.showBanner(
                iconType: .error,
                messageText: localizationHelper.string(for: .favoriteRegisterFailed),
                callToActionText: nil,
                onTapAction: nil
            )
            return .failure(error)
        }
    }

    @discardableResult
    public func invoke(_ favoriteItem: FavoriteItemVO) async -> Result<Void, Error> {
        await callAsFunction(favoriteItem)
    }
}
