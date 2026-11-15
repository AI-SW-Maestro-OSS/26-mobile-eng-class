import CommonPresentation
import Foundation

public enum SearchIntent: MviIntent {
    case updateQuery(String)
    case submitSearch
    case searchWithQuery(String)
    case clear
    case loadMore
    case toggleFavorite(MediaItemUiState)
    case clickItem(index: Int, item: MediaItemUiState)
    case loadSduiPage
}
