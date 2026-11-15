import SwiftUI

public struct FavoriteHeartAnimationOverlay: View {
    private let visible: Bool
    private let isLowEndDevice: Bool
    private let onAnimationFinished: () -> Void

    @State private var scale: CGFloat = 0.4
    @State private var opacity: Double = 0.0

    public init(
        visible: Bool,
        isLowEndDevice: Bool = false,
        onAnimationFinished: @escaping () -> Void
    ) {
        self.visible = visible
        self.isLowEndDevice = isLowEndDevice
        self.onAnimationFinished = onAnimationFinished
    }

    public var body: some View {
        Group {
            if visible {
                Image(systemName: "heart.fill")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 64, height: 64)
                    .foregroundColor(DesignSystemThemeImpl.designSystemColor.accentRed)
                    .scaleEffect(isLowEndDevice ? 1.0 : scale)
                    .opacity(isLowEndDevice ? 1.0 : opacity)
                    .onAppear {
                        if isLowEndDevice {
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.35) {
                                onAnimationFinished()
                            }
                        } else {
                            scale = 0.4
                            opacity = 1.0
                            withAnimation(.spring(response: 0.3, dampingFraction: 0.55)) {
                                scale = 1.25
                            }
                            withAnimation(.easeOut(duration: 0.25).delay(0.35)) {
                                scale = 1.0
                                opacity = 0.0
                            }
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.65) {
                                onAnimationFinished()
                            }
                        }
                    }
            }
        }
        .allowsHitTesting(false)
    }
}
