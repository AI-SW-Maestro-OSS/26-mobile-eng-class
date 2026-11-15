import CommonData
import CommonDomain
import Foundation
import SearchDomain
import TTI

public enum SearchDataModule: DIModule {
    public static func register(in container: DIContainer) {
        container.register(SearchPageApiService.self, scope: .singleton) { resolver in
            DefaultSearchPageApiService(json: resolver.resolve(ArchiJson.self))
        }
        container.register(SearchPageDataSource.self, scope: .singleton) { resolver in
            SearchPageDataSource(
                apiService: resolver.resolve(SearchPageApiService.self),
                json: resolver.resolve(ArchiJson.self)
            )
        }
        container.register(SearchPageRepository.self, scope: .singleton) { resolver in
            SearchPageRepositoryImpl(dataSource: resolver.resolve(SearchPageDataSource.self))
        }
        container.register(SearchUseCase.self, scope: .viewModelScoped) { resolver in
            SearchUseCase(
                mediaSearchRepository: resolver.resolve(MediaSearchRepository.self),
                resourceHelper: resolver.resolve(ResourceHelper.self),
                messageHelper: resolver.resolve(MessageHelper.self),
                navigationHelper: resolver.resolve(NavigationHelper.self),
                ttiHelper: resolver.resolve(TTIHelper.self)
            )
        }
        container.register(SearchPageUseCase.self, scope: .viewModelScoped) { resolver in
            SearchPageUseCase(
                repository: resolver.resolve(SearchPageRepository.self),
                resourceHelper: resolver.resolve(ResourceHelper.self),
                messageHelper: resolver.resolve(MessageHelper.self),
                navigationHelper: resolver.resolve(NavigationHelper.self),
                ttiHelper: resolver.resolve(TTIHelper.self)
            )
        }
    }
}
