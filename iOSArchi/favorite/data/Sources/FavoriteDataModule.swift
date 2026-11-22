import CommonData
import CommonDomain
import Foundation
import TTI

public enum FavoriteDataModule: DIModule {
    public static func register(in container: DIContainer) {
        container.register(FavoriteKVStorage.self, scope: .singleton) { resolver in
            FavoriteKVStorage(json: resolver.resolve(ArchiJson.self))
        }
        container.register(FavoriteDataSource.self, scope: .singleton) { resolver in
            FavoriteDataSource(storage: resolver.resolve(FavoriteKVStorage.self))
        }
        container.register(FavoriteRepository.self, scope: .singleton) { resolver in
            FavoriteRepositoryImpl(dataSource: resolver.resolve(FavoriteDataSource.self))
        }
        container.register(GetFavoriteItemsUseCase.self, scope: .scoped) { resolver in
            GetFavoriteItemsUseCase(
                favoriteRepository: resolver.resolve(FavoriteRepository.self),
                localizationHelper: resolver.resolve(LocalizationHelper.self),
                messageHelper: resolver.resolve(MessageHelper.self),
                navigationHelper: resolver.resolve(NavigationHelper.self),
                ttiHelper: resolver.resolve(TTIHelper.self)
            )
        }
        container.register(RegisterFavoriteItemUseCase.self, scope: .scoped) { resolver in
            RegisterFavoriteItemUseCase(
                favoriteRepository: resolver.resolve(FavoriteRepository.self),
                localizationHelper: resolver.resolve(LocalizationHelper.self),
                messageHelper: resolver.resolve(MessageHelper.self),
                navigationHelper: resolver.resolve(NavigationHelper.self),
                ttiHelper: resolver.resolve(TTIHelper.self)
            )
        }
        container.register(RemoveFavoriteItemUseCase.self, scope: .scoped) { resolver in
            RemoveFavoriteItemUseCase(
                favoriteRepository: resolver.resolve(FavoriteRepository.self),
                localizationHelper: resolver.resolve(LocalizationHelper.self),
                messageHelper: resolver.resolve(MessageHelper.self),
                navigationHelper: resolver.resolve(NavigationHelper.self),
                ttiHelper: resolver.resolve(TTIHelper.self)
            )
        }
    }
}
