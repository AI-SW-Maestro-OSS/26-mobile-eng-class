import CommonDomain
import Foundation
import SwiftUI

public enum RichTextBuilder {
    public static func buildAttributedString(
        from richTexts: [RichTextVO],
        colors: DesignSystemSemanticColors = DesignSystemThemeImpl.designSystemColor,
        typeScale: DesignSystemTypeScale = DesignSystemThemeImpl.typeScale
    ) -> AttributedString {
        var result = AttributedString()
        for item in richTexts {
            var segment = AttributedString(item.text)
            let style = typeScale.withStringKey(item.typeScale)
            segment.font = style.font
            segment.foregroundColor = colors.withStringKey(item.color)
            if let link = item.link, let url = URL(string: link) {
                segment.link = url
            }
            result.append(segment)
        }
        return result
    }

    public static func decodeAndBuild(
        from jsonString: String,
        colors: DesignSystemSemanticColors = DesignSystemThemeImpl.designSystemColor,
        typeScale: DesignSystemTypeScale = DesignSystemThemeImpl.typeScale
    ) -> AttributedString {
        if let data = jsonString.data(using: .utf8),
           let items = try? JSONDecoder().decode([RichTextVO].self, from: data) {
            return buildAttributedString(from: items, colors: colors, typeScale: typeScale)
        }
        return AttributedString(jsonString)
    }
}
