import CommonPresentation
import Foundation

public struct FavoriteUIState: UiState, Equatable {
    public var items: [MediaItemUiState]
    public var isLowEndDevice: Bool

    public init(
        items: [MediaItemUiState] = [],
        isLowEndDevice: Bool = false
    ) {
        self.items = items
        self.isLowEndDevice = isLowEndDevice
    }
}
