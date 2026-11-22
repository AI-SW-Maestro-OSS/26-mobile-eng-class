import CommonDomain
import Foundation
import IntroEntity
import TTI

public final class GetIntroUseCase: BaseUseCase, @unchecked Sendable {
    private let introRepository: IntroRepository

    public init(
        introRepository: IntroRepository,
        localizationHelper: LocalizationHelper,
        messageHelper: MessageHelper,
        navigationHelper: NavigationHelper,
        ttiHelper: TTIHelper
    ) {
        self.introRepository = introRepository
        super.init(
            localizationHelper: localizationHelper,
            messageHelper: messageHelper,
            navigationHelper: navigationHelper,
            ttiHelper: ttiHelper
        )
    }

    public func invoke() async -> IntroVO? {
        do {
            return try await introRepository.fetchIntro()
        } catch let e as HTTPResponseError {
            if let introError = e.handlingErrorOnUseCase(IntroErrorType.self) {
                switch introError {
                case .invalidToken:
                    messageHelper.showToast(introError.errorMsg)
                case .maintenance, .forceUpdateRequired:
                    messageHelper.showAlert(
                        titleText: nil,
                        descText: introError.errorMsg,
                        cantIgnore: true,
                        buttonText: "Ok",
                        onTapButton: nil
                    )
                }
            } else if e.isCommonErrorHandling() {
                executeCommonErrorHanding(e)
            } else {
                messageHelper.showToast(e.message ?? "Failed to load intro.")
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
