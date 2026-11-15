import SwiftUI

public struct FavoriteHeartButton: View {
    private let isFavorite: Bool
    private let onClick: () -> Void

    public init(
        isFavorite: Bool,
        onClick: @escaping () -> Void
    ) {
        self.isFavorite = isFavorite
        self.onClick = onClick
    }

    public var body: some View {
        Button(action: onClick) {
            Image(systemName: isFavorite ? "heart.fill" : "heart")
                .font(DesignSystemThemeImpl.typeScale.textStrongM.font)
                .foregroundColor(
                    isFavorite
                        ? DesignSystemThemeImpl.designSystemColor.accentRed
                        : DesignSystemThemeImpl.designSystemColor.white
                )
                .padding(6)
                .background(
                    Circle()
                        .fill(DesignSystemThemeImpl.designSystemColor.dimMedium)
                )
        }
        .buttonStyle(.plain)
    }
}
