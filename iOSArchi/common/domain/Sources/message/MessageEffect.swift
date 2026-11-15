import Foundation

public enum MessageEffect: Sendable {
    case ShowToastMsg(message: String)
    case ShowSnackBarError(message: String)
    case ShowOneButtonDialog(OneButtonDialogSpec)

    public struct OneButtonDialogSpec: Identifiable, Sendable {
        public let id = UUID()
        public let titleText: String?
        public let descText: String
        public let cantIgnore: Bool
        public let buttonText: String
        public let onClickButton: (@Sendable () -> Void)?

        public init(
            titleText: String?,
            descText: String,
            cantIgnore: Bool,
            buttonText: String,
            onClickButton: (@Sendable () -> Void)?
        ) {
            self.titleText = titleText
            self.descText = descText
            self.cantIgnore = cantIgnore
            self.buttonText = buttonText
            self.onClickButton = onClickButton
        }
    }
}
