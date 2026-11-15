import CommonDomain
import CommonEntity
import CommonPresentation
import Foundation
import SearchDomain

private struct FullScreenMediaRoutePage: Page {
    let origin: String
    let initialIndex: Int
    let query: String

    func toRoute() -> NavRoute {
        NavRoute(
            path: "/fullScreenMedia",
            args: [
                "origin": origin,
                "initialIndex": String(initialIndex),
                "query": query
            ]
        )
    }
}

@MainActor
public final class SearchViewModel: MviViewModel<SearchIntent, SearchUIState, SearchReducerEvent> {
    private let searchUseCase: SearchUseCase
    private let searchPageUseCase: SearchPageUseCase
    private let getFavoriteItemsUseCase: GetFavoriteItemsUseCase
    private let registerFavoriteItemUseCase: RegisterFavoriteItemUseCase
    private let removeFavoriteItemUseCase: RemoveFavoriteItemUseCase
    private let remoteConfigRepository: RemoteConfigRepository
    private let globalStore: GlobalStore
    public let messageHelper: MessageHelper
    public let navigationHelper: NavigationHelper
    public let resourceHelper: ResourceHelper
    public let loggingHelper: LoggingHelper

    private var searchTask: Task<Void, Never>?

    public init(
        initialQuery: String? = nil,
        searchUseCase: SearchUseCase,
        searchPageUseCase: SearchPageUseCase,
        getFavoriteItemsUseCase: GetFavoriteItemsUseCase,
        registerFavoriteItemUseCase: RegisterFavoriteItemUseCase,
        removeFavoriteItemUseCase: RemoveFavoriteItemUseCase,
        remoteConfigRepository: RemoteConfigRepository,
        globalStore: GlobalStore,
        messageHelper: MessageHelper,
        navigationHelper: NavigationHelper,
        resourceHelper: ResourceHelper,
        loggingHelper: LoggingHelper
    ) {
        self.searchUseCase = searchUseCase
        self.searchPageUseCase = searchPageUseCase
        self.getFavoriteItemsUseCase = getFavoriteItemsUseCase
        self.registerFavoriteItemUseCase = registerFavoriteItemUseCase
        self.removeFavoriteItemUseCase = removeFavoriteItemUseCase
        self.remoteConfigRepository = remoteConfigRepository
        self.globalStore = globalStore
        self.messageHelper = messageHelper
        self.navigationHelper = navigationHelper
        self.resourceHelper = resourceHelper
        self.loggingHelper = loggingHelper

        super.init(
            initialState: SearchUIState(
                query: initialQuery ?? "",
                isLowEndDevice: !remoteConfigRepository.get(.IsShowFeatureA)
            ),
            globalStore: globalStore
        )

        observeFavorites()
        onIntent(.loadSduiPage)

        if let initialQuery, !initialQuery.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            onIntent(.searchWithQuery(initialQuery))
        }
    }

    public override func onIntent(_ intent: SearchIntent) {
        switch intent {
        case .updateQuery(let query):
            if query.isEmpty {
                clearSearch()
            } else {
                dispatch(.queryChanged(query))
            }
        case .submitSearch:
            performSearch(query: currentState.query)
        case .searchWithQuery(let query):
            if query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                clearSearch()
            } else {
                dispatch(.queryChanged(query))
                performSearch(query: query)
            }
        case .clear:
            clearSearch()
        case .loadMore:
            performLoadMore()
        case .toggleFavorite(let item):
            toggleFavorite(item)
        case .clickItem(let index, _):
            navigationHelper.navigateTo(
                FullScreenMediaRoutePage(
                    origin: "SEARCH",
                    initialIndex: index,
                    query: currentState.submittedQuery
                )
            )
        case .loadSduiPage:
            loadSduiComponents()
        }
    }

    public override func reduce(state: SearchUIState, event: SearchReducerEvent) -> SearchUIState {
        switch event {
        case .queryChanged(let query):
            return state.copy(query: query)

        case .cleared:
            return SearchUIState(
                query: "",
                submittedQuery: "",
                items: [],
                favoriteIds: state.favoriteIds,
                sduiComponents: state.sduiComponents,
                page: 1,
                isEnd: false,
                isLoading: false,
                isLoadingMore: false,
                hasSearched: false,
                isLowEndDevice: state.isLowEndDevice
            )

        case .searchStarted(let query):
            return state.copy(
                submittedQuery: query,
                page: 1,
                isEnd: false,
                isLoading: true,
                isLoadingMore: false,
                hasSearched: true
            )

        case .searchSucceeded(let items, let page, let isEnd):
            let synced = items.map { $0.copy(isFavorite: state.favoriteIds.contains($0.urlKey)) }
            return state.copy(
                items: synced,
                page: page,
                isEnd: isEnd || synced.isEmpty,
                isLoading: false,
                isLoadingMore: false
            )

        case .searchFailed:
            return state.copy(isLoading: false, isLoadingMore: false)

        case .loadMoreStarted:
            return state.copy(isLoadingMore: true)

        case .loadMoreSucceeded(let newItems, let page, let isEnd):
            let syncedNew = newItems.map { $0.copy(isFavorite: state.favoriteIds.contains($0.urlKey)) }
            var combined = state.items
            let existingIds = Set(combined.map(\.urlKey))
            for item in syncedNew where !existingIds.contains(item.urlKey) {
                combined.append(item)
            }
            return state.copy(
                items: combined,
                page: page,
                isEnd: isEnd || newItems.isEmpty,
                isLoadingMore: false
            )

        case .loadMoreFailed:
            return state.copy(isLoadingMore: false)

        case .favoritesUpdated(let favoriteIds):
            let updatedItems = state.items.map { item in
                item.copy(isFavorite: favoriteIds.contains(item.urlKey))
            }
            return state.copy(items: updatedItems, favoriteIds: favoriteIds)

        case .sduiLoaded(let components):
            return state.copy(sduiComponents: components)

        case .deviceTierUpdated(let isLowEndDevice):
            return state.copy(isLowEndDevice: isLowEndDevice)
        }
    }

    private func clearSearch() {
        searchTask?.cancel()
        searchTask = nil
        dispatch(.cleared)
    }

    private func observeFavorites() {
        let stream = getFavoriteItemsUseCase()
        let task = Task { @MainActor [weak self] in
            for await favorites in stream {
                guard let self, !Task.isCancelled else { break }
                let ids = Set(favorites.map(\.urlKey))
                self.dispatch(.favoritesUpdated(favoriteIds: ids))
            }
        }
        backgroundTasks.append(task)
    }

    private func performSearch(query: String) {
        let trimmed = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else {
            clearSearch()
            return
        }

        searchTask?.cancel()
        dispatch(.searchStarted(query: trimmed))

        searchTask = Task { [weak self] in
            guard let self, !Task.isCancelled else { return }
            await self.globalStore.dispatch(SampleAction(msg: "Recent query words : \(trimmed)"))
            if let result = await self.searchUseCase(query: trimmed, page: 1) {
                guard !Task.isCancelled else { return }
                let uiItems = result.items.map {
                    MediaItemUiState(from: $0, isFavorite: self.currentState.favoriteIds.contains($0.urlKey))
                }
                self.dispatch(.searchSucceeded(items: uiItems, page: 1, isEnd: result.isEnd))
            } else {
                guard !Task.isCancelled else { return }
                self.dispatch(.searchFailed)
            }
        }
    }

    private func performLoadMore() {
        let state = currentState
        guard state.hasSearched,
              !state.submittedQuery.isEmpty,
              !state.isLoading,
              !state.isLoadingMore,
              !state.isEnd else {
            return
        }

        let nextPage = state.page + 1
        dispatch(.loadMoreStarted)

        searchTask = Task { [weak self] in
            guard let self, !Task.isCancelled else { return }
            if let result = await self.searchUseCase(query: state.submittedQuery, page: nextPage) {
                guard !Task.isCancelled else { return }
                let uiItems = result.items.map {
                    MediaItemUiState(from: $0, isFavorite: self.currentState.favoriteIds.contains($0.urlKey))
                }
                self.dispatch(.loadMoreSucceeded(newItems: uiItems, page: nextPage, isEnd: result.isEnd))
            } else {
                guard !Task.isCancelled else { return }
                self.dispatch(.loadMoreFailed)
            }
        }
    }

    private func toggleFavorite(_ item: MediaItemUiState) {
        loggingHelper.shotMetricLogging(
            event: .ClickFavoriteToggleSearchPage,
            params: [
                .AllFavoriteItemCount: currentState.favoriteIds.count,
                .ItemImgUrl: item.thumbnailUrl,
                .SearchKeyword: currentState.submittedQuery
            ]
        )
        Task { [weak self] in
            guard let self else { return }
            if item.isFavorite {
                _ = await self.removeFavoriteItemUseCase(item.urlKey)
            } else {
                _ = await self.registerFavoriteItemUseCase(item.toFavoriteItemVO())
            }
        }
    }

    private func loadSduiComponents() {
        Task { [weak self] in
            guard let self else { return }
            if let pageVO = await self.searchPageUseCase() {
                self.dispatch(.sduiLoaded(pageVO.components))
            }
        }
    }
}
