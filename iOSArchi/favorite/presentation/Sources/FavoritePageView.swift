import CommonPresentation
import FavoriteDomain
import SwiftUI

public struct FavoritePageView: View {
    @ObservedObject private var viewModel: FavoriteViewModel

    public init(viewModel: FavoriteViewModel) {
        self.viewModel = viewModel
    }

    public var body: some View {
        FavoritePageContent(
            uiState: viewModel.uiState,
            emptyText: viewModel.localizationHelper.string(for: .favoriteEmpty),
            onItemClick: { idx, item in
                viewModel.onIntent(.tapItem(index: idx, item: item))
            },
            onFavoriteClick: { item in
                viewModel.onIntent(.removeFavorite(item))
            }
        )
        .jankPageEffect(FavoritePage.path)
    }
}

public struct FavoritePageContent: View {
    private let uiState: FavoriteUIState
    private let emptyText: String
    private let onItemClick: (Int, MediaItemUiState) -> Void
    private let onFavoriteClick: (MediaItemUiState) -> Void

    public init(
        uiState: FavoriteUIState,
        emptyText: String = "No favorite items yet.",
        onItemClick: @escaping (Int, MediaItemUiState) -> Void,
        onFavoriteClick: @escaping (MediaItemUiState) -> Void
    ) {
        self.uiState = uiState
        self.emptyText = emptyText
        self.onItemClick = onItemClick
        self.onFavoriteClick = onFavoriteClick
    }

    public var body: some View {
        ZStack {
            DesignSystemThemeImpl.designSystemColor.bgPrimary
                .ignoresSafeArea()

            if uiState.items.isEmpty {
                VStack(spacing: 12) {
                    Image(systemName: "heart.slash")
                        .font(DesignSystemThemeImpl.typeScale.titleStrongL.font)
                        .foregroundColor(DesignSystemThemeImpl.designSystemColor.textCaption)
                    ArchiText(
                        text: emptyText,
                        style: DesignSystemThemeImpl.typeScale.textRegularM,
                        color: DesignSystemThemeImpl.designSystemColor.textSecondary,
                        textAlign: .center
                    )
                }
                .padding(24)
            } else {
                ContentsList(
                    items: uiState.items,
                    isLoadingMore: false,
                    isLowEndDevice: uiState.isLowEndDevice,
                    onItemClick: onItemClick,
                    onFavoriteClick: onFavoriteClick
                )
            }
        }
    }
}
