import CommonData
import Foundation
import IntroEntity

public final class IntroDataSource: BaseRemoteDataSource, @unchecked Sendable {
    private let apiService: IntroApiService

    public init(apiService: IntroApiService) {
        self.apiService = apiService
        super.init()
    }

    public func fetchIntro() async throws -> IntroVO {
        let response = try await checkResponse(apiService.getIntro())
        return response.toVO()
    }
}
