import CommonPresentation
import Foundation

public enum FullScreenMediaReducerEvent: ReducerEvent {
    case loadingChanged(Bool)
    case itemsLoaded(items: [MediaItemUiState], currentIndex: Int)
    case pageUpdated(Int)
    case favoritesSynced(Set<String>)
}
