import SwiftUI

public struct DesignSystemSemanticColors: Sendable {
    public let textPrimary: Color
    public let textSecondary: Color
    public let textCaption: Color

    public let bgPrimary: Color
    public let bgSecondary: Color
    public let bgTertiary: Color

    public let strokePrimary: Color
    public let strokeSecondary: Color

    public let accentRed: Color
    public let accentOrange: Color
    public let accentYellow: Color
    public let accentGreen: Color
    public let accentBlue: Color
    public let accentPurple: Color

    public let white: Color
    public let black: Color

    public let transparent: Color

    public let dimLight: Color
    public let dimMedium: Color
    public let dimHeavy: Color
    public let dimMax: Color

    public let dimWhiteLight: Color
    public let dimWhiteMedium: Color
    public let dimWhiteHeavy: Color
    public let dimWhiteMax: Color

    public init(
        textPrimary: Color,
        textSecondary: Color,
        textCaption: Color,
        bgPrimary: Color,
        bgSecondary: Color,
        bgTertiary: Color,
        strokePrimary: Color,
        strokeSecondary: Color,
        accentRed: Color,
        accentOrange: Color,
        accentYellow: Color,
        accentGreen: Color,
        accentBlue: Color,
        accentPurple: Color,
        white: Color,
        black: Color,
        transparent: Color,
        dimLight: Color,
        dimMedium: Color,
        dimHeavy: Color,
        dimMax: Color,
        dimWhiteLight: Color,
        dimWhiteMedium: Color,
        dimWhiteHeavy: Color,
        dimWhiteMax: Color
    ) {
        self.textPrimary = textPrimary
        self.textSecondary = textSecondary
        self.textCaption = textCaption
        self.bgPrimary = bgPrimary
        self.bgSecondary = bgSecondary
        self.bgTertiary = bgTertiary
        self.strokePrimary = strokePrimary
        self.strokeSecondary = strokeSecondary
        self.accentRed = accentRed
        self.accentOrange = accentOrange
        self.accentYellow = accentYellow
        self.accentGreen = accentGreen
        self.accentBlue = accentBlue
        self.accentPurple = accentPurple
        self.white = white
        self.black = black
        self.transparent = transparent
        self.dimLight = dimLight
        self.dimMedium = dimMedium
        self.dimHeavy = dimHeavy
        self.dimMax = dimMax
        self.dimWhiteLight = dimWhiteLight
        self.dimWhiteMedium = dimWhiteMedium
        self.dimWhiteHeavy = dimWhiteHeavy
        self.dimWhiteMax = dimWhiteMax
    }

    public func withStringKey(_ key: String) -> Color {
        switch key {
        case "textPrimary": return textPrimary
        case "textSecondary": return textSecondary
        case "textCaption": return textCaption
        case "bgPrimary": return bgPrimary
        case "bgSecondary": return bgSecondary
        case "bgTertiary": return bgTertiary
        case "strokePrimary": return strokePrimary
        case "strokeSecondary": return strokeSecondary
        case "accentRed": return accentRed
        case "accentOrange": return accentOrange
        case "accentYellow": return accentYellow
        case "accentGreen": return accentGreen
        case "accentBlue": return accentBlue
        case "accentPurple": return accentPurple
        case "white": return white
        case "black": return black
        case "transparent": return transparent
        case "dimLight": return dimLight
        case "dimMedium": return dimMedium
        case "dimHeavy": return dimHeavy
        case "dimMax": return dimMax
        case "dimWhiteLight": return dimWhiteLight
        case "dimWhiteMedium": return dimWhiteMedium
        case "dimWhiteHeavy": return dimWhiteHeavy
        case "dimWhiteMax": return dimWhiteMax
        default: return textPrimary
        }
    }
}
