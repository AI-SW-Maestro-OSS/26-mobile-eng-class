import CommonDomain
import CommonEntity
import XCTest

final class GetFavoriteItemsUseCaseTest: XCTestCase {
    func testObserveFavoriteItemsEmitsRepositoryStream() async {
        let repo = FakeFavoriteRepository()
        let sample = FavoriteItemVO(
            type: .IMAGE,
            title: "Sample",
            urlKey: "https://example.com/media.jpg",
            thumbnailUrl: "https://example.com/thumb.jpg",
            contentsImageUrl: "https://example.com/media.jpg",
            dateTime: "2026-03-01T12:00:00.000+09:00"
        )
        repo.items = [sample]

        let useCase = GetFavoriteItemsUseCase(
            favoriteRepository: repo,
            resourceHelper: FakeResourceHelper(),
            messageHelper: FakeMessageHelper(),
            navigationHelper: FakeNavigationHelper(),
            ttiHelper: FakeTTIHelper()
        )

        var received: [[FavoriteItemVO]] = []
        for await batch in useCase() {
            received.append(batch)
        }

        XCTAssertEqual(received.count, 1)
        XCTAssertEqual(received.first, [sample])
    }
}
