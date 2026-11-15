import CommonDomain
import Foundation
import FullScreenMediaDomain
import TTI

public enum FullScreenMediaDataModule: DIModule {
    public static func register(in container: DIContainer) {
        container.register(GetFullScreenMediaItemsUseCase.self, scope: .viewModelScoped) { resolver in
            GetFullScreenMediaItemsUseCase(
                mediaSearchRepository: resolver.resolve(MediaSearchRepository.self),
                favoriteRepository: resolver.resolve(FavoriteRepository.self),
                resourceHelper: resolver.resolve(ResourceHelper.self),
                messageHelper: resolver.resolve(MessageHelper.self),
                navigationHelper: resolver.resolve(NavigationHelper.self),
                ttiHelper: resolver.resolve(TTIHelper.self)
            )
        }
    }
}
