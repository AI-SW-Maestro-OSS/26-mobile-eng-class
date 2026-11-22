import CommonDomain
import Foundation
import SwiftUI

public final class MessageHelperImpl: MessageHelper, @unchecked Sendable {
    private let lock = NSLock()
    private var continuations: [UUID: AsyncStream<MessageEffect>.Continuation] = [:]
    private let localizationHelper: LocalizationHelper

    public init(localizationHelper: LocalizationHelper = LocalizationHelperImpl()) {
        self.localizationHelper = localizationHelper
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
        emit(.showToast(message: toastMsg))
    }

    public func showBanner(
        iconType: IconType,
        messageText: String,
        callToActionText: String?,
        onTapAction: (@Sendable () -> Void)?
    ) {
        emit(.showBannerError(message: messageText))
    }

    public func showBanner(
        iconType: IconType,
        messageKey: LocalizedKey,
        callToActionText: String?,
        onTapAction: (@Sendable () -> Void)?
    ) {
        emit(.showBannerError(message: localizationHelper.string(for: messageKey)))
    }

    public func showAlert(
        titleText: String?,
        descText: String,
        cantIgnore: Bool,
        buttonText: String,
        onTapButton: (@Sendable () -> Void)?
    ) {
        emit(
            .showAlert(
                MessageEffect.AlertSpec(
                    titleText: titleText,
                    descText: descText,
                    cantIgnore: cantIgnore,
                    buttonText: buttonText,
                    onTapButton: onTapButton
                )
            )
        )
    }

    public func showConfirmAlert(
        titleText: String?,
        descText: String,
        cantIgnore: Bool,
        leftButtonText: String,
        onTapLeftButton: (@Sendable () -> Void)?,
        rightButtonText: String,
        onTapRightButton: (@Sendable () -> Void)?
    ) {
        emit(
            .showAlert(
                MessageEffect.AlertSpec(
                    titleText: titleText,
                    descText: descText,
                    cantIgnore: cantIgnore,
                    buttonText: rightButtonText,
                    onTapButton: onTapRightButton
                )
            )
        )
    }
}

private struct MessageHelperEnvironmentKey: EnvironmentKey {
    static let defaultValue: MessageHelper = MessageHelperImpl()
}

public extension EnvironmentValues {
    var messageHelper: MessageHelper {
        get { self[MessageHelperEnvironmentKey.self] }
        set { self[MessageHelperEnvironmentKey.self] = newValue }
    }
}
