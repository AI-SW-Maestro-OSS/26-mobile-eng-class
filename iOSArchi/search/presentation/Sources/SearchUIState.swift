import CommonDomain
import CommonPresentation
import Foundation

public struct SearchUIState: UiState, Equatable {
    public let query: String
    public let submittedQuery: String
    public let items: [MediaItemUiState]
    public let favoriteIds: Set<String>
    public let sduiComponents: [SDUIViewTypeVO]
    public let page: Int
    public let isEnd: Bool
    public let isLoading: Bool
    public let isLoadingMore: Bool
    public let hasSearched: Bool
    public let isLowEndDevice: Bool

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

    public func copy(
        query: String? = nil,
        submittedQuery: String? = nil,
        items: [MediaItemUiState]? = nil,
        favoriteIds: Set<String>? = nil,
        sduiComponents: [SDUIViewTypeVO]? = nil,
        page: Int? = nil,
        isEnd: Bool? = nil,
        isLoading: Bool? = nil,
        isLoadingMore: Bool? = nil,
        hasSearched: Bool? = nil,
        isLowEndDevice: Bool? = nil
    ) -> SearchUIState {
        SearchUIState(
            query: query ?? self.query,
            submittedQuery: submittedQuery ?? self.submittedQuery,
            items: items ?? self.items,
            favoriteIds: favoriteIds ?? self.favoriteIds,
            sduiComponents: sduiComponents ?? self.sduiComponents,
            page: page ?? self.page,
            isEnd: isEnd ?? self.isEnd,
            isLoading: isLoading ?? self.isLoading,
            isLoadingMore: isLoadingMore ?? self.isLoadingMore,
            hasSearched: hasSearched ?? self.hasSearched,
            isLowEndDevice: isLowEndDevice ?? self.isLowEndDevice
        )
    }
}
