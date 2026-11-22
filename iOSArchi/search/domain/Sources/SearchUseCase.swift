import CommonDomain
import CommonEntity
import Foundation
import TTI

public final class SearchUseCase: BaseUseCase, @unchecked Sendable {
    private let mediaSearchRepository: MediaSearchRepository

    public init(
        mediaSearchRepository: MediaSearchRepository,
        localizationHelper: LocalizationHelper,
        messageHelper: MessageHelper,
        navigationHelper: NavigationHelper,
        ttiHelper: TTIHelper
    ) {
        self.mediaSearchRepository = mediaSearchRepository
        super.init(
            localizationHelper: localizationHelper,
            messageHelper: messageHelper,
            navigationHelper: navigationHelper,
            ttiHelper: ttiHelper
        )
    }

    public func invoke(query: String, page: Int) async -> MediaSearchResultVO? {
        do {
            return try await mediaSearchRepository.search(query: query, page: page)
        } catch let e as HTTPResponseError {
            if let searchError = e.handlingErrorOnUseCase(SearchErrorType.self) {
                messageHelper.showToast(searchError.errorMsg)
            } else if e.isCommonErrorHandling() {
                executeCommonErrorHanding(e)
            } else {
                messageHelper.showToast(localizationHelper.string(for: .searchFailed))
            }
            return nil
        } catch {
            messageHelper.showToast(localizationHelper.string(for: .searchFailed))
            return nil
        }
    }

    public func callAsFunction(query: String, page: Int) async -> MediaSearchResultVO? {
        await invoke(query: query, page: page)
    }
}
