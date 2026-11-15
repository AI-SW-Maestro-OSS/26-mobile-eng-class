import SwiftUI

public struct ArchiText: View {
    private enum Content {
        case plain(String)
        case attributed(AttributedString)
    }

    private let content: Content
    private let style: TextStyleSpec
    private let color: Color
    private let maxLines: Int?
    private let textAlign: TextAlignment

    public init(
        text: String,
        style: TextStyleSpec = DesignSystemThemeImpl.typeScale.textRegularM,
        color: Color = DesignSystemThemeImpl.designSystemColor.textPrimary,
        maxLines: Int? = nil,
        textAlign: TextAlignment = .leading
    ) {
        self.content = .plain(text)
        self.style = style
        self.color = color
        self.maxLines = maxLines
        self.textAlign = textAlign
    }

    public init(
        attributedText: AttributedString,
        style: TextStyleSpec = DesignSystemThemeImpl.typeScale.textRegularM,
        color: Color = DesignSystemThemeImpl.designSystemColor.textPrimary,
        maxLines: Int? = nil,
        textAlign: TextAlignment = .leading
    ) {
        self.content = .attributed(attributedText)
        self.style = style
        self.color = color
        self.maxLines = maxLines
        self.textAlign = textAlign
    }

    public var body: some View {
        Group {
            switch content {
            case .plain(let text):
                Text(text)
                    .foregroundColor(color)
            case .attributed(let attributed):
                Text(attributed)
            }
        }
        .font(style.font)
        .lineSpacing(style.lineSpacing)
        .lineLimit(maxLines)
        .multilineTextAlignment(textAlign)
    }
}
