import CommonPresentation
import Foundation

public enum FavoriteIntent: MviIntent {
    case removeFavorite(MediaItemUiState)
    case tapItem(index: Int, item: MediaItemUiState)
}
