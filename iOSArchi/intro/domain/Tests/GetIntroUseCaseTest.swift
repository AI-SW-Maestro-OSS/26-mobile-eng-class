import CommonDomain
import CommonEntity
import IntroDomain
import IntroEntity
import TTI
import XCTest

private final class StubIntroRepository: IntroRepository, @unchecked Sendable {
    var response: IntroVO = IntroVO(title: "iOSArchi", subtitle: "Clean", version: "1.0.0")
    var errorToThrow: Error?

    func fetchIntro() async throws -> IntroVO {
        if let errorToThrow { throw errorToThrow }
        return response
    }
}

private final class StubMessageHelper: MessageHelper, @unchecked Sendable {
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

private final class StubNavigationHelper: NavigationHelper, @unchecked Sendable {
    var navigationStream: AsyncStream<NavSignal> { AsyncStream { $0.finish() } }
    func navigateByRoute(_ route: NavRoute) {}
    func navigateTo(_ page: any Page) {}
    func navigateDeepLink(_ route: NavRoute) {}
    func navigateToBack() {}
    func navigateBackTo(_ page: any Page) {}
    func navigateBackToRoute(_ route: NavRoute) {}
    func navigateToInitial() {}
}

private final class StubLocalizationHelper: LocalizationHelper, @unchecked Sendable {
    func string(for key: LocalizedKey) -> String { key.rawValue }
}

private final class StubTTIHelper: TTIHelper, @unchecked Sendable {
    func startTTITracking(_ page: TTIPage) {}
    func startTTITimeline(_ category: TimelineCategory) {}
    func endTTITimeline(_ category: TimelineCategory) {}
    func endTTITracking() {}
    func shotTTILogging() {}
    func addTTIMetaData(_ metadata: TTIMetaData, value: Any?) {}
}

final class GetIntroUseCaseTest: XCTestCase {
    func testFetchIntroSuccessReturnsIntroVO() async {
        let repo = StubIntroRepository()
        let useCase = GetIntroUseCase(
            introRepository: repo,
            localizationHelper: StubLocalizationHelper(),
            messageHelper: StubMessageHelper(),
            navigationHelper: StubNavigationHelper(),
            ttiHelper: StubTTIHelper()
        )

        let vo = await useCase()
        XCTAssertNotNil(vo)
        XCTAssertEqual(vo?.title, "iOSArchi")
    }

    func testFetchIntroHandlesMaintenanceError() async {
        let repo = StubIntroRepository()
        repo.errorToThrow = HTTPResponseError(
            status: .unavailable,
            rawCode: 503,
            errorRequestUrl: "https://example.architecture/intro",
            message: "Maintenance",
            cause: ErrorCause(IntroErrorType.maintenance.type)
        )
        let msgHelper = StubMessageHelper()
        let useCase = GetIntroUseCase(
            introRepository: repo,
            localizationHelper: StubLocalizationHelper(),
            messageHelper: msgHelper,
            navigationHelper: StubNavigationHelper(),
            ttiHelper: StubTTIHelper()
        )

        let vo = await useCase()
        XCTAssertNil(vo)
        XCTAssertEqual(msgHelper.alerts.count, 1)
    }
}
