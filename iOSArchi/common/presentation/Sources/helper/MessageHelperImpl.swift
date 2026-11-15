import CommonDomain
import Foundation
import SwiftUI

public final class MessageHelperImpl: MessageHelper, @unchecked Sendable {
    private let lock = NSLock()
    private var continuations: [UUID: AsyncStream<MessageEffect>.Continuation] = [:]
    private let resourceHelper: ResourceHelper

    public init(resourceHelper: ResourceHelper = ResourceHelperImpl()) {
        self.resourceHelper = resourceHelper
    }

    public var effect: AsyncStream<MessageEffect> {
        AsyncStream { continuation in
            let id = UUID()
            self.lock.lock()
            self.continuations[id] = continuation
            self.lock.unlock()
            continuation.onTermination = { [weak self] _ in
                guard let self else { return }
                self.lock.lock()
                self.continuations.removeValue(forKey: id)
                self.lock.unlock()
            }
        }
    }

    private func emit(_ value: MessageEffect) {
        lock.lock()
        let active = Array(continuations.values)
        lock.unlock()
        for cont in active {
            cont.yield(value)
        }
    }

    public func showToast(_ toastMsg: String) {
        emit(.ShowToastMsg(message: toastMsg))
    }

    public func showSnackBar(
        iconType: IconType,
        messageText: String,
        callToActionText: String?,
        onClickCTA: (@Sendable () -> Void)?
    ) {
        emit(.ShowSnackBarError(message: messageText))
    }

    public func showSnackBar(
        iconType: IconType,
        messageRes: StringResource,
        callToActionText: String?,
        onClickCTA: (@Sendable () -> Void)?
    ) {
        emit(.ShowSnackBarError(message: resourceHelper.getString(messageRes)))
    }

    public func showOneButtonDialog(
        titleText: String?,
        descText: String,
        cantIgnore: Bool,
        buttonText: String,
        onClickButton: (@Sendable () -> Void)?
    ) {
        emit(
            .ShowOneButtonDialog(
                MessageEffect.OneButtonDialogSpec(
                    titleText: titleText,
                    descText: descText,
                    cantIgnore: cantIgnore,
                    buttonText: buttonText,
                    onClickButton: onClickButton
                )
            )
        )
    }

    public func showTwoButtonDialog(
        titleText: String?,
        descText: String,
        cantIgnore: Bool,
        leftButtonText: String,
        onClickLeftButton: (@Sendable () -> Void)?,
        rightButtonText: String,
        onClickRightButton: (@Sendable () -> Void)?
    ) {
        emit(
            .ShowOneButtonDialog(
                MessageEffect.OneButtonDialogSpec(
                    titleText: titleText,
                    descText: descText,
                    cantIgnore: cantIgnore,
                    buttonText: rightButtonText,
                    onClickButton: onClickRightButton
                )
            )
        )
    }
}

private struct LocalMessageHelperKey: EnvironmentKey {
    static let defaultValue: MessageHelper = MessageHelperImpl()
}

public extension EnvironmentValues {
    var localMessageHelper: MessageHelper {
        get { self[LocalMessageHelperKey.self] }
        set { self[LocalMessageHelperKey.self] = newValue }
    }
}
