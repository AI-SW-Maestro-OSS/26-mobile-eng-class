import SwiftUI

public struct DesignSystemTypeScale: Sendable {
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

    public func withStringKey(_ typeScale: String) -> TextStyleSpec {
        switch typeScale {
        case "titleStrongL": return titleStrongL
        case "textStrongL": return textStrongL
        case "textRegularL": return textRegularL
        case "textStrongM": return textStrongM
        case "textRegularM": return textRegularM
        case "textRegularS": return textRegularS
        case "textRegularXS": return textRegularXS
        default: return textRegularM
        }
    }
}
