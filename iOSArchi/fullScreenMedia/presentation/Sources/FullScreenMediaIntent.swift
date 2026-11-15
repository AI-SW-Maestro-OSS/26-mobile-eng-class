import CommonPresentation
import Foundation

public enum FullScreenMediaIntent: MviIntent {
    case loadInitialItems
    case pageChanged(Int)
    case toggleFavorite(MediaItemUiState)
    case onFirstImageLoaded
    case navigateBack
}
