import SwiftUI

public extension Color {
    init(argbHex: UInt64) {
        let a = Double((argbHex >> 24) & 0xFF) / 255.0
        let r = Double((argbHex >> 16) & 0xFF) / 255.0
        let g = Double((argbHex >> 8) & 0xFF) / 255.0
        let b = Double(argbHex & 0xFF) / 255.0
        self.init(.sRGB, red: r, green: g, blue: b, opacity: a)
    }
}

public enum ArchiPaletteColors {
    // FIGMA-TOKEN-INJECTION-POINT: palette
    public static let Gray900 = Color(argbHex: 0xFF212121)
    public static let Gray700 = Color(argbHex: 0xFF616161)
    public static let Gray500 = Color(argbHex: 0xFF9E9E9E)
    public static let Gray300 = Color(argbHex: 0xFFE0E0E0)
    public static let Gray200 = Color(argbHex: 0xFFEEEEEE)
    public static let Gray100 = Color(argbHex: 0xFFF5F5F5)

    public static let Red = Color(argbHex: 0xFFD93025)
    public static let Orange = Color(argbHex: 0xFFF29900)
    public static let Yellow = Color(argbHex: 0xFFF9AB00)
    public static let Green = Color(argbHex: 0xFF1E8E3E)
    public static let Blue = Color(argbHex: 0xFF1A73E8)
    public static let Purple = Color(argbHex: 0xFF9334E6)

    public static let White = Color(argbHex: 0xFFFFFFFF)
    public static let Black = Color(argbHex: 0xFF000000)

    public static let Transparent = Color(argbHex: 0x00000000)

    public static let BlackA20 = Color(argbHex: 0x33000000)
    public static let BlackA40 = Color(argbHex: 0x66000000)
    public static let BlackA60 = Color(argbHex: 0x99000000)
    public static let BlackA80 = Color(argbHex: 0xCC000000)

    public static let WhiteA20 = Color(argbHex: 0x33FFFFFF)
    public static let WhiteA40 = Color(argbHex: 0x66FFFFFF)
    public static let WhiteA60 = Color(argbHex: 0x99FFFFFF)
    public static let WhiteA80 = Color(argbHex: 0xCCFFFFFF)
}

// FIGMA-TOKEN-INJECTION-POINT: semantic-colors
public let DefaultDesignSystemColor = DesignSystemSemanticColors(
    textPrimary: ArchiPaletteColors.Gray900,
    textSecondary: ArchiPaletteColors.Gray700,
    textCaption: ArchiPaletteColors.Gray500,
    bgPrimary: ArchiPaletteColors.White,
    bgSecondary: ArchiPaletteColors.Gray100,
    bgTertiary: ArchiPaletteColors.Gray200,
    strokePrimary: ArchiPaletteColors.Gray900,
    strokeSecondary: ArchiPaletteColors.Gray300,
    accentRed: ArchiPaletteColors.Red,
    accentOrange: ArchiPaletteColors.Orange,
    accentYellow: ArchiPaletteColors.Yellow,
    accentGreen: ArchiPaletteColors.Green,
    accentBlue: ArchiPaletteColors.Blue,
    accentPurple: ArchiPaletteColors.Purple,
    white: ArchiPaletteColors.White,
    black: ArchiPaletteColors.Black,
    transparent: ArchiPaletteColors.Transparent,
    dimLight: ArchiPaletteColors.BlackA20,
    dimMedium: ArchiPaletteColors.BlackA40,
    dimHeavy: ArchiPaletteColors.BlackA60,
    dimMax: ArchiPaletteColors.BlackA80,
    dimWhiteLight: ArchiPaletteColors.WhiteA20,
    dimWhiteMedium: ArchiPaletteColors.WhiteA40,
    dimWhiteHeavy: ArchiPaletteColors.WhiteA60,
    dimWhiteMax: ArchiPaletteColors.WhiteA80
)

// FIGMA-TOKEN-INJECTION-POINT: type-scale
public let DefaultDesignSystemStaticTypeScale = ArchiStaticTypeScale(
    titleStrongL: TextStyleSpec(fontWeight: .semibold, fontSize: 21, lineHeight: 28, letterSpacing: -0.3),
    textStrongL: TextStyleSpec(fontWeight: .semibold, fontSize: 18, lineHeight: 24, letterSpacing: -0.3),
    textRegularL: TextStyleSpec(fontWeight: .regular, fontSize: 18, lineHeight: 24, letterSpacing: -0.3),
    textStrongM: TextStyleSpec(fontWeight: .semibold, fontSize: 16, lineHeight: 21, letterSpacing: -0.3),
    textRegularM: TextStyleSpec(fontWeight: .regular, fontSize: 16, lineHeight: 21, letterSpacing: -0.3),
    textRegularS: TextStyleSpec(fontWeight: .regular, fontSize: 14, lineHeight: 19, letterSpacing: -0.3),
    textRegularXS: TextStyleSpec(fontWeight: .regular, fontSize: 12, lineHeight: 16, letterSpacing: -0.3)
)
