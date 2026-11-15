import CommonDomain
import CommonPresentation
import FavoriteDomain
import Foundation

private struct FullScreenMediaFromFavoritePage: Page {
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
public final class FavoriteViewModel: MviViewModel<FavoriteIntent, FavoriteUIState, FavoriteReducerEvent> {
    private let getFavoriteItemsUseCase: GetFavoriteItemsUseCase
    private let removeFavoriteItemUseCase: RemoveFavoriteItemUseCase
    public let messageHelper: MessageHelper
    public let navigationHelper: NavigationHelper
    public let resourceHelper: ResourceHelper
    public let loggingHelper: LoggingHelper

    public init(
        getFavoriteItemsUseCase: GetFavoriteItemsUseCase,
        removeFavoriteItemUseCase: RemoveFavoriteItemUseCase,
        remoteConfigRepository: RemoteConfigRepository,
        globalStore: GlobalStore,
        messageHelper: MessageHelper,
        navigationHelper: NavigationHelper,
        resourceHelper: ResourceHelper,
        loggingHelper: LoggingHelper
    ) {
        self.getFavoriteItemsUseCase = getFavoriteItemsUseCase
        self.removeFavoriteItemUseCase = removeFavoriteItemUseCase
        self.messageHelper = messageHelper
        self.navigationHelper = navigationHelper
        self.resourceHelper = resourceHelper
        self.loggingHelper = loggingHelper

        super.init(
            initialState: FavoriteUIState(
                items: [],
                isLowEndDevice: !remoteConfigRepository.get(.IsShowFeatureA)
            ),
            globalStore: globalStore,
            observedStateTypes: [SampleState.self]
        )

        observeFavorites()
    }

    public override func onIntent(_ intent: FavoriteIntent) {
        switch intent {
        case .removeFavorite(let item):
            Task { [weak self] in
                guard let self else { return }
                _ = await self.removeFavoriteItemUseCase(item.urlKey)
            }
        case .clickItem(let index, _):
            navigationHelper.navigateTo(
                FullScreenMediaFromFavoritePage(
                    origin: "FAVORITE",
                    initialIndex: index,
                    query: ""
                )
            )
        }
    }

    public override func reduce(state: FavoriteUIState, event: FavoriteReducerEvent) -> FavoriteUIState {
        switch event {
        case .favoritesUpdated(let items):
            return state.copy(items: items)
        }
    }

    public override func observeGlobalState(_ state: GlobalState) {
        if let sampleState = state as? SampleState, !sampleState.msg.isEmpty {
            messageHelper.showToast(sampleState.msg)
        }
    }

    private func observeFavorites() {
        let stream = getFavoriteItemsUseCase()
        let task = Task { @MainActor [weak self] in
            for await vos in stream {
                guard let self, !Task.isCancelled else { break }
                let uiItems = vos.map { MediaItemUiState(from: $0) }
                self.dispatch(.favoritesUpdated(uiItems))
            }
        }
        backgroundTasks.append(task)
    }
}
