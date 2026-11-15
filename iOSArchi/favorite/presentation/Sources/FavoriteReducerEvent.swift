import CommonPresentation
import Foundation

public enum FavoriteReducerEvent: ReducerEvent {
    case favoritesUpdated([MediaItemUiState])
}
