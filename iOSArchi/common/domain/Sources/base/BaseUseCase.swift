import Foundation
import TTI

open class BaseUseCase: @unchecked Sendable {
    public let resourceHelper: ResourceHelper
    public let messageHelper: MessageHelper
    public let navigationHelper: NavigationHelper
    public let ttiHelper: TTIHelper // ViewModel 과 공유하는 인스턴스(@ViewModelScoped)

    public init(
        resourceHelper: ResourceHelper,
        messageHelper: MessageHelper,
        navigationHelper: NavigationHelper,
        ttiHelper: TTIHelper
    ) {
        self.resourceHelper = resourceHelper
        self.messageHelper = messageHelper
        self.navigationHelper = navigationHelper
        self.ttiHelper = ttiHelper
    }

    public func executeCommonErrorHanding(_ e: HttpResponseException) {
        switch e.rawCode {
        case 401:
            messageHelper.showOneButtonDialog(
                titleText: nil,
                descText: "Session expired. Please login again.",
                cantIgnore: true,
                buttonText: "Move to login",
                onClickButton: { [navigationHelper] in
                    // 목적지(로그인 화면)를 직접 지정하지 않는다.
                    // common:domain 이 feature 의 Page를 참조하면 순환 의존이 되므로
                    // "초기 화면으로 이동"이라는 시그널만 날리고 실제 이동은 NavHost가 결정
                    navigationHelper.navigateToInitial()
                }
            )
        case 404:
            messageHelper.showOneButtonDialog(
                titleText: nil,
                descText: "This feature is not currently available in the app version.",
                cantIgnore: true,
                buttonText: "Move to back",
                onClickButton: { [navigationHelper] in
                    navigationHelper.navigateToBack()
                }
            )
        default:
            messageHelper.showOneButtonDialog(
                titleText: "A temporary error occurred.",
                descText: "error status : \(e.print())",
                cantIgnore: false,
                buttonText: "Ok",
                onClickButton: nil
            )
        }
    }
}
