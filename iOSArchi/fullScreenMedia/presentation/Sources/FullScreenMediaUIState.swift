import CommonPresentation
import Foundation

public struct FullScreenMediaUIState: UiState, Equatable {
    public var items: [MediaItemUiState]
    public var currentIndex: Int
    public var favoriteIds: Set<String>
    public var isLoading: Bool

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
}
