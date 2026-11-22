import CommonDomain
import Foundation

public enum MediaSearchDataModule: DIModule {
    public static func register(in container: DIContainer) {
        container.register(MediaSearchApiService.self, scope: .singleton) { c in
            makeKakaoSearchApiService(httpClient: c.resolve(HTTPClient.self))
        }
        container.register(MediaSearchDataSource.self, scope: .singleton) { c in
            makeKakaoSearchDataSource(apiService: c.resolve(MediaSearchApiService.self))
        }
        container.register(MediaSearchRepository.self, scope: .singleton) { c in
            MediaSearchRepositoryImpl(searchDataSource: c.resolve(MediaSearchDataSource.self))
        }
    }

    public static func makeKakaoSearchDataSource(apiService: MediaSearchApiService) -> MediaSearchDataSource {
        MediaSearchDataSource(apiService: apiService)
    }

    public static func makeKakaoSearchApiService(httpClient: HTTPClient) -> MediaSearchApiService {
        MediaSearchApiServiceImpl(httpClient: httpClient)
    }
}
