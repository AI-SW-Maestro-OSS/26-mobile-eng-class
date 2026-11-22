import CommonDomain
import CommonEntity
import XCTest

final class RemoveFavoriteItemUseCaseTest: XCTestCase {
    func testRemoveFavoriteSuccessRemovesTargetUrlKey() async {
        let repo = FakeFavoriteRepository()
        let item1 = FavoriteItemVO(type: .image, title: "One", urlKey: "url-1")
        let item2 = FavoriteItemVO(type: .image, title: "Two", urlKey: "url-2")
        repo.items = [item1, item2]

        let useCase = RemoveFavoriteItemUseCase(
            favoriteRepository: repo,
            localizationHelper: FakeLocalizationHelper(),
            messageHelper: FakeMessageHelper(),
            navigationHelper: FakeNavigationHelper(),
            ttiHelper: FakeTTIHelper()
        )

        let result = await useCase("url-1")

        if case .failure(let error) = result {
            XCTFail("Expected success, got \(error)")
        }
        XCTAssertEqual(repo.items, [item2])
    }
}
