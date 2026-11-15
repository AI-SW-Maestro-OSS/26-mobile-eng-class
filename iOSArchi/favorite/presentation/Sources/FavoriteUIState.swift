import CommonPresentation
import Foundation

public struct FavoriteUIState: UiState, Equatable {
    public let items: [MediaItemUiState]
    public let isLowEndDevice: Bool

    public init(
        items: [MediaItemUiState] = [],
        isLowEndDevice: Bool = false
    ) {
        self.items = items
        self.isLowEndDevice = isLowEndDevice
    }

    public func copy(
        items: [MediaItemUiState]? = nil,
        isLowEndDevice: Bool? = nil
    ) -> FavoriteUIState {
        FavoriteUIState(
            items: items ?? self.items,
            isLowEndDevice: isLowEndDevice ?? self.isLowEndDevice
        )
    }
}
