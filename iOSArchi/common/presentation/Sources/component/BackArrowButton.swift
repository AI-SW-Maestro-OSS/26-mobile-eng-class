import SwiftUI

public struct BackArrowButton: View {
    private let onClick: () -> Void
    private let tintColor: Color

    public init(
        tintColor: Color = DesignSystemThemeImpl.designSystemColor.textPrimary,
        onClick: @escaping () -> Void
    ) {
        self.tintColor = tintColor
        self.onClick = onClick
    }

    public var body: some View {
        Button(action: onClick) {
            Image(systemName: "chevron.left")
                .font(DesignSystemThemeImpl.typeScale.textStrongL.font)
                .foregroundColor(tintColor)
                .frame(width: 40, height: 40)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}
