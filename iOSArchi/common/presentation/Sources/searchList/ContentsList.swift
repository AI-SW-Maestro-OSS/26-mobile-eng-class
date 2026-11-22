import CommonEntity
import SwiftUI

public let SEARCH_ITEM_TEST_TAG = "search_item_"

public struct ContentsList: View {
    private let items: [MediaItemUiState]
    private let isLoadingMore: Bool
    private let isLowEndDevice: Bool
    private let onItemClick: (Int, MediaItemUiState) -> Void
    private let onFavoriteClick: (MediaItemUiState) -> Void
    private let onLoadMore: (() -> Void)?
    private let onFirstImageLoaded: (() -> Void)?

    public init(
        items: [MediaItemUiState],
        isLoadingMore: Bool = false,
        isLowEndDevice: Bool = false,
        onItemClick: @escaping (Int, MediaItemUiState) -> Void,
        onFavoriteClick: @escaping (MediaItemUiState) -> Void,
        onLoadMore: (() -> Void)? = nil,
        onFirstImageLoaded: (() -> Void)? = nil
    ) {
        self.items = items
        self.isLoadingMore = isLoadingMore
        self.isLowEndDevice = isLowEndDevice
        self.onItemClick = onItemClick
        self.onFavoriteClick = onFavoriteClick
        self.onLoadMore = onLoadMore
        self.onFirstImageLoaded = onFirstImageLoaded
    }

    public var body: some View {
        ContentsGrid(
            items: items,
            isLoadingMore: isLoadingMore,
            isLowEndDevice: isLowEndDevice,
            onItemClick: onItemClick,
            onFavoriteClick: onFavoriteClick,
            onLoadMore: onLoadMore,
            onFirstImageLoaded: onFirstImageLoaded
        )
    }
}

public struct ContentsGrid: View {
    private let items: [MediaItemUiState]
    private let isLoadingMore: Bool
    private let isLowEndDevice: Bool
    private let onItemClick: (Int, MediaItemUiState) -> Void
    private let onFavoriteClick: (MediaItemUiState) -> Void
    private let onLoadMore: (() -> Void)?
    private let onFirstImageLoaded: (() -> Void)?

    @State private var firstImageReported = false

    private let columns = [
        GridItem(.flexible(), spacing: 12),
        GridItem(.flexible(), spacing: 12)
    ]

    public init(
        items: [MediaItemUiState],
        isLoadingMore: Bool = false,
        isLowEndDevice: Bool = false,
        onItemClick: @escaping (Int, MediaItemUiState) -> Void,
        onFavoriteClick: @escaping (MediaItemUiState) -> Void,
        onLoadMore: (() -> Void)? = nil,
        onFirstImageLoaded: (() -> Void)? = nil
    ) {
        self.items = items
        self.isLoadingMore = isLoadingMore
        self.isLowEndDevice = isLowEndDevice
        self.onItemClick = onItemClick
        self.onFavoriteClick = onFavoriteClick
        self.onLoadMore = onLoadMore
        self.onFirstImageLoaded = onFirstImageLoaded
    }

    public var body: some View {
        ScrollView {
            LazyVGrid(columns: columns, spacing: 12) {
                ForEach(Array(items.enumerated()), id: \.element.urlKey) { index, item in
                    MediaGridCell(
                        index: index,
                        item: item,
                        isLowEndDevice: isLowEndDevice,
                        onItemClick: { onItemClick(index, item) },
                        onFavoriteClick: { onFavoriteClick(item) },
                        onImageLoaded: {
                            if index == 0, !firstImageReported {
                                firstImageReported = true
                                onFirstImageLoaded?()
                            }
                        }
                    )
                    .accessibilityIdentifier("\(SEARCH_ITEM_TEST_TAG)\(index)")
                    .onAppear {
                        if index >= items.count - 4 {
                            onLoadMore?()
                        }
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            if isLoadingMore {
                ProgressView()
                    .padding(.vertical, 16)
            }
        }
    }
}

private struct MediaGridCell: View {
    let index: Int
    let item: MediaItemUiState
    let isLowEndDevice: Bool
    let onItemClick: () -> Void
    let onFavoriteClick: () -> Void
    let onImageLoaded: () -> Void

    @State private var showHeartBurst = false

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            ZStack(alignment: .topTrailing) {
                ArchiAsyncImage(
                    urlString: item.thumbnailUrl,
                    contentMode: .fill,
                    onSuccess: onImageLoaded
                )
                .frame(height: 150)
                .frame(maxWidth: .infinity)
                .clipped()
                .clipShape(RoundedRectangle(cornerRadius: 10))

                if item.type == .video {
                    VStack {
                        Spacer()
                        HStack {
                            ArchiText(
                                text: "VIDEO",
                                style: DesignSystemThemeImpl.typeScale.textRegularXS,
                                color: DesignSystemThemeImpl.designSystemColor.white
                            )
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(
                                Capsule()
                                    .fill(DesignSystemThemeImpl.designSystemColor.dimHeavy)
                            )
                            Spacer()
                        }
                        .padding(6)
                    }
                }

                FavoriteHeartButton(isFavorite: item.isFavorite) {
                    if !item.isFavorite {
                        showHeartBurst = true
                    }
                    onFavoriteClick()
                }
                .padding(8)

                FavoriteHeartAnimationOverlay(
                    visible: showHeartBurst,
                    isLowEndDevice: isLowEndDevice
                ) {
                    showHeartBurst = false
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }

            ArchiText(
                text: item.title,
                style: DesignSystemThemeImpl.typeScale.textStrongM,
                color: DesignSystemThemeImpl.designSystemColor.textPrimary,
                maxLines: 1
            )

            HStack {
                ArchiText(
                    text: item.source,
                    style: DesignSystemThemeImpl.typeScale.textRegularXS,
                    color: DesignSystemThemeImpl.designSystemColor.textSecondary,
                    maxLines: 1
                )
                Spacer()
                ArchiText(
                    text: formatKakaoDateTime(item.dateTime),
                    style: DesignSystemThemeImpl.typeScale.textRegularXS,
                    color: DesignSystemThemeImpl.designSystemColor.textCaption,
                    maxLines: 1
                )
            }
        }
        .contentShape(Rectangle())
        .onTapGesture {
            onItemClick()
        }
    }
}
