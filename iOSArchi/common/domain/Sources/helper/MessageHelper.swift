import Foundation

public protocol MessageHelper: AnyObject, Sendable {
    var effect: AsyncStream<MessageEffect> { get }

    func showToast(_ toastMsg: String)

    func showSnackBar(
        iconType: IconType,
        messageText: String,
        callToActionText: String?,
        onClickCTA: (@Sendable () -> Void)?
    )

    func showSnackBar(
        iconType: IconType,
        messageRes: StringResource,
        callToActionText: String?,
        onClickCTA: (@Sendable () -> Void)?
    )

    func showOneButtonDialog(
        titleText: String?,
        descText: String,
        cantIgnore: Bool,
        buttonText: String,
        onClickButton: (@Sendable () -> Void)?
    )

    func showTwoButtonDialog(
        titleText: String?,
        descText: String,
        cantIgnore: Bool,
        leftButtonText: String,
        onClickLeftButton: (@Sendable () -> Void)?,
        rightButtonText: String,
        onClickRightButton: (@Sendable () -> Void)?
    )
}

public extension MessageHelper {
    func showSnackBar(
        iconType: IconType = .SUCCESS,
        messageText: String,
        callToActionText: String? = nil,
        onClickCTA: (@Sendable () -> Void)? = nil
    ) {
        showSnackBar(
            iconType: iconType,
            messageText: messageText,
            callToActionText: callToActionText,
            onClickCTA: onClickCTA
        )
    }

    func showSnackBar(
        iconType: IconType = .SUCCESS,
        messageRes: StringResource,
        callToActionText: String? = nil,
        onClickCTA: (@Sendable () -> Void)? = nil
    ) {
        showSnackBar(
            iconType: iconType,
            messageRes: messageRes,
            callToActionText: callToActionText,
            onClickCTA: onClickCTA
        )
    }

    func showOneButtonDialog(
        titleText: String? = nil,
        descText: String,
        cantIgnore: Bool = false,
        buttonText: String = "Ok",
        onClickButton: (@Sendable () -> Void)? = nil
    ) {
        showOneButtonDialog(
            titleText: titleText,
            descText: descText,
            cantIgnore: cantIgnore,
            buttonText: buttonText,
            onClickButton: onClickButton
        )
    }
}
