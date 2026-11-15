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
        resourceHelper: ResourceHelper,
        messageHelper: MessageHelper,
        navigationHelper: NavigationHelper,
        ttiHelper: TTIHelper
    ) {
        self.mediaSearchRepository = mediaSearchRepository
        self.favoriteRepository = favoriteRepository
        super.init(
            resourceHelper: resourceHelper,
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
        ttiHelper.endTTITimeline(.API_REQUEST_READY_TIME)
        ttiHelper.startTTITimeline(.API_RESPONSE_TIME)
        do {
            let items: [MediaItemVO]
            switch origin {
            case .SEARCH:
                let effectiveQuery = query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                    ? "Architecture"
                    : query
                let result = try await mediaSearchRepository.search(query: effectiveQuery, page: page)
                items = result.items
            case .FAVORITE:
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
            ttiHelper.endTTITimeline(.API_RESPONSE_TIME)
            return items
        } catch let e as HttpResponseException {
            ttiHelper.endTTITimeline(.API_RESPONSE_TIME)
            if let mediaError = e.handlingErrorOnUseCase(FullScreenMediaErrorType.self) {
                messageHelper.showToast(mediaError.errorMsg)
            } else if e.isCommonErrorHandling() {
                executeCommonErrorHanding(e)
            } else {
                messageHelper.showToast(resourceHelper.getString(.SEARCH_FAILED))
            }
            return []
        } catch {
            ttiHelper.endTTITimeline(.API_RESPONSE_TIME)
            messageHelper.showToast(resourceHelper.getString(.SEARCH_FAILED))
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
