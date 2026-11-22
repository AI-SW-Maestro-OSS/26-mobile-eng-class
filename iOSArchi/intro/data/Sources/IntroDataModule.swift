import CommonData
import CommonDomain
import Foundation
import IntroDomain
import TTI

public enum IntroDataModule: DIModule {
    public static func register(in container: DIContainer) {
        container.register(IntroApiService.self, scope: .singleton) { resolver in
            DefaultIntroApiService(httpClient: resolver.resolve(HTTPClient.self))
        }
        container.register(IntroDataSource.self, scope: .singleton) { resolver in
            IntroDataSource(apiService: resolver.resolve(IntroApiService.self))
        }
        container.register(IntroRepository.self, scope: .singleton) { resolver in
            IntroRepositoryImpl(dataSource: resolver.resolve(IntroDataSource.self))
        }
        container.register(GetIntroUseCase.self, scope: .scoped) { resolver in
            GetIntroUseCase(
                introRepository: resolver.resolve(IntroRepository.self),
                localizationHelper: resolver.resolve(LocalizationHelper.self),
                messageHelper: resolver.resolve(MessageHelper.self),
                navigationHelper: resolver.resolve(NavigationHelper.self),
                ttiHelper: resolver.resolve(TTIHelper.self)
            )
        }
    }
}
