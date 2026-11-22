import CommonDomain
import CommonPresentation
import Foundation

public struct SearchUIState: UiState, Equatable {
    public var query: String
    public var submittedQuery: String
    public var items: [MediaItemUiState]
    public var favoriteIds: Set<String>
    public var sduiComponents: [SDUIViewTypeVO]
    public var page: Int
    public var isEnd: Bool
    public var isLoading: Bool
    public var isLoadingMore: Bool
    public var hasSearched: Bool
    public var isLowEndDevice: Bool

    public init(
        query: String = "",
        submittedQuery: String = "",
        items: [MediaItemUiState] = [],
        favoriteIds: Set<String> = [],
        sduiComponents: [SDUIViewTypeVO] = [],
        page: Int = 1,
        isEnd: Bool = false,
        isLoading: Bool = false,
        isLoadingMore: Bool = false,
        hasSearched: Bool = false,
        isLowEndDevice: Bool = false
    ) {
        self.query = query
        self.submittedQuery = submittedQuery
        self.items = items
        self.favoriteIds = favoriteIds
        self.sduiComponents = sduiComponents
        self.page = page
        self.isEnd = isEnd
        self.isLoading = isLoading
        self.isLoadingMore = isLoadingMore
        self.hasSearched = hasSearched
        self.isLowEndDevice = isLowEndDevice
    }
}
