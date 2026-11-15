import Foundation
import SearchDomain

public final class SearchPageRepositoryImpl: SearchPageRepository, @unchecked Sendable {
    private let dataSource: SearchPageDataSource

    public init(dataSource: SearchPageDataSource) {
        self.dataSource = dataSource
    }

    public func fetchSearchPage() async throws -> SearchPageVO {
        try await dataSource.fetchSearchPage()
    }
}
