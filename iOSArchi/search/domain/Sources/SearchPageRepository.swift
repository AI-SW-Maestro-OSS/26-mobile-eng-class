import Foundation

public protocol SearchPageRepository: Sendable {
    func fetchSearchPage() async throws -> SearchPageVO
}
