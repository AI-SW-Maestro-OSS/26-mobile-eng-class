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
    var dialogs: [String] = []

    func showToast(_ toastMsg: String) { toasts.append(toastMsg) }
    func showSnackBar(iconType: IconType, messageText: String, callToActionText: String?, onClickCTA: (@Sendable () -> Void)?) {}
    func showSnackBar(iconType: IconType, messageRes: StringResource, callToActionText: String?, onClickCTA: (@Sendable () -> Void)?) {}
    func showOneButtonDialog(titleText: String?, descText: String, cantIgnore: Bool, buttonText: String, onClickButton: (@Sendable () -> Void)?) {
        dialogs.append(descText)
    }
    func showTwoButtonDialog(titleText: String?, descText: String, cantIgnore: Bool, leftButtonText: String, onClickLeftButton: (@Sendable () -> Void)?, rightButtonText: String, onClickRightButton: (@Sendable () -> Void)?) {
        dialogs.append(descText)
    }
}

private final class StubNavigationHelper: NavigationHelper, @unchecked Sendable {
    var navigationFlow: AsyncStream<NavSignal> { AsyncStream { $0.finish() } }
    func navigateByRoute(_ route: NavRoute) {}
    func navigateTo(_ page: any Page) {}
    func navigateDeepLink(_ route: NavRoute) {}
    func navigateToBack() {}
    func navigateBackTo(_ page: any Page) {}
    func navigateBackToRoute(_ route: NavRoute) {}
    func navigateToInitial() {}
}

private final class StubResourceHelper: ResourceHelper, @unchecked Sendable {
    func getString(_ resource: StringResource) -> String { resource.rawValue }
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
            resourceHelper: StubResourceHelper(),
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
        repo.errorToThrow = HttpResponseException(
            status: .Unavailable,
            rawCode: 503,
            errorRequestUrl: "https://example.architecture/intro",
            msg: "Maintenance",
            cause: ErrorCause(IntroErrorType.MAINTENANCE.type)
        )
        let msgHelper = StubMessageHelper()
        let useCase = GetIntroUseCase(
            introRepository: repo,
            resourceHelper: StubResourceHelper(),
            messageHelper: msgHelper,
            navigationHelper: StubNavigationHelper(),
            ttiHelper: StubTTIHelper()
        )

        let vo = await useCase()
        XCTAssertNil(vo)
        XCTAssertEqual(msgHelper.dialogs.count, 1)
    }
}
