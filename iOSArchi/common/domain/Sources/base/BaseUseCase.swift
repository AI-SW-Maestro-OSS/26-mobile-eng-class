import Foundation
import TTI

open class BaseUseCase: @unchecked Sendable {
    public let localizationHelper: LocalizationHelper
    public let messageHelper: MessageHelper
    public let navigationHelper: NavigationHelper
    public let ttiHelper: TTIHelper // ViewModel 과 공유하는 인스턴스(.scoped)

    public init(
        localizationHelper: LocalizationHelper,
        messageHelper: MessageHelper,
        navigationHelper: NavigationHelper,
        ttiHelper: TTIHelper
    ) {
        self.localizationHelper = localizationHelper
        self.messageHelper = messageHelper
        self.navigationHelper = navigationHelper
        self.ttiHelper = ttiHelper
    }

    public func executeCommonErrorHanding(_ e: HTTPResponseError) {
        switch e.rawCode {
        case 401:
            messageHelper.showAlert(
                titleText: nil,
                descText: "Session expired. Please login again.",
                cantIgnore: true,
                buttonText: "Move to login",
                onTapButton: { [navigationHelper] in
                    // 목적지(로그인 화면)를 직접 지정하지 않는다.
                    // common:domain 이 feature 의 Page를 참조하면 순환 의존이 되므로
                    // "초기 화면으로 이동"이라는 시그널만 날리고 실제 이동은 NavigationHost가 결정
                    navigationHelper.navigateToInitial()
                }
            )
        case 404:
            messageHelper.showAlert(
                titleText: nil,
                descText: "This feature is not currently available in the app version.",
                cantIgnore: true,
                buttonText: "Move to back",
                onTapButton: { [navigationHelper] in
                    navigationHelper.navigateToBack()
                }
            )
        default:
            messageHelper.showAlert(
                titleText: "A temporary error occurred.",
                descText: "error status : \(e.print())",
                cantIgnore: false,
                buttonText: "Ok",
                onTapButton: nil
            )
        }
    }
}
