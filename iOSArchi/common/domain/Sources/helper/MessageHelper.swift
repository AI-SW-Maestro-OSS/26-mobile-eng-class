import Foundation

public protocol MessageHelper: AnyObject, Sendable {
    var effect: AsyncStream<MessageEffect> { get }

    func showToast(_ toastMsg: String)

    func showBanner(
        iconType: IconType,
        messageText: String,
        callToActionText: String?,
        onTapAction: (@Sendable () -> Void)?
    )

    func showBanner(
        iconType: IconType,
        messageKey: LocalizedKey,
        callToActionText: String?,
        onTapAction: (@Sendable () -> Void)?
    )

    func showAlert(
        titleText: String?,
        descText: String,
        cantIgnore: Bool,
        buttonText: String,
        onTapButton: (@Sendable () -> Void)?
    )

    func showConfirmAlert(
        titleText: String?,
        descText: String,
        cantIgnore: Bool,
        leftButtonText: String,
        onTapLeftButton: (@Sendable () -> Void)?,
        rightButtonText: String,
        onTapRightButton: (@Sendable () -> Void)?
    )
}

public extension MessageHelper {
    func showBanner(
        iconType: IconType = .success,
        messageText: String,
        callToActionText: String? = nil,
        onTapAction: (@Sendable () -> Void)? = nil
    ) {
        showBanner(
            iconType: iconType,
            messageText: messageText,
            callToActionText: callToActionText,
            onTapAction: onTapAction
        )
    }

    func showBanner(
        iconType: IconType = .success,
        messageKey: LocalizedKey,
        callToActionText: String? = nil,
        onTapAction: (@Sendable () -> Void)? = nil
    ) {
        showBanner(
            iconType: iconType,
            messageKey: messageKey,
            callToActionText: callToActionText,
            onTapAction: onTapAction
        )
    }

    func showAlert(
        titleText: String? = nil,
        descText: String,
        cantIgnore: Bool = false,
        buttonText: String = "Ok",
        onTapButton: (@Sendable () -> Void)? = nil
    ) {
        showAlert(
            titleText: titleText,
            descText: descText,
            cantIgnore: cantIgnore,
            buttonText: buttonText,
            onTapButton: onTapButton
        )
    }
}
