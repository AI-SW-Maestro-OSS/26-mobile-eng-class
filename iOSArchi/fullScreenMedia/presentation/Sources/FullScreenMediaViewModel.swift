import CommonDomain
import CommonPresentation
import Foundation
import FullScreenMediaDomain
import TTI

@MainActor
public final class FullScreenMediaViewModel: MviViewModel<FullScreenMediaIntent, FullScreenMediaUIState, FullScreenMediaReducerEvent> {
    public final class Factory: @unchecked Sendable {
        private let resolver: DIContainer

        public init(resolver: DIContainer) {
            self.resolver = resolver
        }

        @MainActor
        public func create(args: FullScreenMediaPage.Args) -> FullScreenMediaViewModel {
            FullScreenMediaViewModel(
                args: args,
                getFullScreenMediaItemsUseCase: resolver.resolve(GetFullScreenMediaItemsUseCase.self),
                getFavoriteItemsUseCase: resolver.resolve(GetFavoriteItemsUseCase.self),
                registerFavoriteItemUseCase: resolver.resolve(RegisterFavoriteItemUseCase.self),
                removeFavoriteItemUseCase: resolver.resolve(RemoveFavoriteItemUseCase.self),
                ttiHelper: resolver.resolve(TTIHelper.self),
                messageHelper: resolver.resolve(MessageHelper.self),
                navigationHelper: resolver.resolve(NavigationHelper.self),
                loggingHelper: resolver.resolve(LoggingHelper.self)
            )
        }
    }

    public let args: FullScreenMediaPage.Args
    private let getFullScreenMediaItemsUseCase: GetFullScreenMediaItemsUseCase
    private let getFavoriteItemsUseCase: GetFavoriteItemsUseCase
    private let registerFavoriteItemUseCase: RegisterFavoriteItemUseCase
    private let removeFavoriteItemUseCase: RemoveFavoriteItemUseCase
    public let ttiHelper: TTIHelper
    public let messageHelper: MessageHelper
    public let navigationHelper: NavigationHelper
    public let loggingHelper: LoggingHelper
    private var hasRecordedFirstImageTTI = false

    public init(
        args: FullScreenMediaPage.Args,
        getFullScreenMediaItemsUseCase: GetFullScreenMediaItemsUseCase,
        getFavoriteItemsUseCase: GetFavoriteItemsUseCase,
        registerFavoriteItemUseCase: RegisterFavoriteItemUseCase,
        removeFavoriteItemUseCase: RemoveFavoriteItemUseCase,
        ttiHelper: TTIHelper,
        messageHelper: MessageHelper,
        navigationHelper: NavigationHelper,
        loggingHelper: LoggingHelper
    ) {
        self.args = args
        self.getFullScreenMediaItemsUseCase = getFullScreenMediaItemsUseCase
        self.getFavoriteItemsUseCase = getFavoriteItemsUseCase
        self.registerFavoriteItemUseCase = registerFavoriteItemUseCase
        self.removeFavoriteItemUseCase = removeFavoriteItemUseCase
        self.ttiHelper = ttiHelper
        self.messageHelper = messageHelper
        self.navigationHelper = navigationHelper
        self.loggingHelper = loggingHelper

        super.init(
            initialState: FullScreenMediaUIState(
                items: [],
                currentIndex: max(0, args.initialIndex),
                isLoading: true
            )
        )

        ttiHelper.startTTITracking(FullScreenMediaTTIPage.shared)
        ttiHelper.startTTITimeline(.viewCreationTime)
        observeFavorites()
    }

    public func markViewCreated() {
        ttiHelper.endTTITimeline(.viewCreationTime)
    }

    public override func onIntent(_ intent: FullScreenMediaIntent) {
        switch intent {
        case .loadInitialItems:
            loadItems()
        case .pageChanged(let index):
            dispatch(.pageUpdated(index))
        case .toggleFavorite(let item):
            Task { [weak self] in
                guard let self else { return }
                if item.isFavorite {
                    _ = await self.removeFavoriteItemUseCase(item.urlKey)
                } else {
                    _ = await self.registerFavoriteItemUseCase(item.toFavoriteItemVO())
                }
            }
        case .onFirstImageLoaded:
            guard !hasRecordedFirstImageTTI else { return }
            hasRecordedFirstImageTTI = true
            ttiHelper.endTTITimeline(.imageLoadedTime)
            ttiHelper.endTTITracking()
            ttiHelper.shotTTILogging()
        case .navigateBack:
            navigationHelper.navigateToBack()
        }
    }

    public override func reduce(
        state: FullScreenMediaUIState,
        event: FullScreenMediaReducerEvent
    ) -> FullScreenMediaUIState {
        var next = state
        switch event {
        case .loadingChanged(let isLoading):
            next.isLoading = isLoading
        case .itemsLoaded(let items, let currentIndex):
            let synced = items.map { item -> MediaItemUiState in
                var updated = item
                updated.isFavorite = state.favoriteIds.contains(item.urlKey)
                return updated
            }
            let clamped = synced.isEmpty ? 0 : min(max(0, currentIndex), synced.count - 1)
            next.items = synced
            next.currentIndex = clamped
            next.isLoading = false
        case .pageUpdated(let index):
            let clamped = state.items.isEmpty ? 0 : min(max(0, index), state.items.count - 1)
            next.currentIndex = clamped
        case .favoritesSynced(let favoriteIds):
            next.items = state.items.map { item in
                var updated = item
                updated.isFavorite = favoriteIds.contains(item.urlKey)
                return updated
            }
            next.favoriteIds = favoriteIds
        }
        return next
    }

    private func observeFavorites() {
        let stream = getFavoriteItemsUseCase()
        let task = Task { @MainActor [weak self] in
            for await favorites in stream {
                guard let self, !Task.isCancelled else { break }
                let ids = Set(favorites.map(\.urlKey))
                self.dispatch(.favoritesSynced(ids))
            }
        }
        backgroundTasks.append(task)
    }

    private func loadItems() {
        dispatch(.loadingChanged(true))
        ttiHelper.startTTITimeline(.apiRequestReadyTime)

        Task { [weak self] in
            guard let self else { return }
            let vos = await self.getFullScreenMediaItemsUseCase(
                origin: self.args.origin,
                query: self.args.query
            )

            self.ttiHelper.startTTITimeline(.viewBindingTime)
            let uiItems = vos.map {
                MediaItemUiState(from: $0, isFavorite: self.currentState.favoriteIds.contains($0.urlKey))
            }
            self.dispatch(.itemsLoaded(items: uiItems, currentIndex: self.args.initialIndex))
            self.ttiHelper.endTTITimeline(.viewBindingTime)
            self.ttiHelper.startTTITimeline(.imageLoadedTime)
        }
    }
}
