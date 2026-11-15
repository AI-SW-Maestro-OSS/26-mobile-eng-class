import SwiftUI

public let SEARCH_TEXT_FIELD_TEST_TAG = "search_text_field"

public struct MediaSearchBar: View {
    private let query: String
    private let placeholder: String
    private let buttonTitle: String
    private let onQueryChange: (String) -> Void
    private let onSearch: () -> Void
    private let onClear: () -> Void

    public init(
        query: String,
        placeholder: String,
        buttonTitle: String,
        onQueryChange: @escaping (String) -> Void,
        onSearch: @escaping () -> Void,
        onClear: (() -> Void)? = nil
    ) {
        self.query = query
        self.placeholder = placeholder
        self.buttonTitle = buttonTitle
        self.onQueryChange = onQueryChange
        self.onSearch = onSearch
        self.onClear = onClear ?? { onQueryChange("") }
    }

    public var body: some View {
        HStack(spacing: 8) {
            HStack(spacing: 8) {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(DesignSystemThemeImpl.designSystemColor.textCaption)
                TextField(
                    placeholder,
                    text: Binding(
                        get: { query },
                        set: { newValue in
                            if newValue.isEmpty && !query.isEmpty {
                                onClear()
                            } else {
                                onQueryChange(newValue)
                            }
                        }
                    )
                )
                .font(DesignSystemThemeImpl.typeScale.textRegularM.font)
                .foregroundColor(DesignSystemThemeImpl.designSystemColor.textPrimary)
                .submitLabel(.search)
                .onSubmit {
                    onSearch()
                }
                .accessibilityIdentifier(SEARCH_TEXT_FIELD_TEST_TAG)

                if !query.isEmpty {
                    Button(action: onClear) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(DesignSystemThemeImpl.designSystemColor.textCaption)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(
                RoundedRectangle(cornerRadius: 10)
                    .fill(DesignSystemThemeImpl.designSystemColor.bgSecondary)
            )

            Button(action: onSearch) {
                ArchiText(
                    text: buttonTitle,
                    style: DesignSystemThemeImpl.typeScale.textStrongM,
                    color: DesignSystemThemeImpl.designSystemColor.white
                )
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .background(
                    RoundedRectangle(cornerRadius: 10)
                        .fill(DesignSystemThemeImpl.designSystemColor.accentBlue)
                )
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }
}
