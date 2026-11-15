import CommonPresentation
import Foundation

public struct FullScreenMediaUIState: UiState, Equatable {
    public let items: [MediaItemUiState]
    public let currentIndex: Int
    public let favoriteIds: Set<String>
    public let isLoading: Bool

    public init(
        items: [MediaItemUiState] = [],
        currentIndex: Int = 0,
        favoriteIds: Set<String> = [],
        isLoading: Bool = false
    ) {
        self.items = items
        self.currentIndex = currentIndex
        self.favoriteIds = favoriteIds
        self.isLoading = isLoading
    }

    public var currentItem: MediaItemUiState? {
        guard currentIndex >= 0 && currentIndex < items.count else { return nil }
        return items[currentIndex]
    }

    public func copy(
        items: [MediaItemUiState]? = nil,
        currentIndex: Int? = nil,
        favoriteIds: Set<String>? = nil,
        isLoading: Bool? = nil
    ) -> FullScreenMediaUIState {
        FullScreenMediaUIState(
            items: items ?? self.items,
            currentIndex: currentIndex ?? self.currentIndex,
            favoriteIds: favoriteIds ?? self.favoriteIds,
            isLoading: isLoading ?? self.isLoading
        )
    }
}
