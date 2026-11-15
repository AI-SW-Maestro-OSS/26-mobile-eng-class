import CommonData
import Foundation
import SearchDomain

public final class SearchPageDataSource: BaseRemoteDataSource, @unchecked Sendable {
    private let apiService: SearchPageApiService
    private let json: ArchiJson

    public init(apiService: SearchPageApiService, json: ArchiJson = ArchiJson()) {
        self.apiService = apiService
        self.json = json
        super.init()
    }

    public func fetchSearchPage() async throws -> SearchPageVO {
        let dto = try checkResponse(await apiService.getSearchPage())
        return dto.toVO(json)
    }
}
