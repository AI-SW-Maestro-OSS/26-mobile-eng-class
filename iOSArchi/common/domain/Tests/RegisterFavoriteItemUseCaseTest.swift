import CommonDomain
import CommonEntity
import XCTest

final class RegisterFavoriteItemUseCaseTest: XCTestCase {
    func testRegisterSuccessStoresItem() async {
        let repo = FakeFavoriteRepository()
        let messageHelper = FakeMessageHelper()
        let useCase = RegisterFavoriteItemUseCase(
            favoriteRepository: repo,
            localizationHelper: FakeLocalizationHelper(),
            messageHelper: messageHelper,
            navigationHelper: FakeNavigationHelper(),
            ttiHelper: FakeTTIHelper()
        )

        let item = FavoriteItemVO(type: .image, title: "Architecture", urlKey: "id-100")
        let result = await useCase(item)

        switch result {
        case .success:
            break
        case .failure(let error):
            XCTFail("Expected success, got \(error)")
        }
        XCTAssertEqual(repo.items, [item])
        XCTAssertTrue(messageHelper.banners.isEmpty)
    }

    func testRegisterFailureShowsErrorBanner() async {
        let repo = FakeFavoriteRepository()
        repo.errorToThrow = HTTPResponseError(
            status: .badRequest,
            rawCode: 400,
            errorRequestUrl: "local://favorite",
            message: "Storage full"
        )
        let messageHelper = FakeMessageHelper()
        let useCase = RegisterFavoriteItemUseCase(
            favoriteRepository: repo,
            localizationHelper: FakeLocalizationHelper(),
            messageHelper: messageHelper,
            navigationHelper: FakeNavigationHelper(),
            ttiHelper: FakeTTIHelper()
        )

        let result = await useCase(FavoriteItemVO(type: .image, title: "Fail", urlKey: "id-101"))

        if case .success = result {
            XCTFail("Expected failure")
        }
        XCTAssertEqual(messageHelper.banners.count, 1)
    }
}
