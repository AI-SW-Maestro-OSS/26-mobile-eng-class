import CommonDomain
import CommonEntity
import Foundation
import TTI

public final class GetFullScreenMediaItemsUseCase: BaseUseCase, @unchecked Sendable {
    private let mediaSearchRepository: MediaSearchRepository
    private let favoriteRepository: FavoriteRepository

    public init(
        mediaSearchRepository: MediaSearchRepository,
        favoriteRepository: FavoriteRepository,
        localizationHelper: LocalizationHelper,
        messageHelper: MessageHelper,
        navigationHelper: NavigationHelper,
        ttiHelper: TTIHelper
    ) {
        self.mediaSearchRepository = mediaSearchRepository
        self.favoriteRepository = favoriteRepository
        super.init(
            localizationHelper: localizationHelper,
            messageHelper: messageHelper,
            navigationHelper: navigationHelper,
            ttiHelper: ttiHelper
        )
    }

    public func invoke(
        origin: FullScreenMediaOrigin,
        query: String,
        page: Int = 1
    ) async -> [MediaItemVO] {
        ttiHelper.endTTITimeline(.apiRequestReadyTime)
        ttiHelper.startTTITimeline(.apiResponseTime)
        do {
            let items: [MediaItemVO]
            switch origin {
            case .search:
                let effectiveQuery = query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                    ? "Architecture"
                    : query
                let result = try await mediaSearchRepository.search(query: effectiveQuery, page: page)
                items = result.items
            case .favorite:
                let favorites = await readFavoriteSnapshot()
                items = favorites.map { fav in
                    MediaItemVO(
                        type: fav.type,
                        title: fav.title,
                        urlKey: fav.urlKey,
                        thumbnailImageUrl: fav.thumbnailUrl,
                        contentsImageUrl: fav.contentsImageUrl,
                        dateTime: fav.dateTime
                    )
                }
            }
            ttiHelper.endTTITimeline(.apiResponseTime)
            return items
        } catch let e as HTTPResponseError {
            ttiHelper.endTTITimeline(.apiResponseTime)
            if let mediaError = e.handlingErrorOnUseCase(FullScreenMediaErrorType.self) {
                messageHelper.showToast(mediaError.errorMsg)
            } else if e.isCommonErrorHandling() {
                executeCommonErrorHanding(e)
            } else {
                messageHelper.showToast(localizationHelper.string(for: .searchFailed))
            }
            return []
        } catch {
            ttiHelper.endTTITimeline(.apiResponseTime)
            messageHelper.showToast(localizationHelper.string(for: .searchFailed))
            return []
        }
    }

    public func callAsFunction(
        origin: FullScreenMediaOrigin,
        query: String,
        page: Int = 1
    ) async -> [MediaItemVO] {
        await invoke(origin: origin, query: query, page: page)
    }

    private func readFavoriteSnapshot() async -> [FavoriteItemVO] {
        let stream = favoriteRepository.getFavoriteItemsFlow()
        for await items in stream {
            return items
        }
        return []
    }
}
