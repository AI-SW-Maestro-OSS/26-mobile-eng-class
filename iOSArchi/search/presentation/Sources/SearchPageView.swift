import CommonDomain
import CommonPresentation
import Foundation
import SearchDomain
import SwiftUI

public struct SearchPageView: View {
    @ObservedObject private var viewModel: SearchViewModel
    private let onOpenDeeplink: ((String) -> Void)?

    public init(
        viewModel: SearchViewModel,
        onOpenDeeplink: ((String) -> Void)? = nil
    ) {
        self.viewModel = viewModel
        self.onOpenDeeplink = onOpenDeeplink
    }

    public var body: some View {
        SearchPageContent(
            uiState: viewModel.uiState,
            placeholder: viewModel.localizationHelper.string(for: .searchHint),
            buttonTitle: viewModel.localizationHelper.string(for: .navTabSearch),
            emptyResultText: viewModel.localizationHelper.string(for: .mediaEmpty),
            initialGuideText: viewModel.localizationHelper.string(for: .searchPrompt),
            onQueryChange: { viewModel.onIntent(.updateQuery($0)) },
            onSearch: { viewModel.onIntent(.submitSearch) },
            onClear: { viewModel.onIntent(.clear) },
            onLoadMore: { viewModel.onIntent(.loadMore) },
            onItemClick: { idx, item in viewModel.onIntent(.tapItem(index: idx, item: item)) },
            onFavoriteClick: { item in viewModel.onIntent(.toggleFavorite(item)) },
            onSduiDeeplinkClick: { deeplink in
                if let onOpenDeeplink {
                    onOpenDeeplink(deeplink)
                } else if let url = URL(string: deeplink),
                          let components = URLComponents(url: url, resolvingAgainstBaseURL: false),
                          let query = components.queryItems?.first(where: { $0.name == "query" })?.value {
                    viewModel.onIntent(.searchWithQuery(query))
                }
            }
        )
        .jankPageEffect(SearchPage.path)
    }
}

public struct SearchPageContent: View {
    private let uiState: SearchUIState
    private let placeholder: String
    private let buttonTitle: String
    private let emptyResultText: String
    private let initialGuideText: String
    private let onQueryChange: (String) -> Void
    private let onSearch: () -> Void
    private let onClear: () -> Void
    private let onLoadMore: () -> Void
    private let onItemClick: (Int, MediaItemUiState) -> Void
    private let onFavoriteClick: (MediaItemUiState) -> Void
    private let onSduiDeeplinkClick: (String) -> Void

    public init(
        uiState: SearchUIState,
        placeholder: String = "Search images & videos",
        buttonTitle: String = "Search",
        emptyResultText: String = "No results found.",
        initialGuideText: String = "Enter a keyword to search images and videos.",
        onQueryChange: @escaping (String) -> Void,
        onSearch: @escaping () -> Void,
        onClear: @escaping () -> Void = {},
        onLoadMore: @escaping () -> Void,
        onItemClick: @escaping (Int, MediaItemUiState) -> Void,
        onFavoriteClick: @escaping (MediaItemUiState) -> Void,
        onSduiDeeplinkClick: @escaping (String) -> Void = { _ in }
    ) {
        self.uiState = uiState
        self.placeholder = placeholder
        self.buttonTitle = buttonTitle
        self.emptyResultText = emptyResultText
        self.initialGuideText = initialGuideText
        self.onQueryChange = onQueryChange
        self.onSearch = onSearch
        self.onClear = onClear
        self.onLoadMore = onLoadMore
        self.onItemClick = onItemClick
        self.onFavoriteClick = onFavoriteClick
        self.onSduiDeeplinkClick = onSduiDeeplinkClick
    }

    public var body: some View {
        VStack(spacing: 0) {
            MediaSearchBar(
                query: uiState.query,
                placeholder: placeholder,
                buttonTitle: buttonTitle,
                onQueryChange: onQueryChange,
                onSearch: onSearch,
                onClear: onClear
            )

            ZStack {
                if uiState.isLoading && uiState.items.isEmpty {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if !uiState.hasSearched {
                    VStack(spacing: 12) {
                        Image(systemName: "magnifyingglass")
                            .font(DesignSystemThemeImpl.typeScale.titleStrongL.font)
                            .foregroundColor(DesignSystemThemeImpl.designSystemColor.textCaption)
                        ArchiText(
                            text: initialGuideText,
                            style: DesignSystemThemeImpl.typeScale.textRegularL,
                            color: DesignSystemThemeImpl.designSystemColor.textCaption,
                            textAlign: .center
                        )
                    }
                    .padding(24)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if uiState.items.isEmpty {
                    VStack(spacing: 12) {
                        ArchiText(
                            text: emptyResultText,
                            style: DesignSystemThemeImpl.typeScale.textRegularL,
                            color: DesignSystemThemeImpl.designSystemColor.textCaption,
                            textAlign: .center
                        )
                    }
                    .padding(24)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ContentsList(
                        items: uiState.items,
                        isLoadingMore: uiState.isLoadingMore,
                        isLowEndDevice: uiState.isLowEndDevice,
                        onItemClick: onItemClick,
                        onFavoriteClick: onFavoriteClick,
                        onLoadMore: onLoadMore
                    )
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .background(DesignSystemThemeImpl.designSystemColor.bgPrimary)
    }
}
