import Foundation
import IntroDomain
import IntroEntity

public final class IntroRepositoryImpl: IntroRepository, @unchecked Sendable {
    private let dataSource: IntroDataSource

    public init(dataSource: IntroDataSource) {
        self.dataSource = dataSource
    }

    public func fetchIntro() async throws -> IntroVO {
        try await dataSource.fetchIntro()
    }
}
