import CommonDomain
import CommonEntity
import XCTest

final class RegisterFavoriteItemUseCaseTest: XCTestCase {
    func testRegisterSuccessStoresItem() async {
        let repo = FakeFavoriteRepository()
        let messageHelper = FakeMessageHelper()
        let useCase = RegisterFavoriteItemUseCase(
            favoriteRepository: repo,
            resourceHelper: FakeResourceHelper(),
            messageHelper: messageHelper,
            navigationHelper: FakeNavigationHelper(),
            ttiHelper: FakeTTIHelper()
        )

        let item = FavoriteItemVO(type: .IMAGE, title: "Architecture", urlKey: "id-100")
        let result = await useCase(item)

        switch result {
        case .success:
            break
        case .failure(let error):
            XCTFail("Expected success, got \(error)")
        }
        XCTAssertEqual(repo.items, [item])
        XCTAssertTrue(messageHelper.snackBars.isEmpty)
    }

    func testRegisterFailureShowsErrorSnackBar() async {
        let repo = FakeFavoriteRepository()
        repo.errorToThrow = HttpResponseException(
            status: .BadRequest,
            rawCode: 400,
            errorRequestUrl: "local://favorite",
            msg: "Storage full"
        )
        let messageHelper = FakeMessageHelper()
        let useCase = RegisterFavoriteItemUseCase(
            favoriteRepository: repo,
            resourceHelper: FakeResourceHelper(),
            messageHelper: messageHelper,
            navigationHelper: FakeNavigationHelper(),
            ttiHelper: FakeTTIHelper()
        )

        let result = await useCase(FavoriteItemVO(type: .IMAGE, title: "Fail", urlKey: "id-101"))

        if case .success = result {
            XCTFail("Expected failure")
        }
        XCTAssertEqual(messageHelper.snackBars.count, 1)
    }
}
