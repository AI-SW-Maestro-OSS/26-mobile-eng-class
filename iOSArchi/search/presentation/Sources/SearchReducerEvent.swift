import CommonDomain
import CommonPresentation
import Foundation

public enum SearchReducerEvent: ReducerEvent {
    case queryChanged(String)
    case cleared
    case searchStarted(query: String)
    case searchSucceeded(items: [MediaItemUiState], page: Int, isEnd: Bool)
    case searchFailed
    case loadMoreStarted
    case loadMoreSucceeded(newItems: [MediaItemUiState], page: Int, isEnd: Bool)
    case loadMoreFailed
    case favoritesUpdated(favoriteIds: Set<String>)
    case sduiLoaded([SDUIViewTypeVO])
    case deviceTierUpdated(isLowEndDevice: Bool)
}
