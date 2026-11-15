import CommonDomain
import Foundation

public enum MediaSearchDataModule: DIModule {
    public static func register(in container: DIContainer) {
        container.register(MediaSearchApiService.self, scope: .singleton) { c in
            provideKakaoSearchApiService(retrofit: c.resolve(RetrofitClient.self))
        }
        container.register(MediaSearchDataSource.self, scope: .singleton) { c in
            provideKakaoSearchDataSource(apiService: c.resolve(MediaSearchApiService.self))
        }
        container.register(MediaSearchRepository.self, scope: .singleton) { c in
            MediaSearchRepositoryImpl(searchDataSource: c.resolve(MediaSearchDataSource.self))
        }
    }

    public static func provideKakaoSearchDataSource(apiService: MediaSearchApiService) -> MediaSearchDataSource {
        MediaSearchDataSource(apiService: apiService)
    }

    public static func provideKakaoSearchApiService(retrofit: RetrofitClient) -> MediaSearchApiService {
        MediaSearchApiServiceImpl(retrofit: retrofit)
    }
}
