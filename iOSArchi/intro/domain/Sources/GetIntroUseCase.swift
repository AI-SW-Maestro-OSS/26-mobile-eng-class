import CommonDomain
import Foundation
import IntroEntity
import TTI

public final class GetIntroUseCase: BaseUseCase, @unchecked Sendable {
    private let introRepository: IntroRepository

    public init(
        introRepository: IntroRepository,
        resourceHelper: ResourceHelper,
        messageHelper: MessageHelper,
        navigationHelper: NavigationHelper,
        ttiHelper: TTIHelper
    ) {
        self.introRepository = introRepository
        super.init(
            resourceHelper: resourceHelper,
            messageHelper: messageHelper,
            navigationHelper: navigationHelper,
            ttiHelper: ttiHelper
        )
    }

    public func invoke() async -> IntroVO? {
        do {
            return try await introRepository.fetchIntro()
        } catch let e as HttpResponseException {
            if let introError = e.handlingErrorOnUseCase(IntroErrorType.self) {
                switch introError {
                case .INVALID_TOKEN:
                    messageHelper.showToast(introError.errorMsg)
                case .MAINTENANCE, .FORCE_UPDATE_REQUIRED:
                    messageHelper.showOneButtonDialog(
                        titleText: nil,
                        descText: introError.errorMsg,
                        cantIgnore: true,
                        buttonText: "Ok",
                        onClickButton: nil
                    )
                }
            } else if e.isCommonErrorHandling() {
                executeCommonErrorHanding(e)
            } else {
                messageHelper.showToast(e.msg ?? "Failed to load intro.")
            }
            return nil
        } catch {
            messageHelper.showToast(error.localizedDescription)
            return nil
        }
    }

    public func callAsFunction() async -> IntroVO? {
        await invoke()
    }
}
