import Foundation

public enum MessageEffect: Sendable {
    case showToast(message: String)
    case showBannerError(message: String)
    case showAlert(AlertSpec)

    public struct AlertSpec: Identifiable, Sendable {
        public let id = UUID()
        public let titleText: String?
        public let descText: String
        public let cantIgnore: Bool
        public let buttonText: String
        public let onTapButton: (@Sendable () -> Void)?

        public init(
            titleText: String?,
            descText: String,
            cantIgnore: Bool,
            buttonText: String,
            onTapButton: (@Sendable () -> Void)?
        ) {
            self.titleText = titleText
            self.descText = descText
            self.cantIgnore = cantIgnore
            self.buttonText = buttonText
            self.onTapButton = onTapButton
        }
    }
}
