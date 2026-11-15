import CommonPresentation
import Foundation

public enum FavoriteIntent: MviIntent {
    case removeFavorite(MediaItemUiState)
    case clickItem(index: Int, item: MediaItemUiState)
}
