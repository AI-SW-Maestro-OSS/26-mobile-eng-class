import SwiftUI

private struct DesignSystemColorEnvironmentKey: EnvironmentKey {
    static let defaultValue: DesignSystemSemanticColors = DefaultDesignSystemColor
}

private struct DesignSystemTypeScaleEnvironmentKey: EnvironmentKey {
    static let defaultValue: DesignSystemTypeScale = DefaultDesignSystemStaticTypeScale.toDesignSystemTypeScale()
}

public extension EnvironmentValues {
    var designSystemColors: DesignSystemSemanticColors {
        get { self[DesignSystemColorEnvironmentKey.self] }
        set { self[DesignSystemColorEnvironmentKey.self] = newValue }
    }

    var designSystemTypeScale: DesignSystemTypeScale {
        get { self[DesignSystemTypeScaleEnvironmentKey.self] }
        set { self[DesignSystemTypeScaleEnvironmentKey.self] = newValue }
    }
}

public struct DesignSystemTheme<Content: View>: View {
    private let colors: DesignSystemSemanticColors
    private let typeScale: DesignSystemTypeScale
    private let content: () -> Content

    public init(
        colors: DesignSystemSemanticColors = DefaultDesignSystemColor,
        typeScale: DesignSystemTypeScale = DefaultDesignSystemStaticTypeScale.toDesignSystemTypeScale(),
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.colors = colors
        self.typeScale = typeScale
        self.content = content
    }

    public var body: some View {
        content()
            .environment(\.designSystemColors, colors)
            .environment(\.designSystemTypeScale, typeScale)
    }
}

public enum DesignSystemThemeImpl {
    public static var designSystemColor: DesignSystemSemanticColors {
        DefaultDesignSystemColor
    }

    public static var archiColor: DesignSystemSemanticColors {
        DefaultDesignSystemColor
    }

    public static var typeScale: DesignSystemTypeScale {
        DefaultDesignSystemStaticTypeScale.toDesignSystemTypeScale()
    }
}

public typealias ArchiThemeImpl = DesignSystemThemeImpl
public typealias ArchiTheme = DesignSystemTheme
