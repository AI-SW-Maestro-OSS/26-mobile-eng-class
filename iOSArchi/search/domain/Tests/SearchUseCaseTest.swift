import CommonDomain
import CommonEntity
import SearchDomain
import TTI
import XCTest

private final class StubMediaSearchRepository: MediaSearchRepository, @unchecked Sendable {
    var result = MediaSearchResultVO(
        items: [
            MediaItemVO(
                type: .image,
                title: "SwiftUI Clean Architecture",
                urlKey: "https://example.com/1.jpg",
                thumbnailImageUrl: "https://example.com/1.jpg",
                contentsImageUrl: "https://example.com/1.jpg",
                collection: "Blog",
                dateTime: "2026-03-01T12:00:00.000+09:00"
            )
        ],
        isEnd: true
    )
    var errorToThrow: Error?

    func search(query: String, page: Int) async throws -> MediaSearchResultVO {
        if let errorToThrow { throw errorToThrow }
        return result
    }
}

private final class SearchTestMessageHelper: MessageHelper, @unchecked Sendable {
    var effect: AsyncStream<MessageEffect> { AsyncStream { $0.finish() } }
    var toasts: [String] = []
    var alerts: [String] = []

    func showToast(_ toastMsg: String) { toasts.append(toastMsg) }
    func showBanner(iconType: IconType, messageText: String, callToActionText: String?, onTapAction: (@Sendable () -> Void)?) {}
    func showBanner(iconType: IconType, messageKey: LocalizedKey, callToActionText: String?, onTapAction: (@Sendable () -> Void)?) {}
    func showAlert(titleText: String?, descText: String, cantIgnore: Bool, buttonText: String, onTapButton: (@Sendable () -> Void)?) {
        alerts.append(descText)
    }
    func showConfirmAlert(titleText: String?, descText: String, cantIgnore: Bool, leftButtonText: String, onTapLeftButton: (@Sendable () -> Void)?, rightButtonText: String, onTapRightButton: (@Sendable () -> Void)?) {
        alerts.append(descText)
    }
}

private final class SearchTestNavigationHelper: NavigationHelper, @unchecked Sendable {
    var navigationStream: AsyncStream<NavSignal> { AsyncStream { $0.finish() } }
    func navigateByRoute(_ route: NavRoute) {}
    func navigateTo(_ page: any Page) {}
    func navigateDeepLink(_ route: NavRoute) {}
    func navigateToBack() {}
    func navigateBackTo(_ page: any Page) {}
    func navigateBackToRoute(_ route: NavRoute) {}
    func navigateToInitial() {}
}

private final class SearchTestLocalizationHelper: LocalizationHelper, @unchecked Sendable {
    func string(for key: LocalizedKey) -> String { key.rawValue }
}

private final class SearchTestTTIHelper: TTIHelper, @unchecked Sendable {
    func startTTITracking(_ page: TTIPage) {}
    func startTTITimeline(_ category: TimelineCategory) {}
    func endTTITimeline(_ category: TimelineCategory) {}
    func endTTITracking() {}
    func shotTTILogging() {}
    func addTTIMetaData(_ metadata: TTIMetaData, value: Any?) {}
}

final class SearchUseCaseTest: XCTestCase {
    func testSearchSuccessReturnsMediaSearchResult() async {
        let repo = StubMediaSearchRepository()
        let useCase = SearchUseCase(
            mediaSearchRepository: repo,
            localizationHelper: SearchTestLocalizationHelper(),
            messageHelper: SearchTestMessageHelper(),
            navigationHelper: SearchTestNavigationHelper(),
            ttiHelper: SearchTestTTIHelper()
        )

        let response = await useCase(query: "Swift", page: 1)
        XCTAssertNotNil(response)
        XCTAssertEqual(response?.items.count, 1)
        XCTAssertEqual(response?.items.first?.urlKey, "https://example.com/1.jpg")
    }

    func testSearchHandlesRateLimitErrorWithToast() async {
        let repo = StubMediaSearchRepository()
        repo.errorToThrow = HTTPResponseError(
            status: .reqTooMany,
            rawCode: 429,
            errorRequestUrl: "https://dapi.kakao.com/v2/search/image",
            message: "Rate limit",
            cause: ErrorCause(SearchErrorType.rateLimitExceeded.type)
        )
        let msgHelper = SearchTestMessageHelper()
        let useCase = SearchUseCase(
            mediaSearchRepository: repo,
            localizationHelper: SearchTestLocalizationHelper(),
            messageHelper: msgHelper,
            navigationHelper: SearchTestNavigationHelper(),
            ttiHelper: SearchTestTTIHelper()
        )

        let response = await useCase(query: "Swift", page: 1)
        XCTAssertNil(response)
        XCTAssertEqual(msgHelper.toasts.count, 1)
    }
}
