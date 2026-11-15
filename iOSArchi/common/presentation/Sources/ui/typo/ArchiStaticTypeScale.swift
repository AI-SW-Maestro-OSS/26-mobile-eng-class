import SwiftUI

public struct TextStyleSpec: Sendable, Equatable {
    public let fontWeight: Font.Weight
    public let fontSize: CGFloat
    public let lineHeight: CGFloat
    public let letterSpacing: CGFloat

    public init(
        fontWeight: Font.Weight,
        fontSize: CGFloat,
        lineHeight: CGFloat,
        letterSpacing: CGFloat
    ) {
        self.fontWeight = fontWeight
        self.fontSize = fontSize
        self.lineHeight = lineHeight
        self.letterSpacing = letterSpacing
    }

    public var font: Font {
        .system(size: fontSize, weight: fontWeight)
    }

    public var lineSpacing: CGFloat {
        max(0, lineHeight - fontSize)
    }
}

public struct ArchiStaticTypeScale: Sendable {
    public let titleStrongL: TextStyleSpec
    public let textStrongL: TextStyleSpec
    public let textRegularL: TextStyleSpec
    public let textStrongM: TextStyleSpec
    public let textRegularM: TextStyleSpec
    public let textRegularS: TextStyleSpec
    public let textRegularXS: TextStyleSpec

    public init(
        titleStrongL: TextStyleSpec,
        textStrongL: TextStyleSpec,
        textRegularL: TextStyleSpec,
        textStrongM: TextStyleSpec,
        textRegularM: TextStyleSpec,
        textRegularS: TextStyleSpec,
        textRegularXS: TextStyleSpec
    ) {
        self.titleStrongL = titleStrongL
        self.textStrongL = textStrongL
        self.textRegularL = textRegularL
        self.textStrongM = textStrongM
        self.textRegularM = textRegularM
        self.textRegularS = textRegularS
        self.textRegularXS = textRegularXS
    }

    public func toDesignSystemTypeScale() -> DesignSystemTypeScale {
        DesignSystemTypeScale(
            titleStrongL: titleStrongL,
            textStrongL: textStrongL,
            textRegularL: textRegularL,
            textStrongM: textStrongM,
            textRegularM: textRegularM,
            textRegularS: textRegularS,
            textRegularXS: textRegularXS
        )
    }
}
